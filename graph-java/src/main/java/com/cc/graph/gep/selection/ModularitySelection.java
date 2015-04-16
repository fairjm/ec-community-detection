package com.cc.graph.gep.selection;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.cc.graph.algorithm.Modularity;
import com.cc.graph.base.Graph;
import com.cc.graph.gep.Chromosome;

public class ModularitySelection implements ChromosomeSelection {

    private static final double ANTI_IMPOSITIVE = 0.25;

    @Override
    public SelectionResult choose(List<Chromosome> chroms, Graph graph,
            int chooseNum) {
        if (chroms.size() == 0) {
            return new SelectionResult(Optional.empty(), chroms);
        }
        List<Double> modularities = chroms
                .stream()
                .map(c -> {
                    List<Set<String>> communities = c.genes.stream()
                            .map(g -> g.nodes).collect(Collectors.toList());
                    List<Double> result = Modularity
                            .compute(communities, graph);
                    return result.stream()
                            .mapToDouble(v -> v + ANTI_IMPOSITIVE).sum();
                }).collect(Collectors.toList());

        //TODO not implemented yet
        return null;
    }

}
