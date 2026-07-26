# Entity Relationship Diagram (ERD)

# Enterprise Telecom Campaign Management Platform

**Version:** 1.0

---

# 1. Purpose

This document defines the logical database design for the Enterprise Telecom Campaign Management Platform.

It identifies the core entities, their relationships, and the business rules governing those relationships. The ERD serves as the foundation for designing the relational database schema and implementing JPA entity mappings.

---

# 2. Core Entities

The application consists of the following entities.

## Identity & Security

* User
* Role
* Permission
* RefreshToken
* LoginHistory

---

## Customer Management

* Customer

---

## Campaign Management

* Campaign
* Segment
* SegmentRule
* CampaignCustomer

---

## Audit & Notifications

* AuditLog
* Notification

---

# 3. Entity Relationships

```text
+-----------+          +-----------+
|   User    |          |   Role    |
+-----------+          +-----------+
| id        |<-------> | id        |
| username  |    M:N   | name      |
| email     |          | desc      |
| password  |          +-----------+
| status    |                 |
+-----------+                 |
      |                       | M:N
      |1                      |
      |                       ▼
      |                +---------------+
      |                | Permission    |
      |                +---------------+
      |                | id            |
      |                | name          |
      |                | description   |
      |                +---------------+
      |
      |1
      ▼
+----------------+
| RefreshToken   |
+----------------+
| id             |
| token          |
| expiryDate     |
+----------------+

      |
      |1
      ▼
+----------------+
| LoginHistory   |
+----------------+
| id             |
| loginTime      |
| ipAddress      |
+----------------+

==========================================================

+----------------+
|   Customer     |
+----------------+
| id             |
| name           |
| mobile         |
| state          |
| circle         |
| plan           |
| revenue        |
| customerType   |
| kycStatus      |
+----------------+

        ^
        |
        | M:N
        |
+----------------------+
| CampaignCustomer     |
+----------------------+
| id                   |
| campaignId           |
| customerId           |
| deliveryStatus       |
| deliveredAt          |
+----------------------+
        |
        | M:1
        ▼
+----------------+
|   Campaign     |
+----------------+
| id             |
| name           |
| description    |
| type           |
| priority       |
| status         |
| scheduleTime   |
+----------------+
        |
        | M:1
        ▼
+----------------+
|    Segment     |
+----------------+
| id             |
| name           |
| description    |
+----------------+
        |
        |1
        ▼
+----------------------+
|   SegmentRule        |
+----------------------+
| id                   |
| fieldName            |
| operator             |
| value                |
+----------------------+

==========================================================

+----------------+
| AuditLog       |
+----------------+
| id             |
| action         |
| module         |
| userId         |
| createdAt      |
+----------------+

+----------------+
| Notification   |
+----------------+
| id             |
| type           |
| recipient       |
| status         |
+----------------+
```

---

# 4. Relationship Summary

## User ↔ Role

Relationship:

Many-to-Many

Reason:

A user can have multiple roles, and each role can be assigned to multiple users.

Intermediate Table:

* user_roles

---

## Role ↔ Permission

Relationship:

Many-to-Many

Reason:

A role contains multiple permissions, and permissions may be reused by multiple roles.

Intermediate Table:

* role_permissions

---

## User ↔ RefreshToken

Relationship:

One-to-Many

Reason:

A user may log in from multiple devices, each with its own refresh token.

---

## User ↔ LoginHistory

Relationship:

One-to-Many

Reason:

Every successful login is recorded.

---

## Campaign ↔ Segment

Relationship:

Many-to-One

Reason:

Many campaigns can target the same customer segment.

---

## Segment ↔ SegmentRule

Relationship:

One-to-Many

Reason:

Each segment consists of one or more filtering rules.

Example:

Segment:

"Premium Karnataka Users"

Rules:

* State = Karnataka
* Plan >= 299
* Revenue > 1000

---

## Campaign ↔ Customer

Relationship:

Many-to-Many

Implemented using:

CampaignCustomer

Reason:

A campaign targets many customers.

A customer may receive multiple campaigns.

CampaignCustomer stores execution-specific information such as delivery status and timestamps.

---

## User ↔ AuditLog

Relationship:

One-to-Many

Reason:

Every important user action is recorded for auditing purposes.

---

# 5. Database Normalization

The database is designed to satisfy Third Normal Form (3NF).

Objectives:

* Eliminate redundant data
* Maintain referential integrity
* Avoid update anomalies
* Improve maintainability

---

# 6. Primary Keys

Every table uses a surrogate primary key.

Example:

* user_id
* customer_id
* campaign_id

All primary keys are generated using database identity columns.

---

# 7. Foreign Keys

Examples:

CampaignCustomer.customer_id → Customer.id

CampaignCustomer.campaign_id → Campaign.id

SegmentRule.segment_id → Segment.id

RefreshToken.user_id → User.id

AuditLog.user_id → User.id

---

# 8. Indexing Strategy

Indexes will be created on frequently queried columns.

Examples:

Customer

* mobile
* state
* customerType
* revenue

Campaign

* status
* scheduleTime
* createdAt

AuditLog

* userId
* createdAt

---

# 9. Soft Delete Strategy

Business entities such as Customer and Campaign will support soft deletion using an active flag or deleted timestamp.

This preserves historical data while preventing accidental data loss.

---

# 10. Future Enhancements

The ERD has been designed to support future additions with minimal schema changes.

Potential enhancements include:

* SMS gateway integration
* Email notifications
* Kafka event processing
* Campaign templates
* File attachments
* Multi-tenant support
* Customer preferences
* Campaign analytics

---

# Conclusion

The Version 1 Entity Relationship Diagram defines the core business entities and their relationships for the Enterprise Telecom Campaign Management Platform. The schema follows relational database design best practices, supports enterprise scalability, and provides a strong foundation for implementing the application using Spring Data JPA and MySQL.
