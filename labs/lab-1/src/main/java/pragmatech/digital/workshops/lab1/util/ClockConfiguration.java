package pragmatech.digital.workshops.lab1.util;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ClockConfiguration {

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
