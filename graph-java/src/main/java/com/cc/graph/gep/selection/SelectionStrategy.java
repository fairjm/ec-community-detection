package com.cc.graph.gep.selection;

import java.util.List;

import com.cc.graph.base.Graph;
import com.cc.graph.gep.Chromosome;

public interface SelectionStrategy {

    SelectionResult choose(List<Chromosome> chroms, Graph graph, int chooseNum);

    default SelectionResult choose(List<Chromosome> chroms,  Graph graph){
        return choose(chroms, graph, chroms.size());
    }
}
