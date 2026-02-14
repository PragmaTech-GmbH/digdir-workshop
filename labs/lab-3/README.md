# Lab 3: Sliced Testing - Persistence Layer & Testcontainers

## Learning Objectives

- Learn how to test JPA repositories with `@DataJpaTest`
- Understand the differences between in-memory databases and real databases for testing
- Get started with Testcontainers for PostgreSQL
- Understand transaction management, flushing, and cleanup in tests
- Practice using `@ServiceConnection` for Testcontainers integration

## Hints

- `@DataJpaTest` focuses on JPA repositories and configures an in-memory database by default
- Use `@Testcontainers` and `@Container` for PostgreSQL-based tests
- Spring Boot's `@ServiceConnection` simplifies Testcontainers configuration
- Flyway migrations run automatically with `spring.jpa.hibernate.ddl-auto=validate`
- Be mindful of transaction boundaries in your tests

## Exercises

### Exercise 1: Testing Repositories with @DataJpaTest

This exercise focuses on testing the BookRepository with JPA-specific test configuration.

**Tasks:**
1. Open `Exercise1DataJpaTest.java` in the `exercises` package
2. Implement tests for the repository methods
3. Test both standard CRUD operations and custom queries
4. Consider using Testcontainers for PostgreSQL-specific features

**Tips:**
- `@DataJpaTest` automatically configures an in-memory database
- For PostgreSQL-specific tests, use Testcontainers with `@ServiceConnection`
- Test both positive and negative scenarios
- Be aware of the `searchBooksByTitleWithRanking` native query that requires PostgreSQL
- The solution is available in `Solution1DataJpaTest.java`
