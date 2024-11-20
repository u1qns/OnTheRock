package ontherock.auth.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import ontherock.auth.application.AuthService;
import ontherock.auth.domain.OauthType;
import ontherock.auth.dto.OauthRequest;
import ontherock.auth.dto.TokenResponse;
import ontherock.auth.common.OntherockException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    AuthService authService;
    @SpyBean
    AuthController authController;

    @AfterEach
    void tearDown() {
        Mockito.reset(authService);
    }

    @Test
    void authenticate_success() throws Exception {
        OauthRequest oauthRequest = new OauthRequest("sample-token", OauthType.NAVER, "state");
        TokenResponse tokenResponse = new TokenResponse("access-token", "refresh-token", 1L);
        String requestJson = objectMapper.writeValueAsString(oauthRequest);

        when(authService.authenticate(any(OauthRequest.class))).thenReturn(tokenResponse);

        RequestBuilder request = post("/auth/oauth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string("access-token"))
                .andExpect(cookie().value("refreshToken", "refresh-token"));
    }

    @Test
    void reissue_success() throws Exception {
        TokenResponse tokenResponse = new TokenResponse("new-access-token", "new-refresh-token", 1L);

        when(authService.reissue(anyString())).thenReturn(tokenResponse);

        Cookie refreshTokenCookie = new Cookie("refreshToken", "old-refresh-token");

        RequestBuilder request = MockMvcRequestBuilders.get("/auth/reissue")
                .cookie(refreshTokenCookie);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string("new-access-token"))
                .andExpect(cookie().value("refreshToken", "new-refresh-token"));
    }

    @Test
    void reissue_invalidToken_fail() throws Exception {
        given(authService.reissue(anyString())).willThrow(new OntherockException(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token"));

        RequestBuilder request = MockMvcRequestBuilders.get("/auth/reissue");

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("{\"status\":401,\"message\":\"Invalid Refresh Token\"}"));
    }

    @Test
    void naverLogin_success() throws Exception {
        String code = "sample-code";
        String state = "sample-state";
        TokenResponse tokenResponse = new TokenResponse("access-token", "refresh-token", 1L);

        when(authService.authenticate(any(OauthRequest.class))).thenReturn(tokenResponse);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/auth/naver/callback")
                .param("code", code)
                .param("state", state));

        result.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://ontherock.lol"))
                .andExpect(cookie().value("refreshToken", "refresh-token"))
                .andExpect(cookie().value("accessToken", "access-token"));

    }
}