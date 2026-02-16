package pragmatech.digital.workshops.lab1.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;
import pragmatech.digital.workshops.lab1.domain.Book;
import pragmatech.digital.workshops.lab1.repository.BookRepository;

@Service
public class BookService {

  private final BookRepository bookRepository;

  public BookService(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public Long create(String isbn, String title, String author) {

    Optional<Book> existingBook = bookRepository.findByIsbn(isbn);

    if (existingBook.isPresent()) {
      throw new BookAlreadyExistsException(isbn);
    }

    Book book = new Book(isbn, title, author, LocalDate.now());

    Book savedBook = bookRepository.save(book);

    return savedBook.getId();
  }
}
