# Low Level Design (LLD)

# Telecom Campaign Management Backend

## Version

**Version:** 1.0
**Status:** Current implementation

---

## 1. Purpose

This document describes the current low-level design of the repository’s backend. It maps the actual Java classes, package layout, controllers, services, repositories, DTOs, entities, and security flow implemented in the project.

---

## 2. Actual Project Structure

```text
backend/src/main/java/com/telecom/campaign
├── CampaignApplication.java
├── campaign
│   ├── controller
│   │   └── CampaignController.java
│   ├── dto
│   │   ├── CampaignRequest.java
│   │   ├── CampaignResponse.java
│   │   └── CampaignStatusRequest.java
│   ├── entity
│   │   └── Campaign.java
│   ├── mapper
│   │   └── CampaignMapper.java
│   ├── repository
│   │   └── CampaignRepository.java
│   ├── service
│   │   ├── CampaignService.java
│   │   └── CampaignServiceImpl.java
│   └── specifications
│       └── CampaignSpecification.java
├── common
│   ├── dto
│   │   └── ApiResponse.java
│   └── enums
│       ├── CampaignStatus.java
│       └── Role.java
├── config
│   ├── AdminBootstrapConfig.java
│   ├── OpenApiConfig.java
│   └── SecurityConfig.java
├── exception
│   ├── EmailAlreadyExistsException.java
│   ├── GlobalExceptionHandler.java
│   ├── InvalidCampaignStatusException.java
│   ├── InvalidUserRoleException.java
│   └── ResourceNotFoundException.java
├── security
│   └── JwtAuthenticationFilter.java
└── user
    ├── controller
    │   ├── AuthController.java
    │   └── UserController.java
    ├── dto
    │   ├── CreateUserRequest.java
    │   ├── LoginRequest.java
    │   ├── LoginResponse.java
    │   ├── RegisterRequest.java
    │   ├── UserResponse.java
    │   └── UserResponse.java
    ├── entity
    │   └── User.java
    ├── mapper
    │   └── UserMapper.java
    ├── repository
    │   └── UserRepository.java
    ├── service
    │   ├── AuthService.java
    │   ├── AuthServiceImpl.java
    │   ├── JwtService.java
    │   ├── UserService.java
    │   └── UserServiceImpl.java
    └── ...
```

---

## 3. Package Responsibilities

### user

Responsible for:

- registration
- login
- JWT generation
- role assignment
- admin user creation

### campaign

Responsible for:

- campaign creation
- search and filtering
- status transitions
- access validation
- persistence via JPA

### common

Shared cross-cutting concerns:

- `ApiResponse<T>`
- enum definitions
- exception handling
- reusable response structure

### config

Provides application configuration:

- Spring Security setup
- OpenAPI metadata
- initial admin bootstrap

### security

Provides JWT processing for each request:

- reads Authorization Bearer header
- validates JWT
- loads user from repository
- sets authentication context

---

## 4. Controller Design

## AuthController

Path: `/api/auth`

Endpoints:

- `POST /api/auth/login`

Responsibilities:

- accept login request body
- delegate to `AuthService`
- wrap response in `ApiResponse<LoginResponse>`

## UserController

Path: `/api/users`

Endpoints:

- `POST /api/users/register`
- `GET /api/users/admin-test`
- `POST /api/users`

Responsibilities:

- register public user
- allow admin-only access checks
- create user with role requirement

## CampaignController

Path: `/api/campaigns`

Endpoints:

- `POST /api/campaigns`
- `GET /api/campaigns/{id}`
- `GET /api/campaigns`
- `PUT /api/campaigns/{id}`
- `DELETE /api/campaigns/{id}`
- `PATCH /api/campaigns/{id}/status`

Responsibilities:

- create, fetch, search, update, delete campaigns
- validate manager access
- enforce status transition rules

---

## 5. Service Layer Design

## AuthServiceImpl

Responsibilities:

- look up user by email
- verify password with `PasswordEncoder`
- generate JWT using `JwtService`
- throw `BadCredentialsException` on failure

## UserServiceImpl

Responsibilities:

- validate duplicate email
- encode password
- set `Role.USER` on registration
- deny creating another admin user
- return DTO response via mapper

## CampaignServiceImpl

Responsibilities:

- create campaign with current authenticated manager
- fetch campaign by ID
- apply dynamic specifications for filtering
- authorize manager/admin access
- validate status transitions
- save updated campaign state

---

## 6. Entity Design

## User

Fields:

- `id`
- `username`
- `email`
- `password`
- `role`
- `enabled`
- `createdAt`
- `updatedAt`

Table:

- `users`

## Campaign

Fields:

- `id`
- `name`
- `description`
- `status`
- `startDate`
- `endDate`
- `createdAt`
- `updatedAt`
- `manager`

Table:

- `campaign`

Relationship:

- `campaign.manager_id` -> `users.id`

---

## 7. Enum Design

### Role

```java
ADMIN, MANAGER, USER
```

### CampaignStatus

```java
DRAFT, ACTIVE, PAUSED, COMPLETED, CANCELLED
```

---

## 8. Validation and Error Handling

### DTO validation

Validators used in code include:

- `@NotBlank`
- `@Email`
- `@Size`
- `@NotNull`

### Exception handling

Handled globally by `GlobalExceptionHandler` and custom exceptions:

- `EmailAlreadyExistsException`
- `InvalidUserRoleException`
- `InvalidCampaignStatusException`
- `ResourceNotFoundException`

### Security exception flow

- unauthorized requests return `401`
- forbidden requests return `403`

---

## 9. Response Contract

The backend wraps every successful or failed response using `ApiResponse<T>`:

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Campaign Fetched Successfully",
  "data": {}
}
```

This is the current response format implemented in the application.

---

## 10. Status Transition Rules

Implemented in `CampaignServiceImpl`:

- `DRAFT -> ACTIVE` or `CANCELLED`
- `ACTIVE -> PAUSED`, `COMPLETED`, or `CANCELLED`
- `PAUSED -> ACTIVE` or `CANCELLED`
- `COMPLETED` and `CANCELLED` are terminal states

---

## 11. Design Summary

The current backend is intentionally compact and matches the repository’s actual code. It is not a full telecom CRM platform yet; it is a Spring Boot backend with user authentication and campaign management capabilities that are implemented and working in the codebase today.
