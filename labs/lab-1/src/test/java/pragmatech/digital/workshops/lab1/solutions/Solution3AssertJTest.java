package pragmatech.digital.workshops.lab1.solutions;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class Solution3AssertJTest {

  @Test
  void testDateAssertions() {

    LocalDate today = LocalDate.now();
    LocalDate yesterday = today.minusDays(1);
    LocalDate tomorrow = today.plusDays(1);
    LocalDate nextWeek = today.plusWeeks(1);

    // Date assertions
    assertThat(today).isBefore(tomorrow);
    assertThat(yesterday).isBefore(today);
    assertThat(nextWeek).isAfter(tomorrow);
    assertThat(nextWeek).isEqualTo(today.plusDays(7));
  }

  @Test
  void testListAssertions() {

    List<String> programmingLanguages = Arrays.asList(
      "Java", "Python", "JavaScript", "Kotlin", "Go");

    // List assertions
    assertThat(programmingLanguages)
      .hasSize(5)
      .contains("Java", "Kotlin")
      .startsWith("Java")
      .containsExactlyInAnyOrder("Java", "Python", "JavaScript", "Kotlin", "Go");

    // Using a predicate
    assertThat(programmingLanguages)
      .allMatch(language -> language.length() >= 2);
  }

  @Test
  void testExceptionAssertions() {
    // Exception assertions
    assertThatThrownBy(() -> checkNotNull(null))
      .isInstanceOf(NullPointerException.class)
      .hasMessage("Object cannot be null");

    // Alternative syntax
    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> checkPositive(-1))
      .withMessageContaining("positive");

    // Assert no exceptions
    assertThatNoException().isThrownBy(() -> {
      checkNotNull("valid");
      checkPositive(10);
    });
  }

  @Test
  void testChainedAssertions() {
    User user = new User(
      "john_doe",
      "John",
      "Doe",
      25,
      Arrays.asList("java", "spring", "testing"));

    // Approach 1: Using extracting with multiple properties
    assertThat(user)
      .extracting(
        User::username,
        User::firstName,
        User::lastName,
        User::age,
        User::tags)
      .containsExactly(
        "john_doe",
        "John",
        "Doe",
        25,
        Arrays.asList("java", "spring", "testing"));

    // Approach 2: Using separate assertions with same assertThat
    assertThat(user.username()).isEqualTo("john_doe");
    assertThat(user.firstName()).startsWith("J");
    assertThat(user.lastName()).endsWith("e");
    assertThat(user.age()).isBetween(20, 30);
    assertThat(user.tags()).hasSize(3).contains("java");

    // Approach 3: Using satisfies (most recommended for complex assertions)
    assertThat(user)
      .satisfies(u -> {
        assertThat(u.username()).isEqualTo("john_doe");
        assertThat(u.firstName()).startsWith("J");
        assertThat(u.lastName()).endsWith("e");
        assertThat(u.age()).isBetween(20, 30);
        assertThat(u.tags()).hasSize(3).contains("java");
      });
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
