package com.cc.graph.algorithm

import com.cc.graph.base.Graph
import scala.collection.mutable.ListBuffer
import com.cc.graph.base.Edge
import com.cc.graph.base.Vertex
import com.cc.graph.base.Vertex
import scala.collection.mutable.HashSet

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
    val e = graph.getEdges.size

    val inSizes = ListBuffer[Int]()
    val outSizes = ListBuffer[Int]()
    for (comm <- comms) {
      var in = 0
      var out = 0
      val inEdges = HashSet[Edge]()
      comm.foreach(node =>{
        val neighbors = graph.getNeighborVertexes(node)
        neighbors.intersect(comm).foreach(e => inEdges += Edge.cons(node, e))
        out += neighbors.diff(comm).size
      })
      inSizes += inEdges.size
      outSizes += out
    }
    (outSizes.toList zip inSizes.toList).collect {
      case (o, i) =>
        1.0 * i / e - Math.pow((1.0 * 2 * i + o) / (2 * e), 2)
    }
  }
}