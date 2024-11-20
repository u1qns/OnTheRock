package ontherock.streaming.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamingResponse {
    private String message;
    private String sessionId;
    private String token;

    public StreamingResponse(String message) {
        this.message = message;
    }
}