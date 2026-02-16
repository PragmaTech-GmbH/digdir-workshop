package pragmatech.digital.workshops.lab4.experiment;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import pragmatech.digital.workshops.lab4.client.OpenLibraryApiClient;
import pragmatech.digital.workshops.lab4.entity.Book;
import pragmatech.digital.workshops.lab4.entity.BookStatus;

/**
 * This test demonstrates JUnit 4 style testing.
 * <p>
 * Key characteristics of JUnit 4:
 * - Uses @RunWith for extension mechanism
 * - @Before and @After for setup/teardown
 * - @BeforeClass and @AfterClass for static setup/teardown
 * - @Rule for test behavior customization
 * - No nested tests
 * - @Ignore for skipping tests
 * - Uses assertTrue, assertEquals, etc. from Assert class
 * - No parameterized test support without additional extensions
 * - No parallel execution without additional extensions
 * - No assumptions API
 * - Exception testing through @Test(expected=...) or ExpectedException rule
 */
@RunWith(MockitoJUnitRunner.class)
public class JUnit4StyleTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  @Rule
  public Timeout globalTimeout = Timeout.seconds(1);
  private Book book;
  @Mock
  private OpenLibraryApiClient apiClient;

  @BeforeClass
  public static void setUpClass() {
    System.out.println("Set up once before all tests in the class");
  }

  @AfterClass
  public static void tearDownClass() {
    System.out.println("Tear down once after all tests in the class");
  }

  @Before
  public void setUp() {
    book = new Book();
    book.setIsbn("1234567890");
    book.setTitle("Test Book");
    book.setAuthor("Test Author");
    book.setPublishedDate(LocalDate.now());
    book.setStatus(BookStatus.AVAILABLE);
  }

  @Test
  public void testBookProperties() {
    // JUnit 4 style assertions
    Assert.assertEquals("Test Book", book.getTitle());
    Assert.assertEquals("Test Author", book.getAuthor());
    Assert.assertEquals(BookStatus.AVAILABLE, book.getStatus());
    Assert.assertTrue(book.isAvailable());
  }

  @Test
  public void testBookIsAvailable() {
    // Test that book is available by default
    Assert.assertTrue(book.isAvailable());

    // Change status
    book.setStatus(BookStatus.BORROWED);

    // Test that book is not available after status change
    Assert.assertFalse(book.isAvailable());
  }

  @Test(expected = NullPointerException.class)
  public void testExceptionUsingAnnotation() {
    // This will throw NPE
    String str = null;
    str.length();
  }

  @Test
  public void testExceptionUsingRule() {
    // Set up expected exception
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("ISBN cannot be null");

    // This should throw the expected exception
    throw new IllegalArgumentException("ISBN cannot be null");
  }

  @Test
  public void testWithTemporaryFolder() throws IOException {
    // Create temp file using JUnit 4 rule
    File tempFile = folder.newFile("testFile.txt");

    // Assert file was created
    Assert.assertTrue(tempFile.exists());
  }

  @Test(timeout = 100)
  public void testWithTimeout() throws InterruptedException {
    // This test will pass because it completes within 100ms
    Thread.sleep(50);
  }

  @Ignore("Demonstrates how to skip a test in JUnit 4")
  @Test
  public void testThatIsIgnored() {
    Assert.fail("This test should be ignored and not run");
  }

  @After
  public void tearDown() {
    book = null;
  }
}
