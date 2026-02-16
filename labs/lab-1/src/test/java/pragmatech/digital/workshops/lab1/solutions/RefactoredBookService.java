package pragmatech.digital.workshops.lab1.solutions;

import java.time.LocalDate;
import java.util.Optional;

import pragmatech.digital.workshops.lab1.domain.Book;
import pragmatech.digital.workshops.lab1.repository.BookRepository;
import pragmatech.digital.workshops.lab1.service.BookAlreadyExistsException;
import pragmatech.digital.workshops.lab1.util.TimeProvider;

import static java.time.DayOfWeek.SUNDAY;

public class RefactoredBookService {

  private final BookRepository bookRepository;
  private final TimeProvider timeProvider;

  public RefactoredBookService(BookRepository bookRepository, TimeProvider timeProvider) {
    this.bookRepository = bookRepository;
    this.timeProvider = timeProvider;
  }

  public Long registerBook(String isbn, String title, String author) {

    Optional<Book> existingBook = bookRepository.findByIsbn(isbn);

    if (existingBook.isPresent()) {
      throw new BookAlreadyExistsException(isbn);
    }

    LocalDate today = timeProvider.getCurrentDate();

    if (today.getDayOfWeek() == SUNDAY) {
      throw new IllegalArgumentException("Books cannot be registered on Sunday");
    }

    Book book = new Book(isbn, title, author, LocalDate.of(2000, 1, 1), timeProvider.getCurrentInstant());

    Book savedBook = bookRepository.save(book);

    return savedBook.getId();
  }
}
