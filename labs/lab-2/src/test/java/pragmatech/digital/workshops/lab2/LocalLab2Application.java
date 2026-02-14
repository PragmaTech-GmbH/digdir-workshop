package pragmatech.digital.workshops.lab2;

import org.springframework.boot.SpringApplication;

class LocalLab2Application {

  public static void main(String[] args) {
    SpringApplication
      .from(Lab2Application::main)
      .with(LocalDevTestcontainerConfig.class)
      .run(args);
  }
}
