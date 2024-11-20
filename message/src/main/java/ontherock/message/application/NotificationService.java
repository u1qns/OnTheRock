package ontherock.message.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ontherock.message.client.SenderServiceClient;
import ontherock.message.common.OntherockException;
import ontherock.message.domain.Follower;
import ontherock.message.domain.FollowerRepository;
import ontherock.message.domain.User;
import ontherock.message.domain.UserRepository;
import ontherock.message.dto.NotificationRequest;
import ontherock.message.dto.SendRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final FollowerRepository followerRepository;
    private final UserRepository userRepository;
    private final MessagesConfig messagesConfig;
    private final SenderServiceClient senderServiceClient;

    public void sendNotification(NotificationRequest notificationRequest) {
        String messageTemplate = messagesConfig.getMessage(notificationRequest.getType());
        User sender = userRepository.findById(notificationRequest.getSenderId())
                .orElseThrow(() -> {
                    log.error("sendNotification : {}", notificationRequest);
                    return null;
                });
        String message = MessageFormat.format(messageTemplate, sender.getNickname());

        if (!Objects.isNull(notificationRequest.getRecipientId())) {
            SendRequest sendRequest = new SendRequest(List.of(notificationRequest.getRecipientId()), message);
            senderServiceClient.send(sendRequest);
            return;
        }

        List<Long> recipients = followerRepository.findByUser(sender)
                .stream()
                .map(Follower::getFollower)
                .map(User::getUserId)
                .toList();
        try {
            senderServiceClient.send(new SendRequest(recipients, message));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
