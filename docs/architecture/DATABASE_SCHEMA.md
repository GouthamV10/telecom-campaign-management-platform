# Database Schema

# Enterprise Telecom Campaign Management Platform

**Version:** 1.0

---

# 1. Purpose

This document defines the physical database schema for the Enterprise Telecom Campaign Management Platform.

It includes the table definitions, primary keys, foreign keys, constraints, indexing strategy, and auditing columns that will be implemented in MySQL using Spring Data JPA.

---

# 2. Database Information

| Property        | Value               |
| --------------- | ------------------- |
| Database        | telecom_campaign_db |
| Database Engine | MySQL 8             |
| Character Set   | UTF-8               |
| Storage Engine  | InnoDB              |

---

# 3. Common Audit Columns

Every business table will contain the following columns.

| Column     | Type      | Description                  |
| ---------- | --------- | ---------------------------- |
| created_at | TIMESTAMP | Record creation time         |
| updated_at | TIMESTAMP | Last modification time       |
| created_by | BIGINT    | User who created the record  |
| updated_by | BIGINT    | User who modified the record |

---

# 4. Tables

---

## USERS

| Column        | Type         | Constraint               |
| ------------- | ------------ | ------------------------ |
| id            | BIGINT       | PK                       |
| username      | VARCHAR(50)  | UNIQUE, NOT NULL         |
| email         | VARCHAR(100) | UNIQUE                   |
| password      | VARCHAR(255) | NOT NULL                 |
| first_name    | VARCHAR(50)  | NOT NULL                 |
| last_name     | VARCHAR(50)  |                          |
| mobile_number | VARCHAR(15)  |                          |
| status        | ENUM         | ACTIVE, INACTIVE, LOCKED |
| created_at    | TIMESTAMP    |                          |
| updated_at    | TIMESTAMP    |                          |

Indexes

* username
* email

---

## ROLES

| Column      | Type         |
| ----------- | ------------ |
| id          | BIGINT       |
| role_name   | VARCHAR(50)  |
| description | VARCHAR(255) |

Examples

* ADMIN
* MARKETING_MANAGER
* OPERATOR
* VIEWER

---

## PERMISSIONS

| Column          | Type         |
| --------------- | ------------ |
| id              | BIGINT       |
| permission_name | VARCHAR(100) |
| description     | VARCHAR(255) |

Examples

* CREATE_CAMPAIGN
* APPROVE_CAMPAIGN
* VIEW_REPORTS

---

## USER_ROLES

| Column  | Type      |
| ------- | --------- |
| user_id | BIGINT FK |
| role_id | BIGINT FK |

Many-to-Many Mapping

---

## ROLE_PERMISSIONS

| Column        | Type      |
| ------------- | --------- |
| role_id       | BIGINT FK |
| permission_id | BIGINT FK |

---

## REFRESH_TOKENS

| Column      | Type         |
| ----------- | ------------ |
| id          | BIGINT       |
| token       | VARCHAR(512) |
| user_id     | BIGINT FK    |
| expiry_date | DATETIME     |
| revoked     | BOOLEAN      |

---

## LOGIN_HISTORY

| Column      | Type         |
| ----------- | ------------ |
| id          | BIGINT       |
| user_id     | BIGINT FK    |
| login_time  | TIMESTAMP    |
| logout_time | TIMESTAMP    |
| ip_address  | VARCHAR(45)  |
| device_info | VARCHAR(255) |

---

## CUSTOMERS

| Column             | Type               |
| ------------------ | ------------------ |
| id                 | BIGINT             |
| customer_number    | VARCHAR(30) UNIQUE |
| first_name         | VARCHAR(50)        |
| last_name          | VARCHAR(50)        |
| mobile_number      | VARCHAR(15)        |
| email              | VARCHAR(100)       |
| state              | VARCHAR(50)        |
| telecom_circle     | VARCHAR(50)        |
| customer_type      | ENUM               |
| recharge_plan      | DECIMAL(10,2)      |
| monthly_revenue    | DECIMAL(10,2)      |
| preferred_language | VARCHAR(30)        |
| kyc_status         | ENUM               |
| account_status     | ENUM               |
| created_at         | TIMESTAMP          |
| updated_at         | TIMESTAMP          |

Indexes

* customer_number
* mobile_number
* state
* telecom_circle
* customer_type

---

## SEGMENTS

