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
- Fresh Spring Boot project (`SpringBootJpaPractice`), Java 17, Spring Boot 3.x.
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
- **`spring.jpa.show-sql=true`** — prints the SQL Hibernate generates to the console. Great for learning.

### The payoff (what happened on run)
- Console showed: `Hibernate: create table student (...)` — Hibernate generated + ran the CREATE TABLE. I never wrote it.
- Verified in MySQL: `USE studentdb; SHOW TABLES; DESCRIBE student;` → `student` table with `id`, `name`, `course`.

### Common startup errors (for future me)
- `javax` vs `jakarta` import → must be `jakarta.persistence`.
- Wrong MySQL password → `Access denied for user 'root'`.
- DB name mismatch → `Unknown database 'studentdb'` (check the URL).
- MySQL not running → `Communications link failure` (start the MySQL service).

---

## Part 7.4 — The Repository (empty interface = free CRUD)

### The whole thing
```java
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Integer> {
}
```
- An **empty interface**. No methods, no code, no SQL. This is COMPLETE, not a placeholder.

### Why an empty interface gives CRUD (interview point)
- It's an **interface**, not a class — I never write an implementation.
- **Spring Data JPA generates the implementing class at runtime.** At startup Spring sees the interface, builds a hidden class that implements it, and hands me a ready object.
- **`extends JpaRepository<Student, Integer>`**:
  - `Student` = the entity type (which table).
  - `Integer` = the type of the `@Id` field (my `id` is `int` → wrapper `Integer`).

### Free methods (no code written)
| Method | Does | Phase 1 hand-written equivalent |
|--------|------|---------------------------------|
| `save(student)` | insert OR update | `students.add(...)` / update loop |
| `findAll()` | get all rows | `return students` |
| `findById(id)` | get one | for-loop search |
| `deleteById(id)` | delete one | `removeIf` |
| `count()` | how many | — |
| `existsById(id)` | exists? | — |

- Each runs real SQL (Hibernate generates it); I write none of it.
- **`save()` is smart**: no/unknown id → INSERT; existing id → UPDATE. One method = both. So Phase 1 "add" + "update" collapse into one `save()`.

### How the controller gets the repository — Dependency Injection (interview point)
- Don't use `new` (can't — it's an interface; Spring builds the real object).
- Spring **injects** it via `@Autowired`.
- **`@Autowired`** — "Spring, find the `StudentRepository` object you built and plug it in here." I declare I need it; Spring provides it. I never instantiate it.
- **Dependency Injection (DI)** = the object I depend on is handed to me by the framework instead of me creating it. A core reason Spring exists.

---

## Part 7.5 — Wire the controller to the repository (REAL PERSISTENCE)

### The controller
```java
@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentRepository repository;

    @PostMapping
    public Student addStudent(@RequestBody Student student) {
        return repository.save(student);
    }

    @GetMapping
    public List<Student> getAllStudents() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Student getStudentById(@PathVariable int id) {
        return repository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public Student updateStudent(@PathVariable int id, @RequestBody Student updated) {
        updated.setId(id);
        return repository.save(updated);
    }

    @DeleteMapping("/{id}")
    public String deleteStudent(@PathVariable int id) {
        repository.deleteById(id);
        return "Deleted student with id " + id;
    }
}
```

### Key points
- Way smaller than Phase 1 — no ArrayList, no loops, no removeIf. Each method is basically one repository call.
- **`@RequestMapping("/students")`** at CLASS level — a shortcut: every endpoint in the class starts with `/students`. So `@PostMapping` (no path) = `POST /students`, `@GetMapping("/{id}")` = `GET /students/{id}`. No repeating `/students` on each method.
- **`@Autowired StudentRepository`** — DI in action; Spring plugs in the repository object. I never `new` it.
- **`save()`** — CREATE (no id → INSERT); returns the student WITH its DB-generated id filled in.
- **`findAll()`** — READ ALL rows.
- **`findById(id).orElse(null)`** — READ ONE. `findById` returns an **`Optional<Student>`** (a wrapper that either has the value or is empty — Java 8's way to avoid null-pointer errors). `.orElse(null)` = the student, or null if not found.
- **UPDATE** — set the id from the URL onto the object, then `save()`. Existing id → `save()` does an UPDATE (not INSERT).
- **`deleteById(id)`** — DELETE, one line.
- **Architecture:** controller handles web requests, repository handles persistence (via Hibernate → MySQL). That separation = a layered/tiered app.

### The payoff — persistence PROVEN
- Added students (no `id` in JSON — DB generates it). Console showed `Hibernate: insert into student ...`.
- Added a few, then **STOPPED and RESTARTED the app** → data was STILL THERE (Phase 1 would have wiped it).
- Data lives in MySQL on disk: `USE studentdb; SELECT * FROM student;` shows the rows.

### DEBUGGING LESSON — error location != error cause
- Got: `The method save(Student) is undefined for the type StudentRepository` (pointing at the controller).
- Real cause was in `StudentRepository`: `save()` only exists if it `extends JpaRepository<Student, Integer>` (+ its import). Reasoning chain: "save undefined" → "repo must not have it" → "so it must not extend JpaRepository."
- Also: a `*` on an Eclipse tab = **unsaved file**. `Ctrl+Shift+S` = save all. Many "not working" moments are just unsaved files.

---

## Phase 2 — DONE
Built a full database-backed CRUD REST API: `@Entity` → table, empty `JpaRepository` → free CRUD, `@Autowired` DI, MySQL persistence that survives restarts. Can explain every layer because I built each one.

### Possible next steps
- Reconnect the `add.html` / `list.html` frontend (drop in `static/`, remove the `id` field from the add form since the DB generates it).
- Add a service layer between controller and repository (cleaner architecture).
- Add validation, proper 404 handling, DTOs.
