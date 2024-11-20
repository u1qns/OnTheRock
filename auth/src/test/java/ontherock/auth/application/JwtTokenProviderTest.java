package ontherock.auth.application;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import ontherock.auth.common.OntherockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String ACCESS_KEY_STRING = "accesstestaccesstestaccesstestaccesstestaccesstest";
    private static final Long ACCESS_PERIOD = 3600000L; // 1 hour
    private static final String REFRESH_KEY_STRING = "refreshtestrefreshtestrefreshtestrefreshtestrefreshtest";
    private static final Long REFRESH_PERIOD = 86400000L; // 1 day
    private static final Key ACCESS_KEY = Keys.hmacShaKeyFor(ACCESS_KEY_STRING.getBytes(StandardCharsets.UTF_8));
    private static final Key REFRESH_KEY = Keys.hmacShaKeyFor(REFRESH_KEY_STRING.getBytes(StandardCharsets.UTF_8));
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(ACCESS_KEY_STRING, ACCESS_PERIOD, REFRESH_KEY_STRING, REFRESH_PERIOD);
    }

    @Test
    void generateAccessToken_success() {
        String subject = "test-user";
        String token = jwtTokenProvider.generateAccessToken(subject);

        assertNotNull(token);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(ACCESS_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(subject, claims.getSubject());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void generateRefreshToken_success() {
        String token = jwtTokenProvider.generateRefreshToken();

        assertNotNull(token);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(REFRESH_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertNotNull(claims.getSubject());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void extractAccessToken_success() {
        String subject = "test-user";
        String token = jwtTokenProvider.generateAccessToken(subject);

        String extractedSubject = jwtTokenProvider.extractAccessToken(token);

        assertEquals(subject, extractedSubject);
    }

    @Test
    void extractAccessToken_invalidToken_fail() {
        String invalidToken = "invalid-token";

        OntherockException exception = assertThrows(OntherockException.class, () -> {
            jwtTokenProvider.extractAccessToken(invalidToken);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
        assertEquals("INVALID_TOKEN", exception.getMessage());
    }

    @Test
    void extractAccessToken_expiredToken_fail() {
        String subject = "test-user";
        String expiredToken = Jwts.builder()
                .setSubject(subject)
                .signWith(ACCESS_KEY)
                .setIssuedAt(new Date(System.currentTimeMillis() - 3600000L)) // 1 hour ago
                .setExpiration(new Date(System.currentTimeMillis() - 1800000L)) // 30 minutes ago
                .compact();

        OntherockException exception = assertThrows(OntherockException.class, () -> {
            jwtTokenProvider.extractAccessToken(expiredToken);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
        assertEquals("INVALID_TOKEN", exception.getMessage());
    }


    @Test
    void validateRefreshToken_success() {
        String token = jwtTokenProvider.generateRefreshToken();

        assertDoesNotThrow(() -> jwtTokenProvider.validateRefreshToken(token));
    }

    @Test
    void validateRefreshToken_invalidToken_fail() {
        String invalidToken = "invalid-token";

        OntherockException exception = assertThrows(OntherockException.class, () -> {
            jwtTokenProvider.validateRefreshToken(invalidToken);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
        assertEquals("INVALID_TOKEN", exception.getMessage());
    }

    @Test
    void validateRefreshToken_expiredToken_fail() {
        String expiredToken = Jwts.builder()
                .setSubject("test-user")
                .signWith(REFRESH_KEY)
                .setIssuedAt(new Date(System.currentTimeMillis() - 86400000L)) // 1 day ago
                .setExpiration(new Date(System.currentTimeMillis() - 3600000L)) // 1 hour ago
                .compact();

        OntherockException exception = assertThrows(OntherockException.class, () -> {
            jwtTokenProvider.validateRefreshToken(expiredToken);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
        assertEquals("INVALID_TOKEN", exception.getMessage());
    }
}
