package pragmatech.digital.workshops.lab3.experiment;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "experiment_book_entries")
class BookEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String title;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id")
  private Author author;

  protected BookEntry() {}

  BookEntry(String title) {
    this.title = title;
  }

  void setAuthor(Author author) {
    this.author = author;
  }

  Long getId() {
    return id;
  }

  String getTitle() {
    return title;
  }

  Author getAuthor() {
    return author;
  }
}
