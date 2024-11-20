package ontherock.chat.domain;

import ontherock.chat.domain.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    void deleteByChatRoomId(String chatRoomId);
    List<ChatMessage> findByChatRoomId(String chatRoomId);
    Page<ChatMessage> findByChatRoomIdAndMessageTimeBeforeOrderByMessageTime(String chatRoomId, Date messageTime, Pageable pageable);


}
