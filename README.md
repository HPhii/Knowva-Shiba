# EXE101 Project

A multi-service application built with Spring Boot backend, Flask microservice, and containerized deployment using Docker and Azure Container Registry.

## 🏗️ Project Structure

```
├── .env                    # Environment variables
├── .gitignore             # Git ignore rules
├── docker-compose.yml     # Docker compose configuration
├── .github/
│   └── workflows/
│       └── deploy.yml     # CI/CD pipeline for ACR deployment
├── backend/               # Spring Boot application
│   ├── src/              # Java source code
│   ├── pom.xml           # Maven dependencies
│   ├── mvnw              # Maven wrapper (Unix)
│   ├── mvnw.cmd          # Maven wrapper (Windows)
│   └── Dockerfile        # Backend container configuration
├── flask-service/         # Flask microservice
│   ├── app.py            # Flask application
│   ├── requirements.txt  # Python dependencies
│   ├── dockerfile        # Flask container configuration
│   └── utils/            # Utility modules
├── mysql-init/
│   └── init.sql          # Database initialization script
└── logo/                 # Project assets
```

## 🚀 Services

### Backend (Spring Boot)

- **Technology**: Java with Spring Boot
- **Build Tool**: Maven
- **Container**: Docker
- **Port**: Configured in application properties

### Flask Service

- **Technology**: Python with Flask
- **Dependencies**: Listed in [`requirements.txt`](flask-service/requirements.txt)
- **Container**: Docker
- **Main Application**: [`app.py`](flask-service/app.py)

### Database

- **Technology**: MySQL
- **Initialization**: [`init.sql`](mysql-init/init.sql)

## 🛠️ Development Setup

### Prerequisites

- Java 11 or higher
- Python 3.8 or higher
- Docker & Docker Compose
- Maven (or use included wrapper)

### Backend Setup

1. Navigate to the backend directory:

   ```bash
   cd backend
   ```

2. Build using Maven wrapper:

   ```bash
   # Unix/Linux/MacOS
   ./mvnw clean install

   # Windows
   mvnw.cmd clean install
   ```

3. Run the application:

   ```bash
   # Unix/Linux/MacOS
   ./mvnw spring-boot:run

   # Windows
   mvnw.cmd spring-boot:run
   ```

### Flask Service Setup

1. Navigate to the flask-service directory:

   ```bash
   cd flask-service
   ```

2. Create virtual environment:

   ```bash
   python -m venv venv
   source venv/bin/activate  # Unix/Linux/MacOS
   # or
   venv\Scripts\activate     # Windows
   ```

3. Install dependencies:

   ```bash
   pip install -r requirements.txt
   ```

4. Run the Flask application:
   ```bash
   python app.py
   ```

## 🐳 Docker Deployment

### Local Development

Run all services using Docker Compose:

```bash
docker-compose up -d
```

### Individual Service Containers

**Backend:**

```bash
cd backend
docker build -t springboot-app .
docker run -p 8080:8080 springboot-app
```

**Flask Service:**

```bash
cd flask-service
docker build -t flask-app .
docker run -p 5000:5000 flask-app
```

### Required Secrets

Configure these secrets in your GitHub repository:

- `ACR_USERNAME`: Azure Container Registry username
- `ACR_PASSWORD`: Azure Container Registry password

## 📝 Environment Variables

The project uses environment variables for configuration. Create appropriate `.env` files:

- **Root**: [`.env`](.env)
- **Backend**: [`backend/.env`](backend/.env)
- **Flask Service**: [`flask-service/.env`](flask-service/.env)

## 🗃️ Database

MySQL database initialization is handled by [`mysql-init/init.sql`](mysql-init/init.sql). This script runs when the database container starts for the first time.

## 🔧 Configuration Files

- **Maven**: [`backend/pom.xml`](backend/pom.xml)
- **Python Dependencies**: [`flask-service/requirements.txt`](flask-service/requirements.txt)
- **Docker Compose**: [`docker-compose.yml`](docker-compose.yml)
- **Git Ignore**: [`.gitignore`](.gitignore)

## 📋 TODO

- [ ] Add frontend (ReactJS) - planned
- [ ] Add API documentation
- [ ] Add unit tests and integration test
- [ ] Add monitoring and logging

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is part of the EXE101 course at FPT University.
