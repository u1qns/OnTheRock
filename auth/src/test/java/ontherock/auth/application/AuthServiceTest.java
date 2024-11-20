package ontherock.auth.application;

import ontherock.auth.common.OntherockException;
import ontherock.auth.client.UserServiceClient;
import ontherock.auth.domain.Auth;
import ontherock.auth.domain.AuthRepository;
import ontherock.auth.domain.OauthType;
import ontherock.auth.domain.redis.RefreshToken;
import ontherock.auth.domain.redis.RefreshTokenRepository;
import ontherock.auth.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void reissue_success() {
        String refreshToken = "sample-refresh-token";
        RefreshToken token = new RefreshToken(refreshToken, 1L);
        TokenResponse tokenResponse = new TokenResponse("new-access-token", "new-refresh-token", 1L);

        when(refreshTokenRepository.findById(refreshToken)).thenReturn(Optional.of(token));
        when(jwtTokenProvider.generateAccessToken(anyString())).thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("new-refresh-token");

        TokenResponse response = authService.reissue(refreshToken);

        assertNotNull(response);
        assertEquals("new-access-token", response.accessToken());
        assertEquals("new-refresh-token", response.refreshToken());
        verify(refreshTokenRepository).deleteAllByUserId(token.getUserId());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void reissue_invalidToken_fail() {
        String refreshToken = "invalid-refresh-token";

        when(refreshTokenRepository.findById(refreshToken)).thenReturn(Optional.empty());

        OntherockException exception = assertThrows(OntherockException.class, () -> {
            authService.reissue(refreshToken);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
        assertEquals("Invalid Refresh Token", exception.getMessage());
    }
}
