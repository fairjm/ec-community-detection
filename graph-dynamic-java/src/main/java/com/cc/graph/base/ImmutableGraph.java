package com.cc.graph.base;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import com.cc.graph.Conf;
import com.cc.graph.util.LockUtil;

public class ImmutableGraph implements Graph {

    private final Set<Edge> edges;

    private final Set<Vertex> vertexes;

    private final SingleGraph displayGraph;

    public ImmutableGraph(final Set<Edge> edges, final Set<Vertex> vertexes, final SingleGraph displayGraph) {
        this.edges = Collections.unmodifiableSet(edges);
        this.vertexes = Collections.unmodifiableSet(vertexes);
        this.displayGraph = displayGraph;
    }

    @Override
    public Set<String> vertexIds() {
        return this.vertexes.stream().map(Vertex::getId).collect(Collectors.toSet());
    }

    @Override
    public Set<Vertex> getVertexs() {
        return this.vertexes;
    }

    @Override
    public Set<Edge> getEdges() {
        return this.edges;
    }

    @Override
    public void display() {
        this.displayGraph.display();
    }

    @Override
    public void displayCommunity(final List<Set<String>> communities) {
        this.displayGraph.getEdgeSet().stream()
        .forEach(e -> e.addAttribute("ui.style", "fill-color: rgba(0,0,0,128);"));
        this.displayGraph.getNodeSet().stream()
        .forEach(n -> n.addAttribute("ui.style", "fill-color: white;"));
        final Set<String> colors = new HashSet<>();
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        for (final Set<String> comm : communities) {
            String color = null;
            do {
                color = (random.nextInt(256) + "," + random.nextInt(256) + "," + random
                        .nextInt(256));
            } while (colors.contains(color));
            colors.add(color);
            final String colorString = "rgb( " + color + ")";
            for (final String node1 : comm) {
                for (final String node2 : comm) {
                    if (node1.compareTo(node2) < 0) {
                        final Edge edge = new Edge(node1, node2);
                        if (this.edges.contains(edge)) {
                            this.displayGraph.getEdge(node1 + "edges" + node2).addAttribute("ui.style",
                                    "fill-color:" + colorString + ";");
                        }
                    }
                    this.displayGraph.getNode(node1).addAttribute("ui.style",
                            "fill-color:" + colorString + ";");

                }
            }
        }
        this.display();
    }
}

class MutableGraph implements Graph {

    {
        System.setProperty("org.graphstream.ui.renderer",
                "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    }

    private final Set<Edge> edges = new HashSet<Edge>();

    private final Set<Vertex> vertexes = new HashSet<Vertex>();

    private final SingleGraph displayGraph = new SingleGraph(Conf.projectName);

    {
        this.displayGraph.addAttribute("ui.stylesheet", "url('"
                + MutableGraph.class.getResource(".").toString() + "stylesheet.css')");
        this.displayGraph.addAttribute("ui.quality");
        this.displayGraph.addAttribute("ui.antialias");
    }

    private final Lock _lock = new ReentrantLock();

    public void removeVertex(final Vertex vertex) {
        LockUtil.withLock(this._lock, () -> {
            if (this.vertexes.contains(vertex)) {
                this.displayGraph.removeNode(vertex.getId());
                this.vertexes.remove(vertex);
                this.edges.removeIf(e -> e.containsNode(vertex));
            }
        });
    }

    public void removeEdge(final Edge edge) {
        LockUtil.withLock(this._lock, () -> {
            if (this.edges.contains(edge)) {
                this.displayGraph.removeEdge(this.displayGraph.getNode(edge.getSide1().getId()),
                        this.displayGraph.getNode(edge.getSide2().getId()));
                this.edges.remove(edge);
            }
        });
    }

    public void addVertex(final Vertex vertex) {
        LockUtil.withLock(this._lock, () -> {
            if (!this.vertexes.contains(vertex)) {
                this.vertexes.add(vertex);
                final Node node = this.displayGraph.addNode(vertex.getId());
                node.addAttribute("ui.label", node.getId());
            }
        });
    }

    public void addEdge(final Edge edge) {
        LockUtil.withLock(this._lock, () -> {
            if (!this.edges.contains(edge)) {
                final Vertex v1 = edge.getSide1();
                final Vertex v2 = edge.getSide2();
                this.addVertex(v1);
                this.addVertex(v2);
                this.edges.add(edge);
                this.displayGraph.addEdge(v1.getId() + "edges" + v2.getId(), v1.getId(), v2.getId());
            }
        });
    }

    @Override
    public Set<String> vertexIds() {
        return this.vertexes.stream().map(Vertex::getId).collect(Collectors.toSet());
    }

    @Override
    public Set<Vertex> getVertexs() {
        return Collections.unmodifiableSet(this.vertexes);
    }

    @Override
    public Set<Edge> getEdges() {
        return Collections.unmodifiableSet(this.edges);
    }

    @Override
    public void display() {
        this.displayGraph.display();
    }

    public ImmutableGraph freeze() {
        return new ImmutableGraph(this.edges, this.vertexes, this.displayGraph);
    }

    @Override
    public String toString() {
        return "MutableGraph [edges size=" + this.edges.size() + ", vertexes size=" + this.vertexes.size()
                + "]";
    }

    @Override
    public void displayCommunity(final List<Set<String>> communities) {
        this.freeze().displayCommunity(communities);
    }

}
