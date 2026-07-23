# GemBlog-Pro

A production-ready Spring Boot backend for a blog platform with AI-assisted content
generation, migrated from the original [GemBlog](.) MERN (Express + MongoDB) stack.

Built with **Java 17, Spring Boot 3, Spring Security (JWT), Spring Data JPA / Hibernate,
and MySQL 8+**. The REST API contract is fully compatible with the original React
frontend — no frontend changes are required to point it at this backend.

---

## Table of contents

- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
- [Environment variables](#environment-variables)
- [Running the tests](#running-the-tests)
- [API reference](#api-reference)
- [Deployment](#deployment)
- [Notes on the MERN → Spring Boot migration](#notes-on-the-mern--spring-boot-migration)

---

## Tech stack

| Layer | Technology |
|---|---|
| Language / runtime | Java 17 |
| Framework | Spring Boot 3.3 |
| Security | Spring Security 6, JWT (JJWT 0.12) |
| Persistence | Spring Data JPA, Hibernate, MySQL 8+ |
| Connection pool | HikariCP (Spring Boot's default) |
| Validation | Jakarta Bean Validation |
| Image hosting | ImageKit (via REST, no SDK dependency) |
| AI content generation | Google Gemini (`gemini-2.5-flash`, via REST, no SDK dependency) |
| Observability | SLF4J logging, Spring Boot Actuator health check |
| Testing | JUnit 5, Mockito, AssertJ, H2 (in-memory, test-only) |
| Build | Maven |
| Containerization | Docker (multi-stage build) |

## Architecture

Layered architecture throughout:

```
Controller  →  Service  →  Repository  →  Entity
    ↓             ↓
   DTO       (business logic, transactions, external calls)
```

- **Controllers** (`controller/`) are thin — they validate input, delegate to a service,
  and shape the HTTP response. No business logic lives here.
- **Services** (`service/`) own all business rules: authorization checks, external API
  calls (ImageKit, Gemini), entity↔DTO mapping, and transaction boundaries.
- **Repositories** (`repository/`) are Spring Data JPA interfaces — no custom SQL, only
  derived queries and `@EntityGraph` for eager-loading associations without N+1 queries.
- **Entities** (`entity/`) are the JPA-mapped relational model: `User (1) —< Blog (1) —< Comment`.
- **DTOs** (`dto/`) are the only objects ever serialized to/from JSON — entities never
  cross the controller boundary directly.
- **Security** (`security/`) implements stateless JWT authentication: a request filter
  reads the token, a token provider issues/validates it, and a `UserDetailsService`
  bridges to Spring Security's authentication machinery.
- **`GlobalExceptionHandler`** (`exception/`) centralizes every error → HTTP status code
  mapping in one place, so every endpoint returns errors in a single consistent JSON
  shape: `{ "success": false, "message": "..." }`.

## Project structure

```
src/main/java/com/gemblogpro/
├── GemBlogProApplication.java
├── config/            # DataSource, JPA auditing, CORS/security, ImageKit/Gemini/REST client config
├── controller/         # AuthController, AdminController, BlogController
├── dto/
│   ├── request/         # Validated inbound payloads
│   └── response/        # Outbound response shapes
├── entity/              # User, Blog, Comment (JPA)
├── exception/           # Custom exceptions + GlobalExceptionHandler
├── repository/          # Spring Data JPA repositories
├── security/            # JWT filter/provider, UserDetailsService, entry points
├── service/              # Business logic (Auth, Blog, Comment, Dashboard, ImageKit, Gemini)
└── util/                 # ImageUrlBuilder
```

## Getting started

### Prerequisites

- Java 17+
- Maven 3.9+ (or use the included `mvn` wrapper if you add one)
- MySQL 8+ running locally, **or** Docker (see [Deployment](#deployment))
- An [ImageKit](https://imagekit.io) account (public/private key + URL endpoint)
- A [Gemini API key](https://ai.google.dev)

### Run locally

```bash
# 1. Create the database (Hibernate will create/update tables automatically
#    at startup - ddl-auto=update, no manual schema step needed)
mysql -u root -p -e "CREATE DATABASE gemblog_pro;"

# 2. Configure environment variables (see below)
cp .env.example .env
# edit .env with real values

# 3. Run
export $(grep -v '^#' .env | xargs)   # or use your preferred env-loading method
mvn spring-boot:run
```

The API is available at `http://localhost:8080`. Health check: `GET /actuator/health`.

### Run with Docker Compose (app + MySQL, no local MySQL install needed)

```bash
cp .env.example .env
# edit .env with real values (DB_PASSWORD, JWT_SECRET, IMAGEKIT_*, GEMINI_*)
docker compose up --build
```

## Environment variables

All configuration is via environment variables — nothing sensitive is hardcoded or
committed. See [`.env.example`](.env.example) for the full list with descriptions and
defaults. The most important ones:

| Variable | Required | Notes |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Yes (prod) | Defaults to `localhost:3306/gemblog_pro` / `root` for local dev |
| `JWT_SECRET` | **Yes, no default** | Must be ≥ 32 bytes (256 bits) once UTF-8 encoded. Generate with `openssl rand -base64 48`. The app fails fast at startup if unset. |
| `IMAGEKIT_PUBLIC_KEY` / `IMAGEKIT_PRIVATE_KEY` / `IMAGEKIT_URL_ENDPOINT` | Yes | From your ImageKit dashboard |
| `GEMINI_API_KEY` | Yes | From Google AI Studio |
| `PORT` | No | Defaults to `8080` |

## Running the tests

```bash
mvn clean test
```

The test suite includes:

- **`JwtTokenProviderTest`** — pure unit test of token generation/parsing (no Spring context).
- **`AuthServiceTest`**, **`BlogServiceTest`** — Mockito-based unit tests of business rules
  (duplicate email rejection, author-ownership checks, not-found handling).
- **`GlobalExceptionHandlerTest`** — verifies every exception maps to the correct HTTP status code.
- **`GemBlogProApplicationTests`** — full Spring context load against an in-memory H2
  database, verifying the entire bean graph wires together correctly.

## API reference

Base path: `/api`. All responses share the envelope `{ "success": boolean, "message"?: string, ... }`.
🔒 = requires `Authorization: <jwt>` header (**raw token, no `Bearer ` prefix**).

### Admin — Auth (`/api/admin`)

| Method | Path | Description |
|---|---|---|
| POST | `/register` | Create an admin account → `201` |
| POST | `/login` | Log in, returns `{ token, user }` |
| POST | `/auth` | Alias of `/login` |

### Admin — Management (`/api/admin`) 🔒

| Method | Path | Description |
|---|---|---|
| GET | `/comments` | All comments (any approval state) |
| GET | `/blogs` | All blogs (published + draft) |
| POST | `/delete-comment` | Body: `{ id }` |
| POST | `/approve-comment` | Body: `{ id }` |
| GET | `/dashboard` | Blog/comment/draft counts + 5 most recent blogs |

### Blog (`/api/blog`)

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/add` | 🔒 | `multipart/form-data`: `blog` (JSON) + `image` (file) → `201` |
| GET | `/all` | — | Published blogs only |
| POST | `/delete` | 🔒 | Body: `{ id }` — author-only |
| POST | `/toggle-publish` | 🔒 | Body: `{ id }` — author-only |
| POST | `/add-comment` | — | Body: `{ blogId, name, content }` → `201` |
| POST | `/comments` | — | Body: `{ blogId }` — approved comments only |
| POST | `/generate` | 🔒 | Body: `{ prompt }` — Gemini-generated blog content |
| GET | `/{blogId}` | — | Single blog by ID |

Standard HTTP status codes are used throughout: `200`/`201` success, `400` validation,
`401` unauthenticated, `403` forbidden (not resource owner), `404` not found, `500`
unexpected/external-service failure.

## Deployment

### Docker

```bash
docker build -t gemblog-pro .
docker run -p 8080:8080 --env-file .env gemblog-pro
```

The image is a multi-stage build (Maven build stage → minimal JRE runtime stage),
runs as a non-root user, and includes a `HEALTHCHECK` against `/actuator/health`.

### Render

A [`render.yaml`](render.yaml) Blueprint is included for one-click deployment on
[Render](https://render.com). **Note:** Render's managed database offering is
PostgreSQL, not MySQL — provision MySQL externally (PlanetScale, Railway, AWS RDS, or
your own instance) and set `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` as Render environment
variables. Every other setting (health check path, port, secrets) is pre-wired in the
Blueprint.

### Any other Docker-compatible host

The image is a standard, self-contained Spring Boot JAR in a container — it runs
anywhere that accepts a `Dockerfile` (Fly.io, Railway, AWS ECS/App Runner, Azure
Container Apps, a bare VM with Docker installed, etc.). Point it at a reachable MySQL
8+ instance and set the environment variables from `.env.example`.

## Notes on the MERN → Spring Boot migration

This backend is a faithful, phase-by-phase migration of the original Express/MongoDB
app, preserving the REST API contract the React frontend already expects. A few
deliberate, called-out differences from the original:

- **HTTP status codes**: the original always returned `200` with a `{success:false}`
  body on failure. This backend uses standard status codes (`400`/`401`/`403`/`404`/`500`)
  instead, per an approved architecture change — the JSON body shape is unchanged.
- **MySQL instead of MongoDB**: relational foreign keys now enforce referential
  integrity that MongoDB never did (e.g. a comment can no longer reference a
  nonexistent blog). Comment moderation actions (`approve`/`delete`) on an unknown ID
  now correctly 404 instead of silently no-opping.
- **ImageKit and Gemini** are integrated via direct REST calls rather than their
  official Node/Java SDKs, avoiding an unverifiable SDK dependency while preserving
  identical external behavior (same upload folder, same image transformation, same
  Gemini model and prompt).
- Two files from the original codebase were **intentionally not migrated**:
  `userController.js` and `AuthContext.jsx` — both were dead code, never wired into
  any route or component in the original app.
