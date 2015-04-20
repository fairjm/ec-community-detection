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
import com.cc.graph.base.Graph;
import com.cc.graph.base.GraphUtil;
import com.cc.graph.gep.selection.ModularitySelection;
import com.cc.graph.gep.selection.SelectionResult;
import com.cc.graph.gep.selection.SelectionStrategy;

public class WorkFlow {

    private SelectionStrategy selection;

    public WorkFlow(SelectionStrategy selection) {
        this.selection = selection;
    }

    public static final class Result {
        private final List<Population> history = new LinkedList<>();
        private Population lastPopulation;

        public List<Population> getHistory() {
            return Collections.unmodifiableList(history);
        }

        public Population getLastPopulation() {
            return lastPopulation;
        }

        public Result pushToHistory(Population pop) {
            history.add(0, pop);
            return this;
        }

        public Result updateTheLast(Population pop) {
            lastPopulation = pop;
            return this;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((history == null) ? 0 : history.hashCode());
            result = prime
                    * result
                    + ((lastPopulation == null) ? 0 : lastPopulation.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Result other = (Result) obj;
            if (history == null) {
                if (other.history != null)
                    return false;
            } else if (!history.equals(other.history))
                return false;
            if (lastPopulation == null) {
                if (other.lastPopulation != null)
                    return false;
            } else if (!lastPopulation.equals(other.lastPopulation))
                return false;
            return true;
        }

    }

    public Result run(Graph graph) {
        Population initPopulation = Population.generate(graph,
                Conf.Gep.populationSize);
        return innerRun(new Result().updateTheLast(initPopulation)
                .pushToHistory(initPopulation), graph, Conf.Gep.generationNum);
    }

    public Result innerRun(Result lastResult, Graph graph, int maxGenerationNum) {
        Population lastPop = lastResult.lastPopulation;
        System.out.println(lastPop.getGenerationNum() + "/" + maxGenerationNum);
        if (lastPop.getGenerationNum() >= maxGenerationNum) {
            return lastResult;
        } else {
            List<Chromosome> lastChroms = lastPop.getChromosomes();
            SelectionResult choosedChroms = selection.choose(lastChroms, graph,
                    lastChroms.size() - 1);
            List<Chromosome> mutatedChroms = choosedChroms.selected.stream()
                    .map(c -> operateChromosome(c))
                    .collect(Collectors.toList());
            Chromosome theRemainedOne = choosedChroms.best.get();
            List<Chromosome> newChromosomes = new ArrayList<>(mutatedChroms);
            newChromosomes.add(theRemainedOne);
            Population population = new Population(newChromosomes,
                    lastPop.getGenerationNum() + 1);
            return innerRun(
                    lastResult.pushToHistory(population).updateTheLast(
                            population), graph, maxGenerationNum);
        }
    }

    private Chromosome operateChromosome(Chromosome chrom) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<Gene> ls = new ArrayList<Gene>();
        ls.addAll(chrom.genes);
        if (random.nextDouble() <= Conf.Gep.geneMove) {
            doGeneMove(ls);
        }
        if (random.nextDouble() <= Conf.Gep.geneExchange) {
            doGeneExchange(ls);
        }
        if (random.nextDouble() <= Conf.Gep.geneMerge) {
            doGeneMerge(ls);
        }
        if (random.nextDouble() <= Conf.Gep.geneSplitoff) {
            doGeneSplitOff(ls);
        }
        return new Chromosome(new HashSet<>(ls));
    }

    private void doGeneMove(List<Gene> genes) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (genes.size() > 1) {
            Gene g1 = genes.remove(random.nextInt(genes.size()));
            Gene g2 = genes.remove(random.nextInt(genes.size()));
            List<Gene> moved = GeneUtil.move(g1, g2);
            if (moved.size() == 1) {
                genes.add(moved.get(0));
            } else if (moved.size() == 2) {
                if (moved.get(0).size() < 3 || moved.get(1).size() < 3) {
                    genes.add(GeneUtil.merge(moved.get(0), moved.get(1)));
                } else {
                    genes.addAll(moved);
                }
            }
        }
    }

    private void doGeneExchange(List<Gene> genes) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (genes.size() > 1) {
            Gene g1 = genes.remove(random.nextInt(genes.size()));
            Gene g2 = genes.remove(random.nextInt(genes.size()));
            genes.addAll(GeneUtil.exchange(g1, g2));
        }
    }

    private void doGeneSplitOff(List<Gene> genes) {
        int maxPosition = 0;
        for (int i = 0; i < genes.size(); i++) {
            if (genes.get(i).size() > genes.get(maxPosition).size()) {
                maxPosition = i;
            }
            Gene g = genes.remove(maxPosition);
            genes.addAll(GeneUtil.splitoff(g));
        }
    }

    private void doGeneMerge(List<Gene> genes) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (genes.size() > 1) {
            Gene g1 = genes.remove(random.nextInt(genes.size()));
            Gene g2 = genes.remove(random.nextInt(genes.size()));
            genes.add(GeneUtil.merge(g1, g2));
        }
    }

    public static void main(String[] args) throws IOException {
        WorkFlow workFlow = new WorkFlow(new ModularitySelection());
        Graph graph = GraphUtil.load("src/main/resources/Zachary.txt");
        WorkFlow.Result r = workFlow.run(graph);
        Population pop = r.getLastPopulation();
        List<Chromosome> cs = pop.getChromosomes();
        List<Chromosome> ccs = new ArrayList<>(cs);
        List<Double> modularities = ccs.stream().map(c -> {
            List<Set<String>> communities = c.toCommunityStyle();
            List<Double> result = Modularity.compute(communities, graph);
            return result.stream().mapToDouble(v -> v).sum();
        }).collect(Collectors.toList());
        double bestModularity = -1;
        int bestIndex = 0;
        for (int i = 0; i < ccs.size(); i++) {
            double current = modularities.get(i);
            if (current > bestModularity) {
                bestIndex = i;
                bestModularity = current;
            }
        }
        System.out.println("best:");
        System.out.println(ccs.get(bestIndex));
        System.out.println(modularities.get(bestIndex));
    }
}
