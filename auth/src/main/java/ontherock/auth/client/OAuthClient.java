package ontherock.auth.client;

import ontherock.auth.domain.OauthType;
import ontherock.auth.dto.OauthUser;

public interface OAuthClient {
    OauthType getType();
    OauthUser login(String code, String state);
}
