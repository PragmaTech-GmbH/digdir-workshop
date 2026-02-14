package pragmatech.digital.workshops.lab7.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import pragmatech.digital.workshops.lab7.entity.Book;
import pragmatech.digital.workshops.lab7.entity.BookStatus;

@Service
public class DiscountService {

  public int calculateDiscount(Book book) {
    if (book.getStatus() != BookStatus.AVAILABLE) {
      return 0;
    }

    LocalDate publishedDate = book.getPublishedDate();
    LocalDate now = LocalDate.now();

    if (publishedDate.isAfter(now.minusMonths(6))) {
      return 0;
    }

    if (publishedDate.isAfter(now.minusYears(2))) {
      return 10;
    }

    if (publishedDate.isAfter(now.minusYears(5))) {
      return 25;
    }

    return 50;
  }
}
