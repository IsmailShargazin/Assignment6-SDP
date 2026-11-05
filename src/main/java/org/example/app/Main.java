package org.example.app;

import org.example.model.DirectoryNode;
import org.example.model.FileNode;
import org.example.visitor.SearchVisitor;
import org.example.visitor.StatisticsVisitor;
import org.example.visitor.TotalSizeVisitor;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        DirectoryNode root = new DirectoryNode("project");
        DirectoryNode documents = new DirectoryNode("documents");
        DirectoryNode drafts = new DirectoryNode("drafts");
        DirectoryNode empty = new DirectoryNode("empty");

        FileNode notes = new FileNode("notes.txt", 200);
        FileNode report = new FileNode("report.pdf", 1500);
        FileNode plan = new FileNode("plan.txt", 300);

        root.addChild(notes);
        root.addChild(documents);
        root.addChild(empty);

        documents.addChild(report);
        documents.addChild(drafts);

        drafts.addChild(plan);

        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();

        long totalSize = sizeVisitor.calculate(root);
        System.out.println("Total size: " + totalSize + " bytes");

        long repeatedSize = sizeVisitor.calculate(root);
        System.out.println(
                "Repeated calculation: " + repeatedSize + " bytes"
        );

        SearchVisitor searchVisitor = new SearchVisitor(".txt", 250);
        List<String> foundFiles = searchVisitor.search(root);

        System.out.println("Search results:");

        for (String path : foundFiles) {
            System.out.println(path);
        }

        StatisticsVisitor statisticsVisitor = new StatisticsVisitor();
        statisticsVisitor.analyze(root);

        System.out.println("Statistics:");
        System.out.println("Files: " + statisticsVisitor.getFileCount());
        System.out.println(
                "Directories: " + statisticsVisitor.getDirectoryCount()
        );
        System.out.println(
                "Maximum depth: " + statisticsVisitor.getMaxDepth()
        );
    }
}