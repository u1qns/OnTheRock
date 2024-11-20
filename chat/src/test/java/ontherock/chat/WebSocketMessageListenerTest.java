package ontherock.chat;

import ontherock.chat.application.WebSocketMessageListener;
import ontherock.chat.domain.ChatMessage;
import ontherock.chat.domain.MessageAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class WebSocketMessageListenerTest {

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @InjectMocks
    private WebSocketMessageListener webSocketMessageListener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleWebSocketDisconnectListener_shouldSendLeaveMessage() {

        SessionDisconnectEvent event = mock(SessionDisconnectEvent.class);
        Message<byte[]> message = mock(Message.class);

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("username", "user1");
        sessionAttributes.put("chatRoomId", "room1");
        accessor.setSessionId("testSessionId");
        accessor.setSessionAttributes(sessionAttributes);

        when(event.getMessage()).thenReturn(message);
        when(message.getHeaders()).thenReturn(accessor.toMessageHeaders());

        webSocketMessageListener.handleWebSocketDisconnectListener(event);

        ArgumentCaptor<ChatMessage> chatMessageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(messagingTemplate).convertAndSend(eq("/chat/sub/room1"), chatMessageCaptor.capture());

        ChatMessage capturedChatMessage = chatMessageCaptor.getValue();
        assertEquals(MessageAction.LEAVE, capturedChatMessage.getMessageAction());
        assertEquals("user1", capturedChatMessage.getChatUser());
        assertEquals("room1", capturedChatMessage.getChatRoomId());
    }
}
