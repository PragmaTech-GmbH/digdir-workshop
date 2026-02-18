package pragmatech.digital.workshops.lab2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class Lab2ApplicationIT {

  @Test
  void contextLoads(@Autowired Environment environment) {
    assertThat(environment.getProperty("spring.application.name")).isEqualTo("test-lab-2");
  }
}
