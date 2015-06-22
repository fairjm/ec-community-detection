package com.cc.graph.algorithm.params;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.cc.graph.base.ImmutableGraph;

public class BaseParams implements Params {

    public final List<Set<String>> comms;
    public final ImmutableGraph graph;

    public static BaseParams construct(final List<Set<String>> comms, final ImmutableGraph graph){
        return new BaseParams(comms, graph);
    }

    protected BaseParams(final List<Set<String>> comms, final ImmutableGraph graph) {
        super();
        this.comms = Collections.unmodifiableList(comms);
        this.graph = graph;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.comms == null) ? 0 : this.comms.hashCode());
        result = prime * result + ((this.graph == null) ? 0 : this.graph.hashCode());
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
        final BaseParams other = (BaseParams) obj;
        if (this.comms == null) {
            if (other.comms != null)
                return false;
        } else if (!this.comms.equals(other.comms))
            return false;
        if (this.graph == null) {
            if (other.graph != null)
                return false;
        } else if (!this.graph.equals(other.graph))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "BaseParams [comms=" + this.comms + ", graph=" + this.graph + "]";
    }

}
