package ontherock.auth.dto;

import ontherock.auth.domain.OauthType;

public record OauthUser(
        String userId,
        String name,
        OauthType type
) {
}
