package ontherock.auth.client;

import lombok.extern.slf4j.Slf4j;
import ontherock.auth.common.OntherockException;
import ontherock.auth.domain.OauthType;
import ontherock.auth.dto.KakaoInfoResponse;
import ontherock.auth.dto.KakaoTokenResponse;
import ontherock.auth.dto.OauthUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@Component
public class OAuthKakaoClient implements OAuthClient {

    private final String INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private final String CODE_URL = "https://kauth.kakao.com/oauth/token";

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Override
    public OauthType getType() {
        return OauthType.KAKAO;
    }

    private OauthUser getUser(String accessToken) {
        log.info("Kakao getUser" + accessToken);
        return Objects.requireNonNull(
                WebClient.builder()
                        .baseUrl(INFO_URL)
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .defaultHeader(HttpHeaders.AUTHORIZATION, "BEARER " + accessToken)
                        .build()
                        .get()
                        .retrieve()
                        .toEntity(KakaoInfoResponse.class)
                        .block()
                        .getBody()
        ).toOAuthUser();
    }

    @Override
    public OauthUser login(String code, String state) {
        KakaoTokenResponse kakaoTokenResponse = WebClient.builder()
                .baseUrl(CODE_URL)
                .build()
                .post()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", clientId)
                        .with("redirect_uri", redirectUri)
                        .with("code", code)
                        .with("client_secret", clientSecret))
                .retrieve()
                .onStatus(
                        status -> !status.is2xxSuccessful(),
                        response -> response.bodyToMono(String.class).flatMap(body -> {
                            log.error("Kakao Client login fail " + response.statusCode() + ": " + body);
                            return Mono.error(new OntherockException(HttpStatus.valueOf(response.statusCode().value()), "Kakao client fail: " + body));
                        })
                )
                .toEntity(KakaoTokenResponse.class)
                .block()
                .getBody();

        return getUser(kakaoTokenResponse.access_token());
    }
}
