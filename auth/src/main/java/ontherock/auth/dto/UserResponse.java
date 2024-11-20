package ontherock.auth.dto;

public record UserResponse(
        String id,
        String nickname,
        String profilePicture
) {
}
