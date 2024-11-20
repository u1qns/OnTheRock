package ontherock.sender.domain;

import ontherock.sender.config.EnableMongoTestServer;
import ontherock.sender.domain.Notification;
import ontherock.sender.domain.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@EnableMongoTestServer
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setup() {
        mongoTemplate.getDb().drop();
    }

    @Test
    void saveAndFindByUserId_success() {
        Notification notification1 = new Notification(1L, "Message 1", LocalDateTime.now().minusDays(1));
        Notification notification2 = new Notification(1L, "Message 2", LocalDateTime.now());

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);

        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(1L);

        assertEquals(2, notifications.size());
        assertEquals("Message 2", notifications.get(0).getMessage());
        assertEquals("Message 1", notifications.get(1).getMessage());
    }

    @Test
    void deleteById_success() {
        Notification notification = new Notification(1L, "Message to delete", LocalDateTime.now());
        notificationRepository.save(notification);
        String savedID = notification.getId();

        assertTrue(notificationRepository.findById(savedID).isPresent());

        notificationRepository.deleteById(savedID);

        assertFalse(notificationRepository.findById(savedID).isPresent());
    }

    @Test
    void findByUserId_empty() {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(1L);
        assertTrue(notifications.isEmpty());
    }
}
