package com.cc.graph.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.cc.graph.gep.Chromosome;

public class NMI implements Algorithm {

    private final Chromosome lastSolution;

    public NMI(final Chromosome lastSolution) {
        this.lastSolution = lastSolution;
    }

    @Override
    public double compute(final Chromosome solution) {
        // 选取元素交集
        List<Set<String>> c1 = solution.toCommunityStyle();
        List<Set<String>> c2 = this.lastSolution.toCommunityStyle();

        final Set<String> c1Element = new HashSet<>();
        final Set<String> c2Element = new HashSet<>();
        for (final Set<String> c : c1) {
            c1Element.addAll(c);
        }
        for (final Set<String> c : c2) {
            c2Element.addAll(c);
        }
        final Set<String> c1Diff = new HashSet<String>(c1Element);
        c1Diff.removeAll(c2Element);

        final Set<String> c2Diff = new HashSet<String>(c2Element);
        c2Diff.removeAll(c1Element);

        if (!c1Diff.isEmpty()) {
            c1 = c1.stream().map(s -> {
                final Set<String> st = new HashSet<>(s);
                st.removeAll(c1Diff);
                return st;
            }).filter(s -> s.size() > 0).collect(Collectors.toList());
        }
        if (!c2Diff.isEmpty()) {
            c2 = c2.stream().map(s -> {
                final Set<String> st = new HashSet<>(s);
                st.removeAll(c2Diff);
                return st;
            }).filter(s -> s.size() > 0).collect(Collectors.toList());
        }
        return this.apply(this.toCommunityNumberList(c1), this.toCommunityNumberList(c2));
    }

    @Override
    public boolean dominate(final double a, final double b) {
        return a > b;
    }

    /**
     * 将社区归组<br/>
     * 例如 List(Set("1","2","3"),Set("4","5","6")) 会得到List(1,1,1,2,2,2)
     *
     * @return
     */
    private List<Integer> toCommunityNumberList(final List<Set<String>> comms) {
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        int communityNum = 1;
        for (final Set<String> comm : comms) {
            for (final String node : comm) {
                mapping.put(node, communityNum);
            }
            communityNum += 1;
        }
        final Map<String, Integer> sorted = new TreeMap<String, Integer>(mapping);
        return new ArrayList<>(sorted.values());
    }

    private double apply(final List<Integer> commNums1, final List<Integer> commNums2) {
        final double i = this.I(commNums1, commNums2);
        final double h1 = this.H(commNums1);
        final double h2 = this.H(commNums2);
        return 2 * i / (h1 + h2);
    }

    /**
     * I(A,B)=H(A)-H(A|B） sigma x,y p(x,y)* log2(p(x,y)/(p(x)*p(y))
     *
     * @param c1
     * @param c2
     * @return
     */
    private double I(final List<Integer> commNums1, final List<Integer> commNums2) {

        final Map<Integer, Double> p1Cache = new HashMap<>();
        final Map<Integer, Double> p2Cache = new HashMap<>();

        final Set<Integer> uniqueNums1 = new HashSet<>(commNums1);
        final Set<Integer> uniqueNums2 = new HashSet<>(commNums2);
        double sum = 0;
        for (final int num1 : uniqueNums1) {
            for (final int num2 : uniqueNums2) {
                final double pxy = this.P(num1, commNums1, num2, commNums2);
                p1Cache.putIfAbsent(num1, this.P(num1, commNums1));
                final double px = p1Cache.get(num1);
                p2Cache.putIfAbsent(num2, this.P(num2, commNums2));
                final double py = p2Cache.get(num2);
                double r = pxy * (this.log2(pxy / (px * py)));
                if (!Double.isFinite(r)) {
                    r = 0;
                }
                sum += r;
            }
        }
        return sum;
    }

    /**
     * H(X) = -p(1)*log2(p(1)) -p(2)*log2(p(2)) -p(3)*log2(p(3))... ...
     *
     * @param commNums
     * @return
     */
    private double H(final List<Integer> commNums) {
        double result = 0;
        final Set<Integer> uniqueNums = new HashSet<>(commNums);
        for (final int num : uniqueNums) {
            final double p = this.P(num, commNums);
            result += -1.0 * p * this.log2(p);
        }
        return result;
    }

    private double P(final int commNum, final List<Integer> commNums) {
        double num = 0;
        for (final int i : commNums) {
            if (i == commNum)
                num += 1;
        }
        return num / commNums.size();
    }

    private double P(final int commNum1, final List<Integer> commNums1, final int commNum2,
            final List<Integer> commNums2) {
        double num = 0;
        final int size = commNums1.size();
        for (int i = 0; i < size; i++) {
            if ((commNums1.get(i) == commNum1) && (commNums2.get(i) == commNum2)) {
                num += 1;
            }
        }
        return num / size;
    }

    private double log2(final double i) {
        return Math.log(i) / Math.log(2);
    }

    public static void main(final String[] args) {
        final List<Set<String>> c1 = new ArrayList<Set<String>>();
        final List<Set<String>> c2 = new ArrayList<Set<String>>();
        final Set<String> h1 = new HashSet<>();
        h1.addAll(Arrays.asList("1", "2", "3", "4", "5", "6"));
        final Set<String> h2 = new HashSet<>();
        h2.addAll(Arrays.asList("7", "8", "9", "10", "11", "12"));
        final Set<String> h3 = new HashSet<>();
        h3.addAll(Arrays.asList("13", "14", "15", "16", "17"));
        c1.add(h1);
        c1.add(h2);
        c1.add(h3);

        final Set<String> h4 = new HashSet<>();
        h4.addAll(Arrays.asList("2", "8", "9", "10", "11"));
        final Set<String> h5 = new HashSet<>();
        h5.addAll(Arrays.asList("1", "3", "4", "5", "6", "7", "13", "14"));
        final Set<String> h6 = new HashSet<>();
        h6.addAll(Arrays.asList("12", "15", "16", "17"));
        c2.add(h4);
        c2.add(h5);
        c2.add(h6);

        System.out.println(new NMI(Chromosome.convertComms(c2)).compute(Chromosome.convertComms(c1)));
        System.out.println(new NMI(Chromosome.convertComms(c1)).compute(Chromosome.convertComms(c1)));
        System.out.println(new NMI(Chromosome.convertComms(c1)).compute(Chromosome.convertComms(c2)));
    }

}
