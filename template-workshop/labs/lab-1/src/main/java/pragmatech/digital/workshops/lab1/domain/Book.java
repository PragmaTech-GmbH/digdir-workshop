package pragmatech.digital.workshops.lab1.domain;

import java.time.LocalDate;

/**
 * Represents a book in the library system.
 */
public class Book {

  private Long id;
  private String isbn;
  private String title;
  private String author;
  private LocalDate publishedDate;
  private BookStatus status;

  public Book(Long id) {
    this.id = id;
  }

  public Book(String isbn, String title, String author, LocalDate publishedDate) {
    if (isbn == null || isbn.isBlank()) {
      throw new IllegalArgumentException("ISBN cannot be null or blank");
    }
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("Title cannot be null or blank");
    }
    if (author == null || author.isBlank()) {
      throw new IllegalArgumentException("Author cannot be null or blank");
    }
    if (publishedDate == null) {
      throw new IllegalArgumentException("Published date cannot be null");
    }

    this.isbn = isbn;
    this.title = title;
    this.author = author;
    this.publishedDate = publishedDate;
    this.status = BookStatus.AVAILABLE;
  }

  public String getIsbn() {
    return isbn;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("Title cannot be null or blank");
    }
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    if (author == null || author.isBlank()) {
      throw new IllegalArgumentException("Author cannot be null or blank");
    }
    this.author = author;
  }

  public LocalDate getPublishedDate() {
    return publishedDate;
  }

  public void setPublishedDate(LocalDate publishedDate) {
    if (publishedDate == null) {
      throw new IllegalArgumentException("Published date cannot be null");
    }
    this.publishedDate = publishedDate;
  }

  public BookStatus getStatus() {
    return status;
  }

  public void setStatus(BookStatus status) {
    if (status == null) {
      throw new IllegalArgumentException("Status cannot be null");
    }
    this.status = status;
  }

  public boolean isAvailable() {
    return status == BookStatus.AVAILABLE;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Book book = (Book) o;
    return isbn.equals(book.isbn);
  }

  @Override
  public int hashCode() {
    return isbn.hashCode();
  }

  @Override
  public String toString() {
    return "Book{" +
      "isbn='" + isbn + '\'' +
      ", title='" + title + '\'' +
      ", author='" + author + '\'' +
      ", publishedDate=" + publishedDate +
      ", status=" + status +
      '}';
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
