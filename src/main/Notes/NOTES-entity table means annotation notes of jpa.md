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

### Interview-ready answer — "What is JPA?"
> "JPA is the Java Persistence API — a specification for ORM (object-relational mapping). It defines how Java objects map to database tables so I don't write SQL and manually convert rows to objects like in JDBC. JPA is just the rules; **Hibernate** is the implementation that does the work. **Spring Data JPA** sits on top and removes boilerplate — I define an `@Entity` and a repository interface, and it generates the CRUD operations and SQL automatically."
- Killer add-on: "I've written the JDBC/DAO version by hand, so I appreciate what JPA automates."

---

## Setup done
- Fresh Spring Boot project (`studentapi`/`SpringBootJpaPractice`), Java 17, Spring Boot 3.x.
- Dependencies from Spring Initializr: **Spring Web**, **Spring Data JPA**, **MySQL Driver**.
- Created empty MySQL database: `CREATE DATABASE studentdb;` (no tables — Hibernate generates them).

---

## Part 7.3 — The `@Entity` class + DB config (Hibernate auto-creates the table)

### The entity
```java
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String course;

    public Student() { }   // no-arg constructor — Hibernate REQUIRES it

    public Student(String name, String course) {
        this.name = name;
        this.course = course;
    }

    // getters + setters ...
}
```

### Entity annotations
- **`@Entity`** — maps this class to a DB table. One object = one row. Table named after the class (`student`) by default. Pulls the class into the ORM world.
- **`@Id`** — marks the primary key (unique id per row). Every entity needs exactly one.
- **`@GeneratedValue(strategy = GenerationType.IDENTITY)`** — DB auto-generates the id. `IDENTITY` = MySQL `AUTO_INCREMENT` (assigns 1, 2, 3...). That's why the constructor takes only name + course — id is the DB's job.
- **No-arg constructor** — Hibernate requires it to build an empty object, then fill via setters (same reason Jackson needed it).
- **IMPORTANT:** use `jakarta.persistence.*`, NOT `javax.persistence.*`. Spring Boot 3 switched to `jakarta`. Wrong import = nothing works. (Common gotcha.)

### Database config (`src/main/resources/application.properties`)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/studentdb
spring.datasource.username=root
spring.datasource.password=MY_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```
- **`spring.datasource.url`** — `jdbc:mysql://` (protocol) + `localhost:3306` (host + MySQL default port) + `/studentdb` (my database).
- **username / password** — my MySQL login (root).
- **`spring.jpa.hibernate.ddl-auto=update`** — THE MAGIC LINE. Hibernate reads `@Entity` classes and auto-creates/updates tables to match. `update` = create table if missing, add columns for new fields, don't delete data. (Other values: `create` wipes + rebuilds each start; `none` does nothing.)
- **`spring.jpa.show-sql=true`** — prints the SQL Hibernate generates to the console. Great for learning — I can watch the INSERT/SELECT it writes for me.

### The payoff (what happened on run)
- Console showed: `Hibernate: create table student (id integer not null auto_increment, course varchar(255), name varchar(255), primary key (id)) ...`
- Hibernate read my `@Entity` and generated + ran the `CREATE TABLE` — I never wrote it.
- Verified in MySQL: `USE studentdb; SHOW TABLES; DESCRIBE student;` → `student` table with `id`, `name`, `course`.
- This IS ORM working: my Java class shape became a table shape, zero SQL from me.

### Common startup errors (for future me)
- `javax` vs `jakarta` import → must be `jakarta.persistence`.
- Wrong MySQL password → `Access denied for user 'root'`.
- DB name mismatch → `Unknown database 'studentdb'` (check the URL).
- MySQL not running → `Communications link failure` (start the MySQL service).

---

## Next up
- **Part 7.4:** create `StudentRepository extends JpaRepository<Student, Integer>` — empty interface, free CRUD (save/findAll/findById/deleteById), no SQL.
- Rewrite the controller to use the repository → data survives restarts.
