package ontherock.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class ApiGatewayConfiguration {
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder
                .routes()
                .route(r -> r.path("/auth/v3/api-docs").and().method(HttpMethod.GET).uri("lb://auth"))
                .route(r -> r.path("/user/v3/api-docs").and().method(HttpMethod.GET).uri("lb://user"))
                .route(r -> r.path("/sender/v3/api-docs").and().method(HttpMethod.GET).uri("lb://sender"))
                .route(r -> r.path("/contents/v3/api-docs").and().method(HttpMethod.GET).uri("lb://contents"))
                .route(r -> r.path("/message/v3/api-docs").and().method(HttpMethod.GET).uri("lb://message"))
                .route(r -> r.path("/streaming/v3/api-docs").and().method(HttpMethod.GET).uri("lb://streaming"))
                .build();
    }
}

