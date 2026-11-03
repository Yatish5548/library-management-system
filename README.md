# Smart Library Management System

A menu-driven Java console application for managing a college library, built as an Object Oriented Programming project at Manipal University Jaipur.

## Features
- **Students:** register, log in with ID, view and search books, issue and return books, view borrowed books and fine
- **Librarian (PIN protected):** add and remove books, view all books and students, view active transactions, view library statistics
- **Rules:** 14-day loan period, maximum 3 books per student, fine of Rs. 5 per overdue day
- **Persistence:** data is saved automatically to `library.dat`

## OOP concepts demonstrated
Abstraction (`Book`, `User`), inheritance (`PrintedBook`, `EBook`, `Student`, `Librarian`), polymorphism (`displayDetails()`, `displayProfile()`), interface (`Borrowable`), encapsulation, collections, serialization and exception handling.

## Project structure
Everything is in a single file, `Main.java`, organised in six sections:
1. Interfaces and abstractions (`Borrowable`, `Book`, `User`)
2. Concrete subclasses (`PrintedBook`, `EBook`, `Student`, `Librarian`)
3. Transaction and fine calculation (`Transaction`)
4. Core business logic (`Library`)
5. File persistence (`FileManager`)
6. Console UI and menus (`Main`)

## How to run
Requires **Java 14 or later**.

```
javac Main.java
java Main
```

## Sample login details
- Student IDs: `1001` (Demo Student), `1002` (Aarav Sharma)
- Librarian PIN: `1234`

Delete `library.dat` to reset to the default sample data.

## Author
Yatish Kumar Yadav, B.Tech CSE, Manipal University Jaipur
