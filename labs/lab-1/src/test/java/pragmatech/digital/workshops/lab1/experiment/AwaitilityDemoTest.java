package pragmatech.digital.workshops.lab1.experiment;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.awaitility.Awaitility.await;

class AwaitilityDemoTest {

  @Test
  void shouldEventuallyCompleteAsyncOperation() {
    CompletableFuture<TestBook> futureBook = CompletableFuture.supplyAsync(() -> {
      try {
        Thread.sleep(300); // Simulate async operation
        return new TestBook("1234", "Async Testing", "Author");
      }
      catch (InterruptedException e) {
        return null;
      }
    });

    await().atMost(1, TimeUnit.SECONDS)
      .until(futureBook::isDone);
  }
}
