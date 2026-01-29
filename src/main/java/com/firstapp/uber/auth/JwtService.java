package com.firstapp.uber.auth;

import com.firstapp.uber.user.UserRepo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import model.User;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final UserRepo userRepo;

    public JwtService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Value("${jwt.secret_key}")
    private String secretKey;

    private SecretKey getSigningKey(){
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Integer userId, String subject) {

        Instant now = Instant.now();
        Instant expiry = now.plus(7, ChronoUnit.DAYS);

        return Jwts.builder()
                .subject(subject)//subject = mobilenum
                .claims(Map.of("userId", userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractSubject(String token){
        return extractClaim(token, Claims::getSubject);
    }


    public Integer extractUserId(String token){
        Claims claims = extractAllClaims(token);
        Object val = claims.get("userId");
        if (val instanceof Integer i) return i;
        if (val instanceof Number n) return n.intValue();
        return null;
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();
            return expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Authentication authenticate(String token) {

        System.out.println("Inside authenticate");
        if (!isTokenValid(token)) {
            throw new RuntimeException("Invalid or expired JWT");
        }
        System.out.println("Inside authenticate token is valid");
        System.out.println("JWT subject (phone)"+ extractSubject(token));
        Integer userIdInt = extractUserId(token);
        Long userIdLng = userIdInt.longValue();

        if (userIdInt == null) {
            throw new RuntimeException("UserId missing in JWT");
        }
        System.out.println("Inside authenticate UserId is found in JWT");
        System.out.println("Inside authenticate userId is:" + userIdLng);


        User user = userRepo.findById(userIdLng)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("Inside authenticate User is found in DB");

        CustomUserDetails userDetails = new CustomUserDetails(user);
        System.out.println("Inside authenticate CustomUserDetails is made");
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

}
