package pragmatech.digital.workshops.lab1.experiment;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

class JSONAssertDemoTest {

  @Test
  void shouldAssertJsonEquality() throws Exception {
    // Given
    String actual = """
      { "isbn": "1234", "title": "JSON Testing", "author": "Test Author"}""";

    String expected = """
      { "isbn": "1234", "title": "JSON Testing"}""";

    // Strict mode would fail as expected is missing the author field
    // Using lenient mode (false) allows additional fields in actual
    JSONAssert.assertEquals(expected, actual, false);

    // Strict comparison would fail
    // JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);

    // We can also explicitly specify the mode
    JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
  }
}
