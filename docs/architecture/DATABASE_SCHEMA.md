# Database Schema (Current Backend Implementation)

This schema reflects the actual persistence model used by the current backend module.

## Database

- Name: telecom_campaign_db
- Engine: MySQL 8 / InnoDB
- DDL Strategy: Spring JPA `hibernate.ddl-auto=update`

---

## 1. users

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

### Notes

- `role` stores enum value as string (`ADMIN`, `MANAGER`, `USER`)
- `enabled` indicates account status
- `created_at` and `updated_at` are managed in Java code

Indexes:

- username (unique)
- email (unique)

---

## 2. campaign

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

### Notes

- `status` stores enum value as string (`DRAFT`, `ACTIVE`, `PAUSED`, `COMPLETED`, `CANCELLED`)
- A campaign is assigned to exactly one manager user
- The table name is singular: `campaign`

Indexes:

- name (unique)
- manager_id (FK)
- status (queryable by enum)

---

## 3. Relationships

### users -> campaign

- One manager can create/manage many campaigns
- `campaign.manager_id` -> `users.id`

---

## 4. Security and auth behavior

The current implementation uses JWT-based authentication and the `User` entity stores the user role directly. There are no separate `roles`, `permissions`, `refresh_tokens`, `customers`, `segments`, or audit tables in this backend version.

---

## 5. Current data model summary

The implemented backend currently includes only the following core entities:

- `User`
- `Campaign`

and the associated enums:

- `Role`
- `CampaignStatus`

This is the schema that matches the code present in the backend module today.
