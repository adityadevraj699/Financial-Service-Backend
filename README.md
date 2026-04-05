# 💰 Financial Service — Backend

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.13-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white"/>
  <img src="https://img.shields.io/badge/JWT-Auth-black?style=for-the-badge&logo=jsonwebtokens&logoColor=white"/>
  <img src="https://img.shields.io/badge/OpenPDF-1.3.30-red?style=for-the-badge"/>
</p>

<p align="center">
  A role-based Finance Dashboard backend built with Spring Boot, JWT Security, and MySQL.<br/>
  Supports financial record management, dashboard analytics, summary reports with PDF export, and full role-based access control.
</p>

---

## 📑 Table of Contents

- [Tech Stack](#-tech-stack)
- [Architecture Overview](#-architecture-overview)
- [How It Works](#-how-it-works)
- [Role-Based Access Control](#-role-based-access-control)
- [Setup & Run](#-setup--run)
- [API Reference](#-api-reference)
- [Error Handling](#-error-handling)
- [Diagrams](#-diagrams)
- [Assumptions](#-assumptions)

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
| OpenPDF | 1.3.30 | Async PDF report generation |
| Lombok | Latest | Boilerplate reduction |
| Bean Validation | Latest | Input validation |

---

## 🏗️ Architecture Overview

```
Client (Postman / Frontend)
        │
        ▼
  JwtFilter (intercepts every request)
        │
        ├── Invalid/missing token → 401 Unauthorized
        │
        ▼
  Spring Security (role-based routing)
        │
        ├── /api/auth/**   → Public (no auth needed)
        ├── /api/admin/**  → ADMIN only
        ├── /api/analyst/** → ANALYST only
        └── /api/viewer/** → VIEWER only
                │
                ▼
        Controllers (AdminController / AnalyticsController / ViewersController)
                │
                ▼
        Service Layer (FinancialService / AdminService / AnalystService / ViewersService)
                │
                ▼
        Repository Layer (JPA Repositories)
                │
                ▼
        MySQL Database
```

**Key design decisions:**
- JWT token stores `userId` as a custom claim — no extra DB call needed to identify the logged-in user
- Soft delete is used — records are never permanently removed (`active = false`)
- PDF generation runs asynchronously via `CompletableFuture` so it never blocks the main thread
- Three separate controllers enforce role isolation at the API level

---

## ⚙️ How It Works

### 1. Authentication Flow

When a user logs in, the system:
1. Finds the user by email in the database
2. Verifies the password using BCrypt
3. Generates a JWT token containing `email`, `role`, and `userId`
4. Returns the token to the client

Every subsequent request must include this token in the `Authorization` header.

### 2. Request Lifecycle

Every secured request passes through `JwtFilter` before reaching any controller:

```
Request arrives
    │
    ├─ No token? → pass through (Spring Security will reject if endpoint needs auth)
    │
    ├─ Token present → extract email, role, userId
    │
    ├─ Token invalid/expired? → 401 Unauthorized
    │
    └─ Valid → set Authentication in SecurityContext
                    (principal = email, details = userId, authority = ROLE_xxx)
```

Controllers then read `userId` directly from the security context — **no DB call needed**:

```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String userId = (String) auth.getDetails(); // from JWT claim, not DB
```

### 3. Financial Records

Each record stores: `amount`, `type` (INCOME/EXPENSE), `category`, `date`, `notes`, and a reference to the user who owns it.

Records support filtering by:
- `type` — INCOME or EXPENSE
- `category` — SALARY, FOOD, RENT, etc.
- `from` / `to` — date range
- `min` / `max` — amount range
- `userId` — specific user (Admin/Analyst only)

### 4. Dashboard & Reports

The dashboard aggregates data for a user (or all users) and returns:
- Total income, total expense, net balance
- Category-wise breakdown
- Monthly and weekly trends
- 10 most recent transactions

Reports include all dashboard data plus a per-user summary and the full filtered record list. A PDF version is generated asynchronously using OpenPDF.

---

## 🔐 Role-Based Access Control

The system has three roles. Each role has strictly separate API endpoints and access levels.

### Role Permissions

| Feature | ADMIN | ANALYST | VIEWER |
|---|:---:|:---:|:---:|
| Login | ✅ | ✅ | ✅ |
| Create user | ✅ | ❌ | ❌ |
| Toggle user active/inactive | ✅ | ❌ | ❌ |
| Update user role | ✅ | ❌ | ❌ |
| Get all users | ✅ | ✅ (active only) | ❌ |
| Create financial record | ✅ | ❌ | ❌ |
| Update financial record | ✅ | ❌ | ❌ |
| Delete financial record | ✅ | ❌ | ❌ |
| View all records (any user) | ✅ | ✅ | ❌ |
| View own records only | ✅ | ✅ | ✅ |
| View dashboard (any user) | ✅ | ✅ | ❌ |
| View own dashboard | ✅ | ✅ | ✅ |
| Generate reports | ✅ | ✅ | ✅ (own only) |
| Download PDF report | ✅ | ✅ | ✅ (own only) |
| View own profile | ❌ | ❌ | ✅ |

### How Role Isolation Works

Spring Security enforces role restrictions at the URL level:

```java
.requestMatchers("/api/auth/**").permitAll()
.requestMatchers("/api/admin/**").hasRole("ADMIN")
.requestMatchers("/api/analyst/**").hasRole("ANALYST")
.requestMatchers("/api/viewer/**").hasRole("VIEWER")
```

VIEWER endpoints automatically restrict data to the logged-in user's own records by reading `userId` from the JWT token — the `userId` is never accepted as a request parameter for viewer endpoints.

---

## ⚙️ Setup & Run

**Prerequisites:** Java 21+, Maven 3.8+, MySQL 8.0+

**Step 1 — Create database**
```sql
CREATE DATABASE financial_service;
```

**Step 2 — Configure `application.properties`**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/financial_service
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
jwt.secret=your_secret_key_min_32_chars_here
jwt.expiration=3600000
```

**Step 3 — Run**
```bash
mvn clean install
mvn spring-boot:run
```

Server runs at: `http://localhost:8081`

> All secured endpoints require: `Authorization: Bearer <token>`

---

## 📡 API Reference

### 🔓 Auth — `/api/auth`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/auth/login` | Login and get JWT token | ❌ Public |

**Login request body:**
```json
{
  "email": "user@example.com",
  "password": "yourpassword"
}
```

**Login response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": "uuid",
      "name": "John Doe",
      "email": "user@example.com",
      "role": "VIEWER"
    }
  }
}
```

---

### 👑 Admin — `/api/admin` *(ADMIN only)*

#### User Management

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/admin/user` | Create a new user |
| `GET` | `/api/admin/users` | Get all users (optional `?role=VIEWER`) |
| `PATCH` | `/api/admin/users/{userId}/status` | Toggle user active/inactive |
| `PATCH` | `/api/admin/user/{userId}/role` | Update user role (`?role=ANALYST`) |

**Create user request body:**
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "password": "securepass",
  "role": "ANALYST"
}
```

#### Financial Record Management

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/admin/record` | Create a financial record |
| `PUT` | `/api/admin/record/{recordId}` | Update a financial record |
| `DELETE` | `/api/admin/record/{recordId}` | Soft delete a record |

**Record request body:**
```json
{
  "userId": "uuid-of-user",
  "amount": 5000.00,
  "type": "INCOME",
  "category": "SALARY",
  "date": "2024-01-15T09:00:00",
  "notes": "January salary"
}
```

#### Records & Analytics

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/admin/records` | Get all records with optional filters |
| `GET` | `/api/admin/recent` | Get recent activities |
| `GET` | `/api/admin/dashboard` | Get dashboard summary |
| `GET` | `/api/admin/reports` | Get report (JSON) |
| `GET` | `/api/admin/reports/download` | Download report as PDF |

**Filter query parameters** (all optional):

| Parameter | Description | Example |
|---|---|---|
| `userId` | Filter by specific user | `?userId=abc-123` |
| `type` | INCOME or EXPENSE | `?type=INCOME` |
| `category` | Record category | `?category=SALARY` |
| `from` | Start date | `?from=2024-01-01T00:00:00` |
| `to` | End date | `?to=2024-12-31T23:59:59` |
| `min` | Minimum amount | `?min=1000` |
| `max` | Maximum amount | `?max=50000` |
| `days` | Last N days (recent only) | `?days=30` |

---

### 📊 Analyst — `/api/analyst` *(ANALYST only)*

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/analyst/users` | Get all active users |
| `GET` | `/api/analyst/records` | Get records with filters (any user) |
| `GET` | `/api/analyst/recent` | Get recent activities |
| `GET` | `/api/analyst/dashboard` | View dashboard for any user |
| `GET` | `/api/analyst/reports` | Generate report (JSON) |
| `GET` | `/api/analyst/reports/download` | Download report as PDF |

Same filter parameters as Admin apply here. Analyst can filter by any `userId` but cannot modify any data.

---

### 👤 Viewer — `/api/viewer` *(VIEWER only)*

> `userId` is automatically taken from the JWT token — viewers can only see their own data.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/viewer/records` | Get own records with filters |
| `GET` | `/api/viewer/recent` | Get own recent activities |
| `GET` | `/api/viewer/dashboard` | View own dashboard |
| `GET` | `/api/viewer/reports` | Generate own report (JSON) |
| `GET` | `/api/viewer/reports/download` | Download own report as PDF |
| `GET` | `/api/viewer/profile` | Get own profile |

Filter parameters available for viewers (no `userId` — always uses token):

| Parameter | Description |
|---|---|
| `type` | INCOME or EXPENSE |
| `category` | Record category |
| `from` / `to` | Date range |
| `min` / `max` | Amount range |
| `days` | Last N days |

---

### 📦 Category Values

**INCOME categories:** `SALARY`, `FREELANCE`, `INVESTMENT`, `BUSINESS`, `OTHER_INCOME`

**EXPENSE categories:** `FOOD`, `RENT`, `TRANSPORT`, `SHOPPING`, `ENTERTAINMENT`, `HEALTHCARE`, `EDUCATION`, `UTILITIES`, `OTHER_EXPENSE`

---

## ❌ Error Handling

All errors return a consistent `ApiResponse` structure:

```json
{
  "success": false,
  "message": "Error description here",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

| Situation | HTTP Status |
|---|---|
| Invalid input / missing fields | `400 Bad Request` |
| Wrong email or password | `401 Unauthorized` |
| Token expired or invalid | `401 Unauthorized` |
| Insufficient role / permission | `403 Forbidden` |
| User or record not found | `404 Not Found` |
| Email already registered | `409 Conflict` |
| PDF generation failed | `500 Internal Server Error` |
| Unexpected server error | `500 Internal Server Error` |

Validation errors return field-level details:
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "must not be blank",
    "amount": "must be greater than 0"
  }
}
```

---

## 📐 Diagrams

### Use Case Diagram
<p align="center">
  <img src="https://res.cloudinary.com/ddtcj9ks5/image/upload/v1775381619/USD_aguq8n.png" alt="Use Case Diagram" width="700"/>
</p>

---

### Class Diagram
<p align="center">
  <img src="https://res.cloudinary.com/ddtcj9ks5/image/upload/v1775381619/CD_wswhuo.png" alt="Class Diagram" width="700"/>
</p>

---

### Sequence Diagram
<p align="center">
  <img src="https://res.cloudinary.com/ddtcj9ks5/image/upload/v1775381619/SD_xxutqp.png" alt="Sequence Diagram" width="700"/>
</p>

---

### Data Flow Diagram (DFD)
<p align="center">
  <img src="https://res.cloudinary.com/ddtcj9ks5/image/upload/v1775381620/DFD_xtj4ff.png" alt="Data Flow Diagram" width="700"/>
</p>

---

### Activity Diagram
<p align="center">
  <img src="https://res.cloudinary.com/ddtcj9ks5/image/upload/v1775381619/AD_jd0jpr.png" alt="Activity Diagram" width="700"/>
</p>

---

## ✅ Assumptions

- New users get the `VIEWER` role by default.
- Soft delete is used — deleted records have `active = false` and never appear in queries.
- Date format used throughout: `yyyy-MM-ddTHH:mm:ss`
- If both `days` and `from/to` are provided, `from/to` takes priority.
- Net balance is always calculated as `totalIncome - totalExpense`.
- PDF generation runs asynchronously so it does not block the main thread.
- JWT token is valid for 1 hour. After expiry, the user must log in again.
- The `userId` stored in the JWT token is a UUID string generated by the database.

---
