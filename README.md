<div align="center">
  <h1>🛒 E-Commerce Backend API</h1>
  <p>A robust, feature-rich backend API built with Spring Boot 3 and Java 21, designed to power modern e-commerce platforms.</p>
</div>

<hr />

## ✨ Features

- **🔐 Security & Authentication:** 
  - OAuth2 Resource Server implementation using JWT.
  - Asymmetric RSA key pair for secure token signing and verification.
  - Google OAuth2 Integration.
- **💾 Database & Persistence:** 
  - Microsoft SQL Server integration via Spring Data JPA.
  - High-performance caching enabled by Caffeine.
- **🔌 Real-time Communication:** 
  - Bidirectional communication via WebSocket.
  - Server-Sent Events (SSE) for instant server-to-client notifications.
- **💳 Payment Gateway:** 
  - Seamless integration with **VNPay** for secure and frictionless transaction processing.
- **☁️ Cloud Storage:** 
  - **Cloudinary** integration for optimized media upload and management.
- **🛠️ Utilities & Architecture:** 
  - **MapStruct** for automatic and efficient Entity-to-DTO mapping.
  - **Lombok** to reduce boilerplate code.
  - Email notifications via Spring Mail.
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
- [Docker](https://www.docker.com/) (Optional, but recommended for running MSSQL via containers)
- Microsoft SQL Server (if not using Docker)

## ⚙️ Environment Configuration

The application requires several environment variables to function correctly. Create a `.env` file in the root directory (or use the existing one) and configure the following parameters:

```env
# --- DATABASE ---
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=YourDatabase;encrypt=true;trustServerCertificate=true;
DB_USERNAME=sa
DB_PASSWORD=yourStrongPassword123!

# --- MAIL SERVER ---
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

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
BACKEND_URL=https://your-ngrok-url.ngrok-free.app
FRONTEND_URL=http://localhost:3000

#CLOUDFLARE TUNNEL
CLOUDFLARE_TUNNEL_TOKEN=<your-token-tunnel>
```

> **Note**: For JWT authentication to work, ensure that your RSA keys (`private.pem` and `public.pem`) are correctly placed in your classpath (e.g., `src/main/resources/`).

## 🌐 Local HTTPS Tunneling (For HttpOnly Cookies)

Because the project relies on **`HttpOnly` cookies** for security (e.g., storing refresh tokens or session IDs), modern browsers require an HTTPS connection to set and send these cookies across different domains/ports (like when the frontend runs on `http://localhost:3000` and the backend on `http://localhost:8080`).

To test locally with HTTPS securely, a **Cloudflare Tunnel** has been integrated directly into the Docker Compose configuration. Alternatively, you can use **Ngrok** for manual setup.

### Using Cloudflare Tunnel (Recommended, via Docker)
1. Obtain your Cloudflare Tunnel token from your Cloudflare Zero Trust dashboard.
2. Add the token to your `.env` file:
   ```env
   CLOUDFLARE_TUNNEL_TOKEN=<your-token-tunnel>
   ```
3. When you start the application using Docker Compose, the `tunnel` service will automatically run and route traffic safely to your backend.

### Using Ngrok (Alternative)
1. Install [Ngrok](https://ngrok.com/download).
2. Assuming the backend runs on port `8080`, run this command in your terminal:
   ```bash
   ngrok http 8080
   ```
3. Update the `BACKEND_URL` in your `.env` file and frontend configuration with the provided HTTPS URL (e.g., `https://<random-id>.ngrok-free.app`).

## 🛠️ Getting Started

### Option 1: Running with Docker Compose (Recommended)

1. Clone the repository and navigate into the directory:
   ```bash
   git clone <repository-url>
   cd ECommerce
   ```
2. Ensure your `.env` file is properly configured.
3. Start the application and the SQL Server database:
   ```bash
   docker-compose up -d --build
   ```

### Option 2: Running Locally with Maven

1. Start your local Microsoft SQL Server instance and ensure the database specified in `DB_URL` exists.
2. Build the project:
   ```bash
   mvn clean install -DskipTests
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
   The backend server will start on port `8080`.

## 📚 API Documentation

Once the application is up and running, you can explore, test, and interact with the REST API endpoints via the Swagger UI:

👉 **[Access Swagger UI](http://localhost:8080/swagger-ui.html)**

---
*Developed with ❤️ using Spring Boot.*
