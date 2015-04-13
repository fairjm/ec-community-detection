package com.cc.graph

import org.scalatest.WordSpecLike
import org.scalatest.MustMatchers
import com.cc.graph.base.Graph

class TestGraph extends WordSpecLike with MustMatchers {

  "graph" must {
    "be read correctly" in {
      val graph = Graph.load("src/main/resources/Dolphin.txt")
      println(graph)
      graph.display
    }
  }
}