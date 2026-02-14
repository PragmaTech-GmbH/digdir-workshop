package pragmatech.digital.workshops.lab1.exercises;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * AssertJ Workshop Exercise
 * <p>
 * In this exercise, you'll practice using AssertJ to write expressive assertions for:
 * - Dates
 * - Lists
 * - Exception handling
 * - Chaining assertions
 * <p>
 * Complete each test method according to the instructions in the comments.
 * The sample data is already provided for you.
 */
class Exercise3AssertJTest {

  /**
   * Exercise 1: Date Assertions
   * <p>
   * Use AssertJ to:
   * 1. Assert that 'today' is before 'tomorrow'
   * 2. Assert that 'yesterday' is before 'today'
   * 3. Assert that 'nextWeek' is after 'tomorrow'
   * 4. Assert that 'nextWeek' is exactly 7 days after 'today'
   */
  @Test
  void testDateAssertions() {

    LocalDate today = LocalDate.now();
    LocalDate yesterday = today.minusDays(1);
    LocalDate tomorrow = today.plusDays(1);
    LocalDate nextWeek = today.plusWeeks(1);

    // TODO: Write your assertions here
  }

  /**
   * Exercise 2: List Assertions
   * <p>
   * Use AssertJ to:
   * 1. Assert that 'programmingLanguages' has exactly 5 elements
   * 2. Assert that 'programmingLanguages' contains "Java" and "Kotlin"
   * 3. Assert that 'programmingLanguages' contains "Java" as its first element
   * 4. Assert that 'programmingLanguages' contains only the expected elements
   * in the exact order (Java, Python, JavaScript, Kotlin, Go)
   * 5. Assert that all elements in the list have at least 2 characters
   */
  @Test
  void testListAssertions() {

    List<String> programmingLanguages = Arrays.asList(
      "Java", "Python", "JavaScript", "Kotlin", "Go");

    // TODO: Write your assertions here

  }

  /**
   * Exercise 3: Exception Assertions
   * <p>
   * Use AssertJ to:
   * 1. Assert that a NullPointerException is thrown when calling checkNotNull(null) from this test class
   * 2. Assert that an IllegalArgumentException is thrown when calling checkPositive(-1) from this test class
   * and that the exception message contains the string "positive"
   * 3. Assert that no exception is thrown when valid values are passed to these methods
   */
  @Test
  void testExceptionAssertions() {
    // TODO: Write your assertions here
  }

  /**
   * Exercise 4: Chaining Assertions
   * <p>
   * Use AssertJ to create a chain of assertions on the 'user' object:
   * 1. Assert that user.getUsername() equals "john_doe"
   * 2. Assert that user.getFirstName() starts with "J" and user.getLastName() ends with "e"
   * 3. Assert that user.getAge() is between 20 and 30
   * 4. Assert that user.getTags() has size 3 and contains "java"
   * <p>
   * Try to do this with a single chain of assertions!
   */
  @Test
  void testChainedAssertions() {

    User user = new User(
      "john_doe",
      "John",
      "Doe",
      25,
      Arrays.asList("java", "spring", "testing"));

    // TODO: Write your assertions here
  }

  // Helper methods for exception testing
  private void checkNotNull(Object obj) {
    if (obj == null) {
      throw new NullPointerException("Object cannot be null");
    }
  }

  private void checkPositive(int number) {
    if (number <= 0) {
      throw new IllegalArgumentException("Number must be positive");
    }
  }

  // User class for testing
  record User(String username, String firstName, String lastName, int age, List<String> tags) {
  }
}
