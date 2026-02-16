package pragmatech.digital.workshops.lab1.experiment;

import java.util.Map;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonPathDemoTest {

  @Test
  void shouldParseAndEvaluateJson() {

    String json = """
      { "book": {"isbn": "1234", "title": "JSON Testing", "author": "Test Author"}}""";

    DocumentContext context = JsonPath.parse(json);

    assertThat(context.read("$.book.isbn", String.class)).isEqualTo("1234");
    assertThat(context.read("$.book.title", String.class)).isEqualTo("JSON Testing");

    // Assert on nested structure
    Map<String, Object> book = context.read("$.book");
    assertThat(book)
      .containsEntry("isbn", "1234")
      .containsEntry("title", "JSON Testing");
  }
}
