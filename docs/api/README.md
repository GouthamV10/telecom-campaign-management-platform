# Backend API Documentation

This folder contains the backend API contract for the current implementation in this repository.

## Files

- API_SPEC.md — backend API contract and endpoint documentation
- Telecom-Campaign-Backend.postman_collection.json — Postman collection for the implemented backend module

## Base URL

- http://localhost:8081

## Main endpoints

- POST /api/auth/login
- POST /api/users/register
- POST /api/users
- GET /api/users/admin-test
- POST /api/campaigns
- GET /api/campaigns
- GET /api/campaigns/{id}
- PUT /api/campaigns/{id}
- DELETE /api/campaigns/{id}
- PATCH /api/campaigns/{id}/status

## Notes

This documentation matches the current backend code and not the older broader enterprise design docs.
