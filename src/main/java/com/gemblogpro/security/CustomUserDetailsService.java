package com.gemblogpro.security;

import com.gemblogpro.entity.User;
import com.gemblogpro.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads a {@link UserDetails} by email for Spring Security's authentication
 * machinery. Replaces the {@code User.findOne({email})} lookup that used to
 * live inline inside {@code adminController.js}'s {@code adminLogin}; here
 * it is the standard Spring Security extension point instead, used by the
 * {@code AuthenticationManager} during login and (indirectly, via
 * {@link JwtAuthenticationFilter}) on every authenticated request.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));
        return new UserPrincipal(user);
    }
}
