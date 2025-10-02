# 🎓 Knowva - Hệ thống Học tập Thông minh với AI

<div align="center">

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-green.svg)](https://spring.io/)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.0-red.svg)](https://redis.io/)
[![Flask](https://img.shields.io/badge/Flask-2.3.3-lightblue.svg)](https://flask.palletsprojects.com/)
[![Docker](https://img.shields.io/badge/Docker-Containerized-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Hệ thống học tập thông minh tích hợp AI để tạo flashcard và quiz từ tài liệu

[🚀 Demo Live](https://knowva.me) | [📖 API Documentation](http://localhost:8080/swagger-ui.html) | [🐛 Bug Report](https://github.com/HPhii/Knowva-Shiba/issues) | [💡 Feature Request](https://github.com/HPhii/Knowva-Shiba/issues)

</div>

---

## 📋 Mục lục

- [🎯 Giới thiệu](#-giới-thiệu)
- [✨ Tính năng chính](#-tính-năng-chính)
- [🛠️ Tech Stack](#️-tech-stack)
- [🏗️ Kiến trúc hệ thống](#️-kiến-trúc-hệ thống)
- [👥 Phân quyền người dùng](#-phân-quyền-người-dùng)
- [⚡ Quick Start](#-quick-start)
- [📁 Cấu trúc dự án](#-cấu-trúc-dự-án)
- [🔐 Bảo mật](#-bảo-mật)
- [📊 API Documentation](#-api-documentation)
- [🧪 Testing](#-testing)
- [🚀 Deployment](#-deployment)
- [🤝 Contributing](#-contributing)

---

## 🎯 Giới thiệu

**Knowva** là một hệ thống học tập thông minh được phát triển với Spring Boot và Flask, tích hợp AI để hỗ trợ người học tạo ra các bộ flashcard và quiz từ tài liệu một cách tự động. Hệ thống áp dụng phương pháp **Spaced Repetition** (Lặp lại ngắt quãng) để tối ưu hóa quá trình ghi nhớ và học tập.

### 🎯 Mục tiêu

- **Tự động hóa việc học**: Sử dụng AI để tạo flashcard và quiz từ tài liệu PDF, hình ảnh, văn bản
- **Học tập hiệu quả**: Áp dụng thuật toán Spaced Repetition để tối ưu thời gian ghi nhớ
- **Trải nghiệm cá nhân hóa**: Theo dõi tiến độ học tập và đưa ra gợi ý phù hợp
- **Chia sẻ kiến thức**: Cho phép chia sẻ bộ học liệu với bạn bè và cộng đồng

---

## ✨ Tính năng chính

### 🧠 **AI-Powered Learning**

- 🤖 Tạo flashcard tự động từ PDF, hình ảnh, văn bản
- 📝 Sinh quiz trắc nghiệm từ tài liệu với AI
- 🔍 Trích xuất văn bản thông minh với OCR (Tesseract)
- 🌐 Hỗ trợ đa ngôn ngữ (Tiếng Việt, English)

### 📚 **Quản lý Flashcard & Quiz**

- 📱 Tạo, chỉnh sửa, xóa flashcard sets
- 🎯 Nhiều loại thẻ học: Standard, Cloze (Fill-in-blank)
- 📊 Tạo quiz từ flashcard sets có sẵn
- 🔒 Quản lý quyền truy cập: Public, Private, với token

### 🧭 **Spaced Repetition System**

- 📅 Thuật toán SM-2 để tối ưu lịch ôn tập
- 📈 Theo dõi tiến độ học tập chi tiết
- ⏰ Nhắc nhở thông minh qua email
- 📊 Thống kê hiệu suất học tập

### 👥 **Cộng tác & Chia sẻ**

- 🤝 Mời bạn bè học chung với quyền hạn khác nhau
- 🌍 Chia sẻ bộ học liệu công khai
- 🔍 Tìm kiếm và khám phá nội dung từ cộng đồng
- 💬 Hệ thống thông báo và hoạt động

### 💰 **Thanh toán & VIP**

- 💳 Tích hợp PayOS cho thanh toán VIP
- 🎁 Gói VIP với tính năng cao cấp
- 📊 Quản lý giao dịch và lịch sử thanh toán
- 🏆 Theo dõi thời hạn VIP

### 📧 **Hệ thống Email & Thông báo**

- 📮 Email chào mừng và xác thực OTP
- 🔔 Nhắc nhở ôn tập định kỳ
- 📨 Thông báo hoạt động và mời học chung
- 📋 Template email đẹp mắt với Thymeleaf

---

## 🛠️ Tech Stack

### **Backend Architecture**

```java
Framework      : Spring Boot 3.3.3
Runtime        : Java 17 (OpenJDK)
Database       : MySQL 8.0 + Spring Data JPA
Cache          : Redis 7.0 + Jedis
Authentication : JWT + Spring Security + OAuth2
File Storage   : Cloudinary
Email Service  : Spring Boot Mail
Payment        : PayOS Gateway
Documentation  : SpringDoc OpenAPI 3.0
Validation     : Spring Boot Validation
Mapping        : MapStruct
Scheduling     : Spring @Scheduled
```

### **AI Service**

```python
Framework      : Flask 2.3.3
AI Provider    : Google Gemini API
OCR Engine     : Tesseract OCR (đa ngôn ngữ)
PDF Processing : PyPDF2
Image Processing: Pillow (PIL)
WSGI Server    : Gunicorn
Environment    : Python 3.10
```

### **Database Design**

```sql
Primary DB     : MySQL 8.0
Tables         : 15+ entities
Relations      : OneToMany, ManyToMany với foreign keys
Caching        : Redis với TTL và polymorphic typing
Optimization   : Connection pooling, query optimization
```

### **DevOps & Infrastructure**

```bash
Containerization : Docker + Docker Compose
CI/CD           : GitHub Actions
Monitoring      : Application logs với Logback
Security        : JWT, BCrypt, CORS, Rate Limiting
Performance     : Connection pooling, Redis caching
Deployment      : VPS với automated deployment
```

---

## 🏗️ Kiến trúc hệ thống

```

```

### **Architectural Patterns**

- **Microservices**: Spring Boot (Main) + Flask (AI Service)
- **Layered Architecture**: Controller → Service → Repository → Entity
- **Dependency Injection**: Spring IoC Container
- **DTO Pattern**: Request/Response objects với validation
- **Strategy Pattern**: AI service templates và interaction strategies

---

## 👥 Phân quyền người dùng

| Vai trò      | Mô tả                  | Quyền hạn chính                                           |
| ------------ | ---------------------- | --------------------------------------------------------- |
| 👑 **ADMIN** | Quản trị viên hệ thống | Quản lý toàn bộ: users, content, analytics, system config |
| 👤 **USER**  | Người dùng thường      | Tạo flashcard/quiz, học tập, chia sẻ content              |
| 👻 **GUEST** | Khách truy cập         | Xem nội dung công khai, đăng ký tài khoản                 |

### **Quyền truy cập Flashcard/Quiz Sets**

| Permission         | Mô tả            | Quyền hạn                              |
| ------------------ | ---------------- | -------------------------------------- |
| 🔧 **EDIT**        | Chỉnh sửa đầy đủ | Thêm/sửa/xóa cards, thay đổi settings  |
| 👁️ **VIEW**        | Chỉ xem          | Học tập, xem nội dung, không được sửa  |
| 👥 **COLLABORATE** | Cộng tác         | Thêm cards mới, tham gia hoạt động học |

---

## ⚡ Quick Start

### **Prerequisites**

```bash
Java 17+
Docker & Docker Compose
Maven 3.6+
Git
```

### **1. Clone Repository**

```bash
git clone https://github.com/HPhii/EXE101.git
cd EXE101
```

### **2. Environment Setup**

Tạo file `.env` trong thư mục root:

```bash
# Database
MYSQL_USER=knowva_user
MYSQL_PASSWORD=your_secure_password

# JWT
JWT_SECRET_KEY=your-super-secure-jwt-secret-key-minimum-32-characters

# Google OAuth
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google

# Email
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Cloudinary
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret

# URLs
CLIENT_URL=http://localhost:3000
SERVER_URL=http://localhost:8080
FLASK_SERVICE_HOST=http://flask_app:5000

# PayOS
PAYOS_CLIENT_ID=your-payos-client-id
PAYOS_API_KEY=your-payos-api-key
PAYOS_CHECKSUM_KEY=your-payos-checksum-key
PAYOS_PRICE=50000

# AI Service (Flask)
LLAMDA_API_KEY1=your-gemini-api-key-1
LLAMDA_API_KEY2=your-gemini-api-key-2
LLAMDA_API_KEY3=your-gemini-api-key-3
LLAMDA_API_KEY4=your-gemini-api-key-4
```

### **3. Quick Start với Docker**

```bash
# Khởi động toàn bộ hệ thống
docker-compose up -d

# Kiểm tra logs
docker-compose logs -f

# Truy cập ứng dụng
# Backend API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
# Flask AI Service: http://localhost:5000
```

### **4. Development Setup**

#### **Backend (Spring Boot)**

```bash
cd backend

# Cài đặt dependencies
./mvnw clean install

# Chạy development mode
./mvnw spring-boot:run

# Hoặc build và chạy
./mvnw clean package
java -jar target/flashcard-0.0.1-SNAPSHOT.jar
```

#### **AI Service (Flask)**

```bash
cd flask-service

# Tạo virtual environment
python -m venv venv
source venv/bin/activate  # Linux/Mac
# venv\Scripts\activate     # Windows

# Cài đặt dependencies
pip install -r requirements.txt

# Chạy development mode
python app.py

# Production mode
gunicorn --workers 3 --bind 0.0.0.0:5000 app:app
```

### **5. Database Setup**

```bash
# Database sẽ được khởi tạo tự động khi chạy lần đầu
# Hoặc thực hiện thủ công:
mysql -u root -p < mysql-init/init.sql
```

---

## 📁 Cấu trúc dự án

```
EXE101/
│
├── 📂 backend/                     # Spring Boot Application
│   ├── 📂 src/main/java/com/example/demo/
│   │   ├── 📂 config/              # Configuration classes
│   │   │   ├── 📄 SecurityConfig.java      # Security & CORS
│   │   │   ├── 📄 DatabaseConfig.java      # JPA Configuration
│   │   │   ├── 📄 RedisConfig.java         # Redis Setup
│   │   │   ├── 📄 CloudinaryConfig.java    # File Storage
│   │   │   ├── 📄 PayOSConfig.java         # Payment Gateway
│   │   │   └── 📄 OpenAPIConfig.java       # Swagger Documentation
│   │   ├── 📂 controller/          # REST Controllers
│   │   │   ├── 📄 AuthenticationController.java  # Auth endpoints
│   │   │   ├── 📄 FlashcardSetController.java     # Flashcard CRUD
│   │   │   ├── 📄 QuizSetController.java          # Quiz management
│   │   │   ├── 📄 SpacedRepetitionController.java # Learning system
│   │   │   ├── 📄 UserController.java             # User management
│   │   │   ├── 📄 PaymentController.java          # VIP payments
│   │   │   └── 📄 AdminController.java            # Admin functions
│   │   ├── 📂 service/             # Business Logic
│   │   │   ├── 📂 impl/            # Service implementations
│   │   │   ├── 📂 template/        # AI service templates
│   │   │   ├── 📂 strategy/        # Design patterns
│   │   │   └── 📂 scheduler/       # Background tasks
│   │   ├── 📂 model/               # Data Models
│   │   │   ├── 📂 entity/          # JPA Entities
│   │   │   │   ├── 📂 flashcard/   # Flashcard entities
│   │   │   │   ├── 📂 quiz/        # Quiz entities
│   │   │   │   ├── 📄 User.java    # User entity
│   │   │   │   └── 📄 Account.java # Account entity
│   │   │   ├── 📂 enums/           # Enumerations
│   │   │   └── 📂 io/              # DTOs & Requests/Responses
│   │   ├── 📂 repository/          # Data Access Layer
│   │   ├── 📂 exception/           # Exception Handling
│   │   ├── 📂 utils/               # Utility Classes
│   │   └── 📄 DemoApplication.java # Main Application
│   ├── 📂 src/main/resources/
│   │   ├── 📄 application.properties       # App Configuration
│   │   └── 📂 templates/email/            # Email Templates
│   ├── 📄 pom.xml                  # Maven Dependencies
│   └── 📄 Dockerfile               # Container Configuration
│
├── 📂 flask-service/               # AI Service
│   ├── 📂 utils/                   # AI Processing Modules
│   │   ├── 📄 quiz_generation.py          # Quiz creation with AI
│   │   ├── 📄 flashcard_generation.py     # Flashcard creation
│   │   ├── 📄 text_extraction.py          # OCR & PDF processing
│   │   └── 📄 exam_feedback.py            # Answer evaluation
│   ├── 📄 app.py                   # Flask Application
│   ├── 📄 requirements.txt         # Python Dependencies
│   └── 📄 Dockerfile               # Container Configuration
│
├── 📂 mysql-init/                  # Database Initialization
│   └── 📄 init.sql                 # Initial DB Setup
│
├── 📂 .github/workflows/           # CI/CD Pipeline
│   └── 📄 build-and-push.yml      # Automated Deployment
│
├── 📄 docker-compose.yml           # Multi-container Setup
├── 📄 .gitignore                   # Git Ignore Rules
└── 📄 README.md                    # Project Documentation
```

---

## 🔐 Bảo mật

### **Authentication & Authorization**

- 🔑 **JWT Authentication** với access tokens
- 🔐 **Password Hashing** sử dụng BCrypt
- 🌐 **Google OAuth 2.0** integration
- 📱 **OTP Verification** cho reset password
- 🚪 **Role-based Access Control**

### **Data Protection**

- 🛡️ **Input Validation** với Spring Validation
- 🔒 **CORS Policy** restricted origins
- 🚫 **SQL Injection Protection** với JPA
- 📊 **Rate Limiting** (có thể cấu hình)
- 🗄️ **Secure File Upload** với Cloudinary

### **Infrastructure Security**

- 🔐 **Environment Variables** cho sensitive data
- 🏥 **Health Checks** trong Docker containers
- 🚀 **HTTPS Ready** cho production
- 📝 **Audit Logging** cho security events
- 🔄 **Regular Security Updates**

---

## 📊 API Documentation

### **Swagger/OpenAPI**

- **Local**: http://localhost:8080/swagger-ui.html
- **Production**: https://your-domain.com/swagger-ui.html

### **Main API Endpoints**

#### Authentication & Account

```http
POST /api/register              # Đăng ký tài khoản
POST /api/login                 # Đăng nhập
POST /api/google                # Google OAuth login
POST /api/send-reset-otp        # Gửi OTP reset password
POST /api/reset-password        # Reset password với OTP
GET  /api/profile               # Lấy thông tin profile
PUT  /api/profile               # Cập nhật profile
```

#### Flashcard Management

```http
POST /api/flashcard-sets/generate     # Tạo flashcard từ AI
POST /api/flashcard-sets/save         # Lưu flashcard set
GET  /api/flashcard-sets/{id}         # Lấy chi tiết flashcard set
PUT  /api/flashcard-sets/{id}         # Cập nhật flashcard set
DELETE /api/flashcard-sets/{id}       # Xóa flashcard set
POST /api/flashcard-sets/{id}/exam-mode  # Chế độ kiểm tra
POST /api/flashcard-sets/{id}/generate-quiz  # Tạo quiz từ flashcard
```

#### Quiz Management

```http
POST /api/quiz-sets/generate          # Tạo quiz từ AI
POST /api/quiz-sets/save              # Lưu quiz set
GET  /api/quiz-sets                   # Lấy danh sách quiz
GET  /api/quiz-sets/{id}              # Chi tiết quiz set
POST /api/quiz-attempts/start         # Bắt đầu làm quiz
POST /api/quiz-attempts/submit        # Nộp bài quiz
```

#### Spaced Repetition

```http
GET  /api/spaced-repetition/mode-data        # Dữ liệu học tập
POST /api/spaced-repetition/start-session    # Bắt đầu phiên học
POST /api/spaced-repetition/submit-review    # Gửi kết quả ôn tập
GET  /api/spaced-repetition/study-history    # Lịch sử học tập
GET  /api/spaced-repetition/performance      # Thống kê hiệu suất
```

#### Search & Discovery

```http
GET /api/search/quiz-sets           # Tìm kiếm quiz sets
GET /api/search/flashcard-sets      # Tìm kiếm flashcard sets
GET /api/search/accounts            # Tìm kiếm người dùng
```

#### Admin Functions

```http
GET    /api/admin/users             # Quản lý người dùng
POST   /api/admin/users/{id}/ban    # Ban/unban user
GET    /api/admin/dashboard         # Dashboard analytics
GET    /api/admin/activity-logs     # Xem activity logs
```

### **Response Format**

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    // Response data here
  },
  "timestamp": "2024-12-15T10:30:00Z"
}

// Error Response
{
  "success": false,
  "message": "Error description",
  "error": "ERROR_CODE",
  "timestamp": "2024-12-15T10:30:00Z"
}
```

---

## 🧪 Testing

### **Backend Testing**

```bash
cd backend

# Chạy unit tests
./mvnw test

# Chạy integration tests
./mvnw test -Dtest=**/*IntegrationTest

# Test coverage report
./mvnw jacoco:report

# Test specific class
./mvnw test -Dtest=FlashcardSetServiceTest
```

### **AI Service Testing**

```bash
cd flask-service

# Chạy tests với pytest
python -m pytest tests/

# Test specific function
python -m pytest tests/test_quiz_generation.py

# Test với coverage
python -m pytest --cov=utils tests/
```

### **API Testing**

```bash
# Import Postman collection
# Sử dụng collection có sẵn để test các endpoints

# Test authentication flow
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com","password":"password123"}'

# Test AI generation
curl -X POST http://localhost:8080/api/flashcard-sets/generate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "flashcardSet={...}" \
  -F "files=@document.pdf"
```

### **Database Testing**

```sql
-- Test database connection
SELECT 1;

-- Check tables creation
SHOW TABLES;

-- Verify sample data
SELECT * FROM users LIMIT 5;
SELECT * FROM flashcard_sets LIMIT 5;
```

---

## 🚀 Deployment

### **Development Environment**

```bash
# Local development với Docker
docker-compose up -d

# Kiểm tra health
curl http://localhost:8080/actuator/health
curl http://localhost:5000/health  # nếu có endpoint

# Logs monitoring
docker-compose logs -f app
docker-compose logs -f flask_app
```

### **Production Deployment**

#### **Docker Deployment (Recommended)**

```bash
# Build production images
docker build -t knowva-backend ./backend
docker build -t knowva-flask ./flask-service

# Deploy với docker-compose
docker-compose -f docker-compose.prod.yml up -d

# Update deployment
docker-compose pull
docker-compose up -d --force-recreate
```

#### **CI/CD với GitHub Actions**

```yaml
# .github/workflows/build-and-push.yml
# Tự động build và deploy khi push lên main branch

# Trigger deployment
git push origin main
# Monitor deployment
# Check GitHub Actions tab
```

#### **Manual Deployment**

```bash
# Backend deployment
cd backend
./mvnw clean package -DskipTests
java -jar target/flashcard-0.0.1-SNAPSHOT.jar

# Flask service deployment
cd flask-service
pip install -r requirements.txt
gunicorn --workers 3 --bind 0.0.0.0:5000 app:app
```

### **Environment Configuration**

#### **Production .env**

```bash
# Production database
MYSQL_URL=jdbc:mysql://your-prod-db:3306/knowva_prod
MYSQL_USER=prod_user
MYSQL_PASSWORD=super_secure_password

# Production URLs
CLIENT_URL=https://knowva.app
SERVER_URL=https://api.knowva.app

# Production services
CLOUDINARY_CLOUD_NAME=prod-cloud
PAYOS_CLIENT_ID=prod-payos-id

# Redis production
SPRING_DATA_REDIS_HOST=redis-prod
SPRING_DATA_REDIS_PORT=6379
```

---

## 🤝 Contributing

### **Development Workflow**

1. **Fork** repository từ GitHub
2. **Clone** forked repo: `git clone https://github.com/HPhii/Knowva-Shiba.git`
3. **Create** feature branch: `git checkout -b feature/amazing-feature`
4. **Develop** và test tính năng mới
5. **Commit** với message rõ ràng: `git commit -m 'feat: add amazing feature'`
6. **Push** to branch: `git push origin feature/amazing-feature`
7. **Create** Pull Request với mô tả chi tiết

### **Commit Convention**

```bash
feat(scope): add new feature
fix(scope): fix bug or issue
docs(scope): update documentation
style(scope): formatting, missing semi colons, etc
refactor(scope): code refactoring
test(scope): add or update tests
chore(scope): maintenance tasks
perf(scope): performance improvements
```

### **Code Quality Standards**

- **Java**: Follow Spring Boot best practices
- **Python**: PEP 8 style guide
- **Documentation**: Comprehensive JavaDoc và docstrings
- **Testing**: Unit tests cho business logic
- **Security**: Validate inputs, sanitize outputs

### **Pull Request Guidelines**

- **Clear Title**: Mô tả ngắn gọn về thay đổi
- **Detailed Description**: Giải thích chi tiết tính năng/bug fix
- **Screenshots**: Nếu có thay đổi UI
- **Testing**: Kết quả test và coverage
- **Breaking Changes**: Ghi chú nếu có breaking changes

### **Development Setup cho Contributors**

```bash
# Setup development environment
git clone https://github.com/HPhii/Knowva-Shiba.git
cd Knowva-Shiba

# Install pre-commit hooks (optional)
pip install pre-commit
pre-commit install

# Setup backend development
cd backend
./mvnw clean install

# Setup Flask development
cd ../flask-service
pip install -r requirements.txt
pip install -r requirements-dev.txt  # dev dependencies

# Run tests before committing
./mvnw test  # Backend tests
pytest       # Flask tests
```

---

## 📞 Support & Contact

### **Documentation & Resources**

- 📖 **API Documentation**: [Swagger UI](http://localhost:8080/swagger-ui.html)
- 🗄️ **Database Schema**: Check entity models trong `/backend/src/main/java/com/example/demo/model/entity/`
- 🔧 **Configuration Guide**: Xem file `application.properties` và `.env` examples
- 📝 **Development Guide**: Follow Spring Boot và Flask best practices

### **Getting Help**

- 🐛 **Bug Reports**: [GitHub Issues](https://github.com/HPhii/Knowva-Shiba/issues)
- 💡 **Feature Requests**: [GitHub Discussions](https://github.com/HPhii/Knowva-Shiba/discussions)
- ❓ **Questions**: Create issue với label `question`
- 📧 **Email**: knowva@gmail.com

### **Community**

- 📱 **Social Media**: Follow updates trên các platform

### **Development Team**

- **Backend Team**: Spring Boot + Java specialists
- **AI Team**: Flask + Python + Machine Learning experts
- **DevOps Team**: Docker + CI/CD + Infrastructure
- **QA Team**: Testing và quality assurance

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **Spring Boot Team** for the amazing framework
- **Google Gemini** for AI capabilities
- **Tesseract** for OCR functionality
- **Docker** for containerization
- **MySQL & Redis** for data storage solutions
- **All contributors** who helped make this project possible

---

## 🚨 Important Notes

### **API Keys Required**

```bash
# Cần thiết để chạy đầy đủ tính năng:
GOOGLE_GEMINI_API_KEY  # Cho AI generation
CLOUDINARY_API_KEY     # Cho file storage
PAYOS_API_KEY         # Cho payment
GOOGLE_OAUTH_KEY      # Cho Google login
```

### **System Requirements**

```bash
Minimum RAM: 2GB
Recommended RAM: 4GB+
Storage: 10GB+ available
Network: Stable internet cho AI services
```

### **Known Limitations**

- AI generation phụ thuộc vào Gemini API quota
- OCR quality phụ thuộc vào chất lượng hình ảnh đầu vào
- Email delivery phụ thuộc vào SMTP configuration
- File upload size giới hạn 5MB

---

<div align="center">

**⭐ If you find this project helpful, please give it a star! ⭐**

Made with ❤️ by [Knowva Development Team](https://github.com/HPhii/Knowva-Shiba)

_Last updated: September 2025_

**🎓 Happy Learning with Knowva! 🚀**

</div>
