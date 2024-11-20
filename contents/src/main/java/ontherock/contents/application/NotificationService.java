package ontherock.contents.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;


import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class NotificationService {

    private final ObjectMapper objectMapper;
    private WebClient webClient;

    @Value("${rabbitmq.url}")
    private String url;

    @Value("${rabbitmq.auth.username}")
    private String username;

    @Value("${rabbitmq.auth.password}")
    private String password;

    public NotificationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    private void init() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(url);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        this.webClient = WebClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(url)
                .build();
    }

    public void sendNotification(NotificationType type, long senderId, Long recipientId) {
        NotificationRequest notificationRequest = new NotificationRequest(type, senderId, recipientId);

        String payload;
        try {
            payload = objectMapper.writeValueAsString(notificationRequest);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize notification request", e);
            return;
        }
        RabbitMQMessageRequest messageRequest = new RabbitMQMessageRequest(payload);

        String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        log.info("Notificationservice Authe Header : " + authHeader);
        webClient.post()
                .uri("/api/exchanges/%2f/amq.default/publish")
                .header("Content-Type", "application/json")
                .header("Authorization", authHeader)
                .bodyValue(messageRequest)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(response -> log.info("Notification sent successfully"))
                .onErrorResume(error -> {
                    log.error("Failed to send notification: " + error.getMessage());
                    return Mono.empty();
                })
                .subscribe();
    }

    @Getter
    @Setter
    @ToString
    private static class RabbitMQMessageRequest {
        private final Map<String, Object> properties = new HashMap<>();
        private final String routing_key = "notificationQueue";
        private String payload;
        private final String payload_encoding = "string";

        RabbitMQMessageRequest(String payload) {
            this.payload = payload;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class NotificationRequest {
        private NotificationType type;
        private Long senderId;
        private Long recipientId;
    }

    @Getter
    @RequiredArgsConstructor
    public enum NotificationType {
        NEW_POST("new_post"),
        COMMENT("comment"),
        LIKE("like");

        private final String value;
    }
}
