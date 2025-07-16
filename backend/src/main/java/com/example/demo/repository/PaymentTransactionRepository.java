package com.example.demo.repository;

import com.example.demo.model.entity.PaymentTransaction;
import com.example.demo.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long>, JpaSpecificationExecutor<PaymentTransaction> {
    Optional<PaymentTransaction> findByOrderCode(String orderCode);
    List<PaymentTransaction> findByUser(User user);
}