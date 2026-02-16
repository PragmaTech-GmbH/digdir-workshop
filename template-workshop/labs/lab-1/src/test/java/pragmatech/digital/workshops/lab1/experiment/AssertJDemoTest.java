package pragmatech.digital.workshops.lab1.experiment;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AssertJDemoTest {

  @Test
  void shouldProvideFluentAssertions() {
    // Given
    List<TestBook> testBooks = List.of(
      new TestBook("1234", "Spring Boot Testing", "Test Author"),
      new TestBook("5678", "Advanced Spring", "Another Author")
    );

    // When/Then
    assertThat(testBooks)
      .hasSize(2)
      .extracting(TestBook::title)
      .containsExactly("Spring Boot Testing", "Advanced Spring");

    assertThat(testBooks.getFirst())
      .extracting(TestBook::isbn, TestBook::title)
      .containsExactly("1234", "Spring Boot Testing");
  }
}
