package com.cc.graph.base;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GraphUtil {

    public static ImmutableGraph load(final String fileName) throws IOException {
        return GraphUtil.load(fileName, ",");
    }

    public static ImmutableGraph load(final String fileName, final String seperator)
            throws IOException {
        final MutableGraph graph = Files.readAllLines(Paths.get(fileName)).stream()
                .map(l -> l.trim()).filter(l -> l.length() > 0).map(l -> l.split(seperator))
                .filter(cs -> cs.length == 2).reduce(new MutableGraph(), (r, e) -> {
                    r.addEdge(new Edge(e[0].trim(), e[1].trim()));
                    return r;
                }, (r1, r2) -> r1);
        return graph.freeze();
    }

    public static void main(final String[] args) throws IOException {
        final ImmutableGraph graph = GraphUtil.load("src/main/resources/test.txt");
        System.out.println(graph.getVertexs());
        System.out.println(graph.getNeighbors("4"));
        graph.display();
    }
}
