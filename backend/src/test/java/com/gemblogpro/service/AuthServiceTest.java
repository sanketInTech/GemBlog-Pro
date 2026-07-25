package com.gemblogpro.service;

import com.gemblogpro.dto.request.AdminLoginRequest;
import com.gemblogpro.dto.request.AdminRegisterRequest;
import com.gemblogpro.dto.response.ApiResponse;
import com.gemblogpro.dto.response.AuthResponse;
import com.gemblogpro.entity.User;
import com.gemblogpro.exception.DuplicateResourceException;
import com.gemblogpro.exception.InvalidCredentialsException;
import com.gemblogpro.repository.UserRepository;
import com.gemblogpro.security.JwtTokenProvider;
import com.gemblogpro.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthService}, mocking every collaborator
 * ({@link UserRepository}, {@link PasswordEncoder}, {@link AuthenticationManager},
 * {@link JwtTokenProvider}) so the business rules (duplicate email rejected,
 * bad credentials rejected, successful login issues a token) are verified in
 * isolation from Spring Security's actual authentication machinery and from
 * the database.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtTokenProvider);
    }

    @Test
    void register_savesNewUser_whenEmailNotAlreadyRegistered() {
        AdminRegisterRequest request = new AdminRegisterRequest("Ada Lovelace", "ada@example.com", "password123");
        when(userRepository.existsByEmail("ada@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");

        ApiResponse response = authService.register(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Registration successful. Please login.");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throwsDuplicateResourceException_whenEmailAlreadyRegistered() {
        AdminRegisterRequest request = new AdminRegisterRequest("Ada Lovelace", "ada@example.com", "password123");
        when(userRepository.existsByEmail("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already registered");
    }

    @Test
    void login_returnsTokenAndUserSummary_onValidCredentials() {
        AdminLoginRequest request = new AdminLoginRequest("ada@example.com", "password123");

        User user = newUserWithId(7L, "Ada Lovelace", "ada@example.com", "hashed-password");
        UserPrincipal principal = new UserPrincipal(user);
        Authentication authentication = new TestingAuthenticationToken(principal, null);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(7L, "ada@example.com")).thenReturn("signed.jwt.token");

        AuthResponse response = authService.login(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getToken()).isEqualTo("signed.jwt.token");
        assertThat(response.getUser().getId()).isEqualTo(7L);
        assertThat(response.getUser().getEmail()).isEqualTo("ada@example.com");
    }

    @Test
    void login_throwsInvalidCredentialsException_onBadCredentials() {
        AdminLoginRequest request = new AdminLoginRequest("ada@example.com", "wrong-password");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    /**
     * {@code User}'s id is normally only ever assigned by Hibernate
     * ({@code @GeneratedValue}) once persisted. Since a test never actually
     * persists this entity, {@code setId} is used directly to build a
     * fully-formed {@code User} for assertions.
     */
    private static User newUserWithId(Long id, String name, String email, String password) {
        User user = new User(name, email, password);
        user.setId(id);
        return user;
    }
}
