# Copilot Instructions — Churn Alert

Purpose: Make AI agents productive in this repo by encoding project-specific architecture, workflows, and conventions.

## Big Picture
- Monorepo with three services:
  - Spring Boot API (core): exposes churn endpoints and auth. See [src/main/java/com/alura/hackatonAlura](../src/main/java/com/alura/hackatonAlura).
  - DS microservice (Flask): computes churn scores. See [ds-service/app.py](../ds-service/app.py).
  - Dashboard (Streamlit): interacts with API. See [dashboard/app.py](../dashboard/app.py).
- Orchestration: [docker-compose.yml](../docker-compose.yml) wires `api` (8080), `ds` (8000), `dashboard` (8501) with health checks and `.env` for config.

## API Architecture (Java)
- Packages: `auth`, `churn`, `security`, `config`, `user`, `web` under [src/main/java/com/alura/hackatonAlura](../src/main/java/com/alura/hackatonAlura).
- Churn flow:
  - Controller: [churn/ChurnController.java](../src/main/java/com/alura/hackatonAlura/churn/ChurnController.java)
  - Request DTO + validation (case-sensitive enums/ranges): [churn/ChurnRequest.java](../src/main/java/com/alura/hackatonAlura/churn/ChurnRequest.java)
  - Service: [churn/ChurnService.java](../src/main/java/com/alura/hackatonAlura/churn/ChurnService.java) calls DS via `RestTemplate` using `churn.ds.url` and persists a minimal `Prediction` in H2.
  - Batch CSV: controller parses headers via Apache Commons CSV; `TotalCharges` blank → 0.0 (Option A).
- Security: JWT auth filter + Spring Security. See [security/JwtAuthenticationFilter.java](../src/main/java/com/alura/hackatonAlura/security/JwtAuthenticationFilter.java) and login in `auth` package. Protected endpoints require `Authorization: Bearer <token>`.

## DS Service (Python)
- Entry: [ds-service/app.py](../ds-service/app.py). Route `POST /predict` accepts `{ "features": { ...20 canonical fields... } }`.
- Behavior: loads model from `CHURN_MODEL_DIR` if present; otherwise heuristic using `tenure`, `Contract`, `OnlineSecurity`, charges. Returns both enriched (`metadata/prediction/business_logic`) and legacy (`prevision/probabilidad/top_features`) keys for backward compatibility.

## Developer Workflows
- One-command local run (Windows): [run.ps1](../run.ps1)
  - Starts Docker Compose, waits for health, logs in (`/api/auth/login`), checks `/actuator/health`, opens dashboard.
  - Flags: `-Build` to force image rebuilds.
- API only (no Docker):
  - Build/test: `./mvnw test` (Windows: `.\\mvnw.cmd test`)
  - Run: `./mvnw spring-boot:run` (Windows: `.\\mvnw.cmd spring-boot:run`)
- Docker Compose (all services): `docker compose up --build` from repo root.

## Conventions & Patterns
- Canonical fields: Exact-case strings and numeric constraints enforced by `@Pattern`, `@Min`, `@DecimalMin` in [ChurnRequest](../src/main/java/com/alura/hackatonAlura/churn/ChurnRequest.java). Keep values aligned to README reference.
- Null handling: `TotalCharges` may be omitted/blank → normalized to `0.0` at service/controller and DS.
- DS contract: API sends nested `{ features: { ... } }`. DS returns enriched + legacy keys; API maps both and fills defaults if missing.
- Persistence: API stores a minimal snapshot (`Prediction`) in H2 for audit/stats; see `PredictionRepository` in [churn](../src/main/java/com/alura/hackatonAlura/churn).
- Stats counters: `ChurnService` maintains in-memory totals for `GET /api/churn/stats`.

## Configuration
- Spring property `churn.ds.url` points to DS `POST /predict`. In Docker, map env `CHURN_DS_URL` via Spring relaxed binding.
- Sensitive settings (JWT secret, etc.) loaded from `.env` when running Compose; see [docker-compose.yml](../docker-compose.yml).

## Testing & Diagnostics
- Java tests: `./mvnw test` (reports under [target/surefire-reports](../target/surefire-reports)).
- Health:
  - API: `/actuator/health` (requires Bearer token when security is on).
  - DS: `/health` (no auth).
  - Swagger (if enabled): `/swagger-ui/index.html`.

## Useful Examples
- Predict (JSON): `POST /api/churn/predict` with 20 canonical fields; Bearer required.
- Batch CSV: `POST /api/churn/predict/batch/csv` multipart `file=@samples/churn_batch_sample.csv`.
- Stats: `GET /api/churn/stats` (Bearer required).
- Postman collection: [postman/ChurnInsight.postman_collection.json](../postman/ChurnInsight.postman_collection.json).

## When Implementing Changes
- Reuse DTO validation patterns and keep string enums exact.
- When adding DS features, preserve legacy keys in responses for compatibility with API and dashboard.
- For new endpoints, follow controller→service separation and log key inputs/summary results as in `ChurnController`.
