package ontherock.message.application;

import ontherock.message.client.SenderServiceClient;
import ontherock.message.common.OntherockException;
import ontherock.message.domain.Follower;
import ontherock.message.domain.FollowerRepository;
import ontherock.message.domain.User;
import ontherock.message.domain.UserRepository;
import ontherock.message.dto.NotificationRequest;
import ontherock.message.dto.NotificationType;
import ontherock.message.dto.SendRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private FollowerRepository followerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessagesConfig messagesConfig;

    @Mock
    private SenderServiceClient senderServiceClient;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendNotification_withRecipient() {
        NotificationRequest notificationRequest = new NotificationRequest(NotificationType.STREAMING, 1L, 2L);

        User sender = User.builder().userId(notificationRequest.getSenderId()).nickname("홍길동").build();
        String messageTemplate = "{0}님이 스트리밍을 시작했습니다.";
        String expectedMessage = "홍길동님이 스트리밍을 시작했습니다.";

        when(userRepository.findById(notificationRequest.getSenderId())).thenReturn(Optional.of(sender));
        when(messagesConfig.getMessage(NotificationType.STREAMING)).thenReturn(messageTemplate);

        SendRequest expectedSendRequest = new SendRequest(List.of(notificationRequest.getRecipientId()), expectedMessage);

        notificationService.sendNotification(notificationRequest);

        verify(senderServiceClient, times(1)).send(expectedSendRequest);
    }

    @Test
    void sendNotification_toFollowers() {
        NotificationRequest notificationRequest = new NotificationRequest(NotificationType.FOLLOW, 1L, null);

        User sender = User.builder().userId(notificationRequest.getSenderId()).nickname("홍길동").build();
        String messageTemplate = "{0}님이 당신을 팔로우 했습니다.";
        String expectedMessage = "홍길동님이 당신을 팔로우 했습니다.";

        Follower follower1 = Follower.builder().follower(User.builder().userId(2L).build()).build();
        Follower follower2 = Follower.builder().follower(User.builder().userId(3L).build()).build();

        when(userRepository.findById(notificationRequest.getSenderId())).thenReturn(Optional.of(sender));
        when(messagesConfig.getMessage(NotificationType.FOLLOW)).thenReturn(messageTemplate);
        when(followerRepository.findByUser(sender)).thenReturn(List.of(follower1, follower2));

        SendRequest expectedSendRequest = new SendRequest(List.of(2L, 3L), expectedMessage);

        notificationService.sendNotification(notificationRequest);

        verify(senderServiceClient, times(1)).send(expectedSendRequest);
    }

    @Test
    void sendNotification_senderNotFound() {
        NotificationRequest notificationRequest = new NotificationRequest(NotificationType.FOLLOW, 1L, null);

        when(userRepository.findById(notificationRequest.getSenderId())).thenReturn(Optional.empty());

        verify(senderServiceClient, times(0)).send(null);
    }
}
