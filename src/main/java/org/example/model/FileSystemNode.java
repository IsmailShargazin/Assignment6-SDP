package org.example.model;

public abstract class FileSystemNode implements Element {

    private final String name;
    private DirectoryNode parent;

    protected FileSystemNode(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }

        if (name.contains("/") || name.contains("\\")
                || name.equals(".") || name.equals("..")) {
            throw new IllegalArgumentException("Invalid name: " + name);
        }

        this.name = name;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final DirectoryNode getParent() {
        return parent;
    }

    final void attachTo(DirectoryNode directory) {
        if (directory == null || parent != null) {
            throw new IllegalArgumentException(
                    "Node must have exactly one parent after attachment."
            );
        }

        parent = directory;
    }
}