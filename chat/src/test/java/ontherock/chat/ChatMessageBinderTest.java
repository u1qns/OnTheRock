package ontherock.chat;


import ontherock.chat.application.ChatMessageBinder;
import ontherock.chat.domain.ChatMessage;
import ontherock.chat.domain.MessageAction;
import ontherock.chat.dto.ChatMessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class ChatMessageBinderTest {

    private ChatMessageBinder chatMessageBinder;

    @BeforeEach
    void setUp() {
        chatMessageBinder = new ChatMessageBinder();
    }

    @Test
    void BindDTOToEntity() {
        ChatMessageDTO dto = ChatMessageDTO.builder()
                .chatUser("user1")
                .message("Hello")
                .messageAction("MESSAGE")
                .build();

        ChatMessage chatMessage = chatMessageBinder.bind(dto);

        assertEquals("user1", chatMessage.getChatUser());
        assertEquals("Hello", chatMessage.getMessage());
        assertEquals(MessageAction.MESSAGE, chatMessage.getMessageAction());
        assertNotNull(chatMessage.getMessageTime());
    }
}
