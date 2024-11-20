package ontherock.chat.domain;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    private String id = UUID.randomUUID().toString();

    private String chatUser;

    private String message;

    private Date messageTime;

    private MessageAction messageAction;

    private String chatRoomId;
}