package ontherock.gateway;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Component
public class GlobalFilter extends AbstractGatewayFilterFactory<GlobalFilter.Config> {

    private final JwtTokenUtil jwtTokenUtil;

    public GlobalFilter(JwtTokenUtil jwtTokenUtil) {
        super(Config.class);
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            Optional<String> optionalUserId = getAuthorizationToken(exchange)
                    .flatMap(jwtTokenUtil::extractUserId);
            log.info("filter" + optionalUserId.orElse(""));
            optionalUserId.ifPresent(s -> modifyRequestWithUserId(exchange, s));

            if (config.isPreLogger()) {
                log.info("Pre Filter baseMessage: " + config.getBaseMessage());
            }

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                if (config.isPostLogger()) {
                    log.info("Post Filter baseMessage: " + config.getBaseMessage());
                }
            }));
        };
    }

    @Data
    public static class Config {
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }

    private Optional<String> getAuthorizationToken(ServerWebExchange exchange) {
        log.info("getAuthorizationToken" + exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        return Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(token -> token.startsWith("Bearer "))
                .map(token -> token.substring(7));
    }

    private void modifyRequestWithUserId(ServerWebExchange exchange, String userId) {
        exchange.mutate()
                .request(r -> r.headers(headers -> {
                    headers.set("X-User-Id", userId);
                    headers.remove(HttpHeaders.AUTHORIZATION);
                }))
                .build();
    }
}
