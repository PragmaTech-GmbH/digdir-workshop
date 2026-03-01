package pragmatech.digital.workshops.lab5.experiment.customizer;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import pragmatech.digital.workshops.lab5.config.OpenLibraryApiStub;
import pragmatech.digital.workshops.lab5.entity.Book;
import pragmatech.digital.workshops.lab5.repository.BookRepository;

@SharedInfrastructureTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class BookControllerCustomizerIT {

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private OpenLibraryApiStub openLibraryApiStub;

  @AfterEach
  void cleanUp() {
    bookRepository.deleteAll();
  }

  @BeforeEach
  void setUp() {
    bookRepository.save(new Book("111-1234567890", "Clean Code", "Robert C. Martin", LocalDate.of(2008, 8, 1)));
    bookRepository.save(new Book("222-1234567890", "Effective Java", "Joshua Bloch", LocalDate.of(2018, 1, 6)));
  }

  @Test
  void shouldReturnAllBooksWhenGettingAllBooks() {
    webTestClient.get()
      .uri("/api/books")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk()
      .expectBody()
      .jsonPath("$").isArray()
      .jsonPath("$.length()").isEqualTo(2);
  }
}
