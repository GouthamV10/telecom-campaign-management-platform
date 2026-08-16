# Requirements Specification

# Telecom Campaign Management Backend

## Version

**Version:** 1.0
**Status:** Current backend implementation

---

## 1. Project Goal

The current backend provides a focused campaign management system for telecom operations. It allows users to register, authenticate, manage secured access, and create or maintain campaigns.

---

## 2. Functional Requirements

## 2.1 Authentication

The system shall allow:

- user login with email and password
- JWT token generation on successful authentication
- request authentication using Bearer token
- public access for `/api/auth/login`
- public access for `/api/users/register`

---

## 2.2 User Management

The system shall allow:

- user registration with username, email, and password
- duplicate email validation
- default role assignment as `USER`
- admin creation of other users with role `MANAGER`
- admin-only access check endpoint

### Business rules

- email must be unique
- password is encrypted using BCrypt
- admin users cannot be created through the create-user endpoint
- only `ADMIN` can create additional users

---

## 2.3 Campaign Management

The system shall allow:

- creation of a campaign by an authenticated admin or manager
- campaign retrieval by ID
- campaign listing with filters
- filtering by status, keyword, start date, and end date
- update of campaign metadata
- deletion of a campaign
- update of campaign lifecycle status

### Campaign data fields

- `name`
- `description`
- `status`
- `startDate`
- `endDate`
- `manager` (the authenticated user who owns the campaign)

---

## 2.4 Campaign Status Rules

The system shall enforce lifecycle transitions:

- DRAFT can move to ACTIVE or CANCELLED
- ACTIVE can move to PAUSED, COMPLETED, or CANCELLED
- PAUSED can move to ACTIVE or CANCELLED
- COMPLETED and CANCELLED are terminal states

---

## 2.5 Authorization Requirements

The backend shall enforce:

- `ADMIN` access for admin-only user endpoints
- `ADMIN` and `MANAGER` access for create/update/delete/status campaign operations
- authentication requirement for all non-public endpoints

---

## 3. Non-Functional Requirements

## 3.1 Security

- JWT-based authentication
- BCrypt password hashing
- validation for request body fields
- role-based access restrictions

## 3.2 Maintainability

- layered architecture
- DTOs for inbound and outbound payloads
- mapper usage for entity-to-response conversion
- centralized exception handling

## 3.3 Data Integrity

- unique email enforcement
- foreign key relationship from campaign to user manager
- validation of campaign state transitions

---

## 4. Current Out-of-Scope Requirements

The current backend does not implement the following modules:

- customer management
- segments and segment rules
- dashboard analytics
- reports
- notifications
- audit log module
- refresh token storage workflow
- role-permission matrix tables

These features remain future enhancements and are not included in the implemented backend contract.
