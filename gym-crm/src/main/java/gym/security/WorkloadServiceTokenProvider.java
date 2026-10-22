package gym.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class WorkloadServiceTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMillis;

    public WorkloadServiceTokenProvider(
            @Value("${trainer-workload-service.jwt.secret}") String secret,
            @Value("${trainer-workload-service.jwt.expiration}") long expirationMillis) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    public String generateToken() {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject("gym-main-app")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}