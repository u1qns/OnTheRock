package ontherock.user.dto;

import java.time.LocalDateTime;

public record StreamingListResponse(
        Long userId,
        String sessionId,
        LocalDateTime localDateTime
) {
}
