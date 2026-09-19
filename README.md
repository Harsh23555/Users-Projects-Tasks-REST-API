# Task Management REST API

**Task 2 — Innovation Hacks Internship**

A production-quality RESTful API for managing Users, Projects, and Tasks with JWT authentication, role-based authorization, pagination, filtering, and full Swagger documentation.

---

## Features

- JWT authentication (register / login)
- Role-based access control (USER / ADMIN)
- Full CRUD for Users, Projects, Tasks
- Ownership-based authorization (users manage their own resources)
- Task assignment and status transitions
- Filtering by status, priority, project, user, and free-text search
- Pagination and sorting on all list endpoints
- Bean Validation with field-level error responses
- Global exception handling with consistent API response format
- Flyway database migrations (PostgreSQL)
- Swagger / OpenAPI UI with JWT Bearer auth support
- 40+ unit and integration tests (JUnit 5, Mockito, MockMvc, H2)
- Dashboard summary endpoint

---

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security + JWT (jjwt 0.12.5) |
| Persistence | Spring Data JPA + Hibernate |
| Database | PostgreSQL (production), H2 (tests) |
| Migrations | Flyway |
| Validation | Jakarta Bean Validation |
| Documentation | SpringDoc OpenAPI / Swagger UI |
| Build | Maven 3.8+ |
| Testing | JUnit 5, Mockito, MockMvc |
| Utilities | Lombok |

---

## Architecture

```
src/main/java/com/example/taskmanagement/
├── config/           # SecurityConfig, JpaConfig, OpenApiConfig
├── controller/       # REST controllers (Auth, User, Project, Task, Dashboard)
├── dto/
│   ├── request/      # RegisterRequest, LoginRequest, ProjectRequest, TaskRequest, …
│   └── response/     # ApiResponse<T>, UserResponse, ProjectResponse, TaskResponse, …
├── entity/           # User, Project, Task (JPA entities)
├── exception/        # Custom exceptions + GlobalExceptionHandler
├── mapper/           # EntityMapper (entity → DTO)
├── repository/       # Spring Data JPA repositories
├── security/         # JwtService, JwtAuthenticationFilter, UserDetailsServiceImpl
├── service/          # Service interfaces
│   └── impl/         # AuthServiceImpl, UserServiceImpl, ProjectServiceImpl, TaskServiceImpl
└── util/             # ProjectSpecification, TaskSpecification
```

---

## Database Design

```
users
  id, name, email (unique), password (bcrypt), role, created_at, updated_at

projects
  id, name, description, status, owner_id → users(id), created_at, updated_at

tasks
  id, title, description, status, priority, due_date,
  project_id → projects(id), assigned_user_id → users(id),
  created_at, updated_at
```

---

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | None | Register new user |
| POST | `/api/auth/login` | None | Login, get JWT |
| GET | `/api/users` | ADMIN | List all users (paginated) |
| GET | `/api/users/{id}` | USER | Get user by ID |
| PUT | `/api/users/{id}` | USER | Update user (self or ADMIN) |
| DELETE | `/api/users/{id}` | ADMIN | Delete user |
| GET | `/api/users/{id}/projects` | USER | Get user's projects |
| GET | `/api/users/{id}/tasks` | USER | Get user's assigned tasks |
| POST | `/api/projects` | USER | Create project |
| GET | `/api/projects` | USER | List projects (filter: status, ownerId, search) |
| GET | `/api/projects/{id}` | USER | Get project |
| PUT | `/api/projects/{id}` | Owner/ADMIN | Update project |
| DELETE | `/api/projects/{id}` | Owner/ADMIN | Delete project |
| GET | `/api/projects/{id}/tasks` | USER | Get project tasks |
| POST | `/api/projects/{id}/tasks` | Owner/ADMIN | Create task in project |
| GET | `/api/tasks` | USER | List tasks (filter: status, priority, projectId, assignedUserId, search) |
| GET | `/api/tasks/{id}` | USER | Get task |
| PUT | `/api/tasks/{id}` | Owner/ADMIN | Update task |
| DELETE | `/api/tasks/{id}` | Owner/ADMIN | Delete task |
| PATCH | `/api/tasks/{id}/status` | Owner/Assignee/ADMIN | Update task status |
| PATCH | `/api/tasks/{id}/assign/{userId}` | Owner/ADMIN | Assign task |
| GET | `/api/dashboard/summary` | USER | System statistics |

---

## Authentication Flow

```
POST /api/auth/register  →  201 + UserResponse (no password)
POST /api/auth/login     →  200 + { token, user }

All protected requests:
  Authorization: Bearer <token>
```

---

## Environment Configuration

Copy `.env.example` to `.env` and fill in your values:

```env
DB_URL=jdbc:postgresql://localhost:5432/taskdb
DB_USERNAME=postgres
DB_PASSWORD=yourpassword
JWT_SECRET=replace-this-with-a-long-random-secret
JWT_EXPIRATION_MS=86400000
```

Set these as environment variables before running, or use your IDE's run configuration.

**Never commit `.env` or real secrets to Git.**

---

## How to Run

### Prerequisites
- Java 21+
- Maven 3.8+
- PostgreSQL running (or use Docker below)

### PostgreSQL setup
```sql
CREATE DATABASE taskdb;
```

### Run
```bash
# Set environment variables (Linux/macOS)
export DB_URL=jdbc:postgresql://localhost:5432/taskdb
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
export JWT_SECRET=your-long-secret-key

# Build
mvn clean install -DskipTests

# Run
mvn spring-boot:run
```

### Docker (optional)
```bash
# Start PostgreSQL
docker run -d \
  --name taskdb \
  -e POSTGRES_DB=taskdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16

# Build and run app
mvn clean package -DskipTests
java -jar target/task-management-1.0.0.jar
```

---

## How to Test

```bash
# Run all tests (uses H2 in-memory, no PostgreSQL needed)
mvn test

# Run specific test class
mvn test -Dtest=AuthControllerIntegrationTest
```

---

## Swagger UI

After starting the app:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs

**To test protected endpoints in Swagger:**
1. Call `POST /api/auth/login`
2. Copy the `token` from the response
3. Click **Authorize** (top right)
4. Enter: `Bearer <your-token>`
5. All subsequent requests will include the JWT

---

## Example Requests

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","password":"secure123"}'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"secure123"}'
```

### Create Project
```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"My Project","description":"A great project"}'
```

### Filter Tasks
```bash
curl "http://localhost:8080/api/tasks?status=TODO&priority=HIGH&page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

---

## Example Responses

### Successful login
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": { "id": 1, "name": "John", "email": "john@example.com", "role": "USER" }
  }
}
```

### Validation error
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "email": "Invalid email format",
    "password": "Password must be at least 8 characters"
  }
}
```

### Resource not found
```json
{
  "success": false,
  "message": "Project not found with id: 42"
}
```

---

## Seed Data

Flyway `V2__seed_data.sql` creates two dev accounts:

| Email | Password | Role |
|-------|----------|------|
| admin@example.com | password | ADMIN |
| john@example.com | password | USER |

(BCrypt hash of "password" is used — change in production.)

---

## Future Improvements

- Refresh token support
- Email verification on registration
- Task comments and attachments
- Notification system
- Rate limiting
- Redis caching for dashboard
- Docker Compose full stack
- CI/CD pipeline (GitHub Actions)
- Soft delete for all entities
