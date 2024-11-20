package ontherock.chat.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {

    private String chatUser;

    private String message;

    private String messageTime;

    private String messageAction;

    private String chatRoomId;
}