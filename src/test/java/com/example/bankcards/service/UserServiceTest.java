package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .build();
    }

    @Nested
    @DisplayName("Find User Tests")
    class FindUserTests {

        @Test
        @DisplayName("Should find user by username")
        void getByUsername_Success() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            User result = userService.getByUsername("testuser");

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should throw exception when username not found")
        void getByUsername_NotFound() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getByUsername("unknown"))
                    .isInstanceOf(UsernameNotFoundException.class);
        }

        @Test
        @DisplayName("Should find user by username or email")
        void getByUsernameOrEmail_Success() {
            when(userRepository.findByUsernameOrEmail("test@example.com", "test@example.com"))
                    .thenReturn(Optional.of(testUser));

            User result = userService.getByUsernameOrEmail("test@example.com");

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should return UserDetailsService")
        void userDetailsService_ShouldWork() {
            when(userRepository.findByUsernameOrEmail("testuser", "testuser"))
                    .thenReturn(Optional.of(testUser));

            UserDetailsService service = userService.userDetailsService();
            var userDetails = service.loadUserByUsername("testuser");

            assertThat(userDetails).isNotNull();
            assertThat(userDetails.getUsername()).isEqualTo("testuser");
        }
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        void create_Success() {
            User newUser = User.builder()
                    .username("newuser")
                    .email("new@example.com")
                    .password("password")
                    .firstName("New")
                    .lastName("User")
                    .role(Role.USER)
                    .build();

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(newUser);

            User result = userService.create(newUser);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("newuser");
            verify(userRepository).save(newUser);
        }

        @Test
        @DisplayName("Should throw exception when username exists")
        void create_UsernameExists() {
            User newUser = User.builder()
                    .username("existinguser")
                    .email("new@example.com")
                    .password("password")
                    .firstName("New")
                    .lastName("User")
                    .role(Role.USER)
                    .build();

            when(userRepository.existsByUsername("existinguser")).thenReturn(true);

            assertThatThrownBy(() -> userService.create(newUser))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("username already exists");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when email exists")
        void create_EmailExists() {
            User newUser = User.builder()
                    .username("newuser")
                    .email("existing@example.com")
                    .password("password")
                    .firstName("New")
                    .lastName("User")
                    .role(Role.USER)
                    .build();

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.create(newUser))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("email already exists");

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Existence Check Tests")
    class ExistenceCheckTests {

        @Test
        @DisplayName("Should check if username exists")
        void existsByUsername() {
            when(userRepository.existsByUsername("testuser")).thenReturn(true);
            when(userRepository.existsByUsername("unknown")).thenReturn(false);

            assertThat(userService.existsByUsername("testuser")).isTrue();
            assertThat(userService.existsByUsername("unknown")).isFalse();
        }

        @Test
        @DisplayName("Should check if email exists")
        void existsByEmail() {
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
            when(userRepository.existsByEmail("unknown@example.com")).thenReturn(false);

            assertThat(userService.existsByEmail("test@example.com")).isTrue();
            assertThat(userService.existsByEmail("unknown@example.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("Save User Tests")
    class SaveUserTests {

        @Test
        @DisplayName("Should save user successfully")
        void save_Success() {
            when(userRepository.save(testUser)).thenReturn(testUser);

            User result = userService.save(testUser);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(userRepository).save(testUser);
        }
    }
}
