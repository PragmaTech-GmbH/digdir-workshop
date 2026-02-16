package pragmatech.digital.workshops.lab4.experiment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pragmatech.digital.workshops.lab4.client.OpenLibraryApiClient;
import pragmatech.digital.workshops.lab4.entity.Book;
import pragmatech.digital.workshops.lab4.entity.BookStatus;

/**
 * This test demonstrates JUnit 5 (Jupiter) style testing.
 * <p>
 * Key characteristics of JUnit 5:
 * - Uses @ExtendWith for extension mechanism
 * - @BeforeEach and @AfterEach for setup/teardown
 * - @BeforeAll and @AfterAll for static setup/teardown
 * - @Nested for nested test classes
 * - @DisplayName for readable test names
 * - @Disabled for skipping tests
 * - Better assertions API through Assertions class
 * - Built-in parameterized test support
 * - Built-in parallel execution support
 * - Built-in assumptions API
 * - Exception testing through assertThrows
 * - Conditional test execution
 * - @Tag for test filtering
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JUnit 5 Style Test")
class JUnit5StyleTest {

  @TempDir
  Path tempDir;
  private Book book;
  @Mock
  private OpenLibraryApiClient apiClient;

  @BeforeAll
  static void setUpAll() {
    System.out.println("Set up once before all tests in the class");
  }

  @AfterAll
  static void tearDownAll() {
    System.out.println("Tear down once after all tests in the class");
  }

  @BeforeEach
  void setUp() {
    book = new Book();
    book.setIsbn("1234567890");
    book.setTitle("Test Book");
    book.setAuthor("Test Author");
    book.setPublishedDate(LocalDate.now());
    book.setStatus(BookStatus.AVAILABLE);
  }

  @Test
  @DisplayName("Book should have correct properties")
  void shouldHaveCorrectProperties() {
    // JUnit 5 style assertions
    Assertions.assertEquals("Test Book", book.getTitle());
    Assertions.assertEquals("Test Author", book.getAuthor());
    Assertions.assertEquals(BookStatus.AVAILABLE, book.getStatus());
    Assertions.assertTrue(book.isAvailable());
  }

  @Test
  @DisplayName("Book availability should change with status")
  void shouldChangeAvailabilityWithStatus() {
    // Test that book is available by default
    Assertions.assertTrue(book.isAvailable());

    // Change status
    book.setStatus(BookStatus.BORROWED);

    // Test that book is not available after status change
    Assertions.assertFalse(book.isAvailable());
  }

  @Test
  @DisplayName("Should throw exception")
  void shouldThrowException() {
    // JUnit 5 exception testing
    IllegalArgumentException exception = Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> {
        throw new IllegalArgumentException("ISBN cannot be null");
      }
    );

    Assertions.assertEquals("ISBN cannot be null", exception.getMessage());
  }

  @Test
  @DisplayName("Should work with temporary directory")
  void shouldWorkWithTemporaryDirectory() throws Exception {
    // Create temp file using JUnit 5 @TempDir
    Path tempFile = tempDir.resolve("testFile.txt");
    Files.write(tempFile, "Test content".getBytes());

    // Assert file was created
    Assertions.assertTrue(Files.exists(tempFile));
  }

  @Test
  @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
  @DisplayName("Should complete within timeout")
  void shouldCompleteWithinTimeout() throws Exception {
    // This test will pass because it completes within 100ms
    Thread.sleep(50);
  }

  @Test
  @EnabledOnOs(OS.MAC)
  @DisplayName("Should only run on macOS")
  void shouldOnlyRunOnMac() {
    // This test will only run on macOS
    Assertions.assertTrue(true);
  }

  @Disabled("Demonstrates how to skip a test in JUnit 5")
  @Test
  @DisplayName("This test is ignored")
  void disabledTest() {
    Assertions.fail("This test should be ignored and not run");
  }

  @Test
  @DisplayName("Should demonstrate assumptions")
  void shouldDemonstrateAssumptions() {
    // If the assumption fails, the test is skipped (not failed)
    Assumptions.assumeTrue(System.getProperty("os.name").contains("Mac"));

    // This code will only run if the assumption passes
    Assertions.assertTrue(true);
  }

  @ParameterizedTest
  @ValueSource(strings = {"1234567890", "0987654321", "abcdefghij"})
  @DisplayName("Should handle multiple ISBN values")
  void shouldHandleMultipleIsbnValues(String isbn) {
    // Test with multiple values
    book.setIsbn(isbn);
    Assertions.assertEquals(isbn, book.getIsbn());
  }

  @ParameterizedTest
  @CsvSource({
    "Book 1, Author 1",
    "Book 2, Author 2",
    "Book 3, Author 3"
  })
  @DisplayName("Should handle multiple title and author combinations")
  void shouldHandleMultipleTitleAndAuthorCombinations(String title, String author) {
    book.setTitle(title);
    book.setAuthor(author);

    Assertions.assertEquals(title, book.getTitle());
    Assertions.assertEquals(author, book.getAuthor());
  }

  @ParameterizedTest
  @EnumSource(BookStatus.class)
  @DisplayName("Should handle all book statuses")
  void shouldHandleAllBookStatuses(BookStatus status) {
    book.setStatus(status);
    Assertions.assertEquals(status, book.getStatus());
  }

  @RepeatedTest(3)
  @DisplayName("Should be able to repeat tests")
  void shouldBeAbleToRepeatTests() {
    // This test will run 3 times
    Assertions.assertNotNull(book);
  }

  @AfterEach
  void tearDown() {
    book = null;
  }

  @Nested
  @DisplayName("When book is borrowed")
  class WhenBookIsBorrowed {

    @BeforeEach
    void setUpBorrowedBook() {
      book.setStatus(BookStatus.BORROWED);
    }

    @Test
    @DisplayName("Book should not be available")
    void shouldNotBeAvailable() {
      Assertions.assertFalse(book.isAvailable());
    }

    @Test
    @DisplayName("Book status should be BORROWED")
    void shouldHaveBorrowedStatus() {
      Assertions.assertEquals(BookStatus.BORROWED, book.getStatus());
    }

    @Nested
    @DisplayName("And then returned")
    class AndThenReturned {

      @BeforeEach
      void setUpReturnedBook() {
        book.setStatus(BookStatus.AVAILABLE);
      }

      @Test
      @DisplayName("Book should be available again")
      void shouldBeAvailableAgain() {
        Assertions.assertTrue(book.isAvailable());
      }
    }
  }
}
