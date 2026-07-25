package com.gemblogpro.dto.response;

/**
 * Response body for {@code POST /api/admin/login} / {@code POST /api/admin/auth}.
 * Replaces {@code res.json({success:true, token, user:{id,name,email}})} in
 * {@code adminController.js}'s {@code adminLogin}.
 */
public class AuthResponse {

    private boolean success;
    private String token;
    private UserSummaryResponse user;

    public AuthResponse() {
    }

    public AuthResponse(boolean success, String token, UserSummaryResponse user) {
        this.success = success;
        this.token = token;
        this.user = user;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserSummaryResponse getUser() {
        return user;
    }

    public void setUser(UserSummaryResponse user) {
        this.user = user;
    }
}
