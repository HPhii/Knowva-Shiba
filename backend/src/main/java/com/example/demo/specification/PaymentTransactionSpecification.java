package com.example.demo.specification;

import com.example.demo.model.entity.Account;
import com.example.demo.model.entity.PaymentTransaction;
import com.example.demo.model.entity.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class PaymentTransactionSpecification {

    public static Specification<PaymentTransaction> withStatus(PaymentTransaction.TransactionStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<PaymentTransaction> withUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<PaymentTransaction> withUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            Join<PaymentTransaction, User> userJoin = root.join("user");
            Join<User, Account> accountJoin = userJoin.join("account");
            return criteriaBuilder.like(accountJoin.get("username"), "%" + username + "%");
        };
    }

    public static Specification<PaymentTransaction> withEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            Join<PaymentTransaction, User> userJoin = root.join("user");
            Join<User, Account> accountJoin = userJoin.join("account");
            return criteriaBuilder.like(accountJoin.get("email"), "%" + email + "%");
        };
    }

    public static Specification<PaymentTransaction> withOrderCode(String orderCode) {
        if (orderCode == null || orderCode.isBlank()) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("orderCode"), "%" + orderCode + "%");
    }

    public static Specification<PaymentTransaction> withCreatedAtAfter(LocalDateTime startDate) {
        if (startDate == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate);
    }

    public static Specification<PaymentTransaction> withCreatedAtBefore(LocalDateTime endDate) {
        if (endDate == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate);
    }
}