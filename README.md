# ClassTrack

ClassTrack is a JavaFX desktop application for recording and analysing student attendance. The teacher adds the students once, then takes attendance by swiping cards. All data is saved in a local SQLite database, so the app works offline.

It was made for the course Comprehensive Design of Data Structure at Southwest University of Science and Technology. Each main feature is built on a data structure from the course.

## Features

- Add, edit and delete students (roll number and name; duplicate roll numbers are rejected)
- Roll Call: mark Present, Absent or Late by dragging the card right, left or down, with the buttons, or with the arrow keys
- Go back one card, clear a mark, or mark all remaining students present
- Pick any date and view or mark attendance for it
- Dashboard: marked count, status bar, summary tiles, last 7 days chart and a "Needs attention" list
- Prefix search on student names (for example, "sh" finds Shelli and Sharmila)
- Student profile with a 28-day heat map, attendance percentage, current streak and advice on reaching 75%
- Light and dark theme, with custom colours that are remembered

## Data structures and algorithms

| Structure | Used for |
|---|---|
| `ArrayList<Student>` | Ordered student list and the roll call position |
| `HashMap` | Roll number lookup, today's status, attendance statistics |
| `Trie` (written from scratch) | Name search by the beginning of any word, O(P + M) |
| `PriorityQueue` (min-heap) | The three students with the lowest attendance |
| `LinkedHashMap`, `HashSet` | Attendance history in date order, search results without duplicates |

The app also includes the 75% advice formula and the attendance streak scan.

## Tech stack

- Java 17 (developed with OpenJDK 26 in IntelliJ IDEA)
- JavaFX 25.0.1 with FXML and CSS
- SQLite through sqlite-jdbc 3.45.1.0
- Maven

## Project structure

## Project structure

```
src/main/java/app/
  Launcher.java         starts the app
  Main.java             loads main.fxml and shows the window
  MainController.java   events, pages, data structures, theme
  Database.java         all SQLite code
  Trie.java             Trie used for name search
  Student.java          record with roll number and name
src/main/resources/
  main.fxml             layout of the three pages
  style.css             colours and theme
```

## How to run

1. Install a recent JDK and Maven. An internet connection is needed the first time so Maven can download JavaFX and the SQLite driver.
2. Open a terminal in the project folder (the one with `pom.xml`).
3. Run:

   ```
   mvn javafx:run
   ```

   The main class is `app.Launcher`. You can also open the folder in IntelliJ IDEA as a Maven project and run `app.Launcher`.

The database file is created automatically at `<home folder>/ClassTrack/classtrack.db`. Delete it to start with an empty class.

## Author

Lachmi Malhi

Supervised by Dr. Bless Lord Y. Agble

School of Computer Science and Technology

/ Every drop counts. Every line of code matters." 🩸
