package com.firstapp.uber.auth;

import com.firstapp.uber.user.UserRepo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepo userRepo;

    public JwtAuthFilter(JwtService jwtService, UserRepo userRepo) {
        this.jwtService = jwtService;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        System.out.println("[JWT] Incoming path =  " + path);

        if (path.startsWith("/api/rides/") || path.startsWith("/api/auth") || "/ping".equals(path)) {

            System.out.println("[JWT] Skipping filter for public path");
            filterChain.doFilter(request, response);
            return;
        }


        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        System.out.println("[JWT] Authorization header = " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[JWT] No Bearer token, continuing without auth");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("[JWT] Token = " + token);

        if (!jwtService.isTokenValid(token)) {
            System.out.println("[JWT] Token invalid");
            filterChain.doFilter(request, response);
            return;
        }

        String mobile = jwtService.extractSubject(token);
        Integer userId = jwtService.extractUserId(token);
        System.out.println("[JWT] Token valid. subject(mobile) = " + mobile + ", userId = " + userId);


        if (mobile != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userRepo.findByMobile(mobile).orElse(null);
            System.out.println("[JWT] DB lookup user by mobile -> " + user);

            if (user != null) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                user, // principal (you can make a custom UserDetails later)
                                null,
                                Collections.emptyList()
                        );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("[JWT] SecurityContext authentication set");
            } else {
                System.out.println("[JWT] No user found with that mobile");
            }
        }

        filterChain.doFilter(request, response);
    }
}
