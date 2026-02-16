package pragmatech.digital.workshops.lab1.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
  public LocalDateTime getCurrentDateTime() {
    return LocalDateTime.now();
  }
}
