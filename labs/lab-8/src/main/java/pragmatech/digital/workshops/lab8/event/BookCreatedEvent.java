package pragmatech.digital.workshops.lab8.event;

public record BookCreatedEvent(Long bookId, String isbn, String title) {}
