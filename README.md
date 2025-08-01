# Digital Wallet API – Spring Boot

This is a Spring Boot RESTful API for managing digital wallets, deposits, withdrawals, and transaction approvals.

## Features
- Create and list wallets
- Make deposits and withdrawals
- Approve or deny transactions
- Role-based access for Customers and Employees

## Technologies
- Java 21
- Spring Boot 3
- Spring Security 
- Spring Data JPA
- H2 in-memory database

## How to Run
- Clone the repo
- Run: `./mvnw spring-boot:run`
- H2 Console: http://localhost:8080/h2-console

## Roles
- EMPLOYEE: Full access
- CUSTOMER: Can only access own wallets & transactions

## Endpoints
- `POST /api/customers/create`
- `GET /api/customers/{customerId}`
- `POST /api/auth/login`
- `POST /api/auth/employee/create`
- `POST /api/wallets/create`
- `GET /api/wallets?customerId=1`
- `POST /api/transactions/deposit`
- `POST /api/transactions/withdraw`
- `POST /api/transactions/approve`
- `GET /api/wallets/{customerId}`
- `GET /api/wallets/{customerId}/currency/{currency}`