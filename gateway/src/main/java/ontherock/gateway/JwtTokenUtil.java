package ontherock.gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Optional;

@Component
public class JwtTokenUtil {
    private final Key accessKey;

    public JwtTokenUtil(@Value("${jwt.accessKey}") String accessKey) {
        this.accessKey = Keys.hmacShaKeyFor(accessKey.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<String> extractUserId(String token) {
        try {
            String userId = Jwts.parserBuilder()
                    .setSigningKey(accessKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            return Optional.of(userId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
