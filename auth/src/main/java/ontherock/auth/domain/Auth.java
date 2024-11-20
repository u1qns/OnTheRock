package ontherock.auth.domain;

import jakarta.persistence.*;
import lombok.*;
import ontherock.auth.domain.OauthType;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Auth {

    @Id
    @Column
    private String authId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('NAVER')")
    private OauthType oauthType;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    void createdAt() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void updatedAt() {
        this.updatedAt = LocalDateTime.now();
    }
}
