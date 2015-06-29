package com.cc.graph.util;

public class Tuple2<T, V> {

    private T t;
    private V v;

    public Tuple2(final T t, final V v) {
        super();
        this.t = t;
        this.v = v;
    }

    public T getT() {
        return this.t;
    }

    public void setT(final T t) {
        this.t = t;
    }

    public V getV() {
        return this.v;
    }

    public void setV(final V v) {
        this.v = v;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.t == null) ? 0 : this.t.hashCode());
        result = prime * result + ((this.v == null) ? 0 : this.v.hashCode());
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
        final Tuple2 other = (Tuple2) obj;
        if (this.t == null) {
            if (other.t != null)
                return false;
        } else if (!this.t.equals(other.t))
            return false;
        if (this.v == null) {
            if (other.v != null)
                return false;
        } else if (!this.v.equals(other.v))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Tuple2 [t=" + this.t + ", v=" + this.v + "]";
    }


}
