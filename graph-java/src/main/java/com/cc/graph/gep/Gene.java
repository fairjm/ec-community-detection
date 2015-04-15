package com.cc.graph.gep;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Gene is immutable.<br/>
 * every operation in Gene will result in a new Gene
 *
 * @author fairjm
 *
 */
public class Gene {

    public final Set<String> nodes;

    public Gene(Set<String> nodes) {
        this.nodes = Collections.unmodifiableSet(nodes);
    }

    public Gene add(String v) {
        if (nodes.contains(v)) {
            return this;
        }
        Set<String> newNodes = new HashSet<>(nodes);
        newNodes.add(v);
        return new Gene(newNodes);
    }

    public Gene add(Gene g) {
        if (nodes.containsAll(g.nodes)) {
            return this;
        }
        Set<String> newNodes = new HashSet<>(nodes);
        newNodes.addAll(g.nodes);
        return new Gene(newNodes);
    }

    public Gene remove(String v) {
        if (!nodes.contains(v)) {
            return this;
        }
        Set<String> newNodes = new HashSet<>(nodes);
        newNodes.remove(v);
        return new Gene(newNodes);
    }

    public int size(){
        return nodes.size();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
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
        Gene other = (Gene) obj;
        if (nodes == null) {
            if (other.nodes != null)
                return false;
        } else if (!nodes.equals(other.nodes))
            return false;
        return true;
    }

    @Override
    public String toString() {
        if (nodes.size() == 0) {
            return "u{}";
        }
        StringBuilder sb = new StringBuilder("u{");
        for (String n : nodes) {
            sb.append(n + ",");
        }
        return sb.substring(0, sb.length() - 1) + "}";
    }
}
