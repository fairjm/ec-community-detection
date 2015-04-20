package com.cc.graph.gep.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.cc.graph.algorithm.Modularity;
import com.cc.graph.base.Graph;
import com.cc.graph.gep.Chromosome;

public class ModularitySelection implements SelectionStrategy {

    private static final double ANTI_IMPOSITIVE = 0.25;

    @Override
    public SelectionResult choose(List<Chromosome> chroms, Graph graph,
            int chooseNum) {
        int chromsSize = chroms.size();
        if (chromsSize == 0) {
            return new SelectionResult(Optional.empty(), chroms);
        }
        List<Double> modularities = chroms
                .stream()
                .map(c -> {
                    List<Set<String>> communities = c.toCommunityStyle();
                    List<Double> result = Modularity
                            .compute(communities, graph);
                    return result.stream()
                            .mapToDouble(v -> v + ANTI_IMPOSITIVE).sum();
                }).collect(Collectors.toList());

        List<Double> accModularities = new ArrayList<>(chromsSize);
        double acc = 0;
        int bestIndex = 0;
        double best = -1;
        for (int i = 0; i < modularities.size(); i++) {
            double modularity = modularities.get(i);
            if (modularity > best) {
                best = modularity;
                bestIndex = i;
            }
            acc += modularity;
            accModularities.add(acc);
        }
        double maxAccValue = accModularities.get(accModularities.size() - 1);
        List<Chromosome> choosed = new ArrayList<>(chooseNum);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < chooseNum; i++) {
            double choosedValue = random.nextDouble(maxAccValue);
            int j = 0;
            for (; j < chromsSize; j++) {
                if (accModularities.get(j) > choosedValue) {
                    break;
                }
            }
            if (j < chromsSize) {
                choosed.add(chroms.get(j));
            } else {
                choosed.add(chroms.get(chromsSize - 1));
            }
        }
        return new SelectionResult(Optional.of(chroms.get(bestIndex)), choosed);
    }

}
