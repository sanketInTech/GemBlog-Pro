package com.gemblogpro.security;

import com.gemblogpro.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapts the JPA {@link User} entity to Spring Security's {@link UserDetails}
 * contract, so it can flow through {@code AuthenticationManager} /
 * {@code DaoAuthenticationProvider} without the entity itself needing to
 * implement Spring Security interfaces.
 * <p>
 * The original Mongoose {@code User} model has no role/permission field -
 * every registered user is an admin with equal access, matching the current
 * app's behavior where any account created via {@code /api/admin/register}
 * can access every {@code auth}-protected route. A single fixed
 * {@code ROLE_ADMIN} authority is granted here rather than inventing a role
 * system the original app never had.
 */
public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    public Long getId() {
        return user.getId();
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
