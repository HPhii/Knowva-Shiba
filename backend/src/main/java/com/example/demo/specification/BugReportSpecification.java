package com.example.demo.specification;

import com.example.demo.model.entity.BugReport;
import com.example.demo.model.enums.BugReportCategory;
import com.example.demo.model.enums.BugReportPriority;
import com.example.demo.model.enums.BugReportStatus;
import org.springframework.data.jpa.domain.Specification;

public class BugReportSpecification {

    public static Specification<BugReport> withStatus(BugReportStatus status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<BugReport> withCategory(BugReportCategory category) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("category"), category);
    }

    public static Specification<BugReport> withPriority(BugReportPriority priority) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("priority"), priority);
    }

    public static Specification<BugReport> withReporterId(Long reporterId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("reporter").get("id"), reporterId);
    }
}