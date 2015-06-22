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
                    r.addEdge(new Edge(e[0], e[1]));
                    return r;
                }, (r1, r2) -> r1);
        return graph.freeze();
    }
}
