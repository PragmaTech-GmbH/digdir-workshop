package pragmatech.digital.workshops.lab3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// @EnableJpaAuditing -> keep this class clean and outsource configuration to a separate class
@SpringBootApplication
public class Lab3Application {

  public static void main(String[] args) {
    SpringApplication.run(Lab3Application.class, args);
  }
}
