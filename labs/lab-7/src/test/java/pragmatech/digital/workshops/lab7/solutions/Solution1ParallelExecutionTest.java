package pragmatech.digital.workshops.lab7.solutions;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Solution for Exercise 1: Configure and Observe JUnit 5 Parallel Test Execution
 *
 * <p>This test class demonstrates how to observe and verify parallel test execution
 * in JUnit 5. The key configuration lives in two places:
 *
 * <p><strong>1. junit-platform.properties</strong> (src/test/resources):
 * <pre>
 * junit.jupiter.execution.parallel.enabled = true
 * junit.jupiter.execution.parallel.mode.default = same_thread
 * junit.jupiter.execution.parallel.mode.classes.default = concurrent
 * </pre>
 *
 * <p>This means:
 * <ul>
 *   <li>Test classes run concurrently (on different threads)</li>
 *   <li>Test methods within a single class run on the same thread (sequentially)</li>
 *   <li>This is the safest default: class-level parallelism without method-level races</li>
 * </ul>
 *
 * <p><strong>2. maven-surefire-plugin in pom.xml</strong>:
 * <pre>
 * &lt;forkCount&gt;2&lt;/forkCount&gt;
 * </pre>
 *
 * <p>This creates 2 separate JVM processes. Combined with JUnit 5 parallel execution,
 * you get multi-process AND multi-threaded test execution.
 *
 * <p><strong>Execution strategies comparison:</strong>
 * <ul>
 *   <li><code>mode.default=same_thread, mode.classes.default=concurrent</code>:
 *       Classes in parallel, methods sequential (safest)</li>
 *   <li><code>mode.default=concurrent, mode.classes.default=concurrent</code>:
 *       Everything in parallel (fastest but requires careful isolation)</li>
 *   <li><code>mode.default=same_thread, mode.classes.default=same_thread</code>:
 *       Fully sequential (slowest but simplest)</li>
 * </ul>
 */
class Solution1ParallelExecutionTest {

  @Test
  void shouldObserveParallelExecution() {
    String threadName = Thread.currentThread().getName();
    System.out.println("[Solution1] Test 1 running on thread: " + threadName);

    // When running with parallel classes, this test class will execute
    // on a different thread than other test classes
    assertThat(threadName).isNotBlank();
  }

  @Test
  void shouldShowSequentialMethodExecution() {
    String threadName = Thread.currentThread().getName();
    System.out.println("[Solution1] Test 2 running on thread: " + threadName);

    // With mode.default=same_thread, methods within this class
    // run on the same thread sequentially
    assertThat(threadName).isNotBlank();
  }

  @Test
  void shouldDemonstrateThreadNaming() {
    String threadName = Thread.currentThread().getName();
    System.out.println("[Solution1] Test 3 running on thread: " + threadName);

    // JUnit 5 parallel execution uses ForkJoinPool threads
    // Thread names will look like: ForkJoinPool-1-worker-1
    // If parallel is disabled, thread name will be: main
    assertThat(threadName).isNotBlank();
  }

  /**
   * This repeated test helps visualize that all repetitions of methods
   * within the same class run on the same thread when mode.default=same_thread.
   */
  @RepeatedTest(3)
  void shouldShowRepeatedTestThreadBehavior() {
    String threadName = Thread.currentThread().getName();
    System.out.println("[Solution1] Repeated test on thread: " + threadName);
    assertThat(threadName).isNotBlank();
  }

  /**
   * Demonstrates that you can override the global configuration at class
   * or method level using the @Execution annotation.
   *
   * <p>Even though the global setting is same_thread for methods,
   * you could annotate a class with @Execution(ExecutionMode.CONCURRENT)
   * to make its methods run in parallel:
   *
   * <pre>
   * {@literal @}Execution(ExecutionMode.CONCURRENT)
   * class MyConcurrentTest { ... }
   * </pre>
   */
  @Test
  @Execution(ExecutionMode.SAME_THREAD)
  void shouldRespectPerMethodExecutionAnnotation() {
    String threadName = Thread.currentThread().getName();
    System.out.println("[Solution1] Annotated method on thread: " + threadName);
    assertThat(threadName).isNotBlank();
  }
}
