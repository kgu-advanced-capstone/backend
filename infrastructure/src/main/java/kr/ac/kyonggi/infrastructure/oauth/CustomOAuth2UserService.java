package kr.ac.kyonggi.infrastructure.oauth;

import kr.ac.kyonggi.domain.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuthProvider provider = OAuthProvider.valueOf(registrationId.toUpperCase());

        String providerId   = String.valueOf(attributes.get("sub"));
        String email        = String.valueOf(attributes.get("email"));
        String name         = String.valueOf(attributes.get("name"));
        String profileImage = String.valueOf(attributes.get("picture"));

        UserSocialCreateCommand command =
                new UserSocialCreateCommand(email, name, profileImage, providerId, provider);
        User user = userService.findOrCreateSocialUser(command);


        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(user.getRole().getGrantedAuthority())),
                attributes,
                "email"  // attributes에서 사용자 이름으로 사용할 키
        );
    }
}