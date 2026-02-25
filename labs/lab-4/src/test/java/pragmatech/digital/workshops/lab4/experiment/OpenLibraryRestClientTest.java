package pragmatech.digital.workshops.lab4.experiment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import pragmatech.digital.workshops.lab4.client.OpenLibraryRestClient;
import pragmatech.digital.workshops.lab4.dto.BookMetadataResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(OpenLibraryRestClient.class)
class OpenLibraryRestClientTest {

  @Autowired
  private OpenLibraryRestClient cut;

  @Autowired
  private MockRestServiceServer server;

  @Test
  void shouldReturnBookMetadataWhenApiReturnsValidResponse() {
    String isbn = "9780132350884";

    server.expect(requestTo(containsString("/isbn/" + isbn)))
      .andRespond(withSuccess("""
        {
          "title": "Clean Code",
          "isbn_13": ["9780132350884"],
          "publishers": ["Prentice Hall"],
          "number_of_pages": 431
        }
        """, MediaType.APPLICATION_JSON));

    BookMetadataResponse result = cut.getBookByIsbn(isbn);

    assertThat(result).isNotNull();
    assertThat(result.title()).isEqualTo("Clean Code");
    assertThat(result.getMainIsbn()).isEqualTo("9780132350884");
    assertThat(result.getPublisher()).isEqualTo("Prentice Hall");
    assertThat(result.numberOfPages()).isEqualTo(431);
  }

  @Test
  void shouldThrowExceptionWhenBookIsNotFound() {
    String isbn = "0000000000000";

    server.expect(requestTo(containsString("/isbn/" + isbn)))
      .andRespond(withStatus(HttpStatus.NOT_FOUND));

    assertThatThrownBy(() -> cut.getBookByIsbn(isbn))
      .isInstanceOf(HttpClientErrorException.NotFound.class);
  }

  @Test
  void shouldThrowExceptionWhenServerReturnsInternalError() {
    String isbn = "9780132350884";

    server.expect(requestTo(containsString("/isbn/" + isbn)))
      .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

    assertThatThrownBy(() -> cut.getBookByIsbn(isbn))
      .isInstanceOf(HttpServerErrorException.class);
  }
}
