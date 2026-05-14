package kr.ac.kyonggi.infrastructure.oauth;

import kr.ac.kyonggi.domain.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
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
        OAuthProvider provider;
        try {
            provider = OAuthProvider.valueOf(registrationId.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_registration"),
                    "지원하지 않는 OAuth 공급자입니다: " + registrationId);
        }

        Object subObj = attributes.get("sub");
        if (subObj == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_attribute"),
                    "OAuth 응답에 sub(providerId)가 없습니다: " + registrationId);
        }
        String providerId = subObj.toString();

        Object emailObj = attributes.get("email");
        if (emailObj == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_attribute"),
                    "OAuth 응답에 email이 없습니다: " + registrationId);
        }
        String email = emailObj.toString();

        Object nameObj = attributes.get("name");
        String name = (nameObj != null) ? nameObj.toString() : null;

        Object pictureObj = attributes.get("picture");
        String profileImage = (pictureObj != null) ? pictureObj.toString() : null;

        UserSocialCreateCommand command =
                new UserSocialCreateCommand(email, name, profileImage, providerId, provider);
        User user = userService.findOrCreateSocialUser(command);


        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(user.getRole().getGrantedAuthority())),
                attributes,
                "sub"
        );
    }
}