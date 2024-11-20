package ontherock.auth.presentation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ontherock.auth.common.OntherockException;
import ontherock.auth.application.AuthService;
import ontherock.auth.domain.OauthType;
import ontherock.auth.dto.OauthRequest;
import ontherock.auth.dto.TestTokenResponse;
import ontherock.auth.dto.TestUserResponse;
import ontherock.auth.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final static String REFRESH_TOKEN = "refreshToken";
    private final static String ACCESS_TOKEN = "accessToken";
    private final static String USER_ID = "userId";

    @Value("${jwt.accessPeriod}")
    int accessTokenPeriod;
    @Value("${jwt.refreshPeriod}")
    int refreshTokenPeriod;

    @PostMapping("/oauth")
    public ResponseEntity<String> authenticate(
            @RequestBody OauthRequest oauthRequest,
            HttpServletResponse response
    ) {
        TokenResponse tokenResponse = authService.authenticate(oauthRequest);

        setRefreshToken(response, tokenResponse.refreshToken());
        setUserId(response, tokenResponse.id());
        return ResponseEntity.ok(tokenResponse.accessToken());
    }

    private void setRefreshToken(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN, refreshToken);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(refreshTokenPeriod / 1000);
        response.addCookie(refreshTokenCookie);
    }

    private void setAccessToken(HttpServletResponse response, String accessToken) {
        Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN, accessToken);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(accessTokenPeriod / 1000);
        response.addCookie(accessTokenCookie);
    }

    private void setUserId(HttpServletResponse response, Long userId) {
        Cookie userIdCookie = new Cookie(USER_ID, userId.toString());
        userIdCookie.setPath("/");
        userIdCookie.setMaxAge(accessTokenPeriod / 1000);
        response.addCookie(userIdCookie);
    }

    @Operation(description = "Naver callback 전용")
    @GetMapping("/{provider}/callback")
    public void naverLogin(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            HttpServletResponse response,
            @PathVariable String provider
    ) {
        OauthRequest oauthRequest = new OauthRequest(code, OauthType.fromString(provider), state);
        TokenResponse tokenResponse = authService.authenticate(oauthRequest);

        setAccessToken(response, tokenResponse.accessToken());
        setRefreshToken(response, tokenResponse.refreshToken());
        setUserId(response, tokenResponse.id());

        try {
            response.sendRedirect("https://ontherock.lol");
        } catch (Exception e) {
            log.error("OAuth Redirection Fail" + e.getMessage());
            throw new OntherockException(HttpStatus.INTERNAL_SERVER_ERROR, "OAuth Redirection Fail");
        }
    }

    @GetMapping("/reissue")
    public ResponseEntity<String> reissue(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(c -> Objects.equals(c.getName(), REFRESH_TOKEN))
                        .findFirst()
                        .map(Cookie::getValue))
                .orElseThrow(() -> new OntherockException(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token"));


        TokenResponse tokenResponse = authService.reissue(refreshToken);

        setRefreshToken(response, tokenResponse.refreshToken());

        return ResponseEntity.ok(tokenResponse.accessToken());
    }

    @Operation(summary = "Test API", description = "조승기, 이찬민 유저 생성 및 토큰을 반환")
    @GetMapping("/test")
    public ResponseEntity<List<TestTokenResponse>> test() {
        return ResponseEntity.ok(authService.test());
    }

    @Operation(summary = "Test User, Auth maker", description = "이름과 가짜 네이버 아이디에 해당하는 userId, accessToken 발급")
    @GetMapping("/test1/{userName}/{authId}")
    public ResponseEntity<TestTokenResponse> test1(@PathVariable String userName, @PathVariable String authId) {
        return ResponseEntity.ok(authService.testWithName(userName, authId));
    }

    @Operation(summary = "Test login", description = "네이버 로그인과 같이 쿠키에 userid, 토큰을 넣어주는 API")
    @GetMapping("/testlogin")
    public void testLogin(HttpServletResponse response) {
        TestTokenResponse tokenResponse = authService.test().get(0);

        setAccessToken(response, tokenResponse.accessToken());
        setRefreshToken(response, tokenResponse.refreshToken());
        setUserId(response, Long.valueOf(tokenResponse.id()));

        try {
            response.sendRedirect("http://localhost:5173");
        } catch (Exception e) {
            log.error("Redirection Fail" + e.getMessage());
            throw new OntherockException(HttpStatus.INTERNAL_SERVER_ERROR, "OAuth Redirection Fail");
        }
    }

    @Operation(summary = "테스트용 모든 유저 출력")
    @GetMapping("/testuser")
    public ResponseEntity<List<TestUserResponse>> testUserResponses() {
        return ResponseEntity.ok(authService.testUser());
    }
}