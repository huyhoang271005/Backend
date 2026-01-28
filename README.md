# Hello Project

This is a Spring Boot application designed to demonstrate a variety of modern backend features, including authenticated REST APIs, real-time communication, payment integration, and cloud file storage.

## Key Features

*   **REST API**: Built with Spring Web to provide robust endpoints.
*   **Security**: Secured with OAuth2 Resource Server implementation using JWT, backed by RSA public/private key pairs for authentication.
*   **Database**: Integration with **SQL Server (MSSQL)** using Spring Data JPA for persistence.
*   **Real-time Communication**:
    *   **WebSocket**: Support for bidirectional communication.
    *   **Server-Sent Events (SSE)**: For real-time server-to-client updates.
*   **Payment Integration**: Integrated with **VNPay** for processing payments.
*   **File Storage**: **Cloudinary** integration for managing and serving media assets.
*   **Caching**: High-performance caching enabled via **Caffeine**.
*   **Documentation**: Interactive API documentation provided by **SpringDoc (Swagger UI)**.
*   **Utilities**:
    *   **MapStruct**: For efficient entity-to-DTO mapping.
    *   **Lombok**: To reduce boilerplate code.
    *   **Validation**: For request data integrity checks.
    *   **Email**: Email sending support via Spring Mail.

## Prerequisites

Before running the application, ensure you have the following installed:

*   **Java 21**: The project requires JDK 21.
*   **Maven**: For building and dependency management.
*   **SQL Server**: A running instance of MSSQL Server.

## Configuration

The application is configured via `src/main/resources/application.yml`. You may need to update the following settings to match your environment:

*   **Database**: Update `spring.datasource.url`, `username`, and `password`.
*   **Mail Server**: Configure SMTP settings in `spring.mail` if email features are used.
*   **Cloudinary**: Add your `cloud-name`, `api-key`, and `api-secret`.
*   **VNPay**: Update `tmnCode`, `hashSecret`, and URLs for payment testing.
*   **Security**: Ensure `private.pem` and `public.pem` are present in the classpath for JWT signing/verification.

## Running the Application

1.  **Clone the repository**:
    ```bash
    git clone <repository-url>
    cd hello
    ```

2.  **Build the project**:
    ```bash
    mvn clean install
    ```

3.  **Run the application**:
    ```bash
    mvn spring-boot:run
    ```
    The server will start on port `8080`.

## API Documentation

Once the application is running, you can explore the API endpoints using Swagger UI:

```
http://localhost:8080/swagger-ui.html
```
