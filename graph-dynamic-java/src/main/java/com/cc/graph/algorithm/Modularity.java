package com.cc.graph.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cc.graph.algorithm.params.BaseParams;
import com.cc.graph.base.Edge;
import com.cc.graph.base.Graph;

public class Modularity implements Algorithm<BaseParams> {

    public static final Modularity instance  = new Modularity();

    private Modularity() {
    }

    @Override
    public double compute(final BaseParams params) {
        final List<Set<String>> comms = params.comms;
        final Graph graph = params.graph;
        final int commSize = comms.size();
        final Set<Edge> remainingEdges = new HashSet<>(graph.getEdges());
        final int eSize = remainingEdges.size();
        final List<Integer> inSizes = new ArrayList<>(commSize);
        comms.forEach(comm -> {
            int inCount = 0;
            for (final String n1 : comm) {
                for (final String n2 : comm) {
                    if (n1.compareTo(n2) < 0) {
                        final Edge edge = new Edge(n1, n2);
                        if (remainingEdges.contains(edge)) {
                            inCount = inCount + 1;
                            remainingEdges.remove(edge);
                        }
                    }
                }
            }
            inSizes.add(inCount);
        });

        final List<Integer> outSizes = new ArrayList<Integer>(commSize);
        comms.forEach(comm -> {
            int outCount = 0;
            for (final String n : comm) {
                for (final Edge e : remainingEdges) {
                    if (e.containsNode(n)) {
                        outCount = outCount + 1;
                    }
                }
            }
            outSizes.add(outCount);
        });

        double result = 0;
        for (int i = 0; i < comms.size(); i++) {
            result += Modularity.compute(outSizes.get(i), inSizes.get(i), eSize);
        }
        return result;
    }

    private static double compute(final int outSize, final int inSize, final int eSize) {
        return 1.0 * inSize / eSize - Math.pow((1.0 * 2 * inSize + outSize) / (2 * eSize), 2);
    }
}