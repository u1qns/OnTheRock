package ontherock.message.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ontherock.message.application.NotificationService;
import ontherock.message.common.RabbitMQConfig;
import ontherock.message.dto.NotificationRequest;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveNotification(NotificationRequest notificationRequest) {
        log.info("Received notification: {}", notificationRequest);
        notificationService.sendNotification(notificationRequest);
        log.info("Notification sent successfully {}", notificationRequest.getSenderId());
    }
}
