package ontherock.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class LogGlobalFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(LogGlobalFilter.class);

    @Override
    @Order(0)
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String incomingUri = exchange.getRequest().getURI().toString();
        logger.info("Incoming request URI: {}", incomingUri);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            String outgoingUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR).toString();
            logger.info("Outgoing request URI: {}", outgoingUri);
        }));
    }
}
