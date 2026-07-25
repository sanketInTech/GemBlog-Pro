package com.gemblogpro.repository;

import com.gemblogpro.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Replaces the direct Mongoose calls made against the {@code User} model in
 * {@code adminController.js} ({@code User.findOne({email})}, {@code new
 * User(...).save()}).
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Replaces {@code User.findOne({email})}. */
    Optional<User> findByEmail(String email);

    /** Replaces the pre-insert existence check {@code User.findOne({email})} in adminRegister. */
    boolean existsByEmail(String email);

}
