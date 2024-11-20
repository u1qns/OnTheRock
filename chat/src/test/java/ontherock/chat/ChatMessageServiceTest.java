package ontherock.chat;

import ontherock.chat.application.ChatMessageBinder;
import ontherock.chat.application.ChatMessageService;
import ontherock.chat.domain.ChatMessage;
import ontherock.chat.domain.ChatMessageRepository;
import ontherock.chat.dto.ChatMessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatMessageServiceTest {

    @Mock
    private ChatMessageBinder chatMessageBinder;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveChatMessageToDB() {
        ChatMessageDTO dto = new ChatMessageDTO();
        ChatMessage chatMessage = new ChatMessage();
        when(chatMessageBinder.bind(dto)).thenReturn(chatMessage);

        chatMessageService.saveChatMessageToDB(dto);

        verify(chatMessageRepository).save(chatMessage);
    }

    @Test
    void deleteMessagesByChatRoomId() {
        String chatRoomId = "room123";

        chatMessageService.deleteMessagesByChatRoomId(chatRoomId);

        verify(chatMessageRepository).deleteByChatRoomId(chatRoomId);
    }
}
