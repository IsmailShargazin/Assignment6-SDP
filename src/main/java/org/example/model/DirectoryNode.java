package org.example.model;

import org.example.visitor.Visitor;

import java.util.ArrayList;
import java.util.List;

public final class DirectoryNode extends FileSystemNode {

    private final List<FileSystemNode> children = new ArrayList<>();

    public DirectoryNode(String name) {
        super(name);
    }

    public void addChild(FileSystemNode child) {
        if (child == null) {
            throw new IllegalArgumentException("Child cannot be null.");
        }

        if (child.getParent() != null) {
            throw new IllegalArgumentException(
                    "Element already belongs to a directory."
            );
        }

        DirectoryNode current = this;

        while (current != null) {
            if (current == child) {
                throw new IllegalArgumentException(
                        "Adding this element would create a cycle."
                );
            }

            current = current.getParent();
        }

        for (FileSystemNode existing : children) {
            if (existing.getName().equals(child.getName())) {
                throw new IllegalArgumentException(
                        "Duplicate name: " + child.getName()
                );
            }
        }

        child.attachTo(this);
        children.add(child);
    }

    public List<FileSystemNode> getChildren() {
        return List.copyOf(children);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}