package pragmatech.digital.workshops.lab3;

import org.springframework.boot.SpringApplication;

class LocalLab3Application {

  public static void main(String[] args) {
    SpringApplication
      .from(Lab3Application::main)
      .with(LocalDevTestcontainerConfig.class)
      .run(args);
  }
}
