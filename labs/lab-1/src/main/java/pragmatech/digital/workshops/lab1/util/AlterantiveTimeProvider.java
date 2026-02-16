package pragmatech.digital.workshops.lab1.util;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

/**
 * Alternative implementation of TimeProvider that uses a Clock to provide time.
 * This allows for more flexible testing by injecting a fixed or mock clock.
 */
@Component
public class AlterantiveTimeProvider implements TimeProvider {

  private final Clock clock;

  public AlterantiveTimeProvider(Clock clock) {
    this.clock = clock;
  }

  @Override
  public LocalDate getCurrentDate() {
    return LocalDate.now(clock);
  }

  @Override
  public Instant getCurrentInstant() {
    return Instant.now(clock);
  }
}
