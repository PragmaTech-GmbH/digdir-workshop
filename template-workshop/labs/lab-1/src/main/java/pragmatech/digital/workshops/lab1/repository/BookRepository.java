package pragmatech.digital.workshops.lab1.repository;

import java.util.Optional;

import org.springframework.stereotype.Service;
import pragmatech.digital.workshops.lab1.domain.Book;

@Service
public class BookRepository {

  public Book save(Book book) {
    book.setId(42L);
    return book;
  }

  public Optional<Book> findByIsbn(String isbn) {
    return Optional.empty();
  }
}
