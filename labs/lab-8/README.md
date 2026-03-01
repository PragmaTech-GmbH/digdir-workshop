# Lab 8: General FAQ and Customer Specific Issues

## AI

```
For my BookController, please implement a new endpoint to export all avialable books as CSV. Include all available fields of the Book and sort it by default by the creation date.

Make sure this can only be done by authenticated users with the "ADMIN" role.
```

This lab covers practical topics that frequently come up in real-world Spring Boot testing projects: GitHub Actions pipeline best practices, Maven tips for faster builds, and test organization using JUnit 5 `@Tag` with Maven profiles.

## Learning Objectives

- Configure GitHub Actions workflows with caching, timeouts, and artifact uploads
- Organize tests using `@Tag` annotations and Maven profiles
- Understand parallel test execution with JUnit 5
- Learn Maven tips for faster local and CI builds

## Prerequisites

- Completed Labs 1-7 (or equivalent Spring Boot testing knowledge)
- Basic familiarity with YAML and CI/CD concepts

## Lab Structure

```
lab-8/
  .github/workflows/
    build.yml              # Basic GHA workflow (enhance in Exercise 1)
    nightly.yml            # Nightly build workflow example
  src/test/java/.../
    exercises/
      Exercise1GithubActionsWorkflow.java
      Exercise2MavenBestPractices.java
    solutions/
      Solution1GithubActionsWorkflow.java
      Solution2MavenBestPractices.java
    experiment/
      TestCategorization.java        # @Tag("unit") demo
      NightlyBuildDemoIT.java        # @Tag("nightly") demo
  src/test/resources/
    junit-platform.properties        # Parallel execution config
  pom.xml                            # Maven profiles for test filtering
```

## Exercise 1: Optimize GitHub Actions Workflow

Review and enhance the sample `.github/workflows/build.yml` file with CI/CD best practices.

### Tasks

1. Open `.github/workflows/build.yml` and identify what is missing
2. Add Maven dependency caching using `setup-java`'s built-in `cache: maven`
3. Add `timeout-minutes` to prevent runaway builds
4. Add a step to upload test reports on failure using `actions/upload-artifact@v4`
5. Review `.github/workflows/nightly.yml` for scheduled build patterns

### Key Concepts

**Maven Caching** reduces build times significantly by caching the `~/.m2/repository` directory between workflow runs. Without caching, every build downloads all dependencies from scratch.

**Timeout** prevents stuck builds from consuming CI/CD minutes. The GitHub Actions default is 6 hours, which is far too long for most builds.

**Artifact Upload** on failure ensures that test reports (Surefire/Failsafe XML and HTML) are available for debugging without re-running tests locally.

## Exercise 2: Test Organization with @Tag and Maven Profiles

Learn how to categorize tests and run them selectively using JUnit 5 tags and Maven profiles.

### Tasks

1. Review `@Tag("unit")` on `TestCategorization.java`
2. Review `@Tag("nightly")` on `NightlyBuildDemoIT.java`
3. Run only unit-tagged tests: `./mvnw test -Punit-tests`
4. Run only nightly-tagged tests: `./mvnw verify -Dgroups=nightly`
5. Compare execution times

### Maven Commands

```bash
# Run all tests (default behavior)
./mvnw verify

# Run only unit-tagged tests (fast feedback loop)
./mvnw test -Punit-tests

# Run only integration-tagged tests
./mvnw verify -Pintegration-tests

# Run tests with a specific tag directly (without profiles)
./mvnw test -Dgroups=unit
./mvnw verify -Dgroups=nightly

# Exclude specific tags (e.g., skip slow nightly tests in PR builds)
./mvnw verify -DexcludedGroups=nightly
```

### Maven Profiles in pom.xml

The `pom.xml` includes two profiles:

- **unit-tests** -- Configures `maven-surefire-plugin` with `<groups>unit</groups>`
- **integration-tests** -- Configures `maven-failsafe-plugin` with `<groups>integration</groups>`

## Parallel Test Execution

The `junit-platform.properties` file enables class-level parallel execution:

```properties
junit.jupiter.execution.parallel.enabled = true
junit.jupiter.execution.parallel.mode.default = same_thread
junit.jupiter.execution.parallel.mode.classes.default = concurrent
```

This means:
- **Within a class**: Tests run sequentially (safe for shared state)
- **Across classes**: Test classes run in parallel (faster overall execution)

To opt a specific class out of parallel execution, use:
```java
@Execution(ExecutionMode.SAME_THREAD)
class MySequentialTest { ... }
```

## Maven Tips for Faster Builds

### Maven Daemon (mvnd)

[Maven Daemon](https://github.com/apache/maven-mvnd) keeps a long-running daemon process to avoid JVM startup overhead:

```bash
# Install via Homebrew (macOS)
brew install mvndaemon/homebrew-mvnd/mvnd

# Use mvnd instead of mvn/mvnw
mvnd verify
mvnd test -Punit-tests
```

Benefits:
- Reuses a warm JVM (no cold start)
- Parallel module builds by default
- Typically 2-3x faster for multi-module projects

### Skip Tests Selectively

```bash
# Skip all tests
./mvnw package -DskipTests

# Skip only integration tests (Failsafe)
./mvnw verify -DskipITs

# Skip only unit tests (Surefire) but run integration tests
./mvnw verify -Dsurefire.skip=true
```

### Offline Mode

```bash
# After initial download, build offline for speed
./mvnw verify -o
```

## CI/CD Strategy Summary

| Build Type    | Command                                  | When                 |
|---------------|------------------------------------------|----------------------|
| PR Build      | `./mvnw verify -DexcludedGroups=nightly` | Every pull request   |
| Merge to Main | `./mvnw verify`                          | Push to main branch  |
| Nightly       | `./mvnw verify -Dgroups=nightly`         | Scheduled (cron)     |

## Sample GitHub Actions Workflows

The `.github/workflows/` directory contains two sample workflows:

- **build.yml** -- A basic CI workflow (starting point for Exercise 1)
- **nightly.yml** -- A scheduled nightly build with tag filtering and artifact upload

## Further Resources

- [JUnit 5 User Guide -- Tagging and Filtering](https://junit.org/junit5/docs/current/user-guide/#writing-tests-tagging-and-filtering)
- [Maven Surefire Plugin -- Filtering by Tags](https://maven.apache.org/surefire/maven-surefire-plugin/examples/junit-platform.html)
- [GitHub Actions -- Caching Dependencies](https://docs.github.com/en/actions/using-workflows/caching-dependencies-to-speed-up-workflows)
- [Maven Daemon (mvnd)](https://github.com/apache/maven-mvnd)
- [JUnit 5 Parallel Execution](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution)
