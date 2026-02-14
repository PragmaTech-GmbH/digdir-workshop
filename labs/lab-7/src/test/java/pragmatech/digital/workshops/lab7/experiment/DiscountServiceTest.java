package pragmatech.digital.workshops.lab7.experiment;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import pragmatech.digital.workshops.lab7.entity.Book;
import pragmatech.digital.workshops.lab7.entity.BookStatus;
import pragmatech.digital.workshops.lab7.service.DiscountService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DiscountService demonstrating parameterized tests
 * and mutation testing readiness.
 *
 * <p>This test class is configured as the target for PIT mutation testing.
 * Run mutation tests with: {@code mvn pitest:mutate}
 *
 * <p>The discount rules are:
 * <ul>
 *   <li>Non-AVAILABLE books: 0% discount</li>
 *   <li>Published less than 6 months ago: 0% discount</li>
 *   <li>Published 6 months to 2 years ago: 10% discount</li>
 *   <li>Published 2 to 5 years ago: 25% discount</li>
 *   <li>Published more than 5 years ago: 50% discount</li>
 * </ul>
 */
class DiscountServiceTest {

  private DiscountService discountService;

  @BeforeEach
  void setUp() {
    discountService = new DiscountService();
  }

  private Book createBook(LocalDate publishedDate, BookStatus status) {
    Book book = new Book("978-0000000001", "Test Book", "Test Author", publishedDate);
    book.setStatus(status);
    return book;
  }

  @Nested
  @DisplayName("Non-available books")
  class NonAvailableBooks {

    @ParameterizedTest(name = "Book with status {0} should have 0% discount")
    @EnumSource(value = BookStatus.class, names = "AVAILABLE", mode = EnumSource.Mode.EXCLUDE)
    void shouldReturnZeroDiscountForNonAvailableBooks(BookStatus status) {
      Book book = createBook(LocalDate.now().minusYears(10), status);

      int discount = discountService.calculateDiscount(book);

      assertThat(discount).isZero();
    }
  }

  @Nested
  @DisplayName("Available books - discount by age")
  class AvailableBooks {

    @Test
    @DisplayName("Brand new book (1 month old) should have 0% discount")
    void shouldReturnZeroDiscountForNewBook() {
      Book book = createBook(LocalDate.now().minusMonths(1), BookStatus.AVAILABLE);

      int discount = discountService.calculateDiscount(book);

      assertThat(discount).isZero();
    }

    @Test
    @DisplayName("Book published exactly 6 months ago should have 10% discount (boundary - isAfter is exclusive)")
    void shouldReturnTenPercentAtSixMonthBoundary() {
      Book book = createBook(LocalDate.now().minusMonths(6), BookStatus.AVAILABLE);

      int discount = discountService.calculateDiscount(book);

      assertThat(discount).isEqualTo(10);
    }

    @Test
    @DisplayName("Book published 7 months ago should have 10% discount")
    void shouldReturnTenPercentForSevenMonthOldBook() {
      Book book = createBook(LocalDate.now().minusMonths(7), BookStatus.AVAILABLE);

      int discount = discountService.calculateDiscount(book);

      assertThat(discount).isEqualTo(10);
    }

    @Test
    @DisplayName("Book published 1 year ago should have 10% discount")
    void shouldReturnTenPercentForOneYearOldBook() {
      Book book = createBook(LocalDate.now().minusYears(1), BookStatus.AVAILABLE);

      int discount = discountService.calculateDiscount(book);

      assertThat(discount).isEqualTo(10);
    }

    @Test
    @DisplayName("Book published exactly 2 years ago should have 25% discount (boundary - isAfter is exclusive)")
    void shouldReturnTwentyFivePercentAtTwoYearBoundary() {
      Book book = createBook(LocalDate.now().minusYears(2), BookStatus.AVAILABLE);

      int discount = discountService.calculateDiscount(book);

      assertThat(discount).isEqualTo(25);
    }

    @Test
    @DisplayName("Book published 2 years and 1 month ago should have 25% discount")
    void shouldReturnTwentyFivePercentForSlightlyOverTwoYears() {
      Book book = createBook(LocalDate.now().minusYears(2).minusMonths(1), BookStatus.AVAILABLE);

      int discount = discountService.calculateDiscount(book);

      assertThat(discount).isEqualTo(25);
    }

    @Test
    @DisplayName("Book published 3 years ago should have 25% discount")
    void shouldReturnTwentyFivePercentForThreeYearOldBook() {
      Book book = createBook(LocalDate.now().minusYears(3), BookStatus.AVAILABLE);

      int discount = discountService.calculateDiscount(book);

      assertThat(discount).isEqualTo(25);
    }

    @Test
    @DisplayName("Book published exactly 5 years ago should have 50% discount (boundary - isAfter is exclusive)")
    void shouldReturnFiftyPercentAtFiveYearBoundary() {
      Book book = createBook(LocalDate.now().minusYears(5), BookStatus.AVAILABLE);

      int discount = discountService.calculateDiscount(book);

      assertThat(discount).isEqualTo(50);
    }

    @Test
    @DisplayName("Book published 5 years and 1 day ago should have 50% discount")
    void shouldReturnFiftyPercentForSlightlyOverFiveYears() {
      Book book = createBook(LocalDate.now().minusYears(5).minusDays(1), BookStatus.AVAILABLE);

      int discount = discountService.calculateDiscount(book);

      assertThat(discount).isEqualTo(50);
    }

    @Test
    @DisplayName("Book published 10 years ago should have 50% discount")
    void shouldReturnFiftyPercentForTenYearOldBook() {
      Book book = createBook(LocalDate.now().minusYears(10), BookStatus.AVAILABLE);

      int discount = discountService.calculateDiscount(book);

      assertThat(discount).isEqualTo(50);
    }
  }

  @Nested
  @DisplayName("Parameterized discount tests")
  class ParameterizedDiscountTests {

    static Stream<Arguments> discountScenarios() {
      LocalDate now = LocalDate.now();
      return Stream.of(
        Arguments.of("New book (1 month)", now.minusMonths(1), BookStatus.AVAILABLE, 0),
        Arguments.of("Recent book (7 months)", now.minusMonths(7), BookStatus.AVAILABLE, 10),
        Arguments.of("Mid-age book (3 years)", now.minusYears(3), BookStatus.AVAILABLE, 25),
        Arguments.of("Old book (10 years)", now.minusYears(10), BookStatus.AVAILABLE, 50),
        Arguments.of("Borrowed old book", now.minusYears(10), BookStatus.BORROWED, 0),
        Arguments.of("Reserved old book", now.minusYears(10), BookStatus.RESERVED, 0)
      );
    }

    @ParameterizedTest(name = "{0} -> {3}% discount")
    @MethodSource("discountScenarios")
    void shouldCalculateCorrectDiscount(
        String scenario,
        LocalDate publishedDate,
        BookStatus status,
        int expectedDiscount) {

      Book book = createBook(publishedDate, status);

      int discount = discountService.calculateDiscount(book);

      assertThat(discount).isEqualTo(expectedDiscount);
    }
  }
}
