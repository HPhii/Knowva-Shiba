package com.example.demo.model.entity;

import com.example.demo.model.enums.LoginProvider;
import com.example.demo.model.enums.Role;
import com.example.demo.model.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "accounts")
public class Account implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginProvider loginProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified;

    @Column(name = "verify_otp")
    @JsonIgnore
    private String verifyOtp;

    @Column(name = "verify_otp_expired_at")
    @JsonIgnore
    private Long verifyOtpExpiredAt;

    @Column(name = "reset_otp")
    @JsonIgnore
    private String resetOtp;

    @Column(name = "reset_otp_expired_at")
    @JsonIgnore
    private Long resetOtpExpiredAt;

    @Column(name = "created_at", nullable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonIgnore
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = Status.ACTIVE;
        isVerified = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // hoặc dùng flag nếu cần
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // hoặc dùng flag nếu cần
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // hoặc dùng flag nếu cần
    }

    @Override
    public boolean isEnabled() {
        return this.status == Status.ACTIVE;
    }
}
