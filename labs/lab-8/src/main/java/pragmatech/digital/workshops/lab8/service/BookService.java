package pragmatech.digital.workshops.lab8.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pragmatech.digital.workshops.lab8.client.OpenLibraryApiClient;
import pragmatech.digital.workshops.lab8.dto.BookCreationRequest;
import pragmatech.digital.workshops.lab8.dto.BookMetadataResponse;
import pragmatech.digital.workshops.lab8.dto.BookUpdateRequest;
import pragmatech.digital.workshops.lab8.entity.Book;
import pragmatech.digital.workshops.lab8.event.BookCreatedEvent;
import pragmatech.digital.workshops.lab8.exception.BookAlreadyExistsException;
import pragmatech.digital.workshops.lab8.repository.BookRepository;

@Service
public class BookService {

  private static final Logger logger = LoggerFactory.getLogger(BookService.class);

  private final BookRepository bookRepository;
  private final OpenLibraryApiClient openLibraryApiClient;
  private final ApplicationEventPublisher eventPublisher;

  public BookService(BookRepository bookRepository, OpenLibraryApiClient openLibraryApiClient,
      ApplicationEventPublisher eventPublisher) {
    this.bookRepository = bookRepository;
    this.openLibraryApiClient = openLibraryApiClient;
    this.eventPublisher = eventPublisher;
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

    BookMetadataResponse metadata = openLibraryApiClient.getBookByIsbn(request.isbn());

    book.setThumbnailUrl(metadata.getCoverUrl());

    Book savedBook = bookRepository.save(book);
    eventPublisher.publishEvent(new BookCreatedEvent(savedBook.getId(), savedBook.getIsbn(), savedBook.getTitle()));
    return savedBook.getId();
  }

  public List<Book> getAllBooks() {
    return bookRepository.findAll();
  }

  public Optional<Book> getBookById(Long id) {
    return bookRepository.findById(id);
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
}
