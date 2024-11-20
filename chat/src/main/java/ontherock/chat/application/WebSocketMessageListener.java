package ontherock.chat.application;

import ontherock.chat.domain.ChatMessage;
import ontherock.chat.domain.MessageAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;

@Component
public class WebSocketMessageListener {

    private final SimpMessageSendingOperations messagingTemplate;

    @Autowired
    public WebSocketMessageListener(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent sessionDisconnectEvent) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(sessionDisconnectEvent.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String chatRoomId = (String) headerAccessor.getSessionAttributes().get("chatRoomId");

        if (Objects.nonNull(username)) {
            ChatMessage chatMessage = ChatMessage.builder()
                    .messageAction(MessageAction.LEAVE)
                    .chatUser(username)
                    .chatRoomId(chatRoomId)
                    .build(); // 사용자 나감 알림

            messagingTemplate.convertAndSend("/chat/sub/" + chatRoomId, chatMessage);
        }
    }
}