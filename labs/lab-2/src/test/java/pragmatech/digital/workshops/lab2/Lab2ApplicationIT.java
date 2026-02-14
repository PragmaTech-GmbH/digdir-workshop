package pragmatech.digital.workshops.lab2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(LocalDevTestcontainerConfig.class)
class Lab2ApplicationIT {

  @Test
  void contextLoads() {
  }
}
