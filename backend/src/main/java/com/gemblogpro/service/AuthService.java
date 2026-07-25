package com.gemblogpro.service;

import com.gemblogpro.dto.request.AdminLoginRequest;
import com.gemblogpro.dto.request.AdminRegisterRequest;
import com.gemblogpro.dto.response.ApiResponse;
import com.gemblogpro.dto.response.AuthResponse;
import com.gemblogpro.dto.response.UserSummaryResponse;
import com.gemblogpro.entity.User;
import com.gemblogpro.exception.DuplicateResourceException;
import com.gemblogpro.exception.InvalidCredentialsException;
import com.gemblogpro.repository.UserRepository;
import com.gemblogpro.security.JwtTokenProvider;
import com.gemblogpro.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for admin registration and login.
 * <p>
 * Replaces the {@code adminRegister} and {@code adminLogin} functions in
 * {@code controllers/adminController.js}. The controller layer
 * ({@code AuthController}) stays thin; this service owns password hashing,
 * credential verification, and token issuance.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager,
                        JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Replaces {@code adminRegister}:
     * <pre>
     *   const userExists = await User.findOne({email});
     *   if(userExists) return res.json({success:false, message:'Email already registered'});
     *   const hashedPassword = await bcrypt.hash(password, 10);
     *   const newUser = new User({name, email, password: hashedPassword});
     *   await newUser.save();
     *   res.json({success:true, message:'Registration successful. Please login.'});
     * </pre>
     * The {@code if(!name || !email || !password)} check is now handled
     * declaratively by {@code @Valid} on {@link AdminRegisterRequest} in the
     * controller, before this method is even invoked.
     */
    @Transactional
    public ApiResponse register(AdminRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration rejected - email already registered: {}", request.getEmail());
            throw new DuplicateResourceException("Email already registered");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = new User(request.getName(), request.getEmail(), hashedPassword);
        userRepository.save(newUser);

        log.info("Registered new admin user id={} email={}", newUser.getId(), newUser.getEmail());
        return ApiResponse.success("Registration successful. Please login.");
    }

    /**
     * Replaces {@code adminLogin}:
     * <pre>
     *   const user = await User.findOne({email});
     *   if(!user || !(await bcrypt.compare(password, user.password))) {
     *     return res.json({success:false, message:'Invalid credentials'});
     *   }
     *   const token = jwt.sign({userId:user._id, email:user.email}, JWT_SECRET, {expiresIn:'7d'});
     *   res.json({success:true, token, user:{id:user._id, name:user.name, email:user.email}});
     * </pre>
     * Uses {@code AuthenticationManager} (backed by
     * {@code CustomUserDetailsService} + {@code PasswordEncoder}) rather
     * than a manual {@code bcrypt.compare} call - the standard Spring
     * Security login path - but the externally observable behavior
     * (credential check, token shape, response shape) is unchanged.
     */
    public AuthResponse login(AdminLoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException ex) {
            log.warn("Login failed - invalid credentials for email={}", request.getEmail());
            throw new InvalidCredentialsException("Invalid credentials");
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        UserSummaryResponse userSummary =
                new UserSummaryResponse(user.getId(), user.getName(), user.getEmail());

        log.info("Login successful for user id={} email={}", user.getId(), user.getEmail());
        return new AuthResponse(true, token, userSummary);
    }
}
