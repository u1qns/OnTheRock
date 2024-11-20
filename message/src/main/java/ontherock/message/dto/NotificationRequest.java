package ontherock.message.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NotificationRequest {
    private NotificationType type;
    private Long senderId;
    private Long recipientId;

    @JsonCreator
    public NotificationRequest(
            @JsonProperty("type") NotificationType type,
            @JsonProperty("senderId") Long senderId,
            @JsonProperty("recipientId") Long recipientId
    ) {
        this.type = type;
        this.senderId = senderId;
        this.recipientId = recipientId;
    }
}