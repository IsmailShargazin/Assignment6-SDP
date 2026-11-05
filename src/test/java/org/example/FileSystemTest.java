package org.example;

import org.example.model.DirectoryNode;
import org.example.model.Element;
import org.example.model.FileNode;
import org.example.visitor.SearchVisitor;
import org.example.visitor.StatisticsVisitor;
import org.example.visitor.TotalSizeVisitor;
import org.example.visitor.Visitor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileSystemTest {

    @Test
    void acceptCallsCorrectVisitMethods() {
        List<String> calls = new ArrayList<>();

        Visitor visitor = new Visitor() {
            @Override
            public void visit(FileNode file) {
                calls.add("file:" + file.getName());
            }

            @Override
            public void visit(DirectoryNode directory) {
                calls.add("directory:" + directory.getName());
            }
        };

        Element file = new FileNode("test.txt", 10);
        Element directory = new DirectoryNode("folder");

        file.accept(visitor);
        directory.accept(visitor);

        assertEquals(
                List.of("file:test.txt", "directory:folder"),
                calls
        );
    }

    @Test
    void totalSizeIncludesFilesAtDifferentDepths() {
        DirectoryNode root = createTree();
        TotalSizeVisitor visitor = new TotalSizeVisitor();

        assertEquals(1030L, visitor.calculate(root));
    }

    @Test
    void searchCombinesNameAndInclusiveSizeConditions() {
        DirectoryNode root = createTree();
        SearchVisitor visitor = new SearchVisitor(".txt", 250);

        assertEquals(
                List.of("docs/a.txt"),
                visitor.search(root)
        );
    }

    @Test
    void searchReturnsSortedRootRelativePaths() {
        DirectoryNode root = createTree();
        SearchVisitor visitor = new SearchVisitor(".txt", 0);

        assertEquals(
                List.of("docs/a.txt", "z.txt"),
                visitor.search(root)
        );
    }

    @Test
    void searchUsesChosenSubtreeAsRoot() {
        DirectoryNode outer = new DirectoryNode("outer");
        DirectoryNode chosen = new DirectoryNode("chosen");
        DirectoryNode nested = new DirectoryNode("nested");

        outer.addChild(chosen);
        chosen.addChild(nested);
        nested.addChild(new FileNode("file.txt", 50));
        outer.addChild(new FileNode("outside.txt", 100));

        SearchVisitor visitor = new SearchVisitor(".txt", 0);

        assertEquals(
                List.of("nested/file.txt"),
                visitor.search(chosen)
        );
    }

    @Test
    void statisticsCountElementsAndMaximumDepth() {
        DirectoryNode root = createTree();
        StatisticsVisitor visitor = new StatisticsVisitor();

        visitor.analyze(root);

        assertEquals(3, visitor.getFileCount());
        assertEquals(4, visitor.getDirectoryCount());
        assertEquals(3, visitor.getMaxDepth());
    }

    @Test
    void emptyDirectoryIsHandledByAllVisitors() {
        DirectoryNode root = new DirectoryNode("empty");

        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        SearchVisitor searchVisitor = new SearchVisitor("", 0);
        StatisticsVisitor statisticsVisitor = new StatisticsVisitor();

        assertEquals(0L, sizeVisitor.calculate(root));
        assertTrue(searchVisitor.search(root).isEmpty());

        statisticsVisitor.analyze(root);

        assertEquals(0, statisticsVisitor.getFileCount());
        assertEquals(1, statisticsVisitor.getDirectoryCount());
        assertEquals(0, statisticsVisitor.getMaxDepth());
    }

    @Test
    void searchReturnsEmptyListWhenNothingMatches() {
        DirectoryNode root = createTree();
        SearchVisitor visitor = new SearchVisitor(".jpg", 0);

        assertTrue(visitor.search(root).isEmpty());
    }

    @Test
    void repeatedOperationsResetVisitorState() {
        DirectoryNode root = createTree();
        FileNode singleFile = new FileNode("single.bin", 9);

        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();

        assertEquals(1030L, sizeVisitor.calculate(root));
        assertEquals(1030L, sizeVisitor.calculate(root));
        assertEquals(9L, sizeVisitor.calculate(singleFile));

        SearchVisitor searchVisitor = new SearchVisitor(".txt", 0);

        List<String> firstResults = searchVisitor.search(root);

        assertEquals(firstResults, searchVisitor.search(root));
        assertTrue(searchVisitor.search(singleFile).isEmpty());
        assertEquals(List.of("docs/a.txt", "z.txt"), firstResults);

        StatisticsVisitor statisticsVisitor = new StatisticsVisitor();

        statisticsVisitor.analyze(root);
        statisticsVisitor.analyze(root);

        assertEquals(3, statisticsVisitor.getFileCount());
        assertEquals(4, statisticsVisitor.getDirectoryCount());
        assertEquals(3, statisticsVisitor.getMaxDepth());

        statisticsVisitor.analyze(singleFile);

        assertEquals(1, statisticsVisitor.getFileCount());
        assertEquals(0, statisticsVisitor.getDirectoryCount());
        assertEquals(0, statisticsVisitor.getMaxDepth());
    }

    @Test
    void negativeFileSizeIsRejectedAndZeroIsAllowed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FileNode("invalid.txt", -1)
        );

        FileNode emptyFile = new FileNode("empty.txt", 0);

        assertEquals(0L, emptyFile.getSize());
    }

    @Test
    void duplicateSiblingNamesAreRejected() {
        DirectoryNode root = new DirectoryNode("root");
        FileNode original = new FileNode("same", 10);
        FileNode duplicateFile = new FileNode("same", 20);
        DirectoryNode duplicateDirectory = new DirectoryNode("same");

        root.addChild(original);

        assertThrows(
                IllegalArgumentException.class,
                () -> root.addChild(duplicateFile)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> root.addChild(duplicateDirectory)
        );

        assertEquals(1, root.getChildren().size());
        assertSame(original, root.getChildren().get(0));
        assertNull(duplicateFile.getParent());
        assertNull(duplicateDirectory.getParent());
    }

    @Test
    void elementCannotBelongToTwoParents() {
        DirectoryNode first = new DirectoryNode("first");
        DirectoryNode second = new DirectoryNode("second");
        FileNode file = new FileNode("shared.txt", 100);

        first.addChild(file);

        assertThrows(
                IllegalArgumentException.class,
                () -> second.addChild(file)
        );

        assertSame(first, file.getParent());
        assertEquals(1, first.getChildren().size());
        assertTrue(second.getChildren().isEmpty());
    }

    @Test
    void cyclesAndSelfInsertionAreRejected() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode child = new DirectoryNode("child");

        root.addChild(child);

        assertThrows(
                IllegalArgumentException.class,
                () -> child.addChild(root)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> root.addChild(root)
        );

        assertNull(root.getParent());
        assertSame(root, child.getParent());
        assertEquals(1, root.getChildren().size());
        assertTrue(child.getChildren().isEmpty());
    }

    @Test
    void childrenCannotBeModifiedThroughReturnedList() {
        DirectoryNode root = new DirectoryNode("root");
        FileNode file = new FileNode("protected.txt", 40);

        root.addChild(file);

        assertThrows(
                UnsupportedOperationException.class,
                () -> root.getChildren().clear()
        );

        assertEquals(1, root.getChildren().size());
        assertSame(file, root.getChildren().get(0));
        assertSame(root, file.getParent());
    }

    @Test
    void nullChildIsRejected() {
        DirectoryNode root = new DirectoryNode("root");

        assertThrows(
                IllegalArgumentException.class,
                () -> root.addChild(null)
        );

        assertTrue(root.getChildren().isEmpty());
    }

    private DirectoryNode createTree() {
        DirectoryNode root = new DirectoryNode("workspace");
        DirectoryNode docs = new DirectoryNode("docs");
        DirectoryNode archive = new DirectoryNode("archive");
        DirectoryNode empty = new DirectoryNode("empty");

        root.addChild(new FileNode("z.txt", 80));
        root.addChild(docs);
        root.addChild(empty);

        docs.addChild(new FileNode("a.txt", 250));
        docs.addChild(archive);

        archive.addChild(new FileNode("b.pdf", 700));

        return root;
    }
}