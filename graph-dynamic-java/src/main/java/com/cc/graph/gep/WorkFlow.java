package com.cc.graph.gep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.cc.graph.Conf;
import com.cc.graph.algorithm.Algorithm;
import com.cc.graph.algorithm.Modularity;
import com.cc.graph.algorithm.NMI;
import com.cc.graph.base.GraphUtil;
import com.cc.graph.base.ImmutableGraph;
import com.cc.graph.gep.selection.ModularitySelection;
import com.cc.graph.mo.NSGAII;

public class WorkFlow {

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

    /**
     * 这个方法用于扩展算法 可以在这个方法的实现中增加所需的算法
     *
     * @param graphs
     * @return
     */
    public List<Result> run(final ImmutableGraph... graphs) {
        if (graphs.length == 0) {
            return Collections.emptyList();
        }
        final List<Result> results = new ArrayList<>(graphs.length);

        Chromosome lastSolution = null;

        for (int i = 0; i < graphs.length; i++) {
            Result thisResult;
            final ImmutableGraph graph = graphs[i];
            // timestamp = 0
            if (i == 0) {
                thisResult = this.runTimestamp(graph, new Modularity(graph));
            } else {
                // timestamp > 0
                final Algorithm a1 = new Modularity(graph);
                final Algorithm a2 = new NMI(lastSolution);
                thisResult = this.runTimestamp(graph, a1, a2);

            }
            results.add(thisResult);
            lastSolution = this.getBestChromosome(thisResult.getLastPopulation(), graph);
        }
        return results;
    }

    /**
     * 在最后一代选择最好的结果 这边以模块度为最好的结果训责算法
     *
     * @param pop
     * @param graph
     * @return
     */
    private Chromosome getBestChromosome(final Population pop, final ImmutableGraph graph) {
        return ModularitySelection.instance.choose(pop.getChromosomes(), graph).best.get();
    }

    /**
     * 此方法用于初始化网络对应的种群 并传递给进行元算的innerRun方法
     * @param graph
     * @param algorithms
     * @return
     */
    private Result runTimestamp(final ImmutableGraph graph, final Algorithm... algorithms) {
        final Population initPopulation = Population.generate(graph, Conf.Gep.populationSize);
        return this.innerRun(
                new Result().updateTheLast(initPopulation).pushToHistory(initPopulation),
                Conf.Gep.generationNum, algorithms);
    }

    /**
     * 世代迭代
     * @param lastResult
     * @param maxGenerationNum
     * @param algorithms
     * @return
     */
    private Result innerRun(final Result lastResult, final int maxGenerationNum,
            final Algorithm... algorithms) {
        final Population lastPop = lastResult.lastPopulation;
        System.out.println(lastPop.getGenerationNum() + "/" + maxGenerationNum);
        if (lastPop.getGenerationNum() >= maxGenerationNum) {
            return lastResult;
        } else {
            final List<Chromosome> lastChroms = lastPop.getChromosomes();
            final List<Chromosome> mixedChroms = lastChroms.stream()
                    .map(c -> this.operateChromosome(c)).collect(Collectors.toList());
            mixedChroms.addAll(lastChroms);

            final List<List<Chromosome>> levels = NSGAII.fastNondominatedSort(mixedChroms,
                    algorithms);
            System.out.println(levels.size());
            final List<Chromosome> newChromosomes = new ArrayList<>();
            int index = 0;
            while ((lastChroms.size() - newChromosomes.size()) >= levels.get(index).size()) {
                newChromosomes.addAll(levels.get(index));
                index += 1;
            }
            final int remaining = lastChroms.size() - newChromosomes.size();
            if (remaining != 0) {
                final List<Chromosome> sorted = NSGAII.crowdingDistanceSort(levels.get(index),
                        algorithms);
                newChromosomes.addAll(sorted.subList(0, remaining));
            }
            final Population population = new Population(newChromosomes,
                    lastPop.getGenerationNum() + 1);
            return this.innerRun(lastResult.pushToHistory(population).updateTheLast(population),
                    maxGenerationNum, algorithms);
        }
    }

    /**
     * 染色体操作
     * @param chrom
     * @return
     */
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
        return new Chromosome(ls);
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
        final WorkFlow workFlow = new WorkFlow();
        final ImmutableGraph graph = GraphUtil.load("src/main/resources/Zachary.txt");
        final WorkFlow.Result r = workFlow.run(graph).get(0);
        final Population pop = r.getLastPopulation();
        final List<Chromosome> cs = pop.getChromosomes();
        final List<Chromosome> ccs = new ArrayList<>(cs);
        final Modularity q = new Modularity(graph);
        final List<Double> modularities = ccs.stream().map(c -> q.compute(c))
                .collect(Collectors.toList());
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
