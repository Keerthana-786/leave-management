# Leave Management App with Approval Chains

A reliable, enterprise-grade Leave Management System built with **Spring Boot 3 (Java 17)**, **H2 In-Memory Database**, and **SpringDoc OpenAPI (Swagger UI)**.

## Project Structure
- `/backend`: Spring Boot 3 Maven application with JPA Entities, Repositories, DTOs, and REST API controllers.
- `/docs`:
  - `RULES.md`: Frozen business logic specifications (pro-rata, working days, 30% team conflict thresholds, escalation timeouts).
  - `SEED_SCENARIOS.md`: Deterministic demo scenarios covering 2 teams, 15 personas, public holidays, and 18 lifecycle test cases.
  - `openapi.yaml`: Exported OpenAPI 3.0 API specification.

## Quickstart (Backend)
```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Docs**: [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml)
- **H2 Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:leavedb`, user: `sa`, password: *(empty)*)
