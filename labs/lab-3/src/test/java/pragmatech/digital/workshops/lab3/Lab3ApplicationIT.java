package pragmatech.digital.workshops.lab3;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
class Lab3ApplicationIT {

  @Test
  void contextLoads() {
  }
}
