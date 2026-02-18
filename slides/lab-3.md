---
marp: true
theme: pragmatech
---

![bg](./assets/digdir-cover.jpg)

---


<!-- _class: title -->
![bg h:500 left:33%](assets/generated/demystify.png)

# Testing Spring Boot Applications Demystified

## First Workshop Day

_Digdir Workshop 02.03.2026_

Philip Riecks - [PragmaTech GmbH](https://pragmatech.digital/) - [@rieckpil](https://x.com/rieckpil)

---

## Discuss Exercises from Lab 2

---


![bg left:33%](assets/generated/lab-3.jpg)

# Lab 3

## Sliced Testing Continued

### Verifying the Persistence Layer with Testcontainers

---

![bg right:25%](assets/evolve2.jpg)


## Enrich the Application

- **PostgreSQL** added as infrastructure dependency (`docker-compose.yml`)
- **Flyway** for versioned schema migrations (`src/main/resources/db/migration/`)
- **JPA entity** mapped to the `books` table
- **Spring Data JPA repository** with basic CRUD and a custom query

---


## Introducing: @DataJpaTest

```java
@DataJpaTest
class BookRepositoryTest {
  
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private BookRepository bookRepository;
}
```

- Tests JPA repositories
- Auto-configures in-memory database
- Provides `TestEntityManager`
- Verify JPA entity mapping, creation and native queries

---

## What to Verify?

- Spring Data query methods → already abstracted, **no need to test**
- Complex entity graphs and Hibernate associations
- Mapping of entities to database columns
- Native (database-specific) queries
- Potential N+1 issues
- Transaction behaviour & connection pool usage in isolation

---

## Spring Data Abstraction

- Spring Data already tests its own CRUD and derived query methods
- No need to write tests for `findById`, `save`, `findAll`, etc.
- Focus your test effort on **your own business logic**:
  - Custom `@Query` methods
  - Projections and DTOs
  - Complex filtering and sorting logic
- Trust the framework; test what you own

---

## Complex Entity Graphs

- Hibernate associations require careful verification:
  - `@OneToMany`, `@ManyToMany`, `@ManyToOne`
  - Cascade types (`CascadeType.PERSIST`, `CascadeType.REMOVE`)
  - Orphan removal (`orphanRemoval = true`)
- Bidirectional mappings must be consistent on both sides
- Use `TestEntityManager` to flush and clear the persistence context:

```java
entityManager.persistAndFlush(book);
entityManager.clear(); // detach to force reload from DB
```

---

## Entity-to-Column Mapping

- Verify that JPA annotations match the actual DDL:
  - **Column names**: `@Column(name = "...")`
  - **Types**: correct Java ↔ SQL type mapping
  - **Nullability**: `@Column(nullable = false)` enforced by the DB
  - **Unique constraints**: `@Column(unique = true)` or `@Table(uniqueConstraints = ...)`
  - **Enum strategies**: `@Enumerated(EnumType.STRING)` vs `ORDINAL`
- H2 compatibility modes can hide real mapping problems — use a real DB

---

## Native Queries

- Native SQL bypasses JPQL and is **database-engine specific**
- Must be tested against a real database engine (not H2):
  - PostgreSQL full-text search (`to_tsvector`, `plainto_tsquery`)
  - Window functions (`ROW_NUMBER()`, `RANK()`)
  - JSON operators, array types, and other vendor extensions
- Testcontainers ensures your native queries run against the same engine as production

---

## N+1 Issues

- Lazy loading can trigger unexpected extra `SELECT` statements
- A single call that returns N entities may fire N additional queries
- Strategies to detect N+1 in tests:
  - Enable Hibernate statistics and assert query counts
  - Use `@EnabledIf` to conditionally run query-count assertions
  - Log SQL with `spring.jpa.show-sql=true` and review output
- Fix with `JOIN FETCH`, `@EntityGraph`, or eager loading where appropriate

---

## Transaction & Connection Pooling

- Verify `@Transactional` propagation and rollback behaviour in isolation:
  - Does a failing operation roll back the entire transaction?
  - Is `REQUIRES_NEW` creating a separate transaction as expected?
- Test HikariCP pool exhaustion scenarios without the full application context
- `@DataJpaTest` wraps each test in a transaction and rolls it back by default — be aware this hides commit-time behaviour
- Use `@Commit` or `@Rollback(false)` when you need to verify post-commit state

---

## Useful Log Levels for Persistence Tests

```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG                  # all SQL statements
    org.hibernate.orm.jdbc.bind: TRACE        # bind parameter values
    org.springframework.transaction: DEBUG    # begin / commit / rollback
    com.zaxxer.hikari: DEBUG                  # connection pool activity
    org.flywaydb: DEBUG                       # migration execution details
    org.springframework.orm.jpa: DEBUG        # EntityManager lifecycle
```

- `hibernate.SQL` + `jdbc.bind` together reveal the full executed query
- `transaction` debug exposes unexpected rollbacks or missing propagation
- `hikari` debug catches pool exhaustion and connection leak warnings

---

## In-Memory vs. Real Database

- By default, Spring Boot tries to autoconfigure an in-memory relational database (H2 or Derby)
- In-memory database pros:
  - Easy to use & fast
  - Less overhead
- In-memory database cons:
  - Mismatch with the infrastructure setup in production
  - Despite having compatibility modes, we can't fully test proprietary database features

---

## Testcontainers

- Java library that manages **real Docker containers** from inside JUnit tests
- Container lifecycle is tied to the test: starts before, stops after
- `static` containers are shared across all tests in the class (faster)
- Modules for PostgreSQL, MySQL, Redis, Kafka, LocalStack, and more
- Eliminates the "works on my machine" database problem

---

## Testcontainers & Spring Boot Integration

```java
@DataJpaTest
@Testcontainers
class BookRepositoryTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine");

}
```

- `@Testcontainers` hooks the container into the JUnit 5 extension lifecycle
- `@ServiceConnection` reads host/port from the running container and **auto-configures** Spring's datasource — no manual URL wiring needed

---

<!--

Notes:

- who is not using Testcontainers
- explain basics

-->

## Solution: Docker & Testcontainers

![bg right:33%](assets/generated/containers.jpg)

---

## Using a Real Database

```java
@Container
@ServiceConnection
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
  .withDatabaseName("testdb")
  .withUsername("test")
  .withPassword("test")
  .withInitScript("init-postgres.sql"); // Initialize PostgreSQL with required extensions
```

---

![](assets/hibernate-persistence-context.svg)

---

## Test Data Management

- Each test should start with a known state
- Tests should not interfere with each other
- Options:
  - Truncate tables between tests
  - Transaction rollback (`@Transactional`)
  - Separate schemas per test
  - Database resets

---

## Preparing Test Data

**`@Sql` — declarative SQL scripts**

```java
@Test
@Sql("/data/sample-books.sql")
void shouldReturnAllAvailableBooks() { ... }
```

**Repository / `TestEntityManager` — programmatic**

```java
@BeforeEach
void setUp() {
  bookRepository.save(new Book("978-0-13-235088-4", "Clean Code", "Robert C. Martin", LocalDate.of(2008, 8, 1)));
}
```

- `@Sql` is ideal for fixed seed data and complex multi-table setups
- Programmatic setup gives full type safety and IDE support

---

## Cleaning Up Test Data

**`@Transactional` on the test — rollback after each test, no commit**

```java
@DataJpaTest          // already wraps each test in a transaction
@Transactional        // add explicitly on @SpringBootTest slices
class BookRepositoryTest { ... }
```

- The transaction is rolled back after each test → database stays clean
- Code under test that opens `REQUIRES_NEW` **commits independently** — rollback won't undo it

**When committed state must be verified:**

```java
@Test
@Commit               // or @Rollback(false)
void shouldPersistAuditTimestampAfterCommit() { ... }
```

---

## Testing Native Queries

```java
/**
 * PostgreSQL-specific: Full text search on book titles with ranking.
 * Uses PostgreSQL's to_tsvector and to_tsquery for sophisticated text searching
 * with ranking based on relevance.
 *
 * @param searchTerms the search terms (e.g. "adventure dragons fantasy")
 * @return list of books matching the search terms, ordered by relevance
 */
@Query(value = """
  SELECT * FROM books
  WHERE to_tsvector('english', title) @@ plainto_tsquery('english', :searchTerms)
  ORDER BY ts_rank(to_tsvector('english', title), plainto_tsquery('english', :searchTerms)) DESC
  """,
  nativeQuery = true)
List<Book> searchBooksByTitleWithRanking(@Param("searchTerms") String searchTerms);
```

---

# Time For Some Exercises
## Lab 3

- Work with the same repository as in lab 1/lab 2
- Navigate to the `labs/lab-3` folder in the repository and complete the tasks as described in the `README` file of that folder
- Time boxed until the end of the coffee break (15:50 AM)
