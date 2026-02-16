package pragmatech.digital.workshops.lab1.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;
import pragmatech.digital.workshops.lab1.domain.Book;
import pragmatech.digital.workshops.lab1.repository.BookRepository;

import static java.time.DayOfWeek.*;

@Service
public class BookService {

  private final BookRepository bookRepository;

  public BookService(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public Long registerBook(String isbn, String title, String author) {

    Optional<Book> existingBook = bookRepository.findByIsbn(isbn);

    if (existingBook.isPresent()) {
      throw new BookAlreadyExistsException(isbn);
    }

    LocalDate today = LocalDate.now();

    if (today.getDayOfWeek() == SUNDAY) {
      throw new IllegalArgumentException("Books cannot be registered on Sunday");
    }

    Book book = new Book(isbn, title, author, LocalDate.of(2000, 1, 1), Instant.now());

    Book savedBook = bookRepository.save(book);

    return savedBook.getId();
  }
}
