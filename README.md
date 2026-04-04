# 💰 Financial Service — Backend

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.13-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white"/>
  <img src="https://img.shields.io/badge/JWT-Auth-black?style=for-the-badge&logo=jsonwebtokens&logoColor=white"/>
  <img src="https://img.shields.io/badge/OpenPDF-1.3.30-red?style=for-the-badge"/>
</p>

<p align="center">
  A role-based Finance Dashboard backend built with Spring Boot, JWT Security, and MySQL.
  Supports financial record management, dashboard analytics, summary reports with PDF export, and full access control.
</p>

---

## 📌 Use Case Diagram

<p align="center">
  <img src="https://res.cloudinary.com/ddtcj9ks5/image/upload/v1775319001/Financial-Service-Use_Case_Diagram_w9drpj.png" alt="Use Case Diagram" width="420"/>
</p>

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
| Lombok | Latest | Boilerplate reduction |
| Bean Validation | Latest | Input validation |

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
jwt.secret=your_secret_key_here
jwt.expiration=3600000
```

**Step 3 — Run**
```bash
mvn clean install
mvn spring-boot:run
```

Server runs at: `http://localhost:8081`

---

## 🔐 Authentication

- **Register** — Creates a new user account. Default role assigned is `VIEWER`.
- **Login** — Returns a JWT token valid for 1 hour.
- All secured endpoints require the token in the request header:
  `Authorization: Bearer <token>`

---

## 👤 Role & Access Control

There are three roles in the system. Each role has different access levels.

| Endpoint | ADMIN | ANALYST | VIEWER |
|---|:---:|:---:|:---:|
| `/api/auth/**` | ✅ | ✅ | ✅ |
| `/api/users/**` | ❌ | ❌ | ✅ |
| `/api/analyst/**` | ❌ | ✅ | ❌ |
| `/api/admin/**` | ✅ | ❌ | ❌ |

- **VIEWER** — Can only view their own records and dashboard.
- **ANALYST** — Can view all records, filter them, and access insights.
- **ADMIN** — Full access: create, update, delete records, manage users, and generate reports.

---

## 💳 Financial Records

Each financial record contains: amount, type (`INCOME` or `EXPENSE`), category, date, and notes.

- `INCOME` means money was received by the user.
- `EXPENSE` means money was spent by the user.

Records support filtering by type, category, date range, amount range, and user. Soft delete is used — records are never permanently removed from the database.

---

## 📊 Dashboard

Returns a summary for one user or all users. Includes total income, total expenses, net balance, category-wise breakdown, monthly and weekly trends, and the 10 most recent transactions.

All filters are optional: date range, last N days, type, category, and amount range.

---

## 📋 Admin Report

Available only to ADMIN. Returns the same data as the dashboard, plus a per-user breakdown and the full list of filtered records.

A **PDF version** of the report can also be downloaded. The PDF is generated asynchronously in a background thread and includes all summary sections and the complete records table.

---

## ❌ Error Handling

| Situation | HTTP Status |
|---|---|
| Invalid input | `400 Bad Request` |
| Email already registered | `409 Conflict` |
| User not found | `404 Not Found` |
| Wrong email or password | `401 Unauthorized` |
| Token expired or invalid | `401 Unauthorized` |
| Insufficient role / permission | `403 Forbidden` |
| Unexpected server error | `500 Internal Server Error` |

---

## ✅ Assumptions

- New users get the `VIEWER` role by default.
- Soft delete is used — deleted records have `active = false`.
- Date format used throughout: `yyyy-MM-ddTHH:mm:ss`
- If both `days` and `from/to` are provided, `from/to` takes priority.
- Net balance is always calculated as `totalIncome - totalExpense`.
- PDF generation runs asynchronously so it does not block the main thread.

---

## 📄 License

This project is built for assessment purposes.
