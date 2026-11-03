import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/*
 ============================================================================
  SMART LIBRARY MANAGEMENT SYSTEM - CONSOLIDATED SINGLE-FILE EDITION
 ============================================================================
  Designed for easy demonstration to examiners, professors, and reviewers
  All OOP components are neatly structured in 6 logical sections:

  TABLE OF CONTENTS:
  ------------------
  1. INTERFACES & ABSTRACTIONS      (Borrowable, Book, User)
  2. CONCRETE SUBCLASSES (OOP)      (PrintedBook, EBook, Student, Librarian)
  3. TRANSACTION & FINE CALCULATION (Transaction)
  4. CORE BUSINESS LOGIC            (Library)
  5. PERSISTENCE & FILE STORAGE     (FileManager -> library.dat)
  6. CONSOLE UI & MENU SYSTEM       (Main - Entry Point)
 ============================================================================
*/

// ============================================================================
// SECTION 1: INTERFACES & ABSTRACTIONS
// ============================================================================

//Interface defining contract for borrowable library items
interface Borrowable {
    void issue();
    void returnBook();
}

//Abstract base class for all library books
//Demonstrates: Abstraction, Encapsulation, Serialization

abstract class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int id;
    protected String title;
    protected String author;
    protected String category;
    protected boolean available;

    public Book(int id, String title, String author, String category) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.available = true;
    }

    // Abstract method overridden by subclasses (Polymorphism)
    public abstract void displayDetails();

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public boolean isAvailable() { return available; }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}


//Abstract base class for all system users (Students, Librarians).
//Demonstrates: Inheritance base, Abstraction.

abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int id;
    protected String name;
    protected String email;

    public User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public abstract void displayProfile();

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}


// ============================================================================
// SECTION 2: CONCRETE SUBCLASSES (INHERITANCE & POLYMORPHISM)
// ============================================================================


//Represents physical books with page count.
//Demonstrates: Inheritance (extends Book), Interface implementation (Borrowable).

class PrintedBook extends Book implements Borrowable {
    private static final long serialVersionUID = 1L;
    private int pages;

    public PrintedBook(int id, String title, String author, String category, int pages) {
        super(id, title, author, category);
        this.pages = pages;
    }

    @Override
    public void displayDetails() {
        System.out.printf("%-5d %-28s %-20s %-15s Printed (%d pages) %-10s%n",
                id, title, author, category, pages, available ? "Available" : "Issued");
    }

    @Override
    public void issue() { available = false; }

    @Override
    public void returnBook() { available = true; }
}


//Represents digital e-books with file size in MB.
//Demonstrates: Inheritance (extends Book), Interface implementation (Borrowable).

class EBook extends Book implements Borrowable {
    private static final long serialVersionUID = 1L;
    private double fileSizeMB;

    public EBook(int id, String title, String author, String category, double fileSizeMB) {
        super(id, title, author, category);
        this.fileSizeMB = fileSizeMB;
    }

    @Override
    public void displayDetails() {
        System.out.printf("%-5d %-28s %-20s %-15s E-Book (%.1f MB) %-10s%n",
                id, title, author, category, fileSizeMB, available ? "Available" : "Issued");
    }

    @Override
    public void issue() { available = false; }

    @Override
    public void returnBook() { available = true; }
}


//Student user who can borrow and return books

class Student extends User {
    private static final long serialVersionUID = 1L;

    public Student(int id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public void displayProfile() {
        System.out.println("Student ID : " + id);
        System.out.println("Name       : " + name);
        System.out.println("Email      : " + email);
    }
}


//Librarian user with administration rights

class Librarian extends User {
    private static final long serialVersionUID = 1L;

    public Librarian(int id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public void displayProfile() {
        System.out.println("Librarian ID : " + id);
        System.out.println("Name         : " + name);
        System.out.println("Email        : " + email);
    }
}


// ============================================================================
// SECTION 3: TRANSACTION & FINE CALCULATION
// ============================================================================


//Handles borrowing lifecycle, due dates, and fine calculation
//Standard loan period: 14 days
//Fine rate: Rs. 5.00 per day overdue

class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private int transactionId;
    private int studentId;
    private int bookId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double fine;

