package pragmatech.digital.workshops.lab3.experiment;

import org.springframework.data.jpa.repository.JpaRepository;

interface AuthorRepository extends JpaRepository<Author, Long> {
//  @Override
//  @EntityGraph(attributePaths = "books")
//  List<Author> findAll();
}
