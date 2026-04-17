package kr.ac.kyonggi.domain.user;

public record UserCreateCommand(
        String email,
        String password,
        String name,
        String profileImage,
        String phone
) {

}
