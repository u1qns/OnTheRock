package ontherock.auth.dto;

public record TestTokenResponse(
        String id,
        String accessToken,
        String refreshToken
) {
}
