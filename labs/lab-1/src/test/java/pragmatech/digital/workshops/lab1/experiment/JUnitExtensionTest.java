package pragmatech.digital.workshops.lab1.experiment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TimingExtension.class)
class JUnitExtensionTest {

  @Test
  void test() {
    System.out.println("Hello World!");
  }
}
