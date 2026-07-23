package com.gemblogpro.controller;

import com.gemblogpro.dto.request.AdminLoginRequest;
import com.gemblogpro.dto.request.AdminRegisterRequest;
import com.gemblogpro.dto.response.ApiResponse;
import com.gemblogpro.dto.response.AuthResponse;
import com.gemblogpro.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Replaces the admin-authentication portion of {@code routes/adminRoutes.js}
 * + {@code controllers/adminController.js} (the {@code getAllComments} /
 * {@code getAllBlogsAdmin} / {@code delete-comment} / {@code approve-comment}
 * / {@code dashboard} endpoints from that same router belong to Phase 4 and
 * are not implemented here).
 * <p>
 * Endpoint paths and JSON field names are unchanged so the React frontend
 * ({@code Login.jsx}, {@code AppContext.jsx}) works without modification.
 * The one contract change is HTTP status codes, per the approved
 * architecture revision: register now returns {@code 201 Created} instead
 * of {@code 200}, and failures return {@code 400}/{@code 401} instead of
 * always {@code 200}.
 */
@RestController
@RequestMapping("/api/admin")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Replaces {@code adminRouter.post("/register", adminRegister)}. */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody AdminRegisterRequest request) {
        log.info("Registration attempt for email={}", request.getEmail());
        ApiResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Replaces {@code adminRouter.post("/login", adminLogin)}. Only the
     * email is ever logged - the raw password never touches a log line
     * anywhere in this codebase.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        log.info("Login attempt for email={}", request.getEmail());
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Replaces {@code adminRouter.post("/auth", adminLogin)} - the Express
     * app wires this route to the exact same handler as {@code /login}, so
     * this method mirrors that alias rather than redirecting or duplicating
     * logic.
     */
    @PostMapping("/auth")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AdminLoginRequest request) {
        log.info("Login attempt (via /auth alias) for email={}", request.getEmail());
        return ResponseEntity.ok(authService.login(request));
    }
}
