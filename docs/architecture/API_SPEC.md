# API Specification

# Enterprise Telecom Campaign Management Platform

**Version:** 1.0

**API Style:** RESTful APIs

**Response Format:** JSON

**Authentication:** JWT Bearer Token

---

# 1. API Standards

## Base URL

```text
/api/v1
```

Example

```text
/api/v1/auth/login
```

---

## Content Type

```http
Content-Type: application/json
```

---

## Authentication Header

```http
Authorization: Bearer <JWT_TOKEN>
```

All APIs except Login and Refresh Token require authentication.

---

# 2. Common Response Format

## Success Response

```json
{
    "timestamp": "2026-07-26T18:30:20",
    "status": 200,
    "message": "Success",
    "data": {},
    "errors": []
}
```

---

## Error Response

```json
{
    "timestamp": "2026-07-26T18:30:20",
    "status": 400,
    "message": "Validation Failed",
    "data": null,
    "errors": [
        "Campaign name is required"
    ]
}
```

---

# 3. Authentication APIs

| Method | Endpoint            | Description               |
| ------ | ------------------- | ------------------------- |
| POST   | /auth/login         | User Login                |
| POST   | /auth/refresh-token | Generate New Access Token |
| POST   | /auth/logout        | Logout User               |
| GET    | /auth/me            | Logged-in User Profile    |

---

# 4. User APIs

| Method | Endpoint           | Description              |
| ------ | ------------------ | ------------------------ |
| GET    | /users             | Get All Users            |
| GET    | /users/{id}        | Get User By ID           |
| POST   | /users             | Create User              |
| PUT    | /users/{id}        | Update User              |
| PATCH  | /users/{id}/status | Activate/Deactivate User |
| DELETE | /users/{id}        | Delete User              |

---

# 5. Role APIs

| Method | Endpoint    | Description |
| ------ | ----------- | ----------- |
| GET    | /roles      | Get Roles   |
| POST   | /roles      | Create Role |
| PUT    | /roles/{id} | Update Role |
| DELETE | /roles/{id} | Delete Role |

---

# 6. Customer APIs

| Method | Endpoint          | Description            |
| ------ | ----------------- | ---------------------- |
| GET    | /customers        | Get Customers          |
| GET    | /customers/{id}   | Get Customer By ID     |
| POST   | /customers        | Create Customer        |
| PUT    | /customers/{id}   | Update Customer        |
| DELETE | /customers/{id}   | Delete Customer        |
| POST   | /customers/import | Import Customers (CSV) |
| GET    | /customers/export | Export Customers       |
| GET    | /customers/search | Search Customers       |

---

# 7. Campaign APIs

| Method | Endpoint                 | Description          |
| ------ | ------------------------ | -------------------- |
| GET    | /campaigns               | Get Campaigns        |
| GET    | /campaigns/{id}          | Get Campaign Details |
| POST   | /campaigns               | Create Campaign      |
| PUT    | /campaigns/{id}          | Update Campaign      |
| DELETE | /campaigns/{id}          | Delete Campaign      |
| POST   | /campaigns/{id}/approve  | Approve Campaign     |
| POST   | /campaigns/{id}/reject   | Reject Campaign      |
| POST   | /campaigns/{id}/schedule | Schedule Campaign    |
| POST   | /campaigns/{id}/execute  | Execute Campaign     |
| GET    | /campaigns/{id}/history  | Campaign History     |

---

# 8. Segment APIs

| Method | Endpoint          | Description                |
| ------ | ----------------- | -------------------------- |
| GET    | /segments         | Get Segments               |
| GET    | /segments/{id}    | Get Segment                |
| POST   | /segments         | Create Segment             |
| PUT    | /segments/{id}    | Update Segment             |
| DELETE | /segments/{id}    | Delete Segment             |
| POST   | /segments/preview | Preview Matching Customers |

---

# 9. Dashboard APIs

| Method | Endpoint             | Description         |
| ------ | -------------------- | ------------------- |
| GET    | /dashboard/summary   | Dashboard Summary   |
| GET    | /dashboard/campaigns | Campaign Statistics |
| GET    | /dashboard/customers | Customer Statistics |

---

# 10. Report APIs

| Method | Endpoint           | Description               |
| ------ | ------------------ | ------------------------- |
| GET    | /reports/campaign  | Campaign Report           |
| GET    | /reports/customer  | Customer Report           |
| GET    | /reports/execution | Campaign Execution Report |

---

# 11. Audit APIs

| Method | Endpoint    | Description    |
| ------ | ----------- | -------------- |
| GET    | /audit      | Get Audit Logs |
| GET    | /audit/{id} | Audit Details  |

---

# 12. Notification APIs

| Method | Endpoint            | Description          |
| ------ | ------------------- | -------------------- |
| GET    | /notifications      | Notification History |
| GET    | /notifications/{id} | Notification Details |

---

# 13. HTTP Status Codes

| Status | Meaning               |
| ------ | --------------------- |
| 200    | Success               |
| 201    | Resource Created      |
| 204    | No Content            |
| 400    | Bad Request           |
| 401    | Unauthorized          |
| 403    | Forbidden             |
| 404    | Resource Not Found    |
| 409    | Duplicate Resource    |
| 500    | Internal Server Error |

---

# 14. Pagination

List APIs support pagination.

Example:

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
