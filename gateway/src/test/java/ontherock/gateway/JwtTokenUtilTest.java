package ontherock.gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import ontherock.gateway.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;
    private Key accessKey;

    @BeforeEach
    void setUp() {
        String accessKeyString = "mysecretkeymysecretkeymysecretkey"; // 테스트용 시크릿 키
        this.accessKey = Keys.hmacShaKeyFor(accessKeyString.getBytes(StandardCharsets.UTF_8));
        this.jwtTokenUtil = new JwtTokenUtil(accessKeyString);
    }

    @Test
    void extractUserId_success() {
        String userId = "12345";
        String token = Jwts.builder()
                .setSubject(userId)
                .setExpiration(new Date(System.currentTimeMillis() + 60000)) // 1분 만료
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();

        Optional<String> result = jwtTokenUtil.extractUserId(token);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get());
    }

    @Test
    void extractUserId_invalidToken() {
        String invalidToken = "invalidToken";

        Optional<String> result = jwtTokenUtil.extractUserId(invalidToken);

        assertFalse(result.isPresent());
    }

    @Test
    void extractUserId_expiredToken() {
        String userId = "12345";
        String expiredToken = Jwts.builder()
                .setSubject(userId)
                .setExpiration(new Date(System.currentTimeMillis() - 60000)) // 만료된 토큰
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();

        Optional<String> result = jwtTokenUtil.extractUserId(expiredToken);

        assertFalse(result.isPresent());
    }
}