| Column       | Type         |
| ------------ | ------------ |
| id           | BIGINT       |
| segment_name | VARCHAR(100) |
| description  | TEXT         |
| active       | BOOLEAN      |

---

## SEGMENT_RULES

| Column           | Type         |
| ---------------- | ------------ |
| id               | BIGINT       |
| segment_id       | BIGINT FK    |
| field_name       | VARCHAR(50)  |
| operator         | VARCHAR(20)  |
| comparison_value | VARCHAR(255) |

Example

State = Karnataka

Revenue > 1000

Plan >= 299

---

## CAMPAIGNS

| Column         | Type         |
| -------------- | ------------ |
| id             | BIGINT       |
| campaign_name  | VARCHAR(150) |
| description    | TEXT         |
| campaign_type  | ENUM         |
| priority       | ENUM         |
| status         | ENUM         |
| segment_id     | BIGINT FK    |
| scheduled_time | DATETIME     |
| start_time     | DATETIME     |
| end_time       | DATETIME     |
| created_by     | BIGINT FK    |

Indexes

* status
* scheduled_time
* campaign_name

---

## CAMPAIGN_CUSTOMERS

| Column          | Type         |
| --------------- | ------------ |
| id              | BIGINT       |
| campaign_id     | BIGINT FK    |
| customer_id     | BIGINT FK    |
| delivery_status | ENUM         |
| delivery_time   | TIMESTAMP    |
| remarks         | VARCHAR(255) |

---

## AUDIT_LOGS

| Column      | Type         |
| ----------- | ------------ |
| id          | BIGINT       |
| user_id     | BIGINT FK    |
| module_name | VARCHAR(100) |
| action      | VARCHAR(100) |
| entity_name | VARCHAR(100) |
| entity_id   | BIGINT       |
| description | TEXT         |
| created_at  | TIMESTAMP    |

---

## NOTIFICATIONS

| Column            | Type         |
| ----------------- | ------------ |
| id                | BIGINT       |
| campaign_id       | BIGINT FK    |
| customer_id       | BIGINT FK    |
| notification_type | ENUM         |
| delivery_status   | ENUM         |
| delivered_at      | TIMESTAMP    |
| error_message     | VARCHAR(255) |

---

# 5. Relationship Summary

| Parent   | Child            | Relationship |
| -------- | ---------------- | ------------ |
| User     | RefreshToken     | One-to-Many  |
| User     | LoginHistory     | One-to-Many  |
| User     | AuditLog         | One-to-Many  |
| User     | Role             | Many-to-Many |
| Role     | Permission       | Many-to-Many |
| Segment  | SegmentRule      | One-to-Many  |
| Segment  | Campaign         | One-to-Many  |
| Campaign | CampaignCustomer | One-to-Many  |
| Customer | CampaignCustomer | One-to-Many  |
| Campaign | Notification     | One-to-Many  |

---

# 6. Naming Conventions

* Table names use snake_case and plural form.
* Column names use snake_case.
* Primary key column is `id`.
* Foreign keys end with `_id`.
* Timestamp columns end with `_at`.

---

# 7. Constraints

* Primary Keys on every table.
* Foreign Keys for referential integrity.
* Unique constraints on username, email, and customer_number.
* NOT NULL on mandatory business fields.
* ENUMs for status-based columns.

---

# 8. Indexing Strategy

Frequently queried columns will be indexed.

Examples:

Users

* username
* email

Customers

* mobile_number
* state
* telecom_circle
* customer_type

Campaigns

* status
* scheduled_time
* campaign_name

Audit Logs

* user_id
* created_at

---

# 9. Soft Delete Strategy

Customer and Campaign records will use a logical deletion mechanism instead of physical deletion.

Version 1 implementation:

* active (BOOLEAN)

Future versions may use:

* deleted_at (TIMESTAMP)

---

# 10. Future Enhancements

The schema supports future additions without major redesign.

Planned enhancements include:

* Campaign Templates
* SMS Gateway Integration
* Email Gateway
* Customer Preferences
* File Attachments
* Kafka Event Tracking
* Campaign Analytics
* Multi-Tenant Support

---

# Conclusion

The Version 1 database schema provides a normalized, scalable, and enterprise-ready relational model for the Telecom Campaign Management Platform. The schema is designed to support secure authentication, customer management, campaign execution, reporting, auditing, and future enhancements while following relational database best practices and Spring Data JPA conventions.
