package com.cc.graph.mo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.cc.graph.algorithm.Algorithm;
import com.cc.graph.gep.Chromosome;
import com.cc.graph.gep.Population;

public class NSGAII {

    public static List<List<Chromosome>> fastNondominatedSort(final Population pop,
            final Algorithm... algorithms) {
        // 如果只有一个算法 那么没必要求支配集
        if (pop.size() <= 1 || algorithms.length == 1) {
            final List<List<Chromosome>> r = new ArrayList<>();
            r.add(pop.getChromosomes());
            return r;
        }
        // 记录下标元素的被支配次数
        final Map<Integer, Integer> dominatedNum = new HashMap<>();
        // 记录下标元素支配的元素的下标集合
        final Map<Integer, List<Integer>> dominateSet = new HashMap<>();
        final List<Chromosome> chroms = pop.getChromosomes();
        for (int i = 0; i < chroms.size(); i++) {
            for (int j = i + 1; j < chroms.size(); j++) {
                dominateSet.putIfAbsent(i, new LinkedList<>());
                dominateSet.putIfAbsent(j, new LinkedList<>());
                dominatedNum.putIfAbsent(i, 0);
                dominatedNum.putIfAbsent(j, 0);
                if (NSGAII.dominated(chroms.get(i), chroms.get(j), algorithms)) {
                    dominateSet.get(i).add(j);
                    final int num = dominatedNum.getOrDefault(j, 0);
                    dominatedNum.put(j, num + 1);
                } else if (NSGAII.dominated(chroms.get(j), chroms.get(i), algorithms)) {
                    dominateSet.get(j).add(i);
                    final int num = dominatedNum.getOrDefault(i, 0);
                    dominatedNum.put(i, num + 1);
                }
            }
        }

        final List<List<Integer>> levels = new ArrayList<>();
        // 先得到level1
        final List<Integer> level1 = new ArrayList<>();
        for (final Entry<Integer, Integer> e : dominatedNum.entrySet()) {
            if (e.getValue() == 0) {
                level1.add(e.getKey());
            }
        }
        levels.add(level1);

        int index = 0;
        while (levels.size() > index) {
            final List<Integer> level = levels.get(index);
            final List<Integer> newLevel = new ArrayList<>();

            for (final Integer i : level) {
                final List<Integer> domainElems = dominateSet.get(i);
                for (final Integer beDomained : domainElems) {
                    int lastNum = dominatedNum.get(beDomained);
                    if (lastNum == 1) {
                        newLevel.add(beDomained);
                    }
                    lastNum = lastNum - 1;
                    dominatedNum.put(beDomained, lastNum);
                }
            }

            if (newLevel.isEmpty()) {
                break;
            } else {
                levels.add(newLevel);
            }
            index += 1;
        }
        return levels.stream()
                .map(l -> l.stream().map(e -> chroms.get(e)).collect(Collectors.toList()))
                .collect(Collectors.toList());

    }

    private static boolean dominated(final Chromosome c1, final Chromosome c2,
            final Algorithm... algorithms) {
        boolean atLeastOne = false;
        for (final Algorithm a : algorithms) {
            final double c1Value = a.compute(c1);
            final double c2Value = a.compute(c2);
            if (a.dominate(c2Value, c1Value)) {
                return false;
            } else if (!atLeastOne && a.dominate(c1Value, c2Value)) {
                atLeastOne = true;
            }
        }
        return true;
    }

    public static List<Chromosome> crowdingDistanceSort(final List<Chromosome> chroms,
            final Algorithm... algorithms) {
        if (chroms.size() <= 1) {
            return chroms;
        }
        // 如果只有一个算法 只需要返回从大到小的排序结果就可以了
        if (algorithms.length == 1) {
            final Algorithm a = algorithms[0];
            final List<Chromosome> sorted = new ArrayList<>(chroms);
            // 从大到校倒序排序
            Collections.sort(sorted, (c1, c2) -> {
                if (a.dominate(a.compute(c1), a.compute(c2))) {
                    return -1;
                } else {
                    return 1;
                }
            });
            return sorted;
        }

        final List<Chromosome> tmp = new ArrayList<>(chroms);
        final Map<Chromosome, Double> distanceMap = new HashMap<>();
        for (final Algorithm a : algorithms) {
            final Map<Chromosome, Double> algorithmValue = new HashMap<>();
            Collections.sort(tmp, (c1, c2) -> {
                final double c1v = a.compute(c1);
                final double c2v = a.compute(c2);
                algorithmValue.put(c1, c1v);
                algorithmValue.put(c2, c2v);
                if (a.dominate(c1v, c2v)) {
                    return 1;
                } else {
                    return -1;
                }
            });
            distanceMap.put(tmp.get(0), Double.POSITIVE_INFINITY);
            distanceMap.put(tmp.get(tmp.size() - 1), Double.POSITIVE_INFINITY);
            for (int i = 1; i < tmp.size() - 1; i++) {
                final double v = distanceMap.getOrDefault(tmp.get(i), 0.0);
                distanceMap
                .put(tmp.get(i),
                        v + algorithmValue.get(tmp.get(i + 1))
                        - algorithmValue.get(tmp.get(i - 1)));
            }
        }

        final List<Entry<Chromosome, Double>> distanceList = new ArrayList<>(distanceMap.entrySet());
        Collections.sort(distanceList, (c1, c2) -> {
            if (c1.getValue() > c2.getValue()) {
                return -1;
            } else {
                return 1;
            }
        });
        return distanceList.stream().map(c -> c.getKey()).collect(Collectors.toList());
    }

}
