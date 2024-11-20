package ontherock.streaming.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StreamingRequest {
    private String userId;     // 사용자 ID
    private String sessionId; // 세션 ID
}