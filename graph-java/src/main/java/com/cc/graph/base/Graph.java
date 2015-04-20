package com.cc.graph.base;

import java.util.Set;

public interface Graph {
    public Set<String> vertexIds();

    public Set<Vertex> getVertexs();

    public Set<Edge> getEdges();

    public void display();

}
