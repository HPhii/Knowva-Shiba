package com.example.demo.specification;

import com.example.demo.model.entity.Account;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Role;
import com.example.demo.model.enums.Status;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> withStatus(Status status) {
        if (status == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            Join<User, Account> accountJoin = root.join("account");
            return criteriaBuilder.equal(accountJoin.get("status"), status);
        };
    }

    public static Specification<User> withRole(Role role) {
        if (role == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            Join<User, Account> accountJoin = root.join("account");
            return criteriaBuilder.equal(accountJoin.get("role"), role);
        };
    }

    public static Specification<User> withUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            Join<User, Account> accountJoin = root.join("account");
            return criteriaBuilder.like(accountJoin.get("username"), "%" + username + "%");
        };
    }

    public static Specification<User> withEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            Join<User, Account> accountJoin = root.join("account");
            return criteriaBuilder.like(accountJoin.get("email"), "%" + email + "%");
        };
    }

    public static Specification<User> withVerified(Boolean isVerified) {
        if (isVerified == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            Join<User, Account> accountJoin = root.join("account");
            return criteriaBuilder.equal(accountJoin.get("isVerified"), isVerified);
        };
    }
}