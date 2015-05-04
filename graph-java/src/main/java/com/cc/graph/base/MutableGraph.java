package com.cc.graph.base;

import static com.cc.graph.util.LockUtil.withLock;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import com.cc.graph.Conf;

public final class MutableGraph implements Graph {

    {
        System.setProperty("org.graphstream.ui.renderer",
                "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }

    private final Set<Edge> edges = new HashSet<Edge>();

    private final Set<Vertex> vertexes = new HashSet<Vertex>();

    private final SingleGraph displayGraph = new SingleGraph(Conf.projectName);

    {
        displayGraph.addAttribute("ui.stylesheet", "url('"
                + MutableGraph.class.getResource(".").toString()
                + "stylesheet.css')");
        displayGraph.addAttribute("ui.quality");
        displayGraph.addAttribute("ui.antialias");
    }

    private Lock _lock = new ReentrantLock();

    public void removeVertex(Vertex vertex) {
        withLock(_lock, () -> {
            if (vertexes.contains(vertex)) {
                displayGraph.removeNode(vertex.getId());
                vertexes.remove(vertex);
                edges.removeIf(e -> e.containsNode(vertex));
            }
        });
    }

    public void removeEdge(Edge edge) {
        withLock(_lock, () -> {
            if (edges.contains(edge)) {
                displayGraph.removeEdge(
                        displayGraph.getNode(edge.getSide1().getId()),
                        displayGraph.getNode(edge.getSide2().getId()));
                edges.remove(edge);
            }
        });
    }

    public void addVertex(Vertex vertex) {
        withLock(_lock, () -> {
            if (!vertexes.contains(vertex)) {
                vertexes.add(vertex);
                Node node = displayGraph.addNode(vertex.getId());
                node.addAttribute("ui.label", node.getId());
            }
        });
    }

    public void addEdge(Edge edge) {
        withLock(
                _lock,
                () -> {
                    if (!edges.contains(edge)) {
                        Vertex v1 = edge.getSide1();
                        Vertex v2 = edge.getSide2();
                        addVertex(v1);
                        addVertex(v2);
                        edges.add(edge);
                        displayGraph.addEdge(v1.getId() + v2.getId(),
                                v1.getId(), v2.getId());
                    }
                });
    }

    @Override
    public Set<String> vertexIds() {
        return vertexes.stream().map(Vertex::getId).collect(Collectors.toSet());
    }

    @Override
    public Set<Vertex> getVertexs() {
        return Collections.unmodifiableSet(vertexes);
    }

    @Override
    public Set<Edge> getEdges() {
        return Collections.unmodifiableSet(edges);
    }

    @Override
    public void display() {
        displayGraph.display();
    }

    public ImmutableGraph freeze() {
        return new ImmutableGraph(edges, vertexes, displayGraph);
    }

    @Override
    public String toString() {
        return "MutableGraph [edges size=" + edges.size() + ", vertexes size="
                + vertexes.size() + "]";
    }

    @Override
    public void displayCommunity(List<Set<String>> communities) {
        freeze().displayCommunity(communities);
    }

}
