# Rating Calculator Service

## Overview

This project implements a simplified service for calculating features used in e-commerce fraud detection models. Given a transaction in JSON format, the service calculates three main features:

- **TOTAL_AMOUNT**: The transaction amount.
- **CPF_RATING**: The CPF rating, obtained via a simulated external API.
- **CC_SCORE**: The credit card score calculated from the transaction history stored in a relational database.

The result is returned in JSON format containing the calculated features.

## Example JSON Input

```json
{
  "total_amount": 87.99,
  "cpf": "94548325069",
  "cc": {
    "bin": "548110",
    "last4": "2104"
  }
}
```

## Example JSON Output

```json
{
  "TOTAL_AMOUNT": 87.99,
  "CPF_RATING": 0.0,
  "CC_SCORE": 3.0
}
```

## Features

- Consumes a transaction JSON with amount, CPF, and credit card data.
- Calls a mocked API via WireMock to fetch the CPF rating.
- Calculates the credit card score using a single SQL query against an in-memory H2 database.
- Initializes the database via Liquibase, including table creation and sample data insertion.
- Supports local execution with mocks and in-memory DB for quick testing and development.
- Comprehensive unit tests using JUnit 5, Mockito, and WireMock.

## Technologies Used

| Technology   | Description                                          |
|--------------|------------------------------------------------------|
| Java 21      | Primary programming language                         |
| Jackson      | JSON serialization and deserialization              |
| HttpClient   | HTTP client for REST calls                          |
| H2 Database  | In-memory relational database for testing and development |
| Liquibase    | Database version control and migrations             |
| WireMock     | API mocking for the external CPF rating service     |
| JUnit 5      | Unit testing framework                               |
| Mockito      | Mocking framework for tests                          |
| AssertJ      | Fluent assertions library                            |
| Gradle       | Build tool and dependency management                 |

## How to Run

1. Clone the repository.
2. Run `./gradlew run` to start the application locally.
3. The service processes a sample JSON transaction in the `main` method and prints the calculated features.
4. Run tests with `./gradlew test`.
---

