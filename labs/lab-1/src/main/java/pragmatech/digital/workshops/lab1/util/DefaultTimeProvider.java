package pragmatech.digital.workshops.lab1.util;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

/**
 * Default implementation of TimeProvider that returns the current system time.
 */
@Component
public class DefaultTimeProvider implements TimeProvider {

  @Override
  public LocalDate getCurrentDate() {
    return LocalDate.now();
  }

  @Override
  public Instant getCurrentInstant() {
    return Instant.now();
  }

}
