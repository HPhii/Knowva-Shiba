package com.example.demo.repository;

import com.example.demo.model.entity.BugReportReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BugReportReplyRepository extends JpaRepository<BugReportReply, Long> {
}