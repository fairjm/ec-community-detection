package com.cc.graph.base;

public class Edge {

    private final Vertex side1;
    private final Vertex side2;

    public Edge(Vertex side1, Vertex side2) {
        if (side1.equals(side2)) {
            throw new IllegalArgumentException(
                    "side1 should not be equal to side2");
        }
        if (side1.getId().compareTo(side2.getId()) < 0) {
            this.side1 = side1;
            this.side2 = side2;
        } else {

            this.side1 = side2;
            this.side2 = side1;
        }
    }

    public Edge(String side1, String side2) {
        this(new Vertex(side1), new Vertex(side2));
    }

    public boolean containsNode(Vertex node) {
        return side1.equals(node) || side2.equals(node);
    }

    public boolean containsNode(String nodeId) {
        return side1.getId().equals(nodeId) || side2.getId().equals(nodeId);
    }

    public Vertex getSide1() {
        return side1;
    }

    public Vertex getSide2() {
        return side2;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((side1 == null) ? 0 : side1.hashCode());
        result = prime * result + ((side2 == null) ? 0 : side2.hashCode());
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
        Edge other = (Edge) obj;
        if (side1 == null) {
            if (other.side1 != null)
                return false;
        } else if (!side1.equals(other.side1))
            return false;
        if (side2 == null) {
            if (other.side2 != null)
                return false;
        } else if (!side2.equals(other.side2))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Edge [side1=" + side1 + ", side2=" + side2 + "]";
    }

}
