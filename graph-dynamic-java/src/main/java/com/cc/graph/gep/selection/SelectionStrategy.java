package com.cc.graph.gep.selection;

import java.util.List;

import com.cc.graph.base.ImmutableGraph;
import com.cc.graph.gep.Chromosome;

public interface SelectionStrategy {

    SelectionResult choose(List<Chromosome> chroms, ImmutableGraph graph, int chooseNum);

    default SelectionResult choose(final List<Chromosome> chroms,  final ImmutableGraph graph){
        return this.choose(chroms, graph, chroms.size());
    }
}
