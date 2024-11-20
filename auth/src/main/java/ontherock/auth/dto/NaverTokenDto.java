package ontherock.auth.dto;

public record NaverTokenDto(
        String access_token,
        String refresh_token,
        String token_type,
        Long expires_in
) {
}
