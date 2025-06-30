package com.example.demo.utils;

import com.example.demo.model.entity.Account;
import com.example.demo.model.enums.Role;

import java.util.List;

public class InputValidationUtils {
    /**
     * Validates input for quiz set generation.
     * @param account The user account.
     * @param text The text input.
     * @param files The uploaded files.
     * @param questionCount The number of questions requested.
     * @return The valid input (text or files).
     */
    public static Object validateInput(Account account, String text, List<?> files, int questionCount) {
        Role role = account.getRole();

        // Restrict file uploads to VIP users only
        if (files != null && !files.isEmpty() && role != Role.VIP) {
            throw new SecurityException("Only VIP users can upload files for quiz set generation.");
        }

        // Enforce question limits
        if (role == Role.REGULAR) {
            if (questionCount < 5 || questionCount > 10) {
                throw new IllegalArgumentException("REGULAR users can request between 5 and 10 questions only.");
            }
        } else if (role == Role.VIP) {
            if (questionCount < 5) {
                throw new IllegalArgumentException("VIP users must request at least 5 questions.");
            }
            // No upper limit for VIP, or set your own if needed
        }

        if (text != null && !text.isBlank()) {
            return text;
        } else if (files != null && !files.isEmpty()) {
            return files;
        } else {
            throw new IllegalArgumentException("Either text or files must be provided.");
        }
    }
}