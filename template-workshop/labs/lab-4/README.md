# Lab 4: Advanced Spring Boot Testing Techniques

## Learning Objectives

- Compare JUnit 4 vs JUnit 5 testing styles
- Understand the differences between `@Mock`, `@MockBean`, and `@MockitoBean`
- Learn best practices for Spring Boot integration testing
- Master techniques for testing asynchronous code
- Optimize Spring test execution with strategic context configuration

## Hints

- JUnit 5 provides a more flexible and powerful extension model than JUnit 4
- `@Mock` is for plain Mockito without Spring context, while `@MockBean/@MockitoBean` are for Spring tests
- `@MockitoBean` is preferred over `@MockBean` for better control and slightly better performance
- Async testing may require special utilities like Awaitility
- Test context caching is critical for fast Spring Boot tests
- Try to reuse test contexts whenever possible
- Consider using test slices to reduce context startup time
- Run mutation tests with `./mvnw test-compile org.pitest:pitest-maven:mutationCoverage`

## Exercises

No exercises for this last lab.
