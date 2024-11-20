package ontherock.sender.presentation;

import lombok.RequiredArgsConstructor;
import ontherock.sender.application.SenderService;
import ontherock.sender.common.UserId;
import ontherock.sender.dto.NotificationResponse;
import ontherock.sender.dto.SendRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sender")
@RequiredArgsConstructor
public class SenderController {

    private final SenderService senderService;

    @PostMapping("/notification")
    public ResponseEntity<Void> send(@RequestBody SendRequest sendRequest) {
        senderService.send(sendRequest);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/notification")
    public ResponseEntity<List<NotificationResponse>> getNotification(@UserId Long userId) {
        return ResponseEntity.ok(senderService.get(userId));
    }

    @DeleteMapping("/notification/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@UserId Long userId, @PathVariable String notificationId) {
        senderService.delete(userId, notificationId);
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/notifications/clear")
    public ResponseEntity<Void> clearNotification(@UserId Long userId) {
        senderService.clear(userId);
        return ResponseEntity.ok(null);
    }
}


