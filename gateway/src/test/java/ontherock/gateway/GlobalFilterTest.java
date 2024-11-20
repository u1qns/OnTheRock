package ontherock.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GlobalFilterTest {

    private JwtTokenUtil jwtTokenUtil;
    private GlobalFilter globalFilter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        jwtTokenUtil = mock(JwtTokenUtil.class);
        globalFilter = new GlobalFilter(jwtTokenUtil);
        chain = mock(GatewayFilterChain.class);

        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    @Test
    void filter_withValidToken_shouldAddUserIdToHeader() {
        String token = "Bearer validToken";
        String userId = "12345";

        when(jwtTokenUtil.extractUserId("validToken")).thenReturn(Optional.of(userId));

        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header(HttpHeaders.AUTHORIZATION, token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = globalFilter.apply(new GlobalFilter.Config()).filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(argThat(argument -> {
            HttpHeaders headers = argument.getRequest().getHeaders();
            return headers.containsKey("X-User-Id") && headers.getFirst("X-User-Id").equals(userId) &&
                    !headers.containsKey(HttpHeaders.AUTHORIZATION);
        }));
    }

    @Test
    void filter_withInvalidToken_shouldNotAddUserIdToHeader() {
        String token = "Bearer invalidToken";

        when(jwtTokenUtil.extractUserId("invalidToken")).thenReturn(Optional.empty());

        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header(HttpHeaders.AUTHORIZATION, token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = globalFilter.apply(new GlobalFilter.Config()).filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(argThat(argument -> {
            HttpHeaders headers = argument.getRequest().getHeaders();
            return !headers.containsKey("X-User-Id") && headers.containsKey(HttpHeaders.AUTHORIZATION);
        }));
    }

    @Test
    void filter_withoutAuthorizationHeader_shouldProceedWithoutModifications() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = globalFilter.apply(new GlobalFilter.Config()).filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(argThat(argument -> {
            HttpHeaders headers = argument.getRequest().getHeaders();
            return !headers.containsKey("X-User-Id") && !headers.containsKey(HttpHeaders.AUTHORIZATION);
        }));
    }
}
