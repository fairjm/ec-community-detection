package com.cc.graph.gep

import com.cc.graph.base.Graph
import java.util.concurrent.{ ThreadLocalRandom => TLRandom }
import util.Random

case class Chromosome private (genes: Set[Gene]) {
  override def toString(): String = genes.mkString

  def toCommunityStyle: Graph.Community = genes.map(_.nodes).toList
}

object Chromosome {

  /**
   * generate a chromosome of the graph nodes<br/>
   * randomly choose a position( >= 2) and make the nodes before the position a community
   */
  def generate(graph: Graph): Chromosome = {
    val nodeIds = Random.shuffle(graph.vertexIds.toVector)
    def gen(ids: Vector[String], genes: Set[Gene]): Chromosome = {
      val idSize = ids.size
      if (idSize == 0) {
        Chromosome(genes)
      } else {
        val position = if (idSize > 3) TLRandom.current().nextInt(3, ids.size) else 3
        val (take, rest) = ids.splitAt(position)
        if (rest.size >= 3) {
          gen(rest, genes + Gene(take: _*))
        } else {
          Chromosome(genes + Gene(ids: _*))
        }
      }
    }
    gen(nodeIds, Set())
  }

  /**
   * construct chromosome <br/>
   * the gene whose node size is zero will be removed
   */
  def apply(gs: Gene*): Chromosome = {
    Chromosome(gs.filterNot(_.nodes.size == 0).toSet)
  }
}


