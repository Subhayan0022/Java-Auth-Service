# Spring Boot JWT Auth Service

A production-ready JWT Authentication Microservice built with Spring Boot 3.5, featuring role-based access control, Redis caching, rate limiting, and full Docker support.

---

## Table of Contents

- [Architecture](#architecture)
- [ER Diagram](#er-diagram)
- [Security](#security)
- [API Reference](#api-reference)
- [Running Locally](#running-locally)
- [Tech Stack](#tech-stack)

---

## Architecture

```
                        ┌─────────────────────────────────────────────────┐
                        │                  Docker Network                  │
                        │                                                  │
  Client                │  ┌─────────────────────────────────────────┐    │
    │                   │  │            authservice-app               │    │
    │   HTTP :8080       │  │                                         │    │
    └───────────────────┼──►  ┌──────────────┐  ┌────────────────┐  │    │
                        │  │  │ RateLimiter  │  │  JwtAuthFilter │  │    │
                        │  │  │   Filter     │  │ (OncePerReq.)  │  │    │
                        │  │  └──────┬───────┘  └───────┬────────┘  │    │
                        │  │         │                   │           │    │
                        │  │  ┌──────▼───────────────────▼────────┐ │    │
                        │  │  │          Controllers               │ │    │
                        │  │  │  /auth/**  /api/user  /api/admin  │ │    │
                        │  │  └──────────────────┬────────────────┘ │    │
                        │  │                     │                   │    │
                        │  │  ┌──────────────────▼────────────────┐ │    │
                        │  │  │           Service Layer            │ │    │
                        │  │  │  UserRegister / UserLogin /        │ │    │
                        │  │  │  AdminService (@Cacheable)         │ │    │
                        │  │  └──────┬───────────────────┬────────┘ │    │
                        │  │         │                   │           │    │
                        │  │  ┌──────▼──────┐   ┌───────▼────────┐ │    │
                        │  │  │  PostgreSQL  │   │     Redis      │ │    │
                        │  │  │  :5432      │   │    :6379        │ │    │
                        │  │  └─────────────┘   └────────────────┘ │    │
                        │  └─────────────────────────────────────────┘    │
                        └─────────────────────────────────────────────────┘
```

**Request flow:**
1. Every request hits `RateLimiterFilter` first — `/auth/login` and `/auth/register` are capped at 5 req/min per IP
2. `JwtAuthFilter` extracts the Bearer token, validates it, and sets the `SecurityContext`
3. Spring Security enforces RBAC — `/api/user/**` requires `ROLE_USER`, `/api/admin/**` requires `ROLE_ADMIN`
4. The service layer reads from Redis cache before hitting PostgreSQL (admin endpoints only)

---

## ER Diagram

```
┌─────────────────────────────────────────────────────────┐
│                         users                           │
├──────────────────┬──────────────────────────────────────┤
│ id               │ UUID (PK, gen_random_uuid())          │
│ email            │ VARCHAR(255) NOT NULL UNIQUE          │
│ password         │ VARCHAR(255) NOT NULL  (BCrypt)       │
│ role             │ VARCHAR(20) NOT NULL DEFAULT 'USER'   │
│ salutation       │ VARCHAR(10) NOT NULL  (MR/MRS/OTHERS) │
│ first_name       │ VARCHAR(100) NOT NULL                 │
│ last_name        │ VARCHAR(100) NOT NULL                 │
│ phone_number     │ VARCHAR(20) NOT NULL                  │
│ date_of_birth    │ DATE NOT NULL                         │
│ created_at       │ TIMESTAMP NOT NULL DEFAULT now()      │
│ is_active        │ BOOLEAN NOT NULL DEFAULT TRUE         │
└──────────────────┴──────────────────────────────────────┘

Indexes
  idx_user_email ON users(email)

Roles
  USER  — default on registration, access to /api/user/**
  ADMIN — manually assigned, access to /api/admin/**

Notes
  - Passwords are never stored in plain text (BCrypt, strength 10)
  - Deletion is soft — is_active = false, record is never removed
  - Migrations managed by Flyway (V1 → V3)
```

---

## Security

### Authentication — JWT (HS256) + Refresh Tokens

- On login, two tokens are issued:
  - **Access token** — signed JWT (HS256), UUID as subject, expires in **15 minutes**
  - **Refresh token** — random UUID, stored in Redis, expires in **7 days**
- When the access token expires, the client uses the refresh token to get a new pair silently
- Refresh tokens are **rotated** on every use — old token is deleted, new one is issued
- Logout deletes the refresh token from Redis, immediately invalidating the session
- The JWT secret is a Base64-encoded 256-bit key set via environment variable `JWT_SECRET`
- Every protected request must include `Authorization: Bearer <access_token>`

### Authorization — RBAC

| Path              | Required role | Notes                        |
|-------------------|---------------|------------------------------|
| `/auth/register`  | None          | Register                     |
| `/auth/login`     | None          | Login                        |
| `/auth/refresh`   | None          | Refresh access token         |
| `/auth/logout`    | `ROLE_USER`   | Invalidate refresh token     |
| `/api/user/**`    | `ROLE_USER`   | Authenticated users          |
| `/api/admin/**`   | `ROLE_ADMIN`  | Admin-only CRUD              |
| `/actuator/**`    | None          | Health check (internal)      |
| `/swagger-ui/**`  | None          | API docs                     |

### Rate Limiting — Bucket4j

- Applied to `/auth/login`, `/auth/register`, and `/auth/refresh`
- **5 requests per minute per IP** (token bucket, greedy refill)
- Returns `429 Too Many Requests` when the bucket is empty

### Password Security

- BCrypt with default strength (10 rounds)
- Minimum 8 characters enforced at the API layer

### Stateless Sessions

- No server-side sessions; `SessionCreationPolicy.STATELESS`
- Each request is independently authenticated via the JWT

---

## API Reference

### Auth — Public

#### Register

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "salutation": "MR",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "securepass123",
    "phoneNumber": "+919876543210",
    "dateOfBirth": "1995-06-15"
  }'
```

Response `201 Created`:
```json
{
  "id": "a1b2c3d4-...",
  "salutation": "MR",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+919876543210",
  "dateOfBirth": "1995-06-15",
  "role": "USER"
}
```

#### Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "securepass123"
  }'
```

Response `200 OK`:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### Refresh

```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "<refreshToken>"}'
```

Response `200 OK`:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "661f9511-f30c-52e5-b827-557766551111"
}
```

#### Logout

```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "<refreshToken>"}'
```

Response `204 No Content`

---

### User — Requires `ROLE_USER` token

```bash
curl http://localhost:8080/api/user/userDetail \
  -H "Authorization: Bearer <token>"
```

---

### Admin — Requires `ROLE_ADMIN` token

#### Get user by ID

```bash
curl http://localhost:8080/api/admin/user/{userId} \
  -H "Authorization: Bearer <admin_token>"
```

#### Query users (with pagination and role filter)

```bash
# All users, page 0, 10 per page
curl "http://localhost:8080/api/admin/user/query?page=0&pageSize=10" \
  -H "Authorization: Bearer <admin_token>"

# Filter by role
curl "http://localhost:8080/api/admin/user/query?role=USER&page=0&pageSize=5" \
  -H "Authorization: Bearer <admin_token>"
```

Response `200 OK`:
```json
{
  "users": [ { "id": "...", "firstName": "John", "role": "USER", ... } ],
  "page": 0,
  "pageSize": 10,
  "totalUsers": 42
}
```

#### Update user (partial)

```bash
curl -X PATCH http://localhost:8080/api/admin/user/{userId} \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "role": "ADMIN"
  }'
```

#### Soft-delete user

```bash
curl -X DELETE http://localhost:8080/api/admin/user/{userId} \
  -H "Authorization: Bearer <admin_token>"
```

Response `204 No Content`

---

### Health

```bash
curl http://localhost:8080/actuator/health
```

---

## Running Locally

### With Docker (recommended)

**Prerequisites:** Docker and Docker Compose installed.

1. Clone the repo
2. Create a `.env` file in the project root:
   ```
   JWT_SECRET=<base64-encoded-256-bit-key>
   ```
   Generate a key: `openssl rand -base64 32`

3. Start all services:
   ```bash
   docker compose up -d
   ```

4. The app is available at `http://localhost:8080`

5. Stop everything:
   ```bash
   docker compose down
   ```

### Without Docker

**Prerequisites:** Java 17, Maven, PostgreSQL 16, Redis 7.

1. Create a PostgreSQL database named `authservice`
2. Set environment variables or edit `application.yaml`:
   ```
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/authservice
   SPRING_DATASOURCE_USERNAME=postgres
   SPRING_DATASOURCE_PASSWORD=yourpassword
   REDIS_HOST=localhost
   JWT_SECRET=<base64-encoded-256-bit-key>
   ```
3. Run:
   ```bash
   ./mvnw spring-boot:run
   ```

### Swagger UI

Available at `http://localhost:8080/swagger-ui/index.html` — use the lock icon to paste your Bearer token.

---

## Tech Stack

| Layer            | Technology                          |
|------------------|-------------------------------------|
| Framework        | Spring Boot 3.5.10                  |
| Language         | Java 17                             |
| Database         | PostgreSQL 16                       |
| Migrations       | Flyway                              |
| ORM              | Spring Data JPA / Hibernate         |
| Security         | Spring Security (stateless)         |
| JWT              | jjwt 0.12.6 (HS256)                 |
| Cache            | Redis 7 + Spring Cache              |
| Rate Limiting    | Bucket4j 8.10.1                     |
| API Docs         | SpringDoc OpenAPI 2.8.14 (Swagger)  |
| Health           | Spring Boot Actuator                |
| Boilerplate      | Lombok                              |
| Containerization | Docker + Docker Compose             |

#AI generated documentation only.