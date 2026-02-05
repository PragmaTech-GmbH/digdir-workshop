Day One: Testing Spring Boot Applications Demystified

Goal: Getting started with testing Spring Boot applications and learning how to write tests for Spring Boot applications

- Explain you the toolbox and how to use it
- Recipes for testing various parts of your application

Each block is 90 min of length:

Optimal agenda:

Start Day 1

09:00 - 10:30 Block 1: Workshop Introduction - Testing basics and unit testing

- Get to know each other with a short introduction round:
  - Name, role and experience 
  - Their relationship to automated testing with Spring Boot
  - What they are hoping to learn during the next two days
- Overview of the covered topics during the next days
- Quickly cover north star: the why is important
- Spend some time for the basics of testing with Spring Boot & Maven
- Quickly cover the various testing libraries
- Focus on hints for better unit testing
- Hands-on test: JUnit Jupiter extension, advanced AssertJ and maybe Awaitility
- Workshop project setup, help everyone to get started locally

10:30 - 11:00 Coffee Break & Exercises
11:00 - 12:30 Block 2: Sliced Testing - Introduction and verifying the web layer

- Showcase the limitations of unit testing using a web controller for an example
- Explain sliced context loading in detail
- Cover webmvctest in detail, also with security
- Test: test a basic CRUD API 

12:30 - 13:30 Lunch Break
13:30 - 15:00 Block 3: Sliced Testing continued, including testing the persistence layer and Testcontainers

- Continue with the persistence layer and the native SQL query
- in-memory db vs. real database, benefits with Flyway/Liquibase and ddl validate
- Some words on testcontainers
- Write first DataJpaTest
- Some words on transaction management, flushing and cleanup
- Let them write their own test for DataJpaTest

15:00 - 15:30 Coffee Break & Exercises
15:30 - 17:00 Block 4: Integration Testing - Introduction and strategies to start the entire context

- Discuss challenges that arise when starting now the entire context
- External infrastructure
- HTTP communication
- Explain airplane mode for tests
- Short recap of the day


Day Two: The Need for Speed: Optimizing Spring Boot Test Suites for Speed & Stability

Goal: Showcase and explain strategies to improve build times and speed up tests to gather fast feedback.

Block 1: Integration Testing continued

- Recap the last session from yesterday
- Write the first actual tests and show the difference between MockMvc and a real servlet container
- Let them write theirs

Block 2: Understanding Spring Test Context Caching for fast builds
Block 3: Test Parallelization and Spring Boot Testing Best Practices

- Parallize the tests with JUnit or Maven
- Maven best practices

Block 4: General FAQ and Customer Specific Issues

- GHA pipeline best practices
- Summing up of the entire two days
- Mention on-demand online course or hands-on support

Customer focus
- Maven setup, and best practices for tests in the context?
  - Test separation
  - Plugin configuration
  - Speed up tricks: mvnwd
- Maven & Pipeline setup for GHA


GHA Tricks Lab:
- Nightly Runs
- Limit minutes
- Don’t pollute logs
- Store and upload screenshots or recordings of failed web tests
- Caching
- Service tasks
