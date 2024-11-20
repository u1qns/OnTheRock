package ontherock.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        Long id
) {
}
