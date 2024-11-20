package ontherock.auth.dto;

import ontherock.auth.domain.OauthType;

public record OauthRequest(
        String token,
        OauthType type,
        String state
) {
}
