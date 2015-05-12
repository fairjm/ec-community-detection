package com.cc.graph

import org.scalatest.WordSpec
import org.scalatest.MustMatchers
import com.cc.graph.base._
import com.cc.graph.algorithm.Modularity

class TestModularity extends WordSpec with MustMatchers {

  "modularity computation" must {
    "return the graph's modularities of the communities" in {
      val graph = MutableGraph("test")

      graph.addEdge(Edge.cons("1", "2"))
      graph.addEdge(Edge.cons("1", "3"))
      graph.addEdge(Edge.cons("2", "3"))
      graph.addEdge(Edge.cons("2", "4"))

      graph.addEdge(Edge.cons("4", "5"))
      graph.addEdge(Edge.cons("5", "6"))
      graph.addEdge(Edge.cons("5", "7"))
      graph.addEdge(Edge.cons("6", "7"))

      graph.addEdge(Edge.cons("4", "8"))
      graph.addEdge(Edge.cons("8", "9"))
      graph.addEdge(Edge.cons("8", "10"))
      graph.addEdge(Edge.cons("9", "10"))

      val r = Modularity.compute(List(Set("1", "2", "3", "4"), Set("5", "6", "7"), Set("8", "9", "10")), ImmutableGraph.from(graph))
      println(r)
      assert(r.size == 3)
    }
  }

}