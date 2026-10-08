package gym.service;

import gym.domain.User;
import gym.repository.UserRepository;
import gym.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void changePassword_withCorrectOldPassword_updatesPassword() {
        User user = new User("John", "Doe", "john.doe", "encoded-old", true);
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encoded-new");

        userService.changePassword("john.doe", "oldPass", "newPass");

        assertThat(user.getPassword()).isEqualTo("encoded-new");
        verify(passwordEncoder).matches("oldPass", "encoded-old");
        verify(passwordEncoder).encode("newPass");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_withWrongOldPassword_throwsIllegalArgumentException() {
        User user = new User("John", "Doe", "john.doe", "encoded-old", true);
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPass", "encoded-old")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("john.doe", "wrongOldPass", "newPass"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_withUnknownUsername_throwsNoSuchElementException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changePassword("ghost", "any", "newPass"))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }
}
