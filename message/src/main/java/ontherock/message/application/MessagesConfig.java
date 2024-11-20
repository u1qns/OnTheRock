package ontherock.message.application;

import lombok.Getter;
import lombok.Setter;
import ontherock.message.dto.NotificationType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "messages")
@Getter
@Setter
public class MessagesConfig {
    private Map<String, String> notification;

    public String getMessage(NotificationType type) {
        return notification.get(type.getValue());
    }
}