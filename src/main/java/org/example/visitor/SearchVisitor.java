package org.example.visitor;

import org.example.model.DirectoryNode;
import org.example.model.Element;
import org.example.model.FileNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchVisitor implements Visitor {

    private final String namePart;
    private final long minimumSize;
    private final List<String> matches = new ArrayList<>();

    private Element root;

    public SearchVisitor(String namePart, long minimumSize) {
        if (namePart == null) {
            throw new IllegalArgumentException(
                    "Name criterion cannot be null."
            );
        }

        if (minimumSize < 0) {
            throw new IllegalArgumentException(
                    "Minimum size cannot be negative."
            );
        }

        this.namePart = namePart;
        this.minimumSize = minimumSize;
    }

    public List<String> search(Element root) {
        if (root == null) {
            throw new IllegalArgumentException("Root cannot be null.");
        }

        matches.clear();
        this.root = root;

        root.accept(this);

        Collections.sort(matches);
        return List.copyOf(matches);
    }

    @Override
    public void visit(FileNode file) {
        boolean nameMatches = file.getName().contains(namePart);
        boolean sizeMatches = file.getSize() >= minimumSize;

        if (nameMatches && sizeMatches) {
            matches.add(getRelativePath(file));
        }
    }

    @Override
    public void visit(DirectoryNode directory) {
        for (Element child : directory.getChildren()) {
            child.accept(this);
        }
    }

    private String getRelativePath(FileNode file) {
        if (file == root) {
            return file.getName();
        }

        String path = file.getName();
        DirectoryNode parent = file.getParent();

        while (parent != null && parent != root) {
            path = parent.getName() + "/" + path;
            parent = parent.getParent();
        }

        return path;
    }
}