    public Transaction(int transactionId, int studentId, int bookId) {
        this.transactionId = transactionId;
        this.studentId = studentId;
        this.bookId = bookId;
        this.issueDate = LocalDate.now();
        this.dueDate = issueDate.plusDays(14);
        this.returnDate = null;
        this.fine = 0;
    }

    public void closeTransaction() {
        returnDate = LocalDate.now();
        long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
        fine = Math.max(0, overdueDays * 5.0);
    }

    public boolean isActive() { return returnDate == null; }
    public int getTransactionId() { return transactionId; }
    public int getStudentId() { return studentId; }
    public int getBookId() { return bookId; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public double getFine() { return fine; }

    public double calculateCurrentFine() {
        if (!isActive()) return fine;
        long overdueDays = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        return Math.max(0, overdueDays * 5.0);
    }
}


// ============================================================================
// SECTION 4: CORE BUSINESS LOGIC (LIBRARY CONTROLLER)
// ============================================================================

/**
 * Core library service maintaining lists of books, students, and transactions.
 * Enforces borrowing limits (max 3 books), duplicate checks, and catalog search.
 */
class Library implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<Book> books;
    private ArrayList<Student> students;
    private ArrayList<Transaction> transactions;
    private int nextTransactionId;

    public Library() {
        books = new ArrayList<>();
        students = new ArrayList<>();
        transactions = new ArrayList<>();
        nextTransactionId = 1001;
    }

    public ArrayList<Book> getBooks() { return books; }
    public ArrayList<Student> getStudents() { return students; }
    public ArrayList<Transaction> getTransactions() { return transactions; }

    public void addBook(Book book) {
        books.add(book);
    }

    public boolean removeBook(int bookId) {
        Book book = findBook(bookId);
        if (book == null || !book.isAvailable()) return false;
        return books.remove(book);
    }

    public Book findBook(int id) {
        for (Book book : books) {
            if (book.getId() == id) return book;
        }
        return null;
    }

    public Student findStudent(int id) {
        for (Student student : students) {
            if (student.getId() == id) return student;
        }
        return null;
    }

    public boolean addStudent(Student student) {
        if (findStudent(student.getId()) != null) return false;
        students.add(student);
        return true;
    }

    public List<Book> searchBooks(String keyword) {
        ArrayList<Book> result = new ArrayList<>();
        String key = keyword.toLowerCase();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(key)
                    || book.getAuthor().toLowerCase().contains(key)
                    || book.getCategory().toLowerCase().contains(key)) {
                result.add(book);
            }
        }
        return result;
    }

    public String issueBook(int studentId, int bookId) {
        Student student = findStudent(studentId);
        Book book = findBook(bookId);

        if (student == null) return "Student not found.";
        if (book == null) return "Book not found.";
        if (!book.isAvailable()) return "Book is already issued.";
        if (hasActiveTransaction(studentId, bookId)) return "You already have this book issued.";
        if (countActiveTransactions(studentId) >= 3) return "Borrowing limit reached. Maximum 3 books.";

        if (book instanceof Borrowable) {
            ((Borrowable) book).issue();
        }

        transactions.add(new Transaction(nextTransactionId++, studentId, bookId));
        return "Book issued successfully. Due date: " +
                transactions.get(transactions.size() - 1).getDueDate();
    }

    public String returnBook(int studentId, int bookId) {
        Transaction transaction = findActiveTransaction(studentId, bookId);
        if (transaction == null) return "No active transaction found for this book.";

        Book book = findBook(bookId);
        if (book != null && book instanceof Borrowable) {
            ((Borrowable) book).returnBook();
        }

        transaction.closeTransaction();
        return String.format("Book returned successfully. Fine: Rs. %.2f", transaction.getFine());
    }

    public Transaction findActiveTransaction(int studentId, int bookId) {
        for (Transaction t : transactions) {
            if (t.getStudentId() == studentId && t.getBookId() == bookId && t.isActive()) {
                return t;
            }
        }
        return null;
    }

    public boolean hasActiveTransaction(int studentId, int bookId) {
        return findActiveTransaction(studentId, bookId) != null;
    }

    public int countActiveTransactions(int studentId) {
        int count = 0;
        for (Transaction t : transactions) {
            if (t.getStudentId() == studentId && t.isActive()) count++;
        }
        return count;
    }

    public double getStudentFine(int studentId) {
        double total = 0;
        for (Transaction t : transactions) {
            if (t.getStudentId() == studentId) total += t.calculateCurrentFine();
        }
        return total;
    }

    public int getAvailableBookCount() {
        int count = 0;
        for (Book b : books) if (b.isAvailable()) count++;
        return count;
    }

    public int getIssuedBookCount() {
        return books.size() - getAvailableBookCount();
    }

    public int getOverdueCount() {
        int count = 0;
        for (Transaction t : transactions) {
            if (t.isActive() && t.calculateCurrentFine() > 0) count++;
        }
        return count;
    }
}


