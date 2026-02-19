package pragmatech.digital.workshops.lab7.solutions;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import pragmatech.digital.workshops.lab7.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab7.entity.Book;
import pragmatech.digital.workshops.lab7.entity.BookStatus;
import pragmatech.digital.workshops.lab7.repository.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Solution for Exercise 3: Using the reusable JUnit 5 Testcontainers extension.
 *
 * <p>Key differences from the Spring Boot {@code @ServiceConnection} approach:
 * <ul>
 *   <li>No {@code @Import(LocalDevTestcontainerConfig.class)} annotation needed</li>
 *   <li>The PostgreSQL container is managed entirely by the JUnit extension</li>
 *   <li>The container is started once per JVM via the static initializer in
 *       {@code SharedPostgresContainerExtension}</li>
 *   <li>Spring picks up the container URL via system properties, not Spring beans</li>
 * </ul>
 *
 * <p>The extension approach trades some Spring integration for reusability:
 * <ul>
 *   <li><strong>Pro:</strong> Works with any test type, not just {@code @SpringBootTest}</li>
 *   <li><strong>Pro:</strong> No {@code @TestConfiguration} class needed per module</li>
 *   <li><strong>Con:</strong> System properties are global — can affect other tests in the same JVM</li>
 *   <li><strong>Con:</strong> Does not integrate with {@code @ServiceConnection} extras (port mapping, etc.)</li>
 * </ul>
 */
@ExtendWith(SharedPostgresContainerExtension.class)
@SpringBootTest
@ContextConfiguration(initializers = WireMockContextInitializer.class)
@Transactional
class Solution3ReusableExtensionTest {

  @Autowired
  private BookRepository bookRepository;

  @Test
  void shouldRunWithSharedContainerExtension() {
    long count = bookRepository.count();

    assertThat(count).isGreaterThanOrEqualTo(0);
  }

  @Test
  void shouldShareContainerAcrossTestMethods() {
    String isbn = UUID.randomUUID().toString().substring(0, 13);
    Book book = new Book(isbn, "Extension Test Book", "Author", LocalDate.of(2024, 1, 1));
    book.setStatus(BookStatus.AVAILABLE);

    Book savedBook = bookRepository.save(book);

    assertThat(savedBook.getId()).isNotNull();
    assertThat(bookRepository.findByIsbn(isbn)).isPresent();
  }

  @Test
  void shouldVerifyContainerConnectionDetails() {
    String jdbcUrl = SharedPostgresContainerExtension.getJdbcUrl();
    String username = SharedPostgresContainerExtension.getUsername();

    assertThat(jdbcUrl).startsWith("jdbc:postgresql://");
    assertThat(username).isEqualTo("test");
  }
}
