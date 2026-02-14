# Lab 4: Integration Testing - Introduction & Strategies

## Learning Objectives

- Learn how to test HTTP clients with WireMock
- Master techniques for testing HTTP-communication-based applications
- Understand the "airplane mode" concept for tests
- Learn to use `@ContextConfiguration` with custom initializers
- Get started with full `@SpringBootTest` integration tests

## Hints

- WireMock provides a flexible API for stubbing HTTP interactions
- Use `@RegisterExtension` for WireMock's JUnit 5 extension
- Sample response JSON files are available in the `__files` directory
- Use `@ContextConfiguration(initializers = ...)` to configure WireMock for Spring integration tests
- All external HTTP calls should be stubbed in tests ("airplane mode")

## Exercises

### Exercise 1: Testing HTTP Clients with WireMock

In this exercise, you'll create unit tests for the `OpenLibraryApiClient` using WireMock to simulate HTTP interactions.

**Tasks:**
1. Open `Exercise1WireMockTest.java` in the `exercises` package
2. Implement test methods to verify client behavior for:
   - Successful API responses (200 status)
   - Server error responses (500 status)
3. Optional: Modify the client to handle 404 responses gracefully by returning null instead of throwing an exception

**Tips:**
- Use WireMock's JUnit 5 extension (`@RegisterExtension`) or bootstrap it manually
- Configure `WebClient` to point to the WireMock server in your test
- Use WireMock's `stubFor()` method to define response behavior
- Sample response JSON files are available in the `__files` directory (WireMock's default source folder for stubbing)
- Check the solution in `Solution1WireMockTest.java`
