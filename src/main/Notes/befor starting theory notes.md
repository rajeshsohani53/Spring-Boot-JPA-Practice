# Spring Boot + JPA — Learning Notes (Phase 2)

Rebuilding my Student REST API with a real MySQL database using Spring Data JPA,
so data survives restarts. Phase 1 (in-memory version) is in a separate repo.

---

## Part 7.1 / 7 — JPA concept (before any code)

### The problem JPA solves
- In JDBC/DAO (my old JSP/Servlet way), I manually translated between two worlds every time:
  - **Database world** = tables, rows, columns, SQL
  - **Java world** = objects, fields
- I was the translator: `rs.getString("name")` → `student.setName(...)`, for every field, every query. Repetitive boilerplate where bugs hide.
- **JPA automates that translation.**

### What JPA is
- **JPA = Java Persistence API.**
  - "Persistence" = saving data so it survives (to a database on disk).
  - "API" here = a **specification** — a set of rules/interfaces. **JPA itself has NO working code.** It's a blueprint, not the building.
- The technique JPA standardizes is **ORM = Object-Relational Mapping:**
  - **O**bject = Java class (`Student`)
  - **R**elational = relational DB (MySQL)
  - **M**apping = auto-connecting the two

### Core mental model — one object = one row
```
Java world                Database world
Student class      <-->   students TABLE
one Student object <-->   one ROW
a field (name)     <-->   a COLUMN (name)
```
- Save an object → JPA turns it into an INSERT → becomes a row.
- Fetch a row → JPA turns it back into an object.
- I never write the SQL or the translation.

### JPA vs Hibernate vs Spring Data JPA (KEY interview point)
- **JPA** = the rules / specification (interfaces, no working code). Like a job description.
- **Hibernate** = the actual implementation that does the real work (generates + runs SQL, does object<->row conversion). Like the employee who does the job. Spring Boot uses Hibernate by default.
- **Spring Data JPA** = a Spring layer on top of Hibernate that removes even more boilerplate — I write an EMPTY repository interface and Spring generates the CRUD implementation at runtime.

Full stack, top to bottom:
```
Spring Data JPA   -> empty interface, get CRUD methods free
   uses
Hibernate         -> does the actual ORM + generates/runs SQL
   implements
JPA               -> the specification (@Entity, @Id, rules)
   stores in
MySQL             -> the real database on disk
```

### Annotations preview (coming in the code)
- **`@Entity`** — marks a class as mapped to a DB table (one object = one row).
- **`@Id`** — marks the primary key field (unique id per row).
- **`@GeneratedValue`** — let the DB auto-generate the id (1, 2, 3...).
- Hibernate reads these and can auto-CREATE the table on startup — no manual `CREATE TABLE`.

### Interview-ready answer — "What is JPA?"
> "JPA is the Java Persistence API — a specification for ORM (object-relational mapping). It defines how Java objects map to database tables so I don't write SQL and manually convert rows to objects like in JDBC. JPA is just the rules; **Hibernate** is the implementation that does the work. **Spring Data JPA** sits on top and removes boilerplate — I define an `@Entity` and a repository interface, and it generates the CRUD operations and SQL automatically."
- Killer add-on: "I've written the JDBC/DAO version by hand, so I appreciate what JPA automates."

---

## Setup done so far
- Created a fresh Spring Boot project (`studentapi`), Java 17, Spring Boot 3.x.
- Dependencies picked from Spring Initializr: **Spring Web**, **Spring Data JPA**, **MySQL Driver**.
- Created an empty MySQL database: `CREATE DATABASE studentdb;` (no tables — Hibernate will generate them).

---

## Next up
- Turn `Student` into an `@Entity` (+ `@Id`, `@GeneratedValue`) and watch Hibernate create the table.
- Configure the DB connection in `application.properties`.
- Create the `StudentRepository` interface (extends `JpaRepository`).
- Rewrite the controller to use the repository → data survives restarts.
