# 💰 Ledger — Full-Stack Personal Finance Tracker

A production-ready full-stack personal bookkeeping application. Register, track your income and expenses by category, and visualize your financial habits with monthly summaries, category breakdowns and trend charts.

Built with **Vue 3 + Spring Boot + MySQL** — a classic, interview-friendly full-stack stack.

---

## ✨ Features

- **Authentication** — JWT-based sign-up / sign-in with BCrypt password hashing
- **Bookkeeping** — create, edit, delete income & expense records (amount, category, date, note)
- **Categories** — 13 system-preset categories (🍜 餐饮, 💰 工资, …) plus user-defined categories
- **Statistics** — monthly income / expense / balance, category share donut charts, 6-month trend lines
- **Filtering & pagination** — filter by month / type / category, server-side pagination
- **Security basics** — passwords never stored in plaintext, per-user data isolation, centralized error handling
- **Deployment ready** — Dockerfiles + `docker-compose` (MySQL + backend + Nginx frontend)

## 🧰 Tech Stack

| Layer       | Technology                                                                    |
|-------------|-------------------------------------------------------------------------------|
| Frontend    | Vue 3, Vite, Vue Router, Pinia, Element Plus, ECharts, Axios                  |
| Backend     | Java 21, Spring Boot 4.1, Spring Data JPA, Spring MVC, Validation             |
| Security    | JWT (jjwt), BCrypt (spring-security-crypto)                                   |
| Database    | MySQL 8 (production), H2 in-memory (zero-install development)                 |
| Build / Run | Maven Wrapper, npm, Docker Compose                                            |
| Testing     | JUnit 5, Spring Boot Test, MockMvc (end-to-end API flow)                      |

## 🏗 Architecture

```
┌─────────────────┐        ┌──────────────────────┐        ┌─────────────────┐
│   Vue 3 SPA     │  HTTP  │   Spring Boot API    │  JPA   │    Database     │
│  (Vite / Nginx) │ ─────► │  Controller → Service│ ─────► │ MySQL / H2      │
│                 │  JWT   │  → Repository        │        │                 │
└─────────────────┘        └──────────────────────┘        └─────────────────┘
```

- The SPA calls `/api/**`; a JWT `Authorization: Bearer` header authenticates every request.
- A `HandlerInterceptor` validates the token and injects the current user id; every query is scoped to that user.
- Layered design: `Controller → Service → Repository`, DTOs on the boundary, business errors mapped by a global exception handler.

## 📁 Project Structure

```
.
├── backend/                 # Spring Boot application
│   ├── src/main/java/com/ledger/
│   │   ├── auth/            # register / login / me
│   │   ├── category/        # preset + user categories
│   │   ├── transaction/     # income & expense records (CRUD, filters)
│   │   ├── stats/           # summary / category breakdown / trend
│   │   ├── security/        # JWT util + interceptor
│   │   ├── config/          # CORS, interceptor registration, data seeding
│   │   └── common/          # API envelope, exceptions, global handler
│   ├── src/test/            # context test + end-to-end API flow test
│   └── pom.xml
├── frontend/                # Vue 3 SPA
│   ├── src/
│   │   ├── api/             # axios instance + typed API modules
│   │   ├── stores/          # Pinia auth store
│   │   ├── router/          # routes + auth guard
│   │   ├── layout/          # sidebar shell
│   │   └── views/           # login / register / dashboard / transactions / categories
│   └── vite.config.js       # dev & preview proxy to backend
├── docker-compose.yml       # MySQL + backend + frontend
└── README.md
```

## 🚀 Getting Started

### Prerequisites

- **Java 21+** (tested with JDK 21 / 25)
- **Node.js 20+** & npm
- Maven is **not** required — the backend ships a Maven Wrapper (`mvnw`).
- MySQL 8 is optional (see Option A).

### Option A — Local development with H2 (zero database install)

```bash
# 1. Start the backend (H2 in-memory database, auto-seeded with preset categories)
cd backend
./mvnw spring-boot:run

# 2. Start the frontend (dev server proxies /api to localhost:8080)
cd ../frontend
npm install
npm run dev
```

Open **http://localhost:5173**, register an account and start bookkeeping.
H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:ledger`, user `sa`, empty password).

### Option B — Local development with MySQL

```bash
# create the database once
mysql -u root -p -e "CREATE DATABASE ledger CHARACTER SET utf8mb4;"

cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
# or with custom connection settings:
#   DB_HOST=localhost DB_PORT=3306 DB_NAME=ledger DB_USER=root DB_PASSWORD=yourpass \
#   ./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

Tables are created automatically by Hibernate (`ddl-auto=update`).

### Option C — Docker Compose (full stack)

```bash
docker compose up --build
```

- Frontend: http://localhost
- Backend API: http://localhost:8080/api
- MySQL: localhost:3306 (user `root`, password `root`, database `ledger`)

> ⚠️ Change `JWT_SECRET` and DB credentials before any real deployment.

## 🔌 API Overview

Base URL: `/api` — every endpoint except `POST /auth/register` and `POST /auth/login` requires `Authorization: Bearer <token>`.

| Method | Path                     | Description                          |
|--------|--------------------------|--------------------------------------|
| POST   | `/auth/register`         | Create account, returns JWT + user   |
| POST   | `/auth/login`            | Login, returns JWT + user            |
| GET    | `/auth/me`               | Current user profile                 |
| GET    | `/categories`            | List categories (`?type=EXPENSE`)    |
| POST   | `/categories`            | Create a user category               |
| PUT    | `/categories/{id}`       | Update a user category               |
| DELETE | `/categories/{id}`       | Delete a category (transactions detached) |
| GET    | `/transactions`          | List records (`?month=2026-10&type=&categoryId=&page=&size=`) |
| POST   | `/transactions`          | Create a record                      |
| PUT    | `/transactions/{id}`     | Update a record                      |
| DELETE | `/transactions/{id}`     | Delete a record                      |
| GET    | `/stats/summary`         | `?month=2026-10` → income/expense/balance |
| GET    | `/stats/breakdown`       | `?month=2026-10` → per-category totals & share |
| GET    | `/stats/trend`           | `?months=6&endMonth=2026-10` → monthly trend |

All responses use the envelope `{ "code": 0, "message": "success", "data": … }`.

## 🧪 Testing

```bash
cd backend
./mvnw test
```

Runs two test classes: a Spring context smoke test and `ApiFlowTest`, an end-to-end MockMvc flow covering **register → login → categories → create expense/income → monthly summary → trend → re-login**, plus an unauthorized-access check.

## ⚙️ Environment Variables

| Variable           | Default                                          | Used by        |
|--------------------|--------------------------------------------------|----------------|
| `JWT_SECRET`       | dev placeholder (≥32 bytes recommended)          | backend        |
| `JWT_EXPIRATION_MS`| `604800000` (7 days)                             | backend        |
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `localhost` / `3306` / `ledger` / `root` / `root` | backend (mysql profile) |

## 🗺 Roadmap Ideas

- [ ] Monthly budget vs. actual comparison
- [ ] Recurring transactions & reminders
- [ ] CSV import/export
- [ ] Dark theme & mobile-friendly layout
- [ ] CI pipeline (GitHub Actions: build + test + publish Docker image)

## 📄 License

[MIT](LICENSE)
