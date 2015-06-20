package com.cc.graph;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public final class Conf {

    private final static Config conf = ConfigFactory.load();

    public final static String projectName = conf.getString("project.name");

    public final static class Gep {
        public static int generationNum = conf.getInt("gep.max-generation-num");
        public static int populationSize = conf.getInt("gep.population-size");
        public static double geneMove = conf.getDouble("gep.gene-move");
        public static double geneExchange = conf.getDouble("gep.gene-exchange");
        public static double geneSplitoff = conf.getDouble("gep.gene-splitoff");
        public static double geneMerge = conf.getDouble("gep.gene-merge");
    }
}