// ============================================================================
// SECTION 5: PERSISTENCE & FILE STORAGE (SERIALIZATION)
// ============================================================================

/**
 * Handles automatic saving and loading using Java binary serialization.
 * Data persists across program restarts in 'library.dat'.
 */
class FileManager {
    private static final String FILE_NAME = "library.dat";

    public static void saveLibrary(Library library) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(library);
        } catch (Exception e) {
            System.out.println("Warning: Could not save library data.");
        }
    }

    public static Library loadLibrary() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (Library) in.readObject();
        } catch (Exception e) {
            return createDefaultLibrary();
        }
    }

    private static Library createDefaultLibrary() {
        Library library = new Library();

        // Seed 5 sample books
        library.addBook(new PrintedBook(101, "Clean Code", "Robert Martin", "Programming", 464));
        library.addBook(new PrintedBook(102, "Java: The Complete Reference", "Herbert Schildt", "Programming", 1248));
        library.addBook(new PrintedBook(103, "The Alchemist", "Paulo Coelho", "Fiction", 208));
        library.addBook(new EBook(104, "Head First Java", "Kathy Sierra", "Programming", 8.5));
        library.addBook(new EBook(105, "Atomic Habits", "James Clear", "Self Help", 4.2));

        // Seed 2 sample students
        library.addStudent(new Student(1001, "Demo Student", "demo@student.com"));
        library.addStudent(new Student(1002, "Aarav Sharma", "aarav@student.com"));

        return library;
    }
}


// ============================================================================
// SECTION 6: CONSOLE UI & MENU SYSTEM (MAIN ENTRY POINT)
// ============================================================================

