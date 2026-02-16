# Testing Spring Boot Applications Demystified Workshop

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/PragmaTech-GmbH/testing-spring-boot-applications-demystified-workshop)

A full-day workshop to help developers become more confident and productive when implementing automated tests for Spring Boot applications.

Proudly presented by [PragmaTech GmbH](https://pragmatech.digital/). 

## Workshop Overview

This workshop is designed to demystify testing in Spring Boot applications through hands-on exercises, covering everything from basic unit testing to advanced integration testing techniques. The workshop is divided into four lab sessions, each focusing on different aspects of testing Spring Boot applications.

- [Lab 1](labs/lab-1): Introduction & Unit Testing
- [Lab 2](labs/lab-2): Sliced Testing
- [Lab 3](labs/lab-3): Integration Testing
- [Lab 4](labs/lab-4): Pitfalls, Best Practices, and AI 

## Workshop Format

- Full-day workshop on-site or remote
- Four main labs, ranging from 70-110 minutes each
- Hands-on exercises with provided solutions
- Building on a consistent domain model (a library management system)

## GitHub Codespaces

This repository is configured for use with GitHub Codespaces, which provides a complete, ready-to-use development environment in the cloud. To use GitHub Codespaces:

1. Click on the "Code" button on the GitHub repository
2. Select the "Codespaces" tab
3. Click "Create codespace on main" and select at least 4 cores (120 hours are [free per month](https://docs.github.com/en/billing/managing-billing-for-your-products/managing-billing-for-github-codespaces/about-billing-for-github-codespaces))
4. Wait for the codespace to start and setup to complete

The codespace includes:
- Java 21
- Maven
- Docker (for Testcontainers)
- VS Code with Spring Boot extensions

For more information, see the [Codespaces documentation](.devcontainer/README.md).

## Lab Structure

See the content of the [labs](labs) folder for the content per lab.

Each lab (`lab-1` through `lab-4`) includes:

- Exercise files with instructions and TODO comments
- Solution files that show the complete implementation
- Supporting code and configurations

## Prerequisites

- Java 21 (or later)
- Maven 3.9+ (wrapper included)
- Docker (for Testcontainers)
- Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code, Eclipse, NetBeans, etc.)

## Getting Started

1. Clone this repository:

 ```bash
 git clone https://github.com/PragmaTech-GmbH/testing-spring-boot-applications-demystified-workshop.git
 ```

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

## On-Demand Online Course

Explore our [Testing Spring Boot Applications Masterclass](https://rieckpil.de/testing-spring-boot-applications-masterclass/) for the on-demand version of this workshop.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
