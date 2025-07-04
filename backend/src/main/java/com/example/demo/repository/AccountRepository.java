package com.example.demo.repository;

import com.example.demo.model.entity.Account;
import com.example.demo.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);

    //    Optional<Account> findByUsername(String username);
//    Optional<Account> findByResetPasswordToken(String token);
    Account findAccountByEmail(String email);
    Account findAccountById(long id);

    boolean existsByUsername(String uniqueUsername);

    @Query("SELECT count(a) FROM Account a WHERE a.role = :role")
    long countByRole(@Param("role") Role role);

    long countByCreatedAtAfter(LocalDateTime createdAt);

    boolean existsByEmail(@Email(message = "Invalid email format") @NotBlank(message = "Email cannot be blank") String email);

    List<Account> findByRole(Role role);
}