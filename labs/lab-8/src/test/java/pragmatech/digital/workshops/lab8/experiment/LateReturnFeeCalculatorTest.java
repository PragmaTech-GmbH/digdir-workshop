package pragmatech.digital.workshops.lab8.experiment;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pragmatech.digital.workshops.lab8.entity.Book;
import pragmatech.digital.workshops.lab8.entity.BookStatus;
import pragmatech.digital.workshops.lab8.service.LateReturnFeeCalculator;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class LateReturnFeeCalculatorTest {

  private static final Clock FIXED_CLOCK = Clock.fixed(
    Instant.parse("2025-06-01T00:00:00Z"),
    ZoneOffset.UTC
  );

  private LateReturnFeeCalculator cut;

  @BeforeEach
  void setUp() {
    cut = new LateReturnFeeCalculator(FIXED_CLOCK);
  }

  @Nested
  class WhenBookIsNotBorrowed {

    @Test
    void shouldReturnZeroFeeWhenBookIsAvailable() {
      Book book = new Book("978-0-13-468599-1", "Clean Code", "Martin", LocalDate.of(2008, 8, 1));
      book.setStatus(BookStatus.AVAILABLE);

      double fee = cut.calculateFee(book, LocalDate.of(2025, 5, 1));

      assertThat(fee).isEqualTo(0.0);
    }

    @Test
    void shouldReturnZeroFeeWhenBookIsReserved() {
      Book book = new Book("978-0-13-468599-1", "Clean Code", "Martin", LocalDate.of(2008, 8, 1));
      book.setStatus(BookStatus.RESERVED);

      double fee = cut.calculateFee(book, LocalDate.of(2025, 5, 1));

      assertThat(fee).isEqualTo(0.0);
    }
  }

  @Nested
  class WhenBookIsBorrowed {

    private Book borrowedBook;

    @BeforeEach
    void setUp() {
      borrowedBook = new Book("978-0-13-468599-1", "Clean Code", "Martin", LocalDate.of(2008, 8, 1));
      borrowedBook.setStatus(BookStatus.BORROWED);
    }

    @Test
    void shouldReturnZeroFeeWhenReturnedOnTime() {
      double fee = cut.calculateFee(borrowedBook, LocalDate.of(2025, 6, 1));

      assertThat(fee).isEqualTo(0.0);
    }

    @Test
    void shouldReturnZeroFeeWhenBorrowedInFuture() {
      double fee = cut.calculateFee(borrowedBook, LocalDate.of(2025, 6, 15));

      assertThat(fee).isEqualTo(0.0);
    }

    @ParameterizedTest(name = "{0} days overdue -> fee ${1}")
    @CsvSource({
      "1,  1.0",
      "3,  3.0",
      "7,  7.0"
    })
    void shouldChargeOneDollarPerDayWhenOneToSevenDaysOverdue(long daysOverdue, double expectedFee) {
      LocalDate borrowedDate = LocalDate.of(2025, 6, 1).minusDays(daysOverdue);

      double fee = cut.calculateFee(borrowedBook, borrowedDate);

      assertThat(fee).isEqualTo(expectedFee);
    }

    @ParameterizedTest(name = "{0} days overdue -> fee ${1}")
    @CsvSource({
      "8,  12.0",
      "10, 15.0",
      "14, 21.0"
    })
    void shouldChargeDollarFiftyPerDayWhenEightToFourteenDaysOverdue(long daysOverdue, double expectedFee) {
      LocalDate borrowedDate = LocalDate.of(2025, 6, 1).minusDays(daysOverdue);

      double fee = cut.calculateFee(borrowedBook, borrowedDate);

      assertThat(fee).isEqualTo(expectedFee);
    }

    @ParameterizedTest(name = "{0} days overdue -> fee ${1}")
    @CsvSource({
      "15, 30.0",
      "20, 40.0",
      "30, 60.0"
    })
    void shouldChargeTwoDollarsPerDayWhenFifteenOrMoreDaysOverdue(long daysOverdue, double expectedFee) {
      LocalDate borrowedDate = LocalDate.of(2025, 6, 1).minusDays(daysOverdue);

      double fee = cut.calculateFee(borrowedBook, borrowedDate);

      assertThat(fee).isEqualTo(expectedFee);
    }

    @Nested
    class BoundaryValues {

      @Test
      void shouldApplyTierOneBoundaryAtSevenDays() {
        LocalDate borrowedDate = LocalDate.of(2025, 5, 25);

        double fee = cut.calculateFee(borrowedBook, borrowedDate);

        assertThat(fee).isEqualTo(7.0);
      }

      @Test
      void shouldApplyTierTwoBoundaryAtEightDays() {
        LocalDate borrowedDate = LocalDate.of(2025, 5, 24);

        double fee = cut.calculateFee(borrowedBook, borrowedDate);

        assertThat(fee).isEqualTo(12.0);
      }

      @Test
      void shouldApplyTierTwoBoundaryAtFourteenDays() {
        LocalDate borrowedDate = LocalDate.of(2025, 5, 18);

        double fee = cut.calculateFee(borrowedBook, borrowedDate);

        assertThat(fee).isEqualTo(21.0);
      }

      @Test
      void shouldApplyTierThreeBoundaryAtFifteenDays() {
        LocalDate borrowedDate = LocalDate.of(2025, 5, 17);

        double fee = cut.calculateFee(borrowedBook, borrowedDate);

        assertThat(fee).isEqualTo(30.0);
      }
    }
  }
}
