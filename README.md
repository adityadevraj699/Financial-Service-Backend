# 💰 Financial Service — Backend

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.13-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white"/>
  <img src="https://img.shields.io/badge/JWT-Auth-black?style=for-the-badge&logo=jsonwebtokens&logoColor=white"/>
  <img src="https://img.shields.io/badge/OpenPDF-1.3.30-red?style=for-the-badge"/>
</p>

<p align="center">
  A role-based Finance Dashboard backend system — built with Spring Boot, JWT Security, and MySQL.
  Supports financial record management, dashboard analytics, summary reports (with PDF export), and full access control.
</p>

---

## 📌 Use Case Diagram

<p align="center">
  <img src="https://res.cloudinary.com/ddtcj9ks5/image/upload/v1775319001/Financial-Service-Use_Case_Diagram_w9drpj.png" alt="Use Case Diagram" width="420"/>
</p>

---

## 📁 Project Structure

```
com.Financial.service
│
├── config/
│   ├── JwtService.java              → Token generate & validate karna
│   ├── JwtFilter.java               → Har request pe token check
│   ├── SecurityConfig.java          → Role-based access control
│   ├── AsyncConfig.java             → @Async enable karna (PDF generation)
│   └── PasswordConfig.java          → BCrypt encoder
│
├── controller/
│   ├── AuthController.java          → /api/auth/** (login, register)
│   ├── AdminController.java         → /api/admin/** (users, records)
│   ├── AnalystController.java       → /api/analyst/** (view + filter)
│   ├── UserController.java          → /api/users/** (own records)
│   ├── DashboardController.java     → /api/v1/dashboard
│   └── ReportController.java        → /api/v1/admin/reports
│
├── service/
│   ├── AuthService.java
│   ├── FinancialRecordService.java
│   ├── DashboardService.java
│   └── ReportService.java
│
├── dto/
│   ├── FinancialFilterRequest.java  → Common filter DTO (dashboard + report)
│   ├── FinancialRecordRequest.java
│   ├── FinancialRecordResponse.java
│   ├── DashboardResponse.java
│   └── report/
│       └── ReportResponse.java
│
├── entity/
│   ├── Users.java
│   └── FinancialRecord.java
│
├── util/
│   └── PdfReportUtil.java           → Async PDF generator (OpenPDF)
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── UserNotFoundException.java
│   ├── UserAlreadyExistsException.java
│   └── InvalidCredentialsException.java
│
└── repository/
    ├── UserRepository.java
    └── FinancialRecordRepository.java
```

---

## 🔧 Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Language |
| Spring Boot | 3.5.13 | Framework |
| Spring Security | Latest | Authentication & Authorization |
| JWT (jjwt) | 0.11.5 | Token generation & validation |
| Hibernate / JPA | 6.6.x | ORM |
| MySQL | 8.0.43 | Database |
| BCrypt | — | Password hashing |
| OpenPDF | 1.3.30 | PDF report generation |
| Lombok | Latest | Boilerplate reduce |
| Bean Validation | Latest | `@Valid` annotations |

---

## ⚙️ Setup & Run

### 1. Prerequisites

- Java 21+
- Maven 3.8+
- MySQL 8.0+

### 2. Database setup

```sql
CREATE DATABASE financial_service;
```

### 3. application.properties configure karo

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/financial_service
spring.datasource.username=your_username
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

jwt.secret=your_secret_key_here
jwt.expiration=3600000
```

### 4. Run karo

```bash
mvn clean install
mvn spring-boot:run
```

Server start hoga: `http://localhost:8081`

---

## 🔐 Authentication Flow

### Register — `POST /api/auth/register`

```json
// Request
{
  "name": "Rahul Sharma",
  "email": "rahul@example.com",
  "password": "rahul@123"
}

// Response — 201 Created
{
  "success": true,
  "message": "User registered successfully",
  "data": { "id": "uuid", "name": "Rahul Sharma", "email": "rahul@example.com", "role": "VIEWER" }
}
```

