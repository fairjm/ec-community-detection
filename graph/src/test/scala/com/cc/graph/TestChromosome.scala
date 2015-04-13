package com.cc.graph

import org.scalatest.MustMatchers
import org.scalatest.WordSpec
import com.cc.graph.base._
import com.cc.graph.gep.Chromosome

class TestChromosome extends WordSpec with MustMatchers {

  "chromosome generation" must {
    "randomly generate chromosomes" in {
      val graph = Graph.load("src/main/resources/Dolphin.txt")

      println(Chromosome.generate(graph))
      println(Chromosome.generate(graph))
      println(Chromosome.generate(graph))
      println(Chromosome.generate(graph))
    }
  }
}