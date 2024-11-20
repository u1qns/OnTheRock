package ontherock.sender.application;

import ontherock.sender.config.EnableMongoTestServer;
import ontherock.sender.domain.Notification;
import ontherock.sender.domain.NotificationRepository;
import ontherock.sender.dto.NotificationResponse;
import ontherock.sender.dto.SendRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnableMongoTestServer
public class SenderServiceTest {

    @Autowired
    private SenderService senderService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.getDb().drop();
    }

    @Test
    void send_success() {
        SendRequest sendRequest = new SendRequest(Arrays.asList(1L, 2L, 3L), "Hello World");

        senderService.send(sendRequest);

        List<Notification> notifications1 = notificationRepository.findByUserIdOrderByCreatedAtDesc(1L);
        List<Notification> notifications2 = notificationRepository.findByUserIdOrderByCreatedAtDesc(2L);
        List<Notification> notifications3 = notificationRepository.findByUserIdOrderByCreatedAtDesc(3L);
        List<Notification> notifications4 = notificationRepository.findByUserIdOrderByCreatedAtDesc(4L);

        assertEquals(1, notifications1.size());
        assertEquals(1, notifications2.size());
        assertEquals(1, notifications3.size());
        assertEquals(0, notifications4.size());
        assertEquals("Hello World", notifications1.get(0).getMessage());
        assertEquals("Hello World", notifications2.get(0).getMessage());
        assertEquals("Hello World", notifications3.get(0).getMessage());
    }

    @Test
    void get_success() {
        Long userId = 1L;
        List<Notification> notifications = Arrays.asList(
                new Notification(1L, "Message 1", LocalDateTime.now().minusDays(1)),
                new Notification(1L, "Message 2", LocalDateTime.now())
        );

        notificationRepository.saveAll(notifications);

        List<NotificationResponse> response = senderService.get(userId);

        assertEquals(2, response.size());
        assertEquals("Message 2", response.get(0).content());
        assertEquals("Message 1", response.get(1).content());
    }

    @Test
    void delete_success() {
        String notificationId = notificationRepository.save(new Notification(1L, "Test Message", LocalDateTime.now())).getId();

        senderService.delete(1L, notificationId);

        assertFalse(notificationRepository.findById(notificationId).isPresent());
    }
}
