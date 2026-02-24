<div align="center">
  <h1>🛒 E-Commerce Backend API</h1>
  <p>A robust, feature-rich backend API built with Spring Boot 3 and Java 21, designed to power modern e-commerce platforms.</p>
</div>

<hr />

## ✨ Features

- **🔐 Security & Authentication:** - OAuth2 Resource Server implementation using JWT.
    - Asymmetric RSA key pair for secure token signing and verification.
    - Google & Facebook OAuth2 Integration with automated user profiling.
    - **HttpOnly Cookie** strategy for secure Refresh Token management.
- **📧 Automated Email System:**
    - **Asynchronous messaging** using `@Async` to prevent API latency.
    - Rich HTML templates powered by **Thymeleaf**.
    - **Security Alerts:** Instant notification when login is detected from a new device.
    - **Localized Timestamps:** All emails use `Asia/Ho_Chi_Minh` timezone (UTC+7) for accuracy.
- **💾 Database & Persistence:** - Microsoft SQL Server (MSSQL) integration with optimized **UUID/Uniqueidentifier** handling.
    - **Cascade Delete** & Shared Primary Key (@MapsId) implementation for data integrity.
    - High-performance caching enabled by **Caffeine**.
- **🔌 Real-time Communication:** - Bidirectional communication via WebSocket.
    - Server-Sent Events (SSE) for instant server-to-client notifications.
- **💳 Payment Gateway:** - Seamless integration with **VNPay** for secure transaction processing.
- **☁️ Cloud Storage:** - **Cloudinary** integration for optimized media upload and management.
- **🛠️ Utilities & Architecture:** - **MapStruct** for automatic and efficient Entity-to-DTO mapping.
    - **Lombok** to reduce boilerplate code.
    - Interactive API Documentation powered by **SpringDoc (Swagger UI)**.

## 🚀 Technology Stack

- **Framework:** Spring Boot 3.5.7
- **Language:** Java 21
- **Database:** Microsoft SQL Server (MSSQL 2022)
- **Caching:** Caffeine
- **Containerization:** Docker & Docker Compose
- **Build Tool:** Maven

## 📋 Prerequisites

To run this application locally, ensure you have the following installed:
- [Java Development Kit (JDK) 21](https://jdk.java.net/21/)
- [Maven 3.8+](https://maven.apache.org/download.cgi)
- [Docker](https://www.docker.com/) (Recommended for running MSSQL via containers)

## ⚙️ Environment Configuration

Create a `.env` file in the root directory and configure the following parameters:

```env
# --- DATABASE ---
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=YourDatabase;encrypt=true;trustServerCertificate=true;
DB_USERNAME=sa
DB_PASSWORD=yourStrongPassword123!

# --- MAIL SERVER (SMTP) ---
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
# Use "App Password" (16 chars), NOT your login password
MAIL_PASSWORD=your-app-password-here 

# --- GOOGLE OAUTH2 ---
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# --- CLOUDINARY ---
CLOUD_NAME=your-cloud-name
CLOUD_API_KEY=your-api-key
CLOUD_API_SECRET=your-api-secret

# --- VNPAY ---
VN_PAY_TMN_CODE=your-tmn-code
VN_PAY_HASH_SECRET=your-hash-secret

# --- APP URLS ---
BACKEND_URL=[https://your-ngrok-url.ngrok-free.app](https://your-ngrok-url.ngrok-free.app)
FRONTEND_URL=http://localhost:3000

# CLOUDFLARE TUNNEL
CLOUDFLARE_TUNNEL_TOKEN=<your-token-tunnel>