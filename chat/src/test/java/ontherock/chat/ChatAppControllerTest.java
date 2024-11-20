package ontherock.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import ontherock.chat.application.ChatMessageService;
import ontherock.chat.dto.ChatMessageDTO;
import ontherock.chat.presentation.ChatAppController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class ChatAppControllerTest {

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    private ChatAppController chatAppController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        chatAppController = new ChatAppController(chatMessageService, messagingTemplate, objectMapper);
    }

    @Test
    void chat_shouldReturnSavedMessage() {
        ChatMessageDTO dto = new ChatMessageDTO();
        when(chatMessageService.saveChatMessageToDB(dto)).thenReturn(dto);

        chatAppController.chat(dto);

        verify(messagingTemplate, times(1)).convertAndSend("/chat/sub/" + dto.getChatRoomId(), dto);
    }

    @Test
    void addUser_shouldAddUserToSession() {
        ChatMessageDTO dto = ChatMessageDTO.builder().chatUser("user1").chatRoomId("room1").build();
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        accessor.setSessionId("testSessionId");
        accessor.setSessionAttributes(new HashMap<>());

        chatAppController.addUser("room1", dto, accessor);

        assertEquals("user1", accessor.getSessionAttributes().get("username"));
        assertEquals("room1", accessor.getSessionAttributes().get("chatRoomId"));
        verify(messagingTemplate, times(1)).convertAndSend("/chat/sub/room1", dto);
    }

    @Test
    void deleteMessagesByChatRoomId_shouldReturnSuccessMessage() {
        String chatRoomId = "room123";

        ResponseEntity<String> response = chatAppController.deleteMessagesByChatRoomId(chatRoomId);

        assertEquals("Messages deleted successfully for chat room ID: " + chatRoomId, response.getBody());
        verify(chatMessageService, times(1)).deleteMessagesByChatRoomId(chatRoomId);
    }

    @Test
    void getMessagesByChatRoomId_shouldReturnChatMessages() {
        String chatRoomId = "room123";
        ChatMessageDTO message = new ChatMessageDTO("user1", "Hello, World!", "2024-07-22T12:52:18.751+09:00", "MESSAGE", chatRoomId);
        List<ChatMessageDTO> messages = Collections.singletonList(message);

        given(chatMessageService.getMessagesByChatRoomId(anyString())).willReturn(messages);

        ResponseEntity<List<ChatMessageDTO>> response = chatAppController.getMessagesByChatRoomId(chatRoomId);

        assertEquals(messages, response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("user1", response.getBody().get(0).getChatUser());
        verify(chatMessageService, times(1)).getMessagesByChatRoomId(chatRoomId);
    }

    @Test
    void downloadMessagesByChatRoomId_shouldReturnFormattedMessages() throws Exception {
        String chatRoomId = "room123";
        ChatMessageDTO message = new ChatMessageDTO("user1", "Hello, World!", "2024-07-22T12:52:18.751+09:00", "MESSAGE", chatRoomId);
        List<ChatMessageDTO> messages = Collections.singletonList(message);

        given(chatMessageService.getMessagesByChatRoomId(anyString())).willReturn(messages);

        ResponseEntity<byte[]> response = chatAppController.downloadMessagesByChatRoomId(chatRoomId);

        String expectedContent = "[ {\n" +
                "  \"chatUser\" : \"user1\",\n" +
                "  \"message\" : \"Hello, World!\",\n" +
                "  \"messageTime\" : \"2024-07-22T12:52:18.751+09:00\",\n" +
                "  \"messageAction\" : \"MESSAGE\",\n" +
                "  \"chatRoomId\" : \"room123\"\n" +
                "} ]";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode expectedJson = mapper.readTree(expectedContent);
        JsonNode actualJson = mapper.readTree(new String(response.getBody()));

        assertEquals(expectedJson, actualJson);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("messages_room123.txt", response.getHeaders().getContentDisposition().getFilename());
        assertEquals("text/plain", response.getHeaders().getContentType().toString());
    }

    @Test
    void getMessagesByChatRoomIdBefore_shouldReturnChatMessages() {
        String chatRoomId = "room123";
        Date beforeDate = new Date();
        ChatMessageDTO message = new ChatMessageDTO("user1", "Hello, World!", "2024-07-22T12:52:18.751+09:00", "MESSAGE", chatRoomId);
        List<ChatMessageDTO> messages = Collections.singletonList(message);

        given(chatMessageService.getMessagesByChatRoomIdBefore(anyString(), any(Date.class), anyInt())).willReturn(messages);

        ResponseEntity<List<ChatMessageDTO>> response = chatAppController.getMessagesByChatRoomId(chatRoomId, beforeDate, 10);

        assertEquals(messages, response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("user1", response.getBody().get(0).getChatUser());
        verify(chatMessageService, times(1)).getMessagesByChatRoomIdBefore(chatRoomId, beforeDate, 10);
    }
}
