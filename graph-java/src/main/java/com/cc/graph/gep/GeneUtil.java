package com.cc.graph.gep;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GeneUtil {

    /**
     * move single terminal symbol from source to target gene<br/>
     * source: u123 target u4 will result in (u12,u34)
     *
     * @param g1
     * @param g2
     * @return
     */
    public static List<Gene> move(Gene g1, Gene g2) {
        int sourceSize = g1.size();
        if (sourceSize == 0)
            return Arrays.asList(g2);
        if (sourceSize <= 3)
            return Arrays.asList(g2.add(g1));
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String[] sNodes = g1.nodes.toArray(new String[0]);
        String movedValue = sNodes[random.nextInt(g1.size())];
        return Arrays.asList(g1.remove(movedValue), g2.add(movedValue));
    }

    public static Gene merge(Gene g1,Gene g2){
        return g1.add(g2);
    }
}
