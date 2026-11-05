package org.example.visitor;

import org.example.model.DirectoryNode;
import org.example.model.FileNode;

public interface Visitor {

    void visit(FileNode file);

    void visit(DirectoryNode directory);
}