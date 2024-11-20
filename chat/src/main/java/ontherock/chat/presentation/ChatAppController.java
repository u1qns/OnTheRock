package ontherock.chat.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import ontherock.chat.application.ChatMessageService;
import ontherock.chat.dto.ChatMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

@Slf4j
@Controller
public class ChatAppController {

    private final ChatMessageService chatMessageService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public ChatAppController(ChatMessageService chatMessageService, SimpMessageSendingOperations messagingTemplate, ObjectMapper objectMapper) {
        this.chatMessageService = chatMessageService;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @MessageMapping("/pub")
    public void chat(@Payload ChatMessageDTO chatMessage) {
        ChatMessageDTO savedMessage = chatMessageService.saveChatMessageToDB(chatMessage);
        messagingTemplate.convertAndSend("/chat/sub/" + savedMessage.getChatRoomId(), savedMessage);
    }



    @MessageMapping("/addUser/{chatRoomId}")
    public void addUser(@DestinationVariable String chatRoomId, @Payload ChatMessageDTO chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", chatMessage.getChatUser());
        headerAccessor.getSessionAttributes().put("chatRoomId", chatRoomId);
        messagingTemplate.convertAndSend("/chat/sub/" + chatRoomId, chatMessage);

        log.info("addUser 작동! chatRoomId: {}", chatRoomId);
    }

    @DeleteMapping("/rooms/{chatRoomId}")
    public ResponseEntity<String> deleteMessagesByChatRoomId(@PathVariable String chatRoomId) {
        chatMessageService.deleteMessagesByChatRoomId(chatRoomId);

        log.info("채팅 기록 삭제 작동 chatRoomId: {}", chatRoomId);

        return ResponseEntity.ok("Messages deleted successfully for chat room ID: " + chatRoomId);
    }

    @GetMapping("/rooms/{chatRoomId}")
    public ResponseEntity<List<ChatMessageDTO>> getMessagesByChatRoomId(@PathVariable String chatRoomId) {
        List<ChatMessageDTO> messages = chatMessageService.getMessagesByChatRoomId(chatRoomId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getMessagesByChatRoomId(
            @PathVariable String chatRoomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date before,
            @RequestParam int size) {
        List<ChatMessageDTO> messages = chatMessageService.getMessagesByChatRoomIdBefore(chatRoomId, before, size);
        return ResponseEntity.ok(messages);
    }


    @GetMapping("/rooms/{chatRoomId}/download")
    public ResponseEntity<byte[]> downloadMessagesByChatRoomId(@PathVariable String chatRoomId) {
        List<ChatMessageDTO> messages = chatMessageService.getMessagesByChatRoomId(chatRoomId);

        try {
            String prettyJsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(messages);
            byte[] txtBytes = prettyJsonString.getBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "messages_" + chatRoomId + ".txt");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(txtBytes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

}
