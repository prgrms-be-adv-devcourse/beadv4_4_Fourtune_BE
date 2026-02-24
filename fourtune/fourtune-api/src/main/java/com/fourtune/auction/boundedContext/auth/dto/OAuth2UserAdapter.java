package com.fourtune.auction.boundedContext.auth.dto;

import com.fourtune.shared.auth.dto.UserContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class OAuth2UserAdapter implements OAuth2User {

    private final UserContext userContext;
    private final Map<String, Object> attributes;

    public OAuth2UserAdapter(UserContext userContext, Map<String, Object> attributes) {
        this.userContext = userContext;
        this.attributes = attributes;
    }

    public UserContext getUserContext() {
        return userContext;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userContext.getAuthorities();
    }

    @Override
    public String getName() {
        return String.valueOf(userContext.id());
    }
}
