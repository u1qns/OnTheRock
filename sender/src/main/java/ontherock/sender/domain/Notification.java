package ontherock.sender.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "notifications")
@NoArgsConstructor
public class Notification {

    @Id
    private String id;

    @Indexed
    private Long userId;

    private String message;

    @CreatedDate
    @Indexed(expireAfterSeconds = 604800) // 7일
    private LocalDateTime createdAt;

    public Notification(Long userId, String message) {
        this.userId = userId;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    public Notification(Long userId, String message, LocalDateTime createdAt) {
        this.userId = userId;
        this.message = message;
        this.createdAt = createdAt;
    }
}