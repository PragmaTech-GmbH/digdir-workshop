package pragmatech.digital.workshops.lab7.exercises;

import org.junit.jupiter.api.Test;

/**
 * Exercise 1: Configure and Observe JUnit 5 Parallel Test Execution
 *
 * <p>Tasks:
 * <ol>
 *   <li>Check the junit-platform.properties file in src/test/resources</li>
 *   <li>Run all tests and observe parallel execution in the log output</li>
 *   <li>Experiment with different parallelism strategies:
 *     <ul>
 *       <li>a) Classes concurrent, methods same_thread (default - current setting)</li>
 *       <li>b) Both classes and methods concurrent</li>
 *       <li>c) Disable parallel execution entirely</li>
 *     </ul>
 *   </li>
 *   <li>Compare build times for each configuration</li>
 *   <li>Note which tests break when running in parallel and why</li>
 * </ol>
 *
 * <p>Hints:
 * <ul>
 *   <li>The junit-platform.properties file controls parallel execution globally</li>
 *   <li>You can also use @Execution(ExecutionMode.CONCURRENT) on individual test classes</li>
 *   <li>Thread names in log output reveal which tests run on which threads</li>
 *   <li>Use 'mvn test' to run all tests and observe parallel behavior</li>
 * </ul>
 */
class Exercise1ParallelExecutionTest {

  @Test
  void shouldObserveParallelExecution() {
    // TODO: Run this test alongside other tests and observe thread names in logs
    // Print the current thread to see parallelism
    System.out.println("Exercise1 running on thread: " + Thread.currentThread().getName());
  }

  @Test
  void shouldCompareExecutionTimes() {
    // TODO:
    // 1. Run 'mvn test' with parallel.enabled = true and note total build time
    // 2. Run 'mvn test' with parallel.enabled = false and note total build time
    // 3. Compare the results
    System.out.println("Execution time comparison on thread: " + Thread.currentThread().getName());
  }

  @Test
  void shouldUnderstandForkCountVsParallelExecution() {
    // TODO:
    // 1. Check the maven-surefire-plugin config in pom.xml (forkCount=2)
    // 2. Understand: forkCount creates separate JVM processes
    // 3. Understand: JUnit 5 parallel execution uses threads within a single JVM
    // 4. These two mechanisms are complementary, not mutually exclusive
    System.out.println("Fork and parallel test on thread: " + Thread.currentThread().getName());
  }
}
