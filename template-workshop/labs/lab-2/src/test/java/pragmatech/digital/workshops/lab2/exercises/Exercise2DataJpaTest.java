package pragmatech.digital.workshops.lab2.exercises;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pragmatech.digital.workshops.lab2.repository.BookRepository;

/**
 * Exercise 2: Data JPA Test
 * <p>
 * This exercise demonstrates testing a Spring Data JPA repository with Testcontainers
 * <p>
 * Tasks:
 * 1. Test the native query searchBooksByTitleWithRanking in BookRepository
 * 2. Ensure proper test data setup
 * 3. Ensure no data exists after the tests (test isolation)
 * 4. Use Testcontainers for a real PostgreSQL instance
 */
@DataJpaTest
class Exercise2DataJpaTest {

  @Autowired
  private BookRepository cut;

  @Nested
  @DisplayName("Full text search tests")
  class FullTextSearchTests {

    @Test
    @DisplayName("Should find books by title with proper ranking")
    void shouldFindBooksByTitleWithProperRanking() {
      // Write a test that adds multiple books with different titles
      // Call the searchBooksByTitleWithRanking method with a search term
      // Verify that books containing the search term are returned and properly ranked
    }

    @Test
    @DisplayName("Should return empty list when no books match search term")
    void shouldReturnEmptyListWhenNoBooksMatchSearchTerm() {
      // Write a test that verifies the repository returns an empty list when no books match the search term
    }
  }
}
