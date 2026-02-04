package com.fourtune.auction.boundedContext.auth.application.service.oauth;

import com.fourtune.auction.boundedContext.user.application.service.UserSupport;
import com.fourtune.auction.boundedContext.user.domain.constant.Role;
import com.fourtune.auction.boundedContext.user.domain.constant.Status;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.auth.dto.UserContext;
import com.fourtune.auction.shared.user.event.UserJoinedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserSupport userSupport;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        return processUser(oAuth2User, registrationId);
    }

    public OAuth2User processUser(OAuth2User oAuth2User, String provider) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");
        Optional<User> existingUser = userSupport.findByEmail(email);

        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.updateOauth(provider, providerId);
            userSupport.save(user);
        } else {
            User newUser = User.builder()
                    .email(email)
                    .nickname(name)
                    .password(UUID.randomUUID().toString())
                    .phoneNumber("")
                    .provider(provider)
                    .providerId(providerId)
                    .role(Role.USER)
                    .status(Status.ACTIVE)
                    .build();
            user = userSupport.save(newUser);

            eventPublisher.publish(new UserJoinedEvent(user.toDto()));
        }

        return new UserContext(
                user.getId(),
                null,
                List.of(new SimpleGrantedAuthority(user.getRole().name())),
                oAuth2User.getAttributes()
        );
    }
}
