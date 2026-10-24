# Vettly

Vettly is a full-stack web app for quickly finding relevant jobs and the matches that fit what you're looking for.

## Project layout

| Path | What it is |
|---|---|
| `backend/` | Spring Boot 4.1 API (Java 21, Maven), PostgreSQL, JWT authentication |
| `frontend/` | React + TypeScript app built with Vite |
| `docker-compose.yml` | Local PostgreSQL for development |

## Prerequisites

- Java 21+ and Maven 3.9+
- Node.js 22+
- Docker (for the local database and the backend tests)

## Running locally

1. **Start PostgreSQL**

   ```sh
   docker compose up -d
   ```

   This creates a `vettly` database (user `vettly`, password `vettly`) on `localhost:5432`.

2. **Start the backend** (port 8080)

   ```sh
   cd backend
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   The `dev` profile supplies a local-only JWT signing key. Flyway creates the schema on startup.

3. **Start the frontend** (port 5173)

   ```sh
   cd frontend
   npm install
   npm run dev
   ```

   Open http://localhost:5173. The Vite dev server forwards `/api` requests to the backend.

## Authentication API

All endpoints are versioned in the URL path (`/api/v1/...`). A breaking change ships under a new prefix (`/api/v2/...`) while the old version keeps working.

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Public | Body `{ "email", "password" }` (password 8–72 chars). Returns `201` with a token, or `409` if the email is taken. |
| `POST` | `/api/v1/auth/login` | Public | Body `{ "email", "password" }`. Returns `200` with a token, or `401`. |
| `GET` | `/api/v1/users/me` | Bearer token | Returns the signed-in user. |

Successful register/login responses look like:

```json
{ "accessToken": "<jwt>", "tokenType": "Bearer", "expiresIn": 3600 }
```

Send the token on later requests as `Authorization: Bearer <jwt>`. Tokens are HS256-signed, stateless, and expire after one hour by default. Errors use the standard problem-details format (`application/problem+json`).

## Configuration

The backend reads these environment variables:

| Variable | Default | Purpose |
|---|---|---|
| `VETTLY_DB_URL` | `jdbc:postgresql://localhost:5432/vettly` | JDBC URL |
| `VETTLY_DB_USERNAME` / `VETTLY_DB_PASSWORD` | `vettly` / `vettly` | Database credentials |
| `VETTLY_JWT_SECRET` | none (required outside `dev`) | Base64 key of at least 32 bytes; generate one with `openssl rand -base64 48` |
| `VETTLY_JWT_EXPIRATION` | `1h` | Token lifetime |
| `PORT` | `8080` | HTTP port |

## Tests

```sh
cd backend && mvn verify      # needs Docker: tests run against a throwaway PostgreSQL
cd frontend && npm run lint && npm run build
```

CI runs both on every push and pull request.
