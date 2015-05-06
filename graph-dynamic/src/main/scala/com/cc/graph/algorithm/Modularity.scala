package com.cc.graph.algorithm

import com.cc.graph.base.Graph
import scala.collection.mutable.ListBuffer
import com.cc.graph.base.Edge
import com.cc.graph.base.Vertex
import com.cc.graph.base.Vertex

object Modularity {
  /**
   * use I/E-((2I+O)/2E)**2 to calculate the Q of each community
   * @param comms
   *  the comms list
   * @param graph
   *  the graph which the communites belong to
   * @return
   *  Q list of the community
   */
  def compute(comms: Graph.Communities, graph: Graph): List[Double] = {
    var remainingEdges = graph.getEdges
    val e = remainingEdges.size

    val inSizes = ListBuffer[Int]()
    comms.foreach { comm =>
      val nodes = comm.toList
      var inCount = 0
      for {
        n1 <- nodes
        n2 <- nodes
        if n2 > n1
      } {
        val edge = Edge.cons(n1, n2)
        if (remainingEdges(edge)) {
          inCount = inCount + 1
          remainingEdges = remainingEdges - edge
        }
      }
      inSizes += inCount
    }
    val outSizes = comms.map { comm =>
      comm.foldLeft(0) { (r, node) =>
        r + remainingEdges.filter(_.containsNode(node)).size
      }
    }
    (outSizes zip inSizes).collect {
      case (o, i) =>
        1.0 * i / e - Math.pow((1.0 * 2 * i + o) / (2 * e), 2)
    }
  }
}