package com.cc.graph.gep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.cc.graph.Conf;
import com.cc.graph.algorithm.Modularity;
import com.cc.graph.algorithm.params.ModularityParams;
import com.cc.graph.base.GraphUtil;
import com.cc.graph.base.ImmutableGraph;
import com.cc.graph.gep.selection.ModularitySelection;
import com.cc.graph.gep.selection.SelectionResult;
import com.cc.graph.gep.selection.SelectionStrategy;

public class WorkFlow {

    private final SelectionStrategy selection;

    public WorkFlow(final SelectionStrategy selection) {
        this.selection = selection;
    }

    public static final class Result {
        private final List<Population> history = new LinkedList<>();
        private Population lastPopulation;

        public List<Population> getHistory() {
            return Collections.unmodifiableList(this.history);
        }

        public Population getLastPopulation() {
            return this.lastPopulation;
        }

        public Result pushToHistory(final Population pop) {
            this.history.add(0, pop);
            return this;
        }

        public Result updateTheLast(final Population pop) {
            this.lastPopulation = pop;
            return this;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.history == null) ? 0 : this.history.hashCode());
            result = prime * result
                    + ((this.lastPopulation == null) ? 0 : this.lastPopulation.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (this.getClass() != obj.getClass())
                return false;
            final Result other = (Result) obj;
            if (this.history == null) {
                if (other.history != null)
                    return false;
            } else if (!this.history.equals(other.history))
                return false;
            if (this.lastPopulation == null) {
                if (other.lastPopulation != null)
                    return false;
            } else if (!this.lastPopulation.equals(other.lastPopulation))
                return false;
            return true;
        }

    }

    public Result run(final ImmutableGraph graph) {
        final Population initPopulation = Population.generate(graph, Conf.Gep.populationSize);
        return this.innerRun(
                new Result().updateTheLast(initPopulation).pushToHistory(initPopulation), graph,
                Conf.Gep.generationNum);
    }

    public Result innerRun(final Result lastResult, final ImmutableGraph graph,
            final int maxGenerationNum) {
        final Population lastPop = lastResult.lastPopulation;
        System.out.println(lastPop.getGenerationNum() + "/" + maxGenerationNum);
        if (lastPop.getGenerationNum() >= maxGenerationNum) {
            return lastResult;
        } else {
            final List<Chromosome> lastChroms = lastPop.getChromosomes();
            final SelectionResult choosedChroms = this.selection.choose(lastChroms, graph,
                    lastChroms.size() - 1);

            final List<Chromosome> mutatedChroms = choosedChroms.selected.stream()
                    .map(c -> this.operateChromosome(c)).collect(Collectors.toList());

            final Chromosome theRemainedOne = choosedChroms.best.get();
            System.out.println(theRemainedOne);
            final List<Chromosome> newChromosomes = new ArrayList<>(mutatedChroms);
            newChromosomes.add(theRemainedOne);
            final Population population = new Population(newChromosomes,
                    lastPop.getGenerationNum() + 1);
            return this.innerRun(lastResult.pushToHistory(population).updateTheLast(population),
                    graph, maxGenerationNum);
        }
    }

    private Chromosome operateChromosome(final Chromosome chrom) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final List<Gene> ls = new ArrayList<Gene>(chrom.genes.size());
        ls.addAll(chrom.genes);
        if (random.nextDouble() <= Conf.Gep.geneMove) {
            this.doGeneMove(ls);
        }
        if (random.nextDouble() <= Conf.Gep.geneExchange) {
            this.doGeneExchange(ls);
        }
        if (random.nextDouble() <= Conf.Gep.geneMerge) {
            this.doGeneMerge(ls);
        }
        if (random.nextDouble() <= Conf.Gep.geneSplitoff) {
            this.doGeneSplitOff(ls);
        }
        return new Chromosome(new HashSet<>(ls));
    }

    private void doGeneMove(final List<Gene> genes) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        if (genes.size() > 1) {
            final Gene g1 = genes.remove(random.nextInt(genes.size()));
            final Gene g2 = genes.remove(random.nextInt(genes.size()));
            final List<Gene> moved = GeneUtil.move(g1, g2);
            genes.addAll(moved);
        }
    }

    private void doGeneExchange(final List<Gene> genes) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        if (genes.size() > 1) {
            final Gene g1 = genes.remove(random.nextInt(genes.size()));
            final Gene g2 = genes.remove(random.nextInt(genes.size()));
            genes.addAll(GeneUtil.exchange(g1, g2));
        }
    }

    private void doGeneSplitOff(final List<Gene> genes) {
        int maxPosition = 0;
        for (int i = 0; i < genes.size(); i++) {
            if (genes.get(i).size() > genes.get(maxPosition).size()) {
                maxPosition = i;
            }
        }
        final Gene g = genes.remove(maxPosition);
        genes.addAll(GeneUtil.splitoff(g));
    }

    private void doGeneMerge(final List<Gene> genes) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        if (genes.size() > 1) {
            final Gene g1 = genes.remove(random.nextInt(genes.size()));
            final Gene g2 = genes.remove(random.nextInt(genes.size()));
            genes.add(GeneUtil.merge(g1, g2));
        }
    }

    public static void main(final String[] args) throws IOException {
        final WorkFlow workFlow = new WorkFlow(new ModularitySelection());
        final ImmutableGraph graph = GraphUtil.load("src/main/resources/test.txt");
        final WorkFlow.Result r = workFlow.run(graph);
        final Population pop = r.getLastPopulation();
        final List<Chromosome> cs = pop.getChromosomes();
        final List<Chromosome> ccs = new ArrayList<>(cs);
        final List<Double> modularities = ccs.stream().map(c -> {
            final List<Set<String>> communities = c.toCommunityStyle();
            return Modularity.instance.compute(ModularityParams.construct(communities, graph));
        }).collect(Collectors.toList());
        double bestModularity = -1;
        int bestIndex = 0;
        for (int i = 0; i < ccs.size(); i++) {
            final double current = modularities.get(i);
            if (current > bestModularity) {
                bestIndex = i;
                bestModularity = current;
            }
        }
        final Chromosome bestChromo = ccs.get(bestIndex);
        System.out.println("best:");
        System.out.println(bestChromo);
        System.out.println(modularities.get(bestIndex));
        graph.displayCommunity(bestChromo.toCommunityStyle());
    }
}
