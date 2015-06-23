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

    public final List<Gene> genes;

    public Chromosome(final List<Gene> genes) {
        this.genes = Collections.unmodifiableList(genes.stream().filter(e -> e.size() > 0)
                .collect(Collectors.toList()));
    }

    public static Chromosome convertComms(final List<Set<String>> list) {
        return new Chromosome(list.stream().map(e -> new Gene(e)).collect(Collectors.toList()));
    }

    public static Chromosome generate(final Graph graph) {
        List<String> nodeIds = new ArrayList<>(graph.vertexIds());
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        Collections.shuffle(nodeIds, random);
        final List<Gene> temps = new ArrayList<Gene>();
        while (nodeIds.size() > 0) {
            final int size = nodeIds.size();
            int position;
            if (size > 5) {
                position = random.nextInt(3, size);
                if (size - position <= 2) {
                    position = size;
                }
                temps.add(new Gene(new HashSet<String>(nodeIds.subList(0, position))));
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
        result = prime * result + ((this.genes == null) ? 0 : this.genes.hashCode());
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
        final Chromosome other = (Chromosome) obj;
        if (this.genes == null) {
            if (other.genes != null)
                return false;
        } else if (!this.genes.equals(other.genes))
            return false;
        return true;
    }

    public List<Set<String>> toCommunityStyle() {
        return this.genes.stream().map(g -> g.nodes).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final Gene g : this.genes) {
            sb.append(g.toString());
        }
        return sb.toString();
    }
}
