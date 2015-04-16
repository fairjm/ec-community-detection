package com.cc.graph.gep.selection;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.cc.graph.gep.Chromosome;

public class SelectionResult {

    public final Optional<Chromosome> best;
    public final List<Chromosome> selected;

    public SelectionResult(Optional<Chromosome> best, List<Chromosome> selected) {
        this.best = best;
        this.selected = Collections.unmodifiableList(selected);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((best == null) ? 0 : best.hashCode());
        result = prime * result
                + ((selected == null) ? 0 : selected.hashCode());
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
        SelectionResult other = (SelectionResult) obj;
        if (best == null) {
            if (other.best != null)
                return false;
        } else if (!best.equals(other.best))
            return false;
        if (selected == null) {
            if (other.selected != null)
                return false;
        } else if (!selected.equals(other.selected))
            return false;
        return true;
    }

}
