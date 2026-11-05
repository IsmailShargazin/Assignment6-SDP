# Assignment 6 — Visitor Pattern

**Student:** Ismail Shargazin  
**Group:** SE-2529  
**Course:** Software Design Patterns

## Purpose

This project models a file system in memory. Directories can contain
files and other directories. The program uses the Visitor pattern
to perform three operations on the same structure.

## Requirements

- Java 17
- Apache Maven

Run all commands from the project root, where pom.xml is located.

## Run the application

```bash
mvn compile
```

```bash
java -cp target/classes org.example.app.Main
```

Expected output:

```text
Total size: 2000 bytes
Repeated calculation: 2000 bytes
Search results:
documents/drafts/plan.txt
Statistics:
Files: 3
Directories: 4
Maximum depth: 3
```

## Run the tests

```bash
mvn test
```

The test class is `org.example.FileSystemTest`.

Verified result:

```text
Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The tests cover dispatch, nested size calculation, search filters,
sorted paths, subtree roots, statistics, empty directories, repeated
operations, and invalid tree changes.

## Main classes

| Class or interface | Responsibility |
| --- | --- |
| Element | Common interface for visited elements |
| FileSystemNode | Stores the name and parent directory |
| FileNode | Represents a file with a size in bytes |
| DirectoryNode | Stores children and protects the tree |
| Visitor | Declares visit methods for files and directories |
| TotalSizeVisitor | Calculates the total file size |
| SearchVisitor | Finds files by name and minimum size |
| StatisticsVisitor | Counts files, directories, and maximum depth |
| Main | Creates sample data and runs the operations |

## Visitor and double dispatch

The client calls `accept(visitor)` on an Element.
Java selects the accept implementation of the concrete element.

That implementation calls `visitor.visit(this)`.
The concrete element type selects the visit overload, and the
concrete visitor provides the operation.

The code does not use instanceof, reflection, or casts to choose
behavior for files and directories.

## Traversal

Visitors own the traversal. Each directory visit passes the same
visitor to its children. Element accept methods only dispatch
the call and do not traverse children.

Each operation visits every reachable element once.
Empty directories are supported.

## Search rules

The filename must contain the configured text.
Matching is case-sensitive. An empty text matches every filename.

The file size must be greater than or equal to the minimum size.
Both conditions must be true.

Paths are relative to the selected root, use `/`, and are sorted
lexicographically. The root directory name is excluded.
If the selected root is a file, its filename is returned when it matches.

## Statistics rules

The directory count includes the selected root when it is a directory.
The root depth is 0. Each child is one level deeper than its parent.
Maximum depth includes both files and directories.

## Visitor state

Visitor instances are reusable through their operation methods:

- TotalSizeVisitor.calculate(root)
- SearchVisitor.search(root)
- StatisticsVisitor.analyze(root)

Each method clears previous state before starting a new operation.
Returned search results are unmodifiable snapshots.

## Tree integrity

The public API rejects negative file sizes, duplicate sibling names,
multiple parents, null children, and cycles.

Names cannot be blank, contain path separators, or equal `.` or `..`.
Names are immutable. Child collections cannot be modified directly.

## Extensibility

StatisticsVisitor was added in a separate commit without changing
FileNode, DirectoryNode, or their accept methods.

New operations can be added as new visitors.
Adding a new element type requires a new method in Visitor and
an implementation of that method in every concrete visitor.

## Study materials

- Course Lecture 9: Visitor Pattern
- Course Lecture 10: SOLID principles and Clean Code