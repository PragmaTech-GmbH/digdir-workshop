# Lab 2: Sliced Testing - Verifying the Web Layer

## Learning Objectives

- Understand Spring Boot's test slice annotations
- Learn how to test REST controllers with `@WebMvcTest`
- Get comfortable with MockMvc for simulating HTTP requests
- Use `@MockitoBean` to replace real beans with mocks in a test slice
- Test security-protected endpoints with `@WithMockUser`

## Hints

- Spring Boot test slices load only the required components for testing specific layers
- Use `@MockitoBean` to replace real beans with mocks in a test slice
- `@WebMvcTest` loads only web-related components (controllers, filters, etc.)
- Use MockMvc to simulate HTTP requests without starting a server
- Include `@Import(SecurityConfig.class)` if you need to test with security enabled

## Exercises

### Exercise 1: Testing Controllers with @WebMvcTest

In this exercise, you'll use `@WebMvcTest` to test the `BookController` in isolation from the rest of the application.

**Tasks:**
1. Open `Exercise1WebMvcTest.java` in the `exercises` package
2. Implement test methods to verify controller behavior
3. Use `MockMvc` to simulate HTTP requests and verify responses
4. Mock the `BookService` to return predetermined data

**Tips:**
- Use `@MockitoBean` to replace the real BookService with a mock
- Configure mock behavior with Mockito's when/thenReturn
- Use MockMvc's `perform()` method to simulate HTTP requests
- Include the Spring Security config with `@Import(SecurityConfig.class)`
- Use `andExpect()` to validate the response
- Check the solution in `Solution1WebMvcTest.java`
