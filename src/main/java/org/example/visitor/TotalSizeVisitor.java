package org.example.visitor;

import org.example.model.DirectoryNode;
import org.example.model.Element;
import org.example.model.FileNode;

public class TotalSizeVisitor implements Visitor {

    private long totalSize;

    public long calculate(Element root) {
        if (root == null) {
            throw new IllegalArgumentException("Root cannot be null.");
        }

        totalSize = 0;
        root.accept(this);

        return totalSize;
    }

    @Override
    public void visit(FileNode file) {
        totalSize = Math.addExact(totalSize, file.getSize());
    }

    @Override
    public void visit(DirectoryNode directory) {
        for (Element child : directory.getChildren()) {
            child.accept(this);
        }
    }
}