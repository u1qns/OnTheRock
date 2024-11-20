package ontherock.auth.domain.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Builder
@RedisHash(value = "refreshToken", timeToLive = 14400)
@AllArgsConstructor
public class RefreshToken {

    @Id
    private String refreshToken;
    private Long userId;
}
