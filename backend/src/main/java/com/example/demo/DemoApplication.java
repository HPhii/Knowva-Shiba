package com.example.demo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Knowva API Documentation",
                version = "1.0",
                description = "Tài liệu API cho ứng dụng học tập Knowva (Shiba)"
        ),
        tags = {
                @Tag(name = "1. Authentication", description = "APIs cho việc Đăng ký, Đăng nhập, Đăng xuất"),
                @Tag(name = "2. Account Management", description = "APIs để quản lý tài khoản như đặt lại mật khẩu và xác thực email"),
                @Tag(name = "3. User Management", description = "APIs để quản lý và lấy thông tin người dùng"),
                @Tag(name = "4. Payment Management", description = "APIs để tạo và xử lý thanh toán qua PayOS"),
                @Tag(name = "5. Search", description = "APIs để tìm kiếm các tài nguyên trong hệ thống"),
                @Tag(name = "6. Notification Management", description = "APIs để quản lý và xem thông báo của người dùng"),
                @Tag(name = "7. Quiz Set Management", description = "APIs để tạo, đọc, cập nhật, xóa và quản lý các bộ câu hỏi (Quiz Sets)"),
                @Tag(name = "8. Quiz Attempt Management", description = "APIs để bắt đầu, nộp bài và xem lại một lần làm bài kiểm tra"),
                @Tag(name = "9. Flashcard Set Management", description = "APIs để tạo, đọc, cập nhật, xóa và quản lý các bộ thẻ học (Flashcard Sets)"),
                @Tag(name = "10. Spaced Repetition", description = "APIs cho chế độ học Lặp lại ngắt quãng"),
                @Tag(name = "11. [ADMIN] Admin Management", description = "APIs dành riêng cho Quản trị viên"),
                @Tag(name = "12. [ADMIN] Dashboard Statistics", description = "APIs để lấy dữ liệu thống kê cho trang tổng quan của quản trị viên"),
                @Tag(name = "13. Feedback Management", description = "APIs để quản lý feedback của người dùng")
        }
)
@SecurityScheme(name = "api", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
