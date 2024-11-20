package ontherock.chat.application;

import ontherock.chat.domain.ChatMessage;
import ontherock.chat.domain.MessageAction;
import ontherock.chat.dto.ChatMessageDTO;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Component
public class ChatMessageBinder {

    public ChatMessage bind(ChatMessageDTO chatMessageDTO) {
        Date messageTime = Calendar.getInstance().getTime();
        setMessageTime(chatMessageDTO, messageTime);

        return ChatMessage.builder()
                .chatUser(chatMessageDTO.getChatUser())
                .message(chatMessageDTO.getMessage())
                .messageAction(Enum.valueOf(MessageAction.class, chatMessageDTO.getMessageAction()))
                .messageTime(messageTime)
                .chatRoomId(chatMessageDTO.getChatRoomId())
                .build();
    }

    private void setMessageTime(ChatMessageDTO chatMessageDTO, Date messageTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String formattedMessageTime = dateFormat.format(messageTime);
        chatMessageDTO.setMessageTime(formattedMessageTime);
    }
}