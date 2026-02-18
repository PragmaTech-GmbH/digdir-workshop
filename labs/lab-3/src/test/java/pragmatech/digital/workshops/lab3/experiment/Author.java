package pragmatech.digital.workshops.lab3.experiment;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "experiment_authors")
class Author {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  // @BatchSize(size=25)
  @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<BookEntry> books = new ArrayList<>();

  protected Author() {}

  Author(String name) {
    this.name = name;
  }

  void addBook(BookEntry book) {
    books.add(book);
    book.setAuthor(this);
  }

  Long getId() {
    return id;
  }

  String getName() {
    return name;
  }

  List<BookEntry> getBooks() {
    return books;
  }
}
