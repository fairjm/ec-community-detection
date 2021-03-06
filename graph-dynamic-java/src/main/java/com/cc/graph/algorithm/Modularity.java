package com.cc.graph.algorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cc.graph.base.Edge;
import com.cc.graph.base.Graph;
import com.cc.graph.base.GraphUtil;
import com.cc.graph.base.ImmutableGraph;
import com.cc.graph.gep.Chromosome;

public class Modularity implements Algorithm {

    private final Graph graph;
    public Modularity(final Graph graph) {
        this.graph = graph;
    }

    @Override
    public double compute(final Chromosome solution) {
        final List<Set<String>> comms = solution.toCommunityStyle();
        final int commSize = comms.size();
        final int eSize = this.graph.getEdges().size();
        final List<Integer> inSizes = new ArrayList<>(commSize);
        final List<Integer> outSizes = new ArrayList<Integer>(commSize);

        for (final Set<String> comm : comms) {
            int outSize = 0;
            final Set<Edge> keeped = new HashSet<Edge>();
            for (final String node : comm) {
                final List<String> neighbors = this.graph.getNeighbors(node);
                for (final String neighbor : neighbors) {
                    if (comm.contains(neighbor)) {
                        keeped.add(new Edge(neighbor, node));
                    } else {
                        outSize += 1;
                    }
                }
            }
            inSizes.add(keeped.size());
            outSizes.add(outSize);
        }

        double result = 0;
        for (int i = 0; i < comms.size(); i++) {
            result += Modularity.compute(outSizes.get(i), inSizes.get(i), eSize);
        }
        return result;
    }

    @Override
    public boolean dominate(final double a, final double b) {
        return a > b;
    }

    private static double compute(final int outSize, final int inSize, final int eSize) {
        return 1.0 * inSize / eSize - Math.pow((1.0 * 2 * inSize + outSize) / (2 * eSize), 2);
    }

    public static void main(final String[] args) throws IOException {
        final ImmutableGraph graph = GraphUtil.load("src/main/resources/Zachary.txt");
        final List<Set<String>> comms = new ArrayList<Set<String>>(2);
        comms.add(new HashSet<>(Arrays.asList("9","10","15","16","19","21","23","24","25","26","27","28","29","30","31","32","33","34")));
        comms.add(new HashSet<>(Arrays.asList("1","2","3","4","5","6","7","8","11","12","13","14","17","18","20","22")));
        final double r = new Modularity(graph).compute(Chromosome.convertComms(comms));
        System.out.println(r);
        graph.display();
    }


}