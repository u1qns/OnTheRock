package ontherock.user.dto;

public record ProfileResponse(
        String id,
        String nickname,
        String profilePicture,
        Integer expr
) {
}
