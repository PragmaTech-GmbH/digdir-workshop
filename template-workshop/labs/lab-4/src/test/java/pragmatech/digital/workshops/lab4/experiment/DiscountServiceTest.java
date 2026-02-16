package pragmatech.digital.workshops.lab4.experiment;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pragmatech.digital.workshops.lab4.entity.Book;
import pragmatech.digital.workshops.lab4.entity.BookStatus;
import pragmatech.digital.workshops.lab4.service.DiscountService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Demonstration of mutation testing with PIT.
 * <p>
 * This test class has decent code coverage but misses some logical issues
 * in the DiscountService implementation that mutation testing can identify.
 */
class DiscountServiceTest {

  private DiscountService cut;
  private Book book;

  @BeforeEach
  void setUp() {
    cut = new DiscountService();
    book = new Book("1234567890", "Test Book", "Test Author", LocalDate.now());
  }

  @Test
  @DisplayName("New books (less than 6 months old) should get no discount")
  void shouldApplyNoDiscountToNewBooks() {
    // Arrange
    book.setPublishedDate(LocalDate.now().minusMonths(3));

    // Act
    int discount = cut.calculateDiscount(book);

    // Assert
    assertThat(discount).isEqualTo(0);
  }

  @Test
  @DisplayName("Medium-aged books (6 months to 2 years) should get 10% discount")
  void shouldApply10PercentDiscountToMediumAgedBooks() {
    // Arrange
    book.setPublishedDate(LocalDate.now().minusMonths(12));

    // Act
    int discount = cut.calculateDiscount(book);

    // Assert
    assertThat(discount).isEqualTo(10);
  }

  @Test
  @DisplayName("Older books (2-5 years) should get 25% discount")
  void shouldApply25PercentDiscountToOlderBooks() {
    // Arrange
    book.setPublishedDate(LocalDate.now().minusYears(3));

    // Act
    int discount = cut.calculateDiscount(book);

    // Assert
    assertThat(discount).isEqualTo(25);
  }

  @Test
  @DisplayName("Very old books (more than 5 years) should get 50% discount")
  void shouldApply50PercentDiscountToVeryOldBooks() {
    // Arrange
    book.setPublishedDate(LocalDate.now().minusYears(6));

    // Act
    int discount = cut.calculateDiscount(book);

    // Assert
    assertThat(discount).isEqualTo(50);
  }

  @Test
  @DisplayName("Unavailable books should get no discount regardless of age")
  void shouldApplyNoDiscountToUnavailableBooks() {
    // Arrange
    book.setPublishedDate(LocalDate.now().minusYears(10)); // Very old but unavailable
    book.setStatus(BookStatus.BORROWED);

    // Act
    int discount = cut.calculateDiscount(book);

    // Assert
    assertThat(discount).isEqualTo(0);
  }
}
