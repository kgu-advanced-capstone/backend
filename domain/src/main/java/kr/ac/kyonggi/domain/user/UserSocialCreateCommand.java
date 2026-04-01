package kr.ac.kyonggi.domain.user;

public record UserSocialCreateCommand(
        String email,
        String name,
        String profileImage,
        String providerId,
        OAuthProvider provider
) {
}
