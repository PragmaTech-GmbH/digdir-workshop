package pragmatech.digital.workshops.lab4;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import pragmatech.digital.workshops.lab4.config.WireMockContextInitializer;

@SpringBootTest
@ContextConfiguration(initializers = WireMockContextInitializer.class)
@Import(LocalDevTestcontainerConfig.class)
class Lab4ApplicationIT {

  @Test
  void contextLoads() {
  }
}
