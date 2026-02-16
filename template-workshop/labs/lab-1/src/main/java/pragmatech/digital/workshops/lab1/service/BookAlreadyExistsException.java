package pragmatech.digital.workshops.lab1.service;

public class BookAlreadyExistsException extends RuntimeException {

  public BookAlreadyExistsException(String isbn) {
    super("Book with ISBN " + isbn + " already exists");
  }
}