### Login — `POST /api/auth/login`

```json
// Request
{
  "email": "rahul@example.com",
  "password": "rahul@123"
}

// Response — 200 OK
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGci...",
    "tokenType": "Bearer",
    "user": { "id": "uuid", "name": "Rahul Sharma", "role": "ADMIN" }
  }
}
```

> Aage sabhi requests mein Header mein bhejo:
> `Authorization: Bearer eyJhbGci...`

---

## 👤 Role Permissions

| Endpoint | ADMIN | ANALYST | VIEWER |
|---|:---:|:---:|:---:|
| `/api/auth/**` | ✅ | ✅ | ✅ |
| `/api/users/**` | ❌ | ❌ | ✅ |
| `/api/analyst/**` | ❌ | ✅ | ❌ |
| `/api/admin/**` | ✅ | ❌ | ❌ |

---

## 💳 Financial Records API

| Method | Endpoint | Role | Description |
|---|---|---|---|
| `POST` | `/api/v1/records` | ADMIN | Record create karo |
| `GET` | `/api/v1/records/{id}` | ALL | Single record by ID |
| `GET` | `/api/v1/records/filter` | ALL | Filtered records |
| `GET` | `/api/v1/records/recent` | ALL | Recent activity |
| `PUT` | `/api/v1/records/{id}` | ADMIN | Record update karo |
| `DELETE` | `/api/v1/records/{id}` | ADMIN | Soft delete |

### Filter Parameters (sabhi optional)

| Param | Type | Example | Description |
|---|---|---|---|
| `userId` | String | `u1` | Specific user, null = all |
| `type` | String | `INCOME` / `EXPENSE` | Transaction type |
| `category` | String | `FOOD` / `SALARY` | Category filter |
| `from` | String | `2024-01-01T00:00:00` | Date range start |
| `to` | String | `2024-03-31T23:59:59` | Date range end |
| `minAmount` | String | `100` | Minimum amount |
| `maxAmount` | String | `5000` | Maximum amount |
| `days` | Integer | `7` / `30` / `90` | Last N days |

```
# Example calls
GET /api/v1/records/filter?userId=u1&type=INCOME&days=30
GET /api/v1/records/recent?userId=u1&days=7
GET /api/v1/records/filter?category=FOOD&minAmount=500&maxAmount=5000
```

---

## 📊 Dashboard API

> Sabke liye — All params optional

```
GET /api/v1/dashboard
```

| Param | Description |
|---|---|
| `userId` | Specific user (null = all users) |
| `days` | Last N days (7 / 15 / 30 / 90) |
| `from` / `to` | Custom date range (overrides days) |
| `type` | `INCOME` / `EXPENSE` |
| `category` | Category filter |
| `minAmount` / `maxAmount` | Amount range |

### Dashboard Response

```json
{
  "totalIncome": 150000,
  "totalExpense": 45000,
  "netBalance": 105000,

  "categoryWiseTotals": {
    "SALARY": 150000,
    "FOOD": -15000,
    "RENT": -30000
  },

  "typeAndCategoryWiseTotals": {
    "INCOME": { "SALARY": 150000 },
    "EXPENSE": { "FOOD": 15000, "RENT": 30000 }
  },

  "monthlyTrends": { "2024-01": 40000, "2024-02": 35000 },
  "weeklyTrends":  { "2024-W01": 10000, "2024-W02": -5000 },

  "recentActivities": [ "...last 10 records..." ],

  "totalRecords": 120,
  "appliedDateRange": "Last 30 days"
}
```

---

## 📋 Admin Report API

> Sirf ADMIN ke liye

```
GET /api/v1/admin/reports         → JSON report
GET /api/v1/admin/reports/download → PDF download
```

### Report Response — extra fields (Dashboard se zyada)

