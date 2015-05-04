package com.cc.graph.gep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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
        if (sourceSize == 1)
            return Arrays.asList(g2.add(g1));
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String[] sNodes = g1.nodes.toArray(new String[0]);
        String movedValue = sNodes[random.nextInt(g1.size())];
        return Arrays.asList(g1.remove(movedValue), g2.add(movedValue));
    }

    public static Gene merge(Gene g1, Gene g2) {
        return g1.add(g2);
    }

    public static List<Gene> exchange(Gene g1, Gene g2) {
        if (g1.size() == 0) {
            if (g2.size() == 0) {
                return Collections.emptyList();
            } else {
                return Arrays.asList(g2);
            }
        } else {
            if (g2.size() == 0) {
                return Arrays.asList(g1);
            } else {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                String[] g1Nodes = g1.nodes.toArray(new String[0]);
                String[] g2Nodes = g2.nodes.toArray(new String[0]);
                String g1MovedValue = g1Nodes[random.nextInt(g1Nodes.length)];
                String g2MovedValue = g2Nodes[random.nextInt(g2Nodes.length)];
                return Arrays.asList(g1.replace(g1MovedValue, g2MovedValue),
                        g2.replace(g2MovedValue, g1MovedValue));
            }
        }
    }

    public static List<Gene> splitoff(Gene g) {
        int nodeSize = g.size();
        if (nodeSize <= 1) {
            return Arrays.asList(g);
        } else {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int position = random.nextInt(nodeSize / 3, nodeSize * 2 / 3);
            List<String> nodes = new ArrayList<>(g.nodes);
            return Arrays.asList(
                    new Gene(new HashSet<String>(nodes.subList(0, position))),
                    new Gene(new HashSet<String>(nodes.subList(position,
                            nodeSize))));
        }
    }
}
