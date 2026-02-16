# Lab 2: Spring Boot Test Slices

## Learning Objectives

- Understand Spring Boot's test slice annotations
- Learn how to test REST controllers with `@WebMvcTest`
- Practice testing JPA repositories with `@DataJpaTest`
- Get comfortable with Spring's testing utilities

## Hints

- Spring Boot test slices load only the required components for testing specific layers
- Use `@MockBean/@MockitoBean` to replace real beans with mocks in a test slice
- `@WebMvcTest` loads only web-related components (controllers, filters, etc.)
- `@DataJpaTest` focuses on JPA repositories and configures an in-memory database by default
- Use MockMvc to simulate HTTP requests without starting a server

## Exercises

### Exercise 1: Testing Controllers with @WebMvcTest

In this exercise, you'll use `@WebMvcTest` to test the `BookController` in isolation from the rest of the application.

**Tasks:**
1. Open `Exercise1WebMvcTest.java` in the `exercises` package
2. Implement test methods to verify controller behavior for
3. Use `MockMvc` to simulate HTTP requests and verify responses
4. Mock the `BookService` to return predetermined data

**Tips:**
- Use `@MockitoBean` to replace the real BookService with a mock
- Configure mock behavior with Mockito's when/thenReturn
- Use MockMvc's `perform()` method to simulate HTTP requests
- Include the Spring Security config with `@Import(SecurityConfig.class)`
- Use `andExpect()` to validate the response
- Check the solution in `Solution1WebMvcTest.java`

### Exercise 2: Testing Repositories with @DataJpaTest

This exercise focuses on testing the BookRepository with JPA-specific test configuration.

**Tasks:**
1. Open `Exercise2DataJpaTest.java` in the `exercises` package
2. Implement tests for the repository methods

**Tips:**
- `@DataJpaTest` automatically configures an in-memory database
- Test both positive and negative scenarios
- The solution is available in `Solution2DataJpaTest.java`