```json
{
  "reportGeneratedAt": "2024-04-01T10:30:00",
  "appliedUserId": "ALL",
  "appliedType": "ALL",
  "appliedCategory": "ALL",

  "typeWiseTotals": { "INCOME": 150000, "EXPENSE": 45000 },

  "perUserSummary": {
    "u1": {
      "userId": "u1", "userName": "Rahul",
      "totalIncome": 80000, "totalExpense": 20000, "netBalance": 60000,
      "categoryWiseTotals": { "SALARY": 80000, "FOOD": -10000 }
    }
  },

  "records": [ "...sabhi filtered records..." ]
}
```

### PDF Download

```
GET /api/v1/admin/reports/download?days=30&type=EXPENSE
```

PDF mein include hoga:
- Title header with generation timestamp
- Applied filters info
- Overall summary (Income / Expense / Net Balance)
- Category wise totals
- Type & Category breakdown
- Monthly trends
- Per user summary
- All records table

---

## ❌ Exception Handling

| Exception | HTTP Status | Message |
|---|---|---|
| `@Valid` fail | `400 Bad Request` | Field-wise validation errors |
| `UserAlreadyExistsException` | `409 Conflict` | Email already registered |
| `UserNotFoundException` | `404 Not Found` | User not found |
| `InvalidCredentialsException` | `401 Unauthorized` | Invalid email or password |
| Token expired | `401 Unauthorized` | Token has expired |
| Token invalid | `401 Unauthorized` | Token signature is invalid |
| Role mismatch | `403 Forbidden` | You don't have permission |
| Unexpected error | `500 Internal Server Error` | Something went wrong |

---

## 📦 Uniform API Response Format

Sabhi responses isi format mein aate hain:

```json
{
  "success": true,
  "message": "Human readable message",
  "data": { "..." },
  "timestamp": "2026-04-03T00:45:00"
}
```

---

## 🗄️ Database Schema

### `users` table

| Column | Type | Description |
|---|---|---|
| `id` | VARCHAR (UUID) | Primary key |
| `name` | VARCHAR | User ka naam |
| `email` | VARCHAR (unique) | Login email |
| `password` | VARCHAR | BCrypt hash |
| `role` | ENUM | `ADMIN` / `ANALYST` / `VIEWER` |
| `active` | BOOLEAN | Account active hai ya nahi |
| `created_at` | DATETIME | Auto set on insert |
| `updated_at` | DATETIME | Auto set on update |
| `last_login` | DATETIME | Last login time |

### `financial_records` table

| Column | Type | Description |
|---|---|---|
| `id` | VARCHAR (UUID) | Primary key |
| `amount` | DECIMAL(15,2) | Transaction amount |
| `type` | ENUM | `INCOME` / `EXPENSE` |
| `category` | ENUM | `SALARY`, `FOOD`, `RENT`, etc. |
| `date` | DATETIME | Transaction date |
| `notes` | VARCHAR(500) | Optional description |
| `active` | BOOLEAN | Soft delete flag |
| `user_id` | VARCHAR (FK) | Reference to users table |
| `created_at` | DATETIME | Auto set |
| `updated_at` | DATETIME | Auto set |

---

## 🔑 JWT Token Structure

```
Header.Payload.Signature

Payload:
{
  "sub":  "user@example.com",   ← email
  "role": "ADMIN",              ← role
  "iat":  1700000000,           ← issued at
  "exp":  1700003600            ← expires in 1 hour
}
```

---

## ✅ Assumptions Made

- Default role on register is `VIEWER`
- Soft delete — records `active = false` ho jaate hain, delete nahi hote
- `INCOME` ka matlab paisa add hua, `EXPENSE` ka matlab paisa cut hua
- Date format: `yyyy-MM-ddTHH:mm:ss` (ISO LocalDateTime)
- `from/to` custom range, `days` se priority zyada hai
- PDF generation async hoti hai — background thread mein chalti hai
- `netBalance = totalIncome - totalExpense`

---

## 📄 License

This project is for assessment purposes.
