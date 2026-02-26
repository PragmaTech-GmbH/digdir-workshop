package pragmatech.digital.workshops.lab4.client;

import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pragmatech.digital.workshops.lab4.dto.BookMetadataResponse;

@Component
public class OpenLibraryApiClient {

  private final WebClient webClient;

  public OpenLibraryApiClient(WebClient openLibraryWebClient) {
    this.webClient = openLibraryWebClient;
  }

  public BookMetadataResponse getBookByIsbn(String isbn) {
    String bibKey = "ISBN:" + isbn;

    Map<String, BookMetadataResponse> result = webClient.get()
      .uri(uriBuilder -> uriBuilder
        .path("/api/books")
        .queryParam("bibkeys", bibKey)
        .queryParam("format", "json")
        .queryParam("jscmd", "data")
        .build())
      .retrieve()
      .bodyToMono(new ParameterizedTypeReference<Map<String, BookMetadataResponse>>() {})
      .block();

    return result != null ? result.get(bibKey) : null;
  }
}
