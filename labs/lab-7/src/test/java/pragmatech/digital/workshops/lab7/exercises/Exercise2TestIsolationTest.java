package pragmatech.digital.workshops.lab7.exercises;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import pragmatech.digital.workshops.lab7.LocalDevTestcontainerConfig;
import pragmatech.digital.workshops.lab7.config.WireMockContextInitializer;
import pragmatech.digital.workshops.lab7.repository.BookRepository;

/**
 * Exercise 2: Fix Test Isolation Issues for Parallel Execution
 *
 * <p>When tests run in parallel against a shared database, they can interfere
 * with each other. This exercise explores strategies to maintain test isolation.
 *
 * <p>Tasks:
 * <ol>
 *   <li>Apply isolation strategies:
 *     <ul>
 *       <li>a) Use @Transactional for automatic rollback after each test</li>
 *       <li>b) Use unique data per test (e.g., random ISBNs with UUID)</li>
 *       <li>c) Use @Sql for explicit setup and cleanup</li>
 *     </ul>
 *   </li>
 *   <li>Verify all tests pass consistently with parallel execution enabled</li>
 * </ol>
 *
 * <p>Hints:
 * <ul>
 *   <li>@Transactional on a test method causes an automatic rollback after the test</li>
 *   <li>UUID.randomUUID().toString().substring(0, 13) creates unique ISBNs</li>
 *   <li>The books table has a UNIQUE constraint on the isbn column</li>
 *   <li>Think about what happens when two tests try to insert a book with the same ISBN</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(LocalDevTestcontainerConfig.class)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
class Exercise2TestIsolationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private BookRepository bookRepository;

  @Test
  void shouldCreateBookWithIsolatedData() {
    // TODO:
    // 1. Generate a unique ISBN using UUID
    // 2. Create a book via the API (POST /api/books)
    // 3. Verify the book was created successfully
    // 4. Ensure this test does not interfere with other tests
    // Hint: Consider adding @Transactional to this test method
  }

  @Test
  void shouldRetrieveBookWithoutSideEffects() {
    // TODO:
    // 1. Insert a book directly using BookRepository with a unique ISBN
    // 2. Retrieve it via GET /api/books/{id}
    // 3. Verify the response
    // 4. Ensure the inserted book is cleaned up after the test
  }

  @Test
  void shouldDeleteBookSafely() {
    // TODO:
    // 1. Insert a book with a unique ISBN
    // 2. Delete it via DELETE /api/books/{id}
    // 3. Verify it was deleted
    // 4. Ensure no leftover data affects other tests
  }
}
