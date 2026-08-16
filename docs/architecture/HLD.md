# High Level Design (HLD)

# Telecom Campaign Management Backend

## Version

**Version:** 1.0
**Status:** Current backend implementation

---

## 1. Purpose

This document describes the current backend architecture for the Telecom Campaign Management Platform. It reflects the implementation that is actually present in this repository and not the earlier broader enterprise design.

The backend currently focuses on:

- user registration and authentication
- JWT-based security
- admin-only user provisioning
- campaign creation, list/search, update, delete, and status change
- persistence with MySQL via Spring Data JPA

---

## 2. Current System Scope

The current implementation contains a focused backend scope with the following modules:

### Authentication

- login endpoint
- JWT token generation and validation
- protected route enforcement
- public access for registration and login

### User Management

- register a normal user
- create additional users by admin
- role values: ADMIN, MANAGER, USER
- user identity stored in `users` table

### Campaign Management

- create campaign
- list campaigns with filters
- fetch by ID
- update campaign
- delete campaign
- update campaign status
- rules for valid status transitions

---

## 3. High-Level Architecture

```text
Client / Postman / Frontend
         |
         | HTTP / JSON
         v
Spring Boot Application
         |
         +--> Security Layer
         |       - JWT filter
         |       - Role-based access
         |
         +--> Controller Layer
         |       - AuthController
         |       - UserController
         |       - CampaignController
         |
         +--> Service Layer
         |       - AuthServiceImpl
         |       - UserServiceImpl
         |       - CampaignServiceImpl
         |
         +--> Repository Layer
         |       - UserRepository
         |       - CampaignRepository
         |
         +--> Persistence Layer
                 - MySQL
                 - JPA entities
```

---

## 4. Backend Technology Stack

### Application Layer

- Java 21
- Spring Boot 3
- Spring Web
- Spring Security
- Spring Data JPA
- Validation
- JWT-based authentication

### Storage Layer

- MySQL 8
- Hibernate/JPA

### Security Layer

- BCrypt password encoding
- JWT bearer authentication
- role-based access checks via `@PreAuthorize`

---

## 5. Current Components

## Authentication Module

Responsibilities:

- validate credentials
- check user existence
- verify password
- return signed JWT token

## User Module

Responsibilities:

- user registration
- admin-created user records
- default user role assignment
- ownership and role checks

## Campaign Module

Responsibilities:

- create campaign tied to a manager
- list/search campaigns by status and keyword/date filters
- update campaign metadata
- delete campaign
- move campaign between allowed lifecycle states

---

## 6. Core Design Principles

- keep the backend small and focused on current features
- use Spring MVC controllers for HTTP handling
- keep validation close to DTOs
- centralize exceptions using `@RestControllerAdvice`
- use standardized `ApiResponse<T>` envelope
- rely on Spring Security for authentication and authorization

---

## 7. Runtime Security Model

Current access rules:

- `/api/auth/**` is public
- `/api/users/register` is public
- `/api/users/admin-test` requires `ADMIN`
- `/api/users` creation requires `ADMIN`
- `/api/campaigns` create/update/delete/status actions require `ADMIN` or `MANAGER`
- all other requests require authentication

---

## 8. Current Data Model Summary

The implemented backend has two primary persistence entities:

- `User`
- `Campaign`

The data model is intentionally minimal and aligns to the code currently checked in.

---

## 9. Notes

This HLD is intentionally limited to the backend implementation that exists today. It does not describe unimplemented modules such as customer management, segment management, reports, dashboards, notifications, or multi-role permission tables.