/**
 * Entry point for the application.
 * Manages menus for Student, Librarian, and Registration workflows.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static Library library;

    public static void main(String[] args) {
        library = FileManager.loadLibrary();
        System.out.println("\n==============================================");
        System.out.println("       SMART LIBRARY MANAGEMENT SYSTEM");
        System.out.println("==============================================");
        System.out.println("Library data loaded successfully.\n");

        while (true) {
            printMainMenu();
            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1 -> studentLogin();
                case 2 -> librarianLogin();
                case 3 -> registerStudent();
                case 4 -> {
                    FileManager.saveLibrary(library);
                    System.out.println("Data saved. Goodbye! 👋");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n--------------- MAIN MENU ----------------");
        System.out.println("1. Student Login");
        System.out.println("2. Librarian Login");
        System.out.println("3. Register New Student");
        System.out.println("4. Save & Exit");
        System.out.println("-------------------------------------------");
    }

    private static void studentLogin() {
        int id = readInt("Enter Student ID: ");
        Student student = library.findStudent(id);

        if (student == null) {
            System.out.println("Student not found.");
            return;
        }

        System.out.println("\nWelcome, " + student.getName() + "!");
        studentMenu(student);
    }

    private static void studentMenu(Student student) {
        while (true) {
            System.out.println("\n=============== STUDENT MENU ===============");
            System.out.println("1. View All Books");
            System.out.println("2. Search Book");
            System.out.println("3. Issue Book");
            System.out.println("4. Return Book");
            System.out.println("5. My Borrowed Books");
            System.out.println("6. View My Profile");
            System.out.println("7. View Current Fine");
            System.out.println("8. Logout");
            System.out.println("============================================");

            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1 -> displayAllBooks();
                case 2 -> searchBooks();
                case 3 -> issueBook(student);
                case 4 -> returnBook(student);
                case 5 -> displayBorrowedBooks(student);
                case 6 -> student.displayProfile();
                case 7 -> System.out.printf("Current fine: Rs. %.2f%n", library.getStudentFine(student.getId()));
                case 8 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void issueBook(Student student) {
        displayAvailableBooks();
        int bookId = readInt("Enter Book ID to issue: ");
        System.out.println(library.issueBook(student.getId(), bookId));
        FileManager.saveLibrary(library);
    }

    private static void returnBook(Student student) {
        displayBorrowedBooks(student);
        int bookId = readInt("Enter Book ID to return: ");
        System.out.println(library.returnBook(student.getId(), bookId));
        FileManager.saveLibrary(library);
    }

    private static void displayBorrowedBooks(Student student) {
        System.out.println("\n---------------- MY BORROWED BOOKS ----------------");
        boolean found = false;

        for (Transaction t : library.getTransactions()) {
            if (t.getStudentId() == student.getId() && t.isActive()) {
                Book book = library.findBook(t.getBookId());
                if (book != null) {
                    System.out.println("Book ID   : " + book.getId());
                    System.out.println("Title     : " + book.getTitle());
                    System.out.println("Issue Date: " + t.getIssueDate());
                    System.out.println("Due Date  : " + t.getDueDate());
                    System.out.printf("Fine      : Rs. %.2f%n", t.calculateCurrentFine());
                    System.out.println("----------------------------------------------");
                    found = true;
                }
            }
        }

        if (!found) System.out.println("You have no borrowed books.");
    }

    private static void librarianLogin() {
        System.out.println("\nLibrarian login");
        String pin = readLine("Enter librarian PIN: ");

        if (pin.equals("1234")) {
            Librarian librarian = new Librarian(1, "Chief Librarian", "library@college.com");
            System.out.println("Login successful.");
            librarianMenu(librarian);
        } else {
            System.out.println("Incorrect PIN.");
        }
    }

    private static void librarianMenu(Librarian librarian) {
        while (true) {
            System.out.println("\n============== LIBRARIAN MENU ==============");
            System.out.println("1. Add Book");
            System.out.println("2. Remove Book");
            System.out.println("3. View All Books");
            System.out.println("4. Search Book");
            System.out.println("5. View All Students");
            System.out.println("6. View Active Transactions");
            System.out.println("7. Library Statistics");
            System.out.println("8. View Profile");
            System.out.println("9. Logout");
            System.out.println("=============================================");

            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1 -> addBook();
                case 2 -> removeBook();
                case 3 -> displayAllBooks();
                case 4 -> searchBooks();
                case 5 -> displayAllStudents();
                case 6 -> displayActiveTransactions();
                case 7 -> displayStatistics();
                case 8 -> librarian.displayProfile();
                case 9 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void addBook() {
        System.out.println("\n1. Printed Book");
        System.out.println("2. E-Book");
        int type = readInt("Choose type: ");

        int id = readInt("Book ID: ");
        if (library.findBook(id) != null) {
            System.out.println("A book with this ID already exists.");
            return;
        }

        String title = readLine("Title: ");
        String author = readLine("Author: ");
        String category = readLine("Category: ");

        if (type == 1) {
            int pages = readInt("Number of pages: ");
            library.addBook(new PrintedBook(id, title, author, category, pages));
        } else if (type == 2) {
            double size = readDouble("File size (MB): ");
            library.addBook(new EBook(id, title, author, category, size));
        } else {
            System.out.println("Invalid book type.");
            return;
        }

        FileManager.saveLibrary(library);
        System.out.println("Book added successfully.");
    }

    private static void removeBook() {
        displayAvailableBooks();
        int id = readInt("Enter Book ID to remove: ");

        if (library.removeBook(id)) {
            FileManager.saveLibrary(library);
            System.out.println("Book removed successfully.");
        } else {
            System.out.println("Book could not be removed. It may not exist or may be issued.");
        }
    }

    private static void registerStudent() {
        System.out.println("\n------------- STUDENT REGISTRATION -------------");
        int id = readInt("Choose Student ID: ");

        if (library.findStudent(id) != null) {
            System.out.println("A student with this ID already exists.");
            return;
        }

        String name = readLine("Name: ");
        String email = readLine("Email: ");

        library.addStudent(new Student(id, name, email));
        FileManager.saveLibrary(library);
        System.out.println("Registration successful. You can now log in using ID " + id + ".");
    }

    private static void displayAllBooks() {
        System.out.println("\n-------------------------------- BOOK CATALOG --------------------------------");
        System.out.printf("%-5s %-28s %-20s %-15s %-25s%n", "ID", "Title", "Author", "Category", "Type / Status");
        System.out.println("-------------------------------------------------------------------------------");
        for (Book book : library.getBooks()) book.displayDetails();
        System.out.println("-------------------------------------------------------------------------------");
    }

    private static void displayAvailableBooks() {
        System.out.println("\n---------------- AVAILABLE BOOKS ----------------");
        boolean found = false;
        for (Book book : library.getBooks()) {
            if (book.isAvailable()) {
                book.displayDetails();
                found = true;
            }
        }
        if (!found) System.out.println("No books are currently available.");
    }

    private static void searchBooks() {
        String keyword = readLine("Enter title, author, or category: ");
        List<Book> results = library.searchBooks(keyword);

        System.out.println("\n---------------- SEARCH RESULTS ----------------");
        if (results.isEmpty()) {
            System.out.println("No matching books found.");
            return;
        }

        for (Book book : results) book.displayDetails();
    }

    private static void displayAllStudents() {
        System.out.println("\n---------------- REGISTERED STUDENTS ----------------");
        if (library.getStudents().isEmpty()) {
            System.out.println("No students registered.");
            return;
        }

        for (Student student : library.getStudents()) {
            System.out.printf("ID: %-6d | Name: %-25s | Email: %s%n",
                    student.getId(), student.getName(), student.getEmail());
        }
    }

    private static void displayActiveTransactions() {
        System.out.println("\n---------------- ACTIVE TRANSACTIONS ----------------");
        boolean found = false;

        for (Transaction t : library.getTransactions()) {
            if (t.isActive()) {
                Student student = library.findStudent(t.getStudentId());
                Book book = library.findBook(t.getBookId());

                System.out.printf("Transaction: %d | Student: %s (%d) | Book: %s (%d) | Due: %s | Fine: Rs. %.2f%n",
                        t.getTransactionId(),
                        student != null ? student.getName() : "Unknown",
                        t.getStudentId(),
                        book != null ? book.getTitle() : "Unknown",
                        t.getBookId(),
                        t.getDueDate(),
                        t.calculateCurrentFine());
                found = true;
            }
        }

        if (!found) System.out.println("No active transactions.");
    }

    private static void displayStatistics() {
        System.out.println("\n============== LIBRARY STATISTICS ==============");
        System.out.println("Total Books       : " + library.getBooks().size());
        System.out.println("Available Books   : " + library.getAvailableBookCount());
        System.out.println("Issued Books      : " + library.getIssuedBookCount());
        System.out.println("Registered Users  : " + library.getStudents().size());
        System.out.println("Active Transactions: " + countActiveTransactions());
        System.out.println("Overdue Books     : " + library.getOverdueCount());
        System.out.println("=================================================");
    }

    private static int countActiveTransactions() {
        int count = 0;
        for (Transaction t : library.getTransactions()) if (t.isActive()) count++;
        return count;
    }

    private static int readInt(String message) {
        while (true) {
            try {
                System.out.print(message);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private static double readDouble(String message) {
        while (true) {
            try {
                System.out.print(message);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static String readLine(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }
}
