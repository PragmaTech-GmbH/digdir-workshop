package pragmatech.digital.workshops.lab1.experiment;

import org.junit.jupiter.api.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import static org.junit.jupiter.api.Assertions.assertFalse;

class XMLUnitDemoTest {

  @Test
  void shouldCompareXmlDocuments() {
    // Given
    String control = "<book><isbn>1234</isbn><title>XML Testing</title></book>";
    String test = "<book><isbn>1234</isbn><title>XML Testing</title></book>";

    // When
    Diff diff = DiffBuilder.compare(Input.fromString(control))
      .withTest(Input.fromString(test))
      .build();

    // Then
    assertFalse(diff.hasDifferences(), diff.toString());

    // Test with ignoring elements (useful for comparing XML where some values may differ)
    Diff lenientDiff = DiffBuilder.compare(Input.fromString(control))
      .withTest(Input.fromString("<book><isbn>1234</isbn><title>Changed</title></book>"))
      .withNodeFilter(node -> !node.getNodeName().equals("title"))
      .build();

    assertFalse(lenientDiff.hasDifferences());
  }
}
