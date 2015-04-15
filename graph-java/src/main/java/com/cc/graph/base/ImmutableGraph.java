package com.cc.graph.base;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.graphstream.graph.implementations.SingleGraph;

import com.cc.graph.Conf;

public class ImmutableGraph implements Graph {

    private final Set<Edge> edges;

    private final Set<Vertex> vertexes;

    private final SingleGraph displayGraph;

    public ImmutableGraph(Set<Edge> edges, Set<Vertex> vertexes,
            SingleGraph displayGraph) {
        this.edges = Collections.unmodifiableSet(edges);
        this.vertexes = Collections.unmodifiableSet(vertexes);
        this.displayGraph = displayGraph;
    }

    @Override
    public Set<String> vertexIds() {
        return vertexes.stream().map(Vertex::getId).collect(Collectors.toSet());
    }

    @Override
    public Set<Vertex> getVertexs() {
        return vertexes;
    }

    @Override
    public Set<Edge> getEdges() {
        return edges;
    }

    @Override
    public void display() {
        displayGraph.display();
    }

}
