package com.cc.graph

import com.typesafe.config.ConfigFactory

/**
 * configuration instance
 */
object Conf {
  private lazy val conf = ConfigFactory.load();

  lazy val projectName = conf.getString("project.name")

  object Gep {
    lazy val generationNum = conf.getInt("gep.max-generation-num")
    lazy val populationSize = conf.getInt("gep.population-size")
    lazy val geneMove = conf.getDouble("gep.gene-move")
    lazy val geneExchange = conf.getDouble("gep.gene-exchange")
    lazy val geneSplitoff = conf.getDouble("gep.gene-splitoff")
    lazy val geneMerge = conf.getDouble("gep.gene-merge")
  }
}