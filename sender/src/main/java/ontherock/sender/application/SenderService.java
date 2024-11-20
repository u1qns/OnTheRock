package ontherock.sender.application;

import lombok.RequiredArgsConstructor;
import ontherock.sender.domain.Notification;
import ontherock.sender.domain.NotificationRepository;
import ontherock.sender.dto.NotificationResponse;
import ontherock.sender.dto.SendRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SenderService {

    private final NotificationRepository notificationRepository;

    public Void send(SendRequest sendRequest) {
        List<Notification> notifications = sendRequest.recipientIds().stream()
                .map(recipientId -> new Notification(recipientId, sendRequest.message()))
                .toList();

        notificationRepository.saveAll(notifications);
        return null;
    }

    public List<NotificationResponse> get(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(n -> new NotificationResponse(n.getId(), n.getMessage(), n.getCreatedAt()))
                .toList();
    }

    @Transactional
    public Void delete(Long userId, String notificationId) {
        notificationRepository.deleteById(notificationId);
        return null;
    }

    @Transactional
    public Void clear(Long userId) {
        notificationRepository.deleteAllByUserId(userId);
        return null;
    }
}
