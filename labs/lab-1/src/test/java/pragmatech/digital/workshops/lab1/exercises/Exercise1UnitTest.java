package pragmatech.digital.workshops.lab1.exercises;

import org.junit.jupiter.api.Test;

/**
 * Exercise 1: Write unit tests for the BookService class using Mockito
 * <p>
 * Tasks:
 * 1. Complete the test method for checking that an exception is thrown when
 * trying to add a book with an ISBN that already exists
 * 2. Complete the test method for the happy path of creating a new book
 * <p>
 * Guidelines:
 * - Use Mockito to mock the BookRepository
 * - Structure tests using the Arrange-Act-Assert pattern
 * - Use assertThrows for testing exceptions
 */

class Exercise1UnitTest {

  @Test
  void shouldThrowExceptionWhenBookWithIsbnAlreadyExists() {
    // TODO: Implement this test
    // 1. Arrange: Create a BookService with the mocked repository
    //    and configure the mock to return an existing book for a specific ISBN
    // 2. Act & Assert: Use assertThrows to verify that BookAlreadyExistsException is thrown
    //    when trying to create a book with that ISBN
  }

  @Test
  void shouldCreateBookWhenIsbnDoesNotExist() {
    // TODO: Implement this test
    // 1. Arrange: Create a BookService with the mocked repository
    //    and configure the mock to return an empty Optional for the ISBN
    //    and configure what the save method should return
    // 2. Act: Call the create method with test data
    // 3. Assert: Verify the returned ID matches the expected value
    //    and that the repository methods were called with the correct arguments
  }
}
