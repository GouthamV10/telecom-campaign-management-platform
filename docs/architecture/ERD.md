# Entity Relationship Diagram (ERD)

# Telecom Campaign Management Backend

## Version

**Version:** 1.0
**Status:** Current backend implementation

---

## 1. Purpose

This ERD reflects the actual database model implemented by the current backend. It documents the entity relationships that exist in the codebase today.

---

## 2. Current Entities

### User

Attributes:

- id
- username
- email
- password
- role
- enabled
- createdAt
- updatedAt

### Campaign

Attributes:

- id
- name
- description
- status
- startDate
- endDate
- createdAt
- updatedAt
- manager_id

### Enums

- Role: ADMIN, MANAGER, USER
- CampaignStatus: DRAFT, ACTIVE, PAUSED, COMPLETED, CANCELLED

---

## 3. Relationship Diagram

```text
+---------------------+
| User                |
+---------------------+
| id                  |
| username            |
| email               |
| password            |
| role                |
| enabled             |
| createdAt           |
| updatedAt           |
+---------------------+
          |
          | 1
          |
          | has many
          v
+---------------------+
| Campaign            |
+---------------------+
| id                  |
| name                |
| description         |
| status              |
| startDate           |
| endDate             |
| createdAt           |
| updatedAt           |
| manager_id          |
+---------------------+
```

---

## 4. Relationship Definition

### User -> Campaign

Relationship: One-to-Many

Reason:

A user can act as a manager for multiple campaigns, and each campaign belongs to exactly one manager.

Foreign key:

- `campaign.manager_id` references `users.id`

---

## 5. Physical Table Structure

### users

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### campaign

```sql
CREATE TABLE campaign (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_date TIMESTAMP NULL,
    end_date TIMESTAMP NULL,
    created_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL,
    manager_id BIGINT NOT NULL,
    CONSTRAINT fk_campaign_manager
        FOREIGN KEY (manager_id) REFERENCES users(id)
);
```

---

## 6. Summary

The current backend has a simple but valid relational model:

- one user can manage many campaigns
- each campaign belongs to a single manager user
- role and campaign lifecycle are stored as enums in the application model

This ERD matches the implemented source code and current database schema in the repository.
