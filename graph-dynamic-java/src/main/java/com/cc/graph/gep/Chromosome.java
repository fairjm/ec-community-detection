package com.cc.graph.gep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.cc.graph.base.Graph;

public class Chromosome {

    public final Set<Gene> genes;

    public Chromosome(Set<Gene> genes) {
        this.genes = Collections.unmodifiableSet(genes.stream()
                .filter(e -> e.size() > 0).collect(Collectors.toSet()));
    }

    public static Chromosome generate(Graph graph) {
        List<String> nodeIds = new ArrayList<>(graph.vertexIds());
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Collections.shuffle(nodeIds, random);
        Set<Gene> temps = new HashSet<Gene>();
        while (nodeIds.size() > 0) {
            int size = nodeIds.size();
            int position;
            if (size > 5) {
                position = random.nextInt(3, size);
                if (size - position <= 2) {
                    position = size;
                }
                temps.add(new Gene(new HashSet<String>(nodeIds.subList(0,
                        position))));
                nodeIds = nodeIds.subList(position, size);
            } else {
                temps.add(new Gene(new HashSet<String>(nodeIds)));
                nodeIds = Collections.emptyList();
            }
        }
        return new Chromosome(temps);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((genes == null) ? 0 : genes.hashCode());
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
        Chromosome other = (Chromosome) obj;
        if (genes == null) {
            if (other.genes != null)
                return false;
        } else if (!genes.equals(other.genes))
            return false;
        return true;
    }

    public List<Set<String>> toCommunityStyle() {
        return genes.stream().map(g -> g.nodes).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Gene g : genes) {
            sb.append(g.toString());
        }
        return sb.toString();
    }
}
