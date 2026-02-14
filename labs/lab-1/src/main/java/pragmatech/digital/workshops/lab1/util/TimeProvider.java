package pragmatech.digital.workshops.lab1.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Interface for providing time-related functionality.
 * This abstraction makes it easier to test time-dependent code.
 */
public interface TimeProvider {
  /**
   * Returns the current date.
   *
   * @return the current date
   */
  LocalDate getCurrentDate();

  /**
   * Returns the current date and time.
   *
   * @return the current date and time
   */
  LocalDateTime getCurrentDateTime();
}
