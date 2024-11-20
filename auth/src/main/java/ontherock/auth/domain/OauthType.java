package ontherock.auth.domain;

import ontherock.auth.common.OntherockException;
import org.springframework.http.HttpStatus;

public enum OauthType {
    NAVER,
    KAKAO,
    ;

    public static OauthType fromString(String value) {
        try {
            return OauthType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new OntherockException(HttpStatus.BAD_REQUEST, "Invalid OauthType: " + value);
        }
    }
}
