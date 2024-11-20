package ontherock.chat.application;

import ontherock.chat.domain.ChatMessage;
import ontherock.chat.dto.ChatMessageDTO;
import ontherock.chat.domain.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatMessageService {

    private final ChatMessageBinder chatMessageBinder;

    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    public ChatMessageService(ChatMessageBinder chatMessageBinder, ChatMessageRepository chatMessageRepository) {
        this.chatMessageBinder = chatMessageBinder;
        this.chatMessageRepository = chatMessageRepository;
    }

    public ChatMessageDTO saveChatMessageToDB(ChatMessageDTO chatMessageDTO) {
        ChatMessage chatMessage = chatMessageBinder.bind(chatMessageDTO);
        chatMessageRepository.save(chatMessage);
        return chatMessageDTO;
    }

    public void deleteMessagesByChatRoomId(String chatRoomId) {
        chatMessageRepository.deleteByChatRoomId(chatRoomId);
    }

    public List<ChatMessageDTO> getMessagesByChatRoomId(String chatRoomId) {
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomId(chatRoomId);
        return chatMessages.stream()
                .map(chatMessage -> new ChatMessageDTO(
                        chatMessage.getChatUser(),
                        chatMessage.getMessage(),
                        chatMessage.getMessageTime().toString(),
                        chatMessage.getMessageAction().name(),
                        chatMessage.getChatRoomId()))
                .collect(Collectors.toList());
    }

    public List<ChatMessageDTO> getMessagesByChatRoomIdBefore(String chatRoomId, Date before, int size) {

        Pageable pageable = PageRequest.of(0, size);
        Page<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomIdAndMessageTimeBeforeOrderByMessageTime(chatRoomId, before, pageable);
        return chatMessages.stream()
                .map(chatMessage -> new ChatMessageDTO(
                        chatMessage.getChatUser(),
                        chatMessage.getMessage(),
                        chatMessage.getMessageTime().toString(),
                        chatMessage.getMessageAction().name(),
                        chatMessage.getChatRoomId()))
                .collect(Collectors.toList());
    }
}