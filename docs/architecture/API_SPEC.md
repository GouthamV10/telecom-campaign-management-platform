# Telecom Campaign Management Backend API

This document reflects the current backend implementation in this repository. It is aligned to the actual Spring Boot code, security rules, request models, and response structure currently in use.

## Base URL

- Local development: http://localhost:8081
- API root: /api

## Authentication

JWT bearer authentication is enabled for protected endpoints.

Header:

```http
Authorization: Bearer <JWT_TOKEN>
```

Public endpoints:

- /api/auth/login
- /api/users/register
- /api/users/admin-test is admin-only
- /swagger-ui/**
- /v3/api-docs/**

## Common Response Format

All endpoints use the following JSON wrapper:

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Campaign Fetched Successfully",
  "data": {}
}
```

For errors, Spring Security and controller exceptions return a JSON response with the same wrapper, with `success: false` and a non-2xx status code.

## Enums

### Role

```json
["ADMIN", "MANAGER", "USER"]
```

### CampaignStatus

```json
["DRAFT", "ACTIVE", "PAUSED", "COMPLETED", "CANCELLED"]
```

---

# Auth APIs

## 1) User Login

Method: POST

Endpoint: /api/auth/login

Request body:

```json
{
  "email": "admin@campaign.com",
  "password": "Admin@123"
}
```

Response body:

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

# User APIs

## 2) Register a user

Method: POST

Endpoint: /api/users/register

Request body:

```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "Password@123"
}
```

Response body:

```json
{
  "success": true,
  "statusCode": 201,
  "message": "Successfully Registered",
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER",
    "enabled": true,
    "createdAt": "2026-08-16T10:20:00",
    "updatedAt": "2026-08-16T10:20:00"
  }
}
```

## 3) Create a user (admin-only)

Method: POST

Endpoint: /api/users

Security: requires ADMIN role

Request body:

```json
{
  "username": "manager1",
  "email": "manager1@example.com",
  "password": "Manager@123",
  "role": "MANAGER"
}
```

Response body:

```json
{
  "success": true,
  "statusCode": 201,
  "message": "User Created Successfully",
  "data": {
    "id": 2,
    "username": "manager1",
    "email": "manager1@example.com",
    "role": "MANAGER",
    "enabled": true,
    "createdAt": "2026-08-16T10:25:00",
    "updatedAt": "2026-08-16T10:25:00"
  }
}
```

## 4) Admin access check

Method: GET

Endpoint: /api/users/admin-test

Security: requires ADMIN role

Response body:

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Admin access granted",
  "data": "You are an ADMIN"
}
```

---

# Campaign APIs

## 5) Create campaign

Method: POST

Endpoint: /api/campaigns

Security: requires ADMIN or MANAGER role

Request body:

```json
{
  "name": "Summer Offer 2026",
  "description": "Promotional campaign for prepaid renewal users",
  "startDate": "2026-08-20T09:00:00",
  "endDate": "2026-08-27T18:00:00"
}
```

Response body:

```json
{
  "success": true,
  "statusCode": 201,
  "message": "Campaign Created Successfully",
  "data": {
    "id": 1,
    "name": "Summer Offer 2026",
    "description": "Promotional campaign for prepaid renewal users",
    "status": "DRAFT",
    "startDate": "2026-08-20T09:00:00",
    "endDate": "2026-08-27T18:00:00",
    "managerId": 2,
    "managerName": "manager1",
    "createdAt": "2026-08-16T10:30:00",
    "updatedAt": "2026-08-16T10:30:00"
  }
}
```

## 6) Get campaign by ID

Method: GET

Endpoint: /api/campaigns/{id}

Response body:

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Campaign Fetched Successfully",
  "data": {
    "id": 1,
    "name": "Summer Offer 2026",
    "description": "Promotional campaign for prepaid renewal users",
    "status": "DRAFT",
    "startDate": "2026-08-20T09:00:00",
    "endDate": "2026-08-27T18:00:00",
    "managerId": 2,
    "managerName": "manager1",
    "createdAt": "2026-08-16T10:30:00",
    "updatedAt": "2026-08-16T10:30:00"
  }
}
```

## 7) Get campaigns list with filters

Method: GET

Endpoint: /api/campaigns

Optional query parameters:

- status: CampaignStatus
- keyword: String
- startDate: LocalDateTime
- endDate: LocalDateTime
- page, size, sort: Spring Data pageable parameters

Example:

```http
GET /api/campaigns?status=DRAFT&keyword=summer&startDate=2026-08-01T00:00:00&endDate=2026-08-31T23:59:59&page=0&size=10
```

Response body:

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Campaigns Fetched Successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Summer Offer 2026",
        "description": "Promotional campaign for prepaid renewal users",
        "status": "DRAFT",
        "startDate": "2026-08-20T09:00:00",
        "endDate": "2026-08-27T18:00:00",
        "managerId": 2,
        "managerName": "manager1",
        "createdAt": "2026-08-16T10:30:00",
        "updatedAt": "2026-08-16T10:30:00"
      }
    ],
    "pageable": {},
    "totalElements": 1,
    "totalPages": 1,
    "last": true,
    "size": 10,
    "number": 0
  }
}
```

## 8) Update campaign

Method: PUT

Endpoint: /api/campaigns/{id}

Request body:

```json
{
  "name": "Updated Summer Offer 2026",
  "description": "Updated promotional campaign description",
  "startDate": "2026-08-21T09:00:00",
  "endDate": "2026-08-28T18:00:00"
}
```

Response body:

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Campaign Updated Successfully",
  "data": {
    "id": 1,
    "name": "Updated Summer Offer 2026",
    "description": "Updated promotional campaign description",
    "status": "DRAFT",
    "startDate": "2026-08-21T09:00:00",
    "endDate": "2026-08-28T18:00:00",
    "managerId": 2,
    "managerName": "manager1",
    "createdAt": "2026-08-16T10:30:00",
    "updatedAt": "2026-08-16T10:35:00"
  }
}
```

## 9) Delete campaign

Method: DELETE

Endpoint: /api/campaigns/{id}

Response body:

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Campaign Deleted Successfully",
  "data": null
}
```

## 10) Update campaign status

Method: PATCH

Endpoint: /api/campaigns/{id}/status

Request body:

```json
{
  "status": "ACTIVE"
}
```

Response body:

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Campaign Status Updated",
  "data": {
    "id": 1,
    "name": "Summer Offer 2026",
    "description": "Promotional campaign for prepaid renewal users",
    "status": "ACTIVE",
    "startDate": "2026-08-20T09:00:00",
    "endDate": "2026-08-27T18:00:00",
    "managerId": 2,
    "managerName": "manager1",
    "createdAt": "2026-08-16T10:30:00",
    "updatedAt": "2026-08-16T10:40:00"
  }
}
```

## Status transition rules

The current backend validates transitions as follows:

- DRAFT -> ACTIVE or CANCELLED only
- ACTIVE -> PAUSED, COMPLETED, or CANCELLED only
- PAUSED -> ACTIVE or CANCELLED only
- COMPLETED and CANCELLED cannot transition further

---

# Security Rules

Current backend security rules are:

- /api/auth/** -> public
- /api/users/register -> public
- /api/users/admin-test -> ADMIN only
- /api/users -> ADMIN only for createUser
- /api/campaigns -> ADMIN or MANAGER for create/update/delete/status actions
- all other requests -> authenticated users only

---

# Notes

This API specification intentionally matches the code present in the backend module. It does not include endpoints that are not implemented in the current repository version, such as customer, segment, role, notification, audit, or dashboard modules.

```text
GET /customers?page=0&size=20&sort=firstName,asc
```

---

# 15. Filtering

Example:

```text
GET /customers?state=Karnataka&customerType=PREPAID
```

---

# 16. Searching

Example:

```text
GET /customers/search?keyword=goutham
```

---

# 17. API Versioning

All endpoints will use URL versioning.

Example

```text
/api/v1/customers
```

Future versions

```text
/api/v2/customers
```

---

# 18. Security

Protected APIs require:

* Valid JWT Access Token
* Active User
* Required Role/Permission

Public APIs

* Login
* Refresh Token

---

# 19. Validation Rules

Examples

User

* Username Required
* Password Minimum 8 Characters

Customer

* Mobile Number Required
* Customer Number Unique

Campaign

* Campaign Name Required
* Schedule Date Cannot Be Past

---

# 20. Future APIs

Future versions may introduce:

* SMS Gateway APIs
* Email APIs
* Campaign Templates
* Notification Preferences
* Analytics APIs
* Kafka Event APIs

---

# Conclusion

This API specification defines the initial REST contract for the Enterprise Telecom Campaign Management Platform. The APIs follow RESTful principles, use JWT-based authentication, standardized response structures, and are designed to support future enhancements while maintaining backward compatibility through API versioning.
