package com.cc.graph.algorithm;

import com.cc.graph.gep.Chromosome;

public interface Algorithm {

    public double compute(final Chromosome solution);


    /**
     * 用来判断对于用这个算法结果A是否好于结果B 若结果A好于结果B 则返回true<br/>
     * 采用这个方法的主要目的是因为 一些函数是以函数值越小为越优的
     *
     * @param a
     * @param b
     * @return
     */
    public boolean dominate(double a, double b);

}
