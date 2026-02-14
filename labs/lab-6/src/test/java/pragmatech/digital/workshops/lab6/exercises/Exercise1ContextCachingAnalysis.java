package pragmatech.digital.workshops.lab6.exercises;

import org.junit.jupiter.api.Test;

/**
 * Exercise 1: Analyze Spring Test Context Caching
 *
 * In this exercise, you'll investigate how test configuration affects context caching.
 *
 * Tasks:
 * 1. Run all five ContextCacheKiller*IT tests in the experiment package
 * 2. Count the number of application contexts created (look for "Initializing Spring" log messages)
 * 3. For each test, identify what configuration difference causes a separate context
 * 4. Document your findings in the comments below:
 *    - Which tests share a context? Why?
 *    - Which tests create new contexts? What caused it?
 * 5. Propose changes to reduce the number of contexts to just ONE
 *
 * Hint: Run the tests with:
 *   ./mvnw test -pl labs/lab-6 -Dtest="ContextCacheKiller*IT"
 * or run them from your IDE and check the console output for "Initializing Spring" lines.
 */
class Exercise1ContextCachingAnalysis {

  @Test
  void analyzeContextCaching() {
    // TODO: Run the ContextCacheKiller*IT tests and document findings here
    //
    // Number of contexts created: ___
    //
    // Context sharing analysis:
    // - ContextCacheKillerOneIT: ___ (reason: ___)
    // - ContextCacheKillerTwoIT: ___ (reason: ___)
    // - ContextCacheKillerThreeIT: ___ (reason: ___)
    // - ContextCacheKillerFourIT: ___ (reason: ___)
    // - ContextCacheKillerFiveIT: ___ (reason: ___)
    //
    // Proposed fix to reduce to ONE context:
    // ___
  }
}
