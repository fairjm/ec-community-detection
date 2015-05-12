package com.cc.graph.mo

import com.cc.graph.gep.Chromosome
import com.cc.graph.base.Graph
import com.cc.graph.algorithm.Modularity
import com.cc.graph.algorithm.NMI
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer

private[this] class ChromosomeWithDistance(chrom: Chromosome) {
  var distance: Double = 0
  def getChrom = chrom
}

object NSGAII {

  private type Pop = Seq[Chromosome]

  /**
   * return the list grouped by level
   */
  def fastNondominatedSort(pop: Pop, graph: Graph, lastTimeBest: Chromosome): Vector[(Int, List[Chromosome])] = {
    val s = HashMap[Chromosome, ListBuffer[Chromosome]]()
    val n = HashMap[Chromosome, Int]()
    val pLevel = HashMap[Int, ListBuffer[Chromosome]]()
    for {
      p <- pop
      q <- pop
      if p != q //no meaning compare itself
    } {
      if (dominated(p, q, graph, lastTimeBest)) {
        s.getOrElseUpdate(p, ListBuffer()) += q
      } else if (dominated(q, p, graph, lastTimeBest)) {
        n.update(p, n.getOrElse(p, 0) + 1)
      }
    }
    for (p <- pop) {
      if (n.getOrElse(p, 0) == 0) {
        pLevel.getOrElseUpdate(1, ListBuffer()) += p
      }
    }

    var i = 1
    while (!pLevel.getOrElse(i, Nil).isEmpty) {
      val h = ListBuffer[Chromosome]()
      val pi = pLevel.get(i).get
      for {
        p <- pi
        q <- s.getOrElse(p, Nil)
      } {
        val nq = n.get(q).get
        if (nq == 0) h += q
        n.update(q, nq - 1)
      }
      i = i + 1
      pLevel.update(i, h)
    }
    pLevel.toStream.filter(_._2.size > 0).map(e => (e._1,e._2.toList)).toVector.sortBy(_._1)
  }

  def crowdingDistanceAssignment(pop: Pop, graph: Graph, lastTimeBest: Chromosome): List[(Chromosome, Double)] = {
    val l = pop.size
    var popWithDistance = ArrayBuffer() ++ pop.map(e => new ChromosomeWithDistance(e))
    //sort by modularity
    val sortedModularityPop = popWithDistance.map(e => (e, Modularity.compute(e.getChrom.toCommunityStyle, graph).sum)).sortBy(_._2)
    // the first and the last are always selected
    sortedModularityPop(0)._1.distance = Double.PositiveInfinity
    sortedModularityPop(l - 1)._1.distance = Double.PositiveInfinity
    for (i <- 1 to l - 2) {
      sortedModularityPop(i)._1.distance += (sortedModularityPop(i + 1)._2 - sortedModularityPop(i - 1)._2)
    }

    popWithDistance = sortedModularityPop.map(_._1)
    //sort by NMI
    val sortedNMIPop = popWithDistance.map(e => (e, NMI(e.getChrom.toCommunityStyle, lastTimeBest.toCommunityStyle))).sortBy(_._2)
    // the first and the last are always selected
    sortedNMIPop(0)._1.distance = Double.PositiveInfinity
    sortedNMIPop(l - 1)._1.distance = Double.PositiveInfinity
    for (i <- 1 to l - 2) {
      sortedNMIPop(i)._1.distance += (sortedNMIPop(i + 1)._2 - sortedNMIPop(i - 1)._2)
    }

    popWithDistance = sortedNMIPop.map(_._1)
    //get the crowding distance
    popWithDistance.map(e => (e.getChrom, e.distance)).toList
  }

  def dominated(c1: Chromosome, c2: Chromosome, graph: Graph, lastTimeBest: Chromosome): Boolean = {
    val mc1 = Modularity.compute(c1.toCommunityStyle, graph).sum
    val mc2 = Modularity.compute(c2.toCommunityStyle, graph).sum
    if (mc1 < mc2) {
      false
    } else {
      val nmic1 = NMI(c1.toCommunityStyle, lastTimeBest.toCommunityStyle)
      val nmic2 = NMI(c2.toCommunityStyle, lastTimeBest.toCommunityStyle)
      if (nmic1 < nmic2) {
        false
      } else {
        val compare = List((mc1, mc2), (nmic1, nmic2))
        compare.exists(c => c._1 > c._2)
      }
    }
  }
}