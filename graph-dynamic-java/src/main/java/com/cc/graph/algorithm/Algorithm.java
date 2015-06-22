package com.cc.graph.algorithm;

import com.cc.graph.algorithm.params.Params;

public interface Algorithm<T extends Params> {

    public double compute(T params);

}
