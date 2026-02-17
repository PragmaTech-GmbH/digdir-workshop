package pragmatech.digital.workshops.lab2.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;
import pragmatech.digital.workshops.lab2.entity.Book;

@Repository
public class BookRepository {

  private final List<Book> books = new ArrayList<>();
  private final AtomicLong idCounter = new AtomicLong(1);

  public Book save(Book book) {
    if (book.getId() == null) {
      book.setId(idCounter.getAndIncrement());
      books.add(book);
    }
    return book;
  }

  public List<Book> findAll() {
    return List.copyOf(books);
  }

  public Optional<Book> findById(Long id) {
    return books.stream()
      .filter(book -> book.getId().equals(id))
      .findFirst();
  }

  public Optional<Book> findByIsbn(String isbn) {
    return books.stream()
      .filter(book -> book.getIsbn().equals(isbn))
      .findFirst();
  }

  public void delete(Book book) {
    books.remove(book);
  }
}
