package org.example.visitor;

import org.example.model.DirectoryNode;
import org.example.model.Element;
import org.example.model.FileNode;

public class StatisticsVisitor implements Visitor {

    private int fileCount;
    private int directoryCount;
    private int maxDepth;
    private int currentDepth;

    public void analyze(Element root) {
        if (root == null) {
            throw new IllegalArgumentException("Root cannot be null.");
        }

        fileCount = 0;
        directoryCount = 0;
        maxDepth = 0;
        currentDepth = 0;

        root.accept(this);
    }

    @Override
    public void visit(FileNode file) {
        fileCount++;
        maxDepth = Math.max(maxDepth, currentDepth);
    }

    @Override
    public void visit(DirectoryNode directory) {
        directoryCount++;
        maxDepth = Math.max(maxDepth, currentDepth);

        currentDepth++;

        for (Element child : directory.getChildren()) {
            child.accept(this);
        }

        currentDepth--;
    }

    public int getFileCount() {
        return fileCount;
    }

    public int getDirectoryCount() {
        return directoryCount;
    }

    public int getMaxDepth() {
        return maxDepth;
    }
}