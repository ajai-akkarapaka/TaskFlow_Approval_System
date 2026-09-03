TaskFlow Approval System - Backend Java Application

A comprehensive Spring Boot application for managing request workflows with multi-level approvals, supporting roles, request types, and comprehensive audit trails.

## Project Overview

This backend application implements a complete workflow management system with:
- Multi-level approval chains
- Request lifecycle management
- Comprehensive audit logging
- Swagger API documentation

## Architecture - Tech Stack
- **Framework**: Spring Boot 3.5.16
- **Build Tool**: Maven
- **Database**: MySQL 8.0+
- **ORM**: JPA/Hibernate
- **API Documentation**: Swagger/OpenAPI 3.0
- **Java Version**: 25


## Core Entities

User
- Roles: EMPLOYEE, MANAGER, FINANCE, ADMIN
- Relationships: Employees have managers
- Validation: Unique email, valid role assignment

Request
- Types: LEAVE, EXPENSE, ACCESS, ASSET
- Status: DRAFT → SUBMITTED → APPROVED/REJECTED/CANCELLED
- Type-specific fields: amount (expense/asset), dates (leave)

ApprovalStep
- Sequential workflow steps
- Assigned to users by role
- Status tracking (PENDING, APPROVED, REJECTED)
- Comments and timestamps

AuditLog
- Tracks all lifecycle actions
- Immutable records
- Timestamps and user tracking

## Workflow Rules
Leave Request
- Manager approval only
Expense Request
- Amount ≤ 50k: Manager
- Amount > 50k: Manager → Finance

## Access Request
- Manager → Admin

## Asset Request
- Amount ≤ 100k: Manager
- Amount > 100k: Manager → Finance → Admin

## Database Setup
1. Install MySQL 8.0+
2. Create a database named `taskflow_db`
3. Update `src/main/resources/application.properties` with your MySQL credentials:
   ```properties
   spring.datasource.username=your_mysql_username
   spring.datasource.password=your_mysql_password
   ```

## Installation
```bash
cd TaskFlow_Approval_System
mvn clean install
```

### Running the Application
```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080`

### Access Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

The REST APIs under `/api/**` use HTTP Basic authentication. In Swagger UI, click
**Authorize** and enter the configured credentials.

Default local test credentials:
```
Username: swagger
Password: swagger-test-password
```

Override them in shared environments:
```bash
export API_AUTH_USERNAME=your_username
export API_AUTH_PASSWORD=your_password
```

For local development, the application automatically loads variables from the
repository `.env` file when it exists. Docker does not load that file
automatically; pass it when starting the container:
```bash
docker run --env-file .env -p 8080:8080 taskflow
```

## 📡 REST APIs

### User APIs
```
POST   /api/users                    # Create user
GET    /api/users                    # Get all users
GET    /api/users/{id}               # Get user by ID
```

### Request APIs
```
POST   /api/requests                 # Create request
PUT    /api/requests/{id}            # Update draft request
POST   /api/requests/{id}/submit     # Submit request
POST   /api/requests/{id}/approve    # Approve step
POST   /api/requests/{id}/reject     # Reject step
POST   /api/requests/{id}/cancel     # Cancel request
GET    /api/requests/pending         # Get pending approvals
GET    /api/requests                 # Get requests
```

### Audit APIs
```
GET    /api/audit/request/{requestId}  # Get audit logs
```


- **API Documentation**: Available at `/swagger-ui/index.html`

## 🔄 Database Schema

### Users Table
- id (PK), name, email (unique), role, department, manager_id (FK), created_at, updated_at

### Requests Table
- id (PK), title, description, type, status, requester_id (FK), amount, start_date, end_date, created_at, updated_at

### ApprovalSteps Table
- id (PK), request_id (FK), step_order, approver_id (FK), status, comments, approved_at

### AuditLogs Table
- id (PK), request_id (FK), user_id (FK), action, timestamp, details
