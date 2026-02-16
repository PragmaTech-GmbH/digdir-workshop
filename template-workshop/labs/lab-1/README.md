# Lab 1: Testing Fundamentals

## Learning Objectives

- Understand basic JUnit 5 annotations and lifecycle hooks
- Learn how to use Mockito for mocking dependencies
- Understand the `spring-boot-starter-test` aka. Testing Swiss-Army Knife
- Master AssertJ for fluent and readable assertions
- Gain familiarity with JUnit extensions

## Hints

- Run `./mvnw dependency:tree` to see transitive dependencies coming from the Spring Boot Starter Test dependency
- Check the Spring Boot [Dependencies Parent POM](https://github.com/spring-projects/spring-boot/blob/main/spring-boot-project/spring-boot-dependencies/build.gradle) for managed dependencies
- JUnit 5 consists of multiple modules: JUnit Platform, JUnit Jupiter, and JUnit Vintage

## Exercises

### Exercise 1: Unit Testing with JUnit 5

In this exercise, you'll create a unit test for the `BookService` class, focusing on basic JUnit 5 functionality.

**Tasks:**
1. Open `Exercise1UnitTest.java` in the `exercises` package
2. Implement the missing test methods for `BookService`
3. Use appropriate JUnit 5 annotations and assertions

**Tips:**
- Use `@BeforeEach` for test setup
- Make sure to test both positive and negative cases
- The solution can be found in `Solution1UnitTest.java`

### Exercise 2: JUnit Extensions

This exercise introduces JUnit 5 extensions for adding cross-cutting concerns to your tests.

**Tasks:**
1. Open `Exercise2JUnitExtensionTest.java` in the `exercises` package
2. Implement the test class using the provided `TimingExtension`
3. Use `@ExtendWith` to register the extension

**Tips:**
- Extensions allow you to intercept test execution at various points
- The TimingExtension will log the execution time of each test method
- Check the solution in `Solution2JUnitExtensionTest.java`

### Exercise 3: AssertJ Assertions

Learn how to write expressive and readable assertions using AssertJ.

**Tasks:**
1. Open `Exercise3AssertJTest.java` in the `exercises` package
2. Replace JUnit assertions with AssertJ's fluent API
3. Use AssertJ's advanced features like exception assertions and collection assertions

**Tips:**
- Start assertions with `assertThat()`
- Chain methods for more specific assertions
- Explore the `AssertJDemoTest` in the `experiment` package for examples
- The solution is in `Solution3AssertJTest.java`
