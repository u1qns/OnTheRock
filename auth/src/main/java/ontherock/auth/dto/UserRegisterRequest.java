package ontherock.auth.dto;

public record UserRegisterRequest(
        String authId,
        String name
) {
}
