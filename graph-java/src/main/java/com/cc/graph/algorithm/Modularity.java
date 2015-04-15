package com.cc.graph.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cc.graph.base.Edge;
import com.cc.graph.base.Graph;

public class Modularity {

    public static List<Double> compute(List<Set<String>> comms, Graph graph) {
        int commSize = comms.size();
        Set<Edge> remainingEdges = new HashSet<>(graph.getEdges());
        int eSize = remainingEdges.size();
        List<Integer> inSizes = new ArrayList<>(commSize);
        comms.forEach(comm -> {
            int inCount = 0;
            for (String n1 : comm) {
                for (String n2 : comm) {
                    if (n1.compareTo(n2) < 0) {
                        Edge edge = new Edge(n1, n2);
                        if (remainingEdges.contains(edge)) {
                            inCount = inCount + 1;
                            remainingEdges.remove(edge);
                        }
                    }
                }
            }
            inSizes.add(inCount);
        });

        List<Integer> outSizes = new ArrayList<Integer>(commSize);
        comms.forEach(comm -> {
            int outCount = 0;
            for (String n : comm) {
                for (Edge e : remainingEdges) {
                    if (e.containsNode(n)) {
                        outCount = outCount + 1;
                    }
                }
            }
            outSizes.add(outCount);
        });

        List<Double> modularities = new ArrayList<>(commSize);
        for (int i = 0; i < comms.size(); i++) {
            modularities.add(compute(outSizes.get(i), inSizes.get(i), eSize));
        }
        return modularities;
    }

    private static double compute(int outSize, int inSize, int eSize) {
        return 1.0 * inSize / eSize
                - Math.pow((1.0 * 2 * inSize + outSize) / (2 * eSize), 2);
    }
}