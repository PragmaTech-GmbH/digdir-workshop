package pragmatech.digital.workshops.lab1.experiment;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;

class HamcrestDemoTest {

  @Test
  void shouldMatchWithHamcrest() {
    TestBook testBook = new TestBook("1234", "Spring Boot Testing", "Test Author");

    assertThat(testBook.isbn(), is("1234"));
    assertThat(testBook.title(), allOf(
      startsWith("Spring"),
      containsString("Testing"),
      not(emptyString())
    ));

    List<TestBook> testBooks = List.of(testBook, new TestBook("5678", "Title 2", "Author 2"));
    assertThat(testBooks, hasSize(2));
  }
}
