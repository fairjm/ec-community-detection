package com.cc.graph.base;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.graphstream.graph.implementations.SingleGraph;

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

    @Override
    public void displayCommunity(List<Set<String>> communities) {
        displayGraph
                .getEdgeSet()
                .stream()
                .forEach(
                        e -> e.addAttribute("ui.style",
                                "fill-color: rgba(0,0,0,128);"));
        displayGraph.getNodeSet().stream()
                .forEach(n -> n.addAttribute("ui.style", "fill-color: white;"));
        Set<String> colors = new HashSet<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (Set<String> comm : communities) {
            String color = null;
            do {
                color = (random.nextInt(256) + "," + random.nextInt(256) + "," + random
                        .nextInt(256));
            } while (colors.contains(color));
            colors.add(color);
            String colorString = "rgb( " + color + ")";
            for (String node1 : comm) {
                for (String node2 : comm) {
                    if (node1.compareTo(node2) < 0) {
                        Edge edge = new Edge(node1, node2);
                        if (edges.contains(edge)) {
                            displayGraph.getEdge(node1 + node2).addAttribute(
                                    "ui.style",
                                    "fill-color:" + colorString + ";");
                        }
                    }
                    displayGraph.getNode(node1).addAttribute("ui.style",
                            "fill-color:" + colorString + ";");

                }
            }
        }
        display();
    }
}
