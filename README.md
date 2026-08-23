# Telecom Campaign Management Platform

A full-stack campaign management application for telecom operations teams. The current release provides secure user access, role-based campaign ownership, campaign search and filtering, and lifecycle status management.

The platform is built with Spring Boot and Java 21 on the backend, React and Vite on the frontend, MySQL for persistence, Redis for rate limiting, and Docker Compose for local deployment.

## Current Scope

Implemented features:

- Public user registration and email/password login
- JWT bearer authentication with BCrypt password hashing
- Roles: `ADMIN`, `MANAGER`, and `USER`
- Admin-only creation of manager and user accounts
- Campaign creation, listing, search, filtering, update, and deletion
- Campaign lifecycle transitions: draft, active, paused, completed, and cancelled
- Campaign ownership through the authenticated manager
- Centralized JSON responses and error handling
- OpenAPI documentation and a Postman collection
- Separate local, Docker, production, and test configuration profiles

The current version does not implement customer management, segments, analytics dashboards, reports, notifications, audit logs, or refresh-token persistence.

## Architecture

```text
React/Vite frontend or Postman client
                 |
                 | HTTP/JSON, JWT Bearer token
                 v
       Spring Boot REST API (:8081)
          |              |
          v              v
       MySQL           Redis
   campaign data   rate limiting/cache support
```

The backend follows a layered structure: controllers handle HTTP, services contain business rules, repositories use Spring Data JPA, Flyway applies database migrations, and Spring Security validates JWTs and enforces access.

## Repository Layout

```text
backend/       Spring Boot API, migrations, unit and integration tests
frontend/      React/Vite web application
database/      Database-related project assets
docker/        Docker and deployment assets
docs/          API, architecture, requirements, schema, and diagrams
postman/       Importable API collection
scripts/       Project utility scripts
```

## Prerequisites

For Docker-based development, install Docker Desktop with Docker Compose v2.

For running services directly, install Java 21, Maven 3.9+ (or use the included wrapper), Node.js 20+, npm, MySQL 8, and Redis 7+.

## Quick Start with Docker

1. Create a root `.env` file and keep it out of version control:

```dotenv
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=telecom_campaign_db
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/telecom_campaign_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Asia/Kolkata
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root
JWT_SECRET=replace-with-a-long-random-secret
CORS_ALLOWED_ORIGINS=http://localhost
BACKEND_URL=http://backend:8081
```

2. Build and start the complete stack:

```bash
docker compose up --build
```

3. Open the web application at [http://localhost](http://localhost).

Useful URLs:

- Frontend: [http://localhost](http://localhost)
- Backend API: [http://localhost:8081](http://localhost:8081)
- Swagger UI: [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
- OpenAPI JSON: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)
- Redis: `localhost:6379`

Stop the stack with `docker compose down`. Add `-v` only when you intentionally want to delete the MySQL and Redis volumes.

## Default Admin Account

The application bootstraps this account when it does not already exist:

```text
Email:    admin@campaign.com
Password: Admin@123
```

Change or remove this bootstrap behavior before production deployment. Set a strong, unique `JWT_SECRET`; never use sample values in a shared environment.

## Run Services Locally

Start MySQL and Redis first, then run the backend with the `dev` profile:

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

On Windows, use `mvnw.cmd` instead of `./mvnw`. The development backend listens on `http://localhost:8081`, connects to `telecom_campaign_db`, and uses Redis at `localhost:6379`.

Run the frontend in a second terminal:

```bash
cd frontend
npm ci
npm run dev
```

The Vite server normally runs at [http://localhost:5173](http://localhost:5173). Set `VITE_BACKEND_URL=http://localhost:8081` when the frontend should call the backend directly. When omitted, the frontend uses relative `/api` requests, suitable for the Nginx Docker deployment.

## Commands

Backend:

```bash
cd backend
./mvnw test
./mvnw package
```

Frontend:

```bash
cd frontend
npm run lint
npm run build
npm run preview
```

Backend tests use an in-memory H2 database and disable Flyway and rate limiting through the test profile. Integration tests are under `backend/src/test/java`.

## API Overview

The API base path is `/api`. Protected requests use:

```http
Authorization: Bearer <JWT_TOKEN>
```

| Method | Endpoint | Access |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Public |
| `POST` | `/api/users/register` | Public |
| `POST` | `/api/users` | `ADMIN` |
| `GET` | `/api/users/admin-test` | `ADMIN` |
| `GET` | `/api/campaigns` | Authenticated |
| `POST` | `/api/campaigns` | `ADMIN`, `MANAGER` |
| `GET` | `/api/campaigns/{id}` | Authenticated |
| `PUT` | `/api/campaigns/{id}` | `ADMIN`, `MANAGER` |
| `PATCH` | `/api/campaigns/{id}/status` | `ADMIN`, `MANAGER` |
| `DELETE` | `/api/campaigns/{id}` | `ADMIN`, `MANAGER` |

Responses use `{ success, statusCode, message, data }`. See the complete [API specification](docs/api/API_SPEC.md), [API usage guide](docs/api/README.md), or import [the Postman collection](postman/Telecom_Campaign_Backend.postman_collection.json).

## Campaign Lifecycle

```text
DRAFT   -> ACTIVE, CANCELLED
ACTIVE  -> PAUSED, COMPLETED, CANCELLED
PAUSED  -> ACTIVE, CANCELLED
COMPLETED and CANCELLED are terminal states
```

## Configuration

| Variable | Purpose | Docker example |
| --- | --- | --- |
| `MYSQL_ROOT_PASSWORD` | MySQL root password | `root` |
| `MYSQL_DATABASE` | Database created by MySQL | `telecom_campaign_db` |
| `SPRING_DATASOURCE_URL` | JDBC connection used by the API | `jdbc:mysql://mysql:3306/...` |
| `SPRING_DATASOURCE_USERNAME` | API database username | `root` |
| `SPRING_DATASOURCE_PASSWORD` | API database password | `root` |
| `JWT_SECRET` | Secret used to sign JWTs | long random value |
| `CORS_ALLOWED_ORIGINS` | Allowed browser origins | `http://localhost` |
| `BACKEND_URL` | Nginx upstream for `/api` | `http://backend:8081` |
| `VITE_BACKEND_URL` | Optional frontend API base URL | `http://localhost:8081` |

Flyway runs the checked-in migrations on startup. Hibernate validates the schema in the `dev`, `docker`, and `prod` profiles rather than creating or updating tables automatically.

## Documentation

- [Documentation index](docs/README.md)
- [API specification](docs/api/API_SPEC.md)
- [API usage guide](docs/api/README.md)
- [Requirements](docs/architecture/REQUIREMENTS.md)
- [High-level design](docs/architecture/HLD.md)
- [Low-level design](docs/architecture/LLD.md)
- [Database schema](docs/architecture/DATABASE_SCHEMA.md)
- [Entity relationship diagram](docs/architecture/ERD.md)

## Troubleshooting

- If the API cannot connect to MySQL, confirm the database health check is ready and that the Docker JDBC host is `mysql`, not `localhost`.
- If the browser receives CORS errors, include the exact frontend origin in `CORS_ALLOWED_ORIGINS`.
- If the UI returns 502 through Docker, verify `BACKEND_URL` is `http://backend:8081` and restart the frontend container after changing `.env`.
- If migrations fail after a schema change, inspect Flyway history and add a new migration instead of editing one already applied to a shared database.

## Project Status

This repository documents and supports the currently implemented authentication, user, and campaign-management workflows. Broader telecom capabilities can be added as separate modules without changing the existing API contract.
