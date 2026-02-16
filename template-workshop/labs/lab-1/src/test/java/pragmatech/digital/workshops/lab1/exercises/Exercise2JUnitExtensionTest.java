package pragmatech.digital.workshops.lab1.exercises;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Exercise 2: Create a custom JUnit Jupiter extension to detect slow tests
 * <p>
 * Tasks:
 * 1. Create a SlowTestDetector extension class that implements the appropriate JUnit Jupiter interfaces
 * - The extension should measure the execution time of each test
 * - If a test takes longer than 100ms, it should print a warning to the console
 * - The extension should allow configuring the threshold via a parameter
 * <p>
 * 2. Apply your extension to the test class or methods
 * <p>
 * Guidelines:
 * - Use JUnit Jupiter extension mechanisms
 * - The extension should implement the appropriate callback interfaces
 * - Consider how you can make the threshold configurable
 * - Explore different ways to activate the extension:
 * a) Using @ExtendWith at the class level
 * b) Using @ExtendWith at the method level
 * c) Using @RegisterExtension for a field
 */
class Exercise2JUnitExtensionTest {

  // TODO: Register your extension here if using @RegisterExtension approach

  @Test
    // TODO: Add extension at method level if using method-level approach
  void fastTest() {
    // This test should run quickly
    int result = 1 + 1;

    assertEquals(2, result);
  }

  @Test
    // TODO: Add extension at method level if using method-level approach
  void slowTest() throws InterruptedException {
    // This test is artificially slowed down to exceed the threshold
    Thread.sleep(150);
    int result = 1 + 1;

    assertEquals(2, result);
  }
}
