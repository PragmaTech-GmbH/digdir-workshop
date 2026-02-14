package pragmatech.digital.workshops.lab2.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import pragmatech.digital.workshops.lab2.dto.BookCreationRequest;
import pragmatech.digital.workshops.lab2.dto.BookUpdateRequest;
import pragmatech.digital.workshops.lab2.entity.Book;
import pragmatech.digital.workshops.lab2.exception.BookAlreadyExistsException;
import pragmatech.digital.workshops.lab2.repository.BookRepository;

@Service
public class BookService {

  private final BookRepository bookRepository;

  public BookService(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public Long createBook(BookCreationRequest request) {
    if (bookRepository.findByIsbn(request.isbn()).isPresent()) {
      throw new BookAlreadyExistsException(request.isbn());
    }

    Book book = new Book(
      request.isbn(),
      request.title(),
      request.author(),
      request.publishedDate()
    );

    Book savedBook = bookRepository.save(book);
    return savedBook.getId();
  }

  public List<Book> getAllBooks() {
    return bookRepository.findAll();
  }

  public Optional<Book> getBookById(Long id) {
    return bookRepository.findById(id);
  }

  public Optional<Book> getBookByIsbn(String isbn) {
    return bookRepository.findByIsbn(isbn);
  }

  public Optional<Book> updateBook(Long id, BookUpdateRequest request) {
    return bookRepository.findById(id)
      .map(book -> {
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setPublishedDate(request.publishedDate());
        book.setStatus(request.status());
        return bookRepository.save(book);
      });
  }

  public boolean deleteBook(Long id) {
    return bookRepository.findById(id)
      .map(book -> {
        bookRepository.delete(book);
        return true;
      })
      .orElse(false);
  }

  public boolean deleteBookByIsbn(String isbn) {
    return bookRepository.findByIsbn(isbn)
      .map(book -> {
        bookRepository.delete(book);
        return true;
      })
      .orElse(false);
  }
}
