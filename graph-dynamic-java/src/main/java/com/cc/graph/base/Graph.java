package com.cc.graph.base;

import java.util.List;
import java.util.Set;

public interface Graph {
    public Set<String> vertexIds();

    public Set<Vertex> getVertexs();

    public Set<Edge> getEdges();

    public void display();

    public void displayCommunity(List<Set<String>> communities);

    public List<String> getNeighbors(String nodeId);

    public List<String> getNeighbors(Vertex node);

}
