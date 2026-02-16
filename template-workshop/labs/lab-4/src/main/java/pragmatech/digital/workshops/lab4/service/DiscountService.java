package pragmatech.digital.workshops.lab4.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import pragmatech.digital.workshops.lab4.entity.Book;
import pragmatech.digital.workshops.lab4.entity.BookStatus;

/**
 * Service for calculating discounts on books.
 * <p>
 * This implementation contains issues that make it a good candidate for mutation testing.
 */
@Service
public class DiscountService {

  /**
   * Calculate the discount percentage for a book.
   * Rules:
   * - New books (published within last 6 months): No discount
   * - Books between 6 months and 2 years: 10% discount
   * - Books between 2 years and 5 years: 25% discount
   * - Books older than 5 years: 50% discount
   * - Unavailable books: No discount
   *
   * @param book The book to calculate discount for
   * @return The discount percentage (0-100)
   */
  public int calculateDiscount(Book book) {
    // No discount for unavailable books
    if (book.getStatus() != BookStatus.AVAILABLE) {
      return 0;
    }

    LocalDate publishedDate = book.getPublishedDate();
    LocalDate now = LocalDate.now();
    LocalDate sixMonthsAgo = now.minusMonths(6);
    LocalDate twoYearsAgo = now.minusYears(2);
    LocalDate fiveYearsAgo = now.minusYears(5);

    if (publishedDate.isAfter(sixMonthsAgo)) {
      return 0; // New book, no discount
    }

    if (publishedDate.isEqual(sixMonthsAgo) || publishedDate.isAfter(twoYearsAgo)) {
      return 10; // Medium-aged book: 10% discount
    }

    if (publishedDate.isEqual(twoYearsAgo) || publishedDate.isAfter(fiveYearsAgo)) {
      return 25; // Older book: 25% discount
    }

    return 50; // Very old book: 50% discount
  }
}
