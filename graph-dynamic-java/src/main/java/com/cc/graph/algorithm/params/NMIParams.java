package com.cc.graph.algorithm.params;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class NMIParams implements Params {

    public final List<Set<String>> comms;

    public final List<Set<String>> lastTimestampComms;

    public static NMIParams construct(final List<Set<String>> comms,
            final List<Set<String>> lastTimestampComms) {
        return new NMIParams(comms, lastTimestampComms);
    }

    private NMIParams(final List<Set<String>> comms, final List<Set<String>> lastTimestampComms) {
        this.comms = Collections.unmodifiableList(comms);
        this.lastTimestampComms = Collections.unmodifiableList(lastTimestampComms);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.comms == null) ? 0 : this.comms.hashCode());
        result = prime * result
                + ((this.lastTimestampComms == null) ? 0 : this.lastTimestampComms.hashCode());
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
        final NMIParams other = (NMIParams) obj;
        if (this.comms == null) {
            if (other.comms != null)
                return false;
        } else if (!this.comms.equals(other.comms))
            return false;
        if (this.lastTimestampComms == null) {
            if (other.lastTimestampComms != null)
                return false;
        } else if (!this.lastTimestampComms.equals(other.lastTimestampComms))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "NMIParams [comms=" + this.comms + ", lastTimestampComms=" + this.lastTimestampComms + "]";
    }

}
