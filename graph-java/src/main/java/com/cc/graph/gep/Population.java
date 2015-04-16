package com.cc.graph.gep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cc.graph.base.Graph;

public class Population {

    private final List<Chromosome> chromosomes;
    private final int generationNum;

    public static Population generate(Graph graph, int populationNum) {
        List<Chromosome> chromosomes = new ArrayList<Chromosome>(populationNum);
        for (int i = 0; i < populationNum; i++) {
            chromosomes.add(Chromosome.generate(graph));
        }
        return new Population(chromosomes);
    }

    public Population(List<Chromosome> chromosomes) {
        this(chromosomes, 0);
    }

    public Population(List<Chromosome> chromosomes, int generationNum) {
        this.chromosomes = new ArrayList<>(chromosomes);
        this.generationNum = generationNum;
    }

    public List<Chromosome> getChromosomes() {
        return Collections.unmodifiableList(chromosomes);
    }

    public int getGenerationNum() {
        return generationNum;
    }

    public int size() {
        return chromosomes.size();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((chromosomes == null) ? 0 : chromosomes.hashCode());
        result = prime * result + generationNum;
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
        Population other = (Population) obj;
        if (chromosomes == null) {
            if (other.chromosomes != null)
                return false;
        } else if (!chromosomes.equals(other.chromosomes))
            return false;
        if (generationNum != other.generationNum)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Population [chromosomes size:" + chromosomes.size()
                + ", generationNum:" + generationNum + "]";
    }

}
