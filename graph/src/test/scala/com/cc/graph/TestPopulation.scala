package com.cc.graph

import org.scalatest.WordSpec
import org.scalatest.MustMatchers
import com.cc.graph.base.Graph
import com.cc.graph.gep.Population

class TestPopulation extends WordSpec with MustMatchers {
  "generate population" must {
    "return the given num populations" in {
      val graph = Graph.load("src/main/resources/Dolphin.txt")
      val givenNum = 100
      val pop = Population.generate(graph, givenNum)
      assert(pop.size == givenNum)
      println(pop)
    }
  }
}