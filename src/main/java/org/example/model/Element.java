package org.example.model;

import org.example.visitor.Visitor;

public interface Element {

    String getName();

    DirectoryNode getParent();

    void accept(Visitor visitor);
}