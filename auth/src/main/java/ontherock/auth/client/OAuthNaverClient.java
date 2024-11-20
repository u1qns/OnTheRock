package ontherock.auth.client;

import ontherock.auth.common.OntherockException;
import ontherock.auth.domain.OauthType;
import ontherock.auth.dto.NaverInfoResponse;
import ontherock.auth.dto.NaverTokenDto;
import ontherock.auth.dto.OauthUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class OAuthNaverClient implements OAuthClient {

    private final String infoURL = "https://openapi.naver.com/v1/nid/me";

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    @Override
    public OauthType getType() {
        return OauthType.NAVER;
    }

    private OauthUser getUser(String accessToken) {
        return Objects.requireNonNull(
                        WebClient.builder()
                                .baseUrl(infoURL)
                                .defaultHeader("Authorization", "Bearer " + accessToken)
                                .build()
                                .get()
                                .retrieve()
                                .toEntity(NaverInfoResponse.class)
                                .block())
                .getBody()
                .toOAuthUser();
    }

    @Override
    public OauthUser login(String code, String state) {
        String url = "https://nid.naver.com/oauth2.0/token"
                + "?client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&grant_type=authorization_code"
                + "&state=" + state
                + "&code=" + code;

        NaverTokenDto naverTokenDto = WebClient.builder()
                .baseUrl(url)
                .build()
                .get()
                .retrieve()
                .onStatus(
                        status -> !status.is2xxSuccessful(),
                        response -> Mono.error(
                                new OntherockException(HttpStatus.valueOf(response.statusCode().value()), "Naver client fail")
                        )
                )
                .toEntity(NaverTokenDto.class)
                .block()
                .getBody();

        return getUser(naverTokenDto.access_token());
    }
}