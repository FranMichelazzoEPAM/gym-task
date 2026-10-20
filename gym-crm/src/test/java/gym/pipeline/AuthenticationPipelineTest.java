package gym.pipeline;

import gym.domain.Trainee;
import gym.domain.User;
import gym.repository.TraineeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class AuthenticationPipelineTest {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TraineeRepository traineeRepository;

    @Test
    void shouldAuthenticateValidCredentials() {
        User user = new User("John", "Doe", "john.doe.test",
                passwordEncoder.encode("Secret123"), true);

        Trainee trainee = new Trainee(user, null, null);
        traineeRepository.save(trainee);

        Authentication result = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("john.doe.test", "Secret123")
        );

        assertThat(result.isAuthenticated()).isTrue();
    }

    @Test
    void shouldRejectWrongPassword() {
        User user = new User("Jane", "Doe", "jane.doe.test",
                passwordEncoder.encode("Secret123"), true);

        Trainee trainee = new Trainee(user, null, null);
        traineeRepository.save(trainee);

        assertThatThrownBy(() -> authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("jane.doe.test", "WrongPassword")
        )).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldLockAccountAfterThreeFailedAttempts() {
        User user = new User("Bob", "Smith", "bob.smith.test",
                passwordEncoder.encode("Secret123"), true);
        Trainee trainee = new Trainee(user, null, null);
        traineeRepository.save(trainee);

        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken("bob.smith.test", "WrongPassword")
            )).isInstanceOf(BadCredentialsException.class);
        }

        // 4th attempt - even with CORRECT password - should now be locked
        assertThatThrownBy(() -> authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("bob.smith.test", "Secret123")
        )).isInstanceOf(LockedException.class);
    }
}
