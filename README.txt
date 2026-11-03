============================================================================
 SMART LIBRARY MANAGEMENT SYSTEM (SINGLE CONSOLIDATED FILE EDITION)
============================================================================

This folder contains the complete project consolidated into a single file:
Main.java

Why this single-file layout?
- Makes it effortless to open in VS Code, IntelliJ, Eclipse, or Notepad
- Allows you to scroll linearly from top to bottom when presenting to an examiner
- No frantic tab switching between 10 different files during viva

STRUCTURE OF Main.java:
-----------------------
Section 1: Interfaces & Abstraction (Borrowable, Book, User)
Section 2: Concrete Subclasses & Polymorphism (PrintedBook, EBook, Student, Librarian)
Section 3: Transaction & Fine Calculation (Transaction -> 14 days, Rs. 5/day fine)
Section 4: Core Business Logic (Library -> 3-book limit, search, loan management)
Section 5: File Persistence (FileManager -> library.dat)
Section 6: User Interface & Menu System (Main -> Student & Librarian menus)

HOW TO RUN:
-----------
1. Open a terminal in this folder
2. Compile:
   javac Main.java
3. Run:
   java Main
(Or in Java 11+: java Main.java)

LOGIN CREDENTIALS:
------------------
- Student ID 1: 1001 (Demo Student)
- Student ID 2: 1002 (Aarav Sharma)
- Librarian PIN: 1234
============================================================================