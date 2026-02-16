# Lab 3: Testing with WireMock and Spring Context Caching

## Learning Objectives

- Learn how to test HTTP clients with WireMock
- Understand Spring Test context caching behavior
- Identify how test configuration impacts context reuse
- Master techniques for testing HTTP-communication-based applications
- Improve test execution time through context optimization

## Hints

- WireMock provides a flexible API for stubbing HTTP interactions
- Spring caches application contexts based on the configuration "fingerprint"
- Small changes in test configuration can lead to separate contexts being created
- Use `@MockBean/@MockitoBean` strategically to avoid unnecessary context recreation
- Check the logs for "Creating new Spring ApplicationContext" messages to identify context creation

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

### Exercise 2: Spring Test Context Caching

This exercise demonstrates how small changes in test configuration affect Spring Test context caching.

**Tasks:**
1. Examine the five Application*IT test classes in the `exercises` package
2. Run all five tests and observe the log output for context creation messages
3. Identify which configuration differences cause new contexts to be created
4. Experiment with additional configuration changes to see their impact

**Tips:**
- Each test class has slightly different configuration (annotations, beans, profiles)
- Look for "Creating new Spring ApplicationContext" in the logs
- Count how many unique application contexts are created
- Consider how you might optimize the tests to reduce context creation
- Application context creation is expensive, so minimizing it can significantly speed up tests
