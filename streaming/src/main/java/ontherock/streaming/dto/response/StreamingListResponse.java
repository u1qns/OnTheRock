package ontherock.streaming.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamingListResponse {
    private String userId;
    private String sessionId;
    private LocalDateTime createdAt;
}
