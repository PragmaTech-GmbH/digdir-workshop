# Testing Spring Boot Applications Demystified Workshop

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/PragmaTech-GmbH/digdir-workshop)

A two-day workshop to help developers become more confident and productive when implementing automated tests for Spring Boot applications.

Proudly presented by [PragmaTech GmbH](https://pragmatech.digital/).

## Workshop Overview

This workshop is designed to demystify testing in Spring Boot applications through hands-on exercises, covering everything from basic unit testing to advanced integration testing techniques, test optimization, and CI/CD best practices. The workshop is divided into eight lab sessions across two days, each focusing on different aspects of testing Spring Boot applications.

### Day One: Testing Spring Boot Applications Demystified

Goal: Getting started with testing Spring Boot applications and learning how to write tests for Spring Boot applications.

| Time | Session |
|------|---------|
| 09:00 - 10:30 | Block 1: [Lab 1](labs/lab-1) - Workshop Introduction, Testing Basics & Unit Testing |
| 10:30 - 11:00 | Coffee Break & Exercises |
| 11:00 - 12:30 | Block 2: [Lab 2](labs/lab-2) - Sliced Testing: Verifying the Web Layer |
| 12:30 - 13:30 | Lunch Break |
| 13:30 - 15:00 | Block 3: [Lab 3](labs/lab-3) - Sliced Testing: Persistence Layer & Testcontainers |
| 15:00 - 15:30 | Coffee Break & Exercises |
| 15:30 - 17:00 | Block 4: [Lab 4](labs/lab-4) - Integration Testing Introduction |

### Day Two: The Need for Speed: Optimizing Spring Boot Test Suites

Goal: Showcase and explain strategies to improve build times and speed up tests to gather fast feedback.

| Time | Session |
|------|---------|
| 09:00 - 10:30 | Block 1: [Lab 5](labs/lab-5) - Integration Testing Continued |
| 10:30 - 11:00 | Coffee Break & Exercises |
| 11:00 - 12:30 | Block 2: [Lab 6](labs/lab-6) - Spring Test Context Caching |
| 12:30 - 13:30 | Lunch Break |
| 13:30 - 15:00 | Block 3: [Lab 7](labs/lab-7) - Test Parallelization & Best Practices |
| 15:00 - 15:30 | Coffee Break & Exercises |
| 15:30 - 17:00 | Block 4: [Lab 8](labs/lab-8) - FAQ & CI/CD Best Practices |

## Workshop Format

- Two-day workshop on-site or remote
- Eight main labs, each 90 minutes
- Hands-on exercises with provided solutions
- Building on a consistent domain model (a library management system)

## Lab Structure

Each lab (`lab-1` through `lab-8`) includes:

- Exercise files with instructions and TODO comments
- Solution files that show the complete implementation
- Supporting code and configurations

## Prerequisites

- Java 21 (or later)
- Maven 3.9+ (wrapper included)
- Docker (for Testcontainers)
- Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code, etc.)

## Getting Started

1. Clone this repository

2. Import the projects into your IDE of choice.

3. Run all builds with:

```bash
./mvnw verify
```

## Additional Resources

- [Spring Boot Testing Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Spring Test Documentation](https://docs.spring.io/spring-framework/reference/testing.html)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html)
- [Testcontainers Documentation](https://www.testcontainers.org/)
- [WireMock Documentation](http://wiremock.org/docs/)

## Contact

[Contact us](https://pragmatech.digital/contact/) to enroll your team in this workshop.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
