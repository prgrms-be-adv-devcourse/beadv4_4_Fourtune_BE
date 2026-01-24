package com.fourtune.auction.shared.auth.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public record UserContext(
        Long id,
        String email,
        String password,
        Collection<? extends GrantedAuthority> authorities,
        Map<String, Object> attributes
) implements UserDetails, OAuth2User {

    public UserContext(Long id, String email, String password, Collection<? extends GrantedAuthority> authorities) {
        this(id, email, password, authorities, Map.of());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Map<String, Object> getAttributes() { return attributes; }

    @Override
    public String getName() { return String.valueOf(id); }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
