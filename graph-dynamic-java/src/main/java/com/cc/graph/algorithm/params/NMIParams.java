package com.cc.graph.algorithm.params;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.cc.graph.base.ImmutableGraph;

public final class NMIParams extends BaseParams implements Params {

    public final List<Set<String>> lastTimestampComms;

    public static NMIParams construct(final List<Set<String>> comms, final ImmutableGraph graph,
            final List<Set<String>> lastTimestampComms) {
        return new NMIParams(comms, graph, lastTimestampComms);
    }

    private NMIParams(final List<Set<String>> comms, final ImmutableGraph graph,
            final List<Set<String>> lastTimestampComms) {
        super(comms, graph);
        this.lastTimestampComms = Collections.unmodifiableList(lastTimestampComms);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((this.lastTimestampComms == null) ? 0 : this.lastTimestampComms.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        final NMIParams other = (NMIParams) obj;
        if (this.lastTimestampComms == null) {
            if (other.lastTimestampComms != null)
                return false;
        } else if (!this.lastTimestampComms.equals(other.lastTimestampComms))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "NMIParams [lastTimestampComms=" + this.lastTimestampComms + "]";
    }

}
