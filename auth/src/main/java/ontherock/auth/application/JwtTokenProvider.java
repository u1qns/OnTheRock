package ontherock.auth.application;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import ontherock.auth.common.OntherockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;


@Component
public class JwtTokenProvider {
    private final Key accessTokenKey;
    private final Long accessTokenPeriod;
    private final Key refreshTokenKey;
    private final Long refreshTokenPeriod;

    public JwtTokenProvider(@Value("${jwt.accessKey}") String accessTokenKey,
                            @Value("${jwt.accessPeriod}") Long accessTokenPeriod,
                            @Value("${jwt.refreshKey}") String refreshTokenKey,
                            @Value("${jwt.refreshPeriod}") Long refreshTokenPeriod
    ) {
        this.accessTokenKey = Keys.hmacShaKeyFor(accessTokenKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenPeriod = accessTokenPeriod;
        this.refreshTokenKey = Keys.hmacShaKeyFor(refreshTokenKey.getBytes(StandardCharsets.UTF_8));
        this.refreshTokenPeriod = refreshTokenPeriod;
    }

    private String generateToken(String subject, Long expiredAfter, Key key) {
        return Jwts.builder()
                .setSubject(subject)
                .signWith(accessTokenKey)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusMillis(expiredAfter))) // 3600000
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAccessToken(String subject) {
        return generateToken(subject, accessTokenPeriod, accessTokenKey);
    }

    public String generateRefreshToken() {
        return generateToken(UUID.randomUUID().toString(), refreshTokenPeriod, refreshTokenKey);
    }

    public String extractAccessToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(accessTokenKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            throw new OntherockException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }
    }

    public void validateRefreshToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(refreshTokenKey)
                .build()
                .parseClaimsJws(token);
        } catch (Exception e) {
            throw new OntherockException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }
    }
}
