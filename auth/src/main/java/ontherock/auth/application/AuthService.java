package ontherock.auth.application;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ontherock.auth.client.OAuthNaverClient;
import ontherock.auth.common.OntherockException;
import ontherock.auth.client.UserServiceClient;
import ontherock.auth.domain.Auth;
import ontherock.auth.domain.AuthRepository;
import ontherock.auth.domain.OauthType;
import ontherock.auth.domain.redis.RefreshToken;
import ontherock.auth.domain.redis.RefreshTokenRepository;
import ontherock.auth.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final OAuthClientFactory oAuthClientFactory;
    private final UserServiceClient userServiceClient;
    private final AuthRepository authRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenResponse authenticate(OauthRequest oauthRequest) {
        OauthUser oauthUser = oAuthClientFactory
                .getClient(oauthRequest.type())
                .login(oauthRequest.token(), oauthRequest.state());

        Auth auth = authRepository.findByAuthIdAndOauthType(oauthUser.userId(), oauthRequest.type())
                .orElseGet(() -> register(oauthUser, oauthRequest.type()));

        return generateToken(auth.getUserId());
    }

    private Auth register(OauthUser oauthUser, OauthType type) {
        UserRegisterRequest registerRequest = new UserRegisterRequest(oauthUser.userId(), oauthUser.name());
        UserRegisterResponse registerResponse = userServiceClient.register(registerRequest);

        return authRepository.save(Auth.builder()
                .authId(oauthUser.userId())
                .userId(registerResponse.userId())
                .oauthType(type)
                .build());
    }

    private TokenResponse generateToken(Long userId) {
        String accessToken = jwtTokenProvider.generateAccessToken(userId.toString());
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        refreshTokenRepository.deleteAllByUserId(userId);
        refreshTokenRepository.save(new RefreshToken(refreshToken, userId));

        return new TokenResponse(accessToken, refreshToken, userId);
    }

    public TokenResponse reissue(String refreshToken) {
        jwtTokenProvider.validateRefreshToken(refreshToken);

        Long userId = refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new OntherockException(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token"))
                .getUserId();

        return generateToken(userId);
    }

    public List<TestTokenResponse> test() {
        Auth seungki = authRepository.findByAuthIdAndOauthType("1", OauthType.NAVER)
                .orElseGet(() -> register(new OauthUser("1", "조승기", OauthType.NAVER), OauthType.NAVER));

        Auth chanmin = authRepository.findByAuthIdAndOauthType("2", OauthType.NAVER)
                .orElseGet(() -> register(new OauthUser("2", "이찬민", OauthType.NAVER), OauthType.NAVER));
        log.error("seungki: {}", seungki.getUserId());
        log.error("chanmin: {}", chanmin.getUserId());

        var seungkiToken = generateToken(seungki.getUserId());
        var chanminToken = generateToken(chanmin.getUserId());

        return List.of(
                new TestTokenResponse(seungki.getUserId().toString(), seungkiToken.accessToken(), seungkiToken.refreshToken()),
                new TestTokenResponse(chanmin.getUserId().toString(), chanminToken.accessToken(), chanminToken.refreshToken())
        );
    }
    public TestTokenResponse testWithName(String name, String authId) {
        var auth = authRepository.findByAuthIdAndOauthType(authId, OauthType.NAVER)
                .orElseGet(() -> register(new OauthUser(authId, name, OauthType.NAVER), OauthType.NAVER));
        var token = generateToken(auth.getUserId());

        return new TestTokenResponse(auth.getUserId().toString(), token.accessToken(), token.refreshToken());
    }

    public List<TestUserResponse> testUser() {
        return authRepository.findAll()
                .stream()
                .map(auth -> {
                    UserResponse ur = userServiceClient.test(auth.getUserId());
                    return new TestUserResponse(auth.getUserId().toString(), ur.nickname(), generateToken(auth.getUserId()).accessToken());
                })
                .toList();
    }
}
