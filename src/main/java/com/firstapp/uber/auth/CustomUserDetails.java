package com.firstapp.uber.auth;

import model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private final Integer userId;
    private final String mobile;
    private final String role;//should i use the enum i made or let it be string

    public CustomUserDetails(User user) {//here User is a record is that an issue
        this.userId = user.id();
        this.mobile = user.mobile_num();
        this.role = user.role();
    }
    public Integer getUserId() {
        return userId;
    }

    public String getMobile() {
        return mobile;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return mobile;
    }

    @Override public String getPassword() { return null; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

}
