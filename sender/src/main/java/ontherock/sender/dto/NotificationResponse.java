package ontherock.sender.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        String id,
        String content,
        LocalDateTime createdAt
) {
}
