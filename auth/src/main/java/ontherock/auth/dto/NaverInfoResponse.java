package ontherock.auth.dto;

import ontherock.auth.domain.OauthType;

public record NaverInfoResponse(
    String resultcode,
    String message,
    ResponseData response
) {
    public record ResponseData(String id, String nickname, String name, String email, String gender, String age, String birthday, String profile_image, String birthyear, String mobile) {}
    public OauthUser toOAuthUser() {
        return new OauthUser(response.id, response.name, OauthType.NAVER);
    }
}
