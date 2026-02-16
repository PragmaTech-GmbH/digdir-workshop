Create the structure for a new Spring Boot Testing Workshop, based on the previous one in `template-workshop`.

Here are the requirements:
- we have a folder called `labs` with `lab-1` until 8, each with a `README.md` and a `pom.xml` file
- Each lab is separate from the others, so we can easily switch between them during the workshop
- The code sequentially builds up, so we can start with lab-1 and end with lab-8
- For each lab, use Spring Boot 4.0 with Maven and Java 21, prepare for Testcontainers with PostgreSQL usage
- The technical domain of the labs is a simple library system, see the existings labs in `template-workshop` for more details
- Each lab has a README with sample instructions for the attendees to follow, and a pom.xml with the necessary dependencies for that lab
- Each lab has execerises in the test folder, which are designed to be completed during the workshop, and they build upon each other as the labs progress, use the `pragmatech.digital.workshops.lab2.exercises` for the exercises package name and `solutions` for the solutions.
- For now, adopt the existing lab-1 to lab-4 code (but use Spring Boot 4) and prepare empty labs for lab-5 to lab-8 with the necessary structure and dependencies with the code of the last lab-4

The existing workshop was done within a day but my plan is to stretch it to two days, so we can go more in-depth and also cover more topics, such as testcontainers, test parallelization, and best practices for testing in Spring Boot. This is the planned agenda:

09:00 - 10:30 Block 1
10:30 - 11:00 Coffee Break & Exercises
11:00 - 12:30 Block 2
12:30 - 13:30 Lunch Break
13:30 - 15:00 Block 3
15:00 - 15:30 Coffee Break & Exercises
15:30 - 17:00 Block 4

Day One: Testing Spring Boot Applications Demystified

Goal: Getting started with testing Spring Boot applications and learning how to write tests for Spring Boot applications

Lab 1: Workshop Introduction - Testing basics and unit testing
Lab 2: Sliced Testing - Introduction and verifying the web layer
Lab 3: Sliced Testing continued, including testing the persistence layer and Testcontainers
Lab 4: Integration Testing - Introduction and strategies to start the entire context

Day Two: The Need for Speed: Optimizing Spring Boot Test Suites for Speed & Stability

Goal: Showcase and explain strategies to improve build times and speed up tests to gather fast feedback.

Lab 5: Integration Testing continued
Lab 6: Understanding Spring Test Context Caching for fast builds
Lab 7: Test Parallelization and Spring Boot Testing Best Practices
Lab 8: General FAQ and Customer Specific Issues

Please already ideate for lab 5 to lab 8, so we can have the structure ready for the next steps. 

All projects bust run within `./mvnw verify` and the only requirement is to have Docker running for the Testcontainers part.
