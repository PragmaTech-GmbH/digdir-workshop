package pragmatech.digital.workshops.lab1.solutions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Solution2JUnitExtensionTest {

  // Option 4: Using a custom annotation with the extension
  @Target({ElementType.TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ExtendWith(ConfigurableSlowTestDetector.class)
  @interface SlowTestThreshold {
    long value() default 100;
  }

  // Extension implementation
  static class SlowTestDetector implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private final long thresholdMs;

    SlowTestDetector() {
      this(100); // Default threshold of 100ms
    }

    SlowTestDetector(long thresholdMs) {
      this.thresholdMs = thresholdMs;
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
      getStore(context).put("start", System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
      long start = getStore(context).remove("start", Long.class);
      long duration = System.currentTimeMillis() - start;

      if (duration > thresholdMs) {
        System.out.println("\n⚠️ SLOW TEST DETECTED ⚠️");
        System.out.println("Test: " + context.getDisplayName());
        System.out.println("Duration: " + duration + "ms");
        System.out.println("Threshold: " + thresholdMs + "ms\n");
      }
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
      return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
    }
  }

  // Extension that reads the threshold from the annotation
  static class ConfigurableSlowTestDetector implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    @Override
    public void beforeTestExecution(ExtensionContext context) {
      getStore(context).put("start", System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
      long start = getStore(context).remove("start", Long.class);
      long duration = System.currentTimeMillis() - start;

      long threshold = getThreshold(context);

      if (duration > threshold) {
        System.out.println("\n⚠️ SLOW TEST DETECTED ⚠️");
        System.out.println("Test: " + context.getDisplayName());
        System.out.println("Duration: " + duration + "ms");
        System.out.println("Threshold: " + threshold + "ms\n");
      }
    }

    private long getThreshold(ExtensionContext context) {
      // Try to get threshold from method annotation
      SlowTestThreshold annotation = context.getRequiredTestMethod().getAnnotation(SlowTestThreshold.class);

      // If not found, try class annotation
      if (annotation == null) {
        annotation = context.getRequiredTestClass().getAnnotation(SlowTestThreshold.class);
      }

      // Return the configured threshold or default to 100ms
      return annotation != null ? annotation.value() : 100;
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
      return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
    }
  }

  // Option 1: Class-level extension using @ExtendWith
  @Nested
  @ExtendWith(SlowTestDetector.class)
  @DisplayName("Tests with class-level extension")
  class ClassLevelExtensionTests {

    @Test
    @DisplayName("Fast test that should not trigger the slow test warning")
    void fastTest() {
      // This test should run quickly
      int result = 1 + 1;
      assert result == 2;
    }

    @Test
    @DisplayName("Slow test that should trigger the slow test warning")
    void slowTest() throws InterruptedException {
      // This test is artificially slowed down to exceed the threshold
      Thread.sleep(150);
      int result = 1 + 1;
      assertEquals(2, result);

    }
  }

  // Option 2: Method-level extension using @ExtendWith
  @Nested
  class MethodLevelExtensionTests {

    @Test
    @DisplayName("Fast test without extension")
    void fastTestWithoutExtension() {
      int result = 1 + 1;

      assertEquals(2, result);
    }

    @Test
    @ExtendWith(SlowTestDetector.class)
    @DisplayName("Slow test with method-level extension")
    void slowTestWithExtension() throws InterruptedException {
      Thread.sleep(150);
      int result = 1 + 1;

      assertEquals(2, result);
    }
  }

  // Option 3: Using @RegisterExtension for a field with custom threshold
  @Nested
  class RegisterExtensionTests {

    @RegisterExtension
    static SlowTestDetector customDetector = new SlowTestDetector(200);

    @Test
    @DisplayName("Test with default threshold (100ms)")
    void testWithDefaultThreshold() throws InterruptedException {
      Thread.sleep(150);
      int result = 1 + 1;
      assertEquals(2, result);
    }

    @Test
    @DisplayName("Test with custom threshold (200ms)")
    void testWithCustomThreshold() throws InterruptedException {
      Thread.sleep(150); // This shouldn't trigger a warning with 200ms threshold
      int result = 1 + 1;
      assertEquals(2, result);
    }
  }

  @Nested
  class AnnotationConfiguredTests {

    @Test
    @SlowTestThreshold
    @DisplayName("Test with default annotation threshold (100ms)")
    void testWithDefaultAnnotationThreshold() throws InterruptedException {
      Thread.sleep(150);
      int result = 1 + 1;

      assertEquals(2, result);
    }

    @Test
    @SlowTestThreshold(300)
    @DisplayName("Test with custom annotation threshold (300ms)")
    void testWithCustomAnnotationThreshold() throws InterruptedException {
      Thread.sleep(150); // This shouldn't trigger a warning with 300ms threshold
      int result = 1 + 1;

      assertEquals(2, result);
    }
  }
}
