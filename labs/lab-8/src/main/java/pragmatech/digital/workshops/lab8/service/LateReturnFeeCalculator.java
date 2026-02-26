package pragmatech.digital.workshops.lab8.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import pragmatech.digital.workshops.lab8.entity.Book;
import pragmatech.digital.workshops.lab8.entity.BookStatus;

@Service
public class LateReturnFeeCalculator {

  private final Clock clock;

  public LateReturnFeeCalculator(Clock clock) {
    this.clock = clock;
  }

  public double calculateFee(Book book, LocalDate borrowedDate) {
    if (book.getStatus() != BookStatus.BORROWED) {
      return 0.0;
    }

    LocalDate today = LocalDate.now(clock);
    long daysOverdue = ChronoUnit.DAYS.between(borrowedDate, today);

    if (daysOverdue <= 0) {
      return 0.0;
    } else if (daysOverdue <= 7) {
      return daysOverdue * 1.0;
    } else if (daysOverdue <= 14) {
      return daysOverdue * 1.5;
    } else {
      return daysOverdue * 2.0;
    }
  }
}
