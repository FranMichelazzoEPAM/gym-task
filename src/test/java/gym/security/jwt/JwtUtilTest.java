package gym.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void shouldGenerateAndValidateToken() {
        String token = jwtUtil.generateToken("john.doe.test", "TRAINEE");

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("john.doe.test");
        assertThat(jwtUtil.isTokenExpired(token)).isFalse();

        UserDetails userDetails = new User("john.doe.test", "irrelevant", java.util.List.of());
        assertThat(jwtUtil.validateToken(token, userDetails)).isTrue();
    }

    @Test
    void shouldRejectTokenForDifferentUser() {
        String token = jwtUtil.generateToken("john.doe.test", "TRAINEE");

        UserDetails someoneElse = new User("someone.else", "irrelevant", java.util.List.of());
        assertThat(jwtUtil.validateToken(token, someoneElse)).isFalse();
    }
}
