package com.cc.graph.mo

import com.cc.graph.gep.Chromosome
import com.cc.graph.base.Graph
import com.cc.graph.algorithm.Modularity
import com.cc.graph.algorithm.NMI
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.concurrent.Await

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
    val popArray = pop.toArray

    // key is the index of the pop
    val s = HashMap[Int, ListBuffer[Int]]()
    // key is the index of the pop
    val n = HashMap[Int, Int]()
    val pLevel = HashMap[Int, ListBuffer[Int]]()

    val cache = TrieMap[(String, Chromosome), Double]()
    val popSize = popArray.size

    println("popSize:" + popSize)

    for {
      pIndex <- 0 until popSize
      qIndex <- pIndex + 1 until popSize
    } {
      val p = popArray(pIndex)
      val q = popArray(qIndex)

      if (dominated(p, q, graph, lastTimeBest, cache)) {
        s.getOrElseUpdate(pIndex, ListBuffer()) += qIndex
        n.update(qIndex, n.getOrElse(qIndex, 0) + 1)
      } else if (dominated(q, p, graph, lastTimeBest, cache)) {
        s.getOrElseUpdate(qIndex, ListBuffer()) += pIndex
        n.update(pIndex, n.getOrElse(pIndex, 0) + 1)
      }
    }

    for (index <- 0 until popSize) {
      if (n.getOrElse(index, 0) == 0) {
        pLevel.getOrElseUpdate(1, ListBuffer()) += index
      }
    }

    var i = 1
    while (!pLevel.getOrElse(i, Nil).isEmpty) {
      val h = ListBuffer[Int]()
      val pi = pLevel.get(i).get
      for {
        pIndex <- pi
        qIndex <- s.getOrElse(pIndex, Nil)
      } {
        val nq = n.get(qIndex).get
        if (nq - 1 == 0) h += qIndex
        n.update(qIndex, nq - 1)
      }
      i = i + 1
      pLevel.update(i, h)
    }
    pLevel.toStream.filter(_._2.size > 0).map(e => (e._1, e._2.map(index => popArray(index)).toList)).toVector.sortBy(_._1)
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

  def dominated(c1: Chromosome, c2: Chromosome, graph: Graph, lastTimeBest: Chromosome, cache: TrieMap[(String, Chromosome), Double]): Boolean = {
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.duration._
    val fmc1 = Future(cache.getOrElseUpdate(("modularity", c1), Modularity.compute(c1.toCommunityStyle, graph).sum))
    val fmc2 = Future(cache.getOrElseUpdate(("modularity", c2), Modularity.compute(c2.toCommunityStyle, graph).sum))

    val fnmic1 = Future(cache.getOrElseUpdate(("nmi", c1), NMI(c1.toCommunityStyle, lastTimeBest.toCommunityStyle)))
    val fnmic2 = Future(cache.getOrElseUpdate(("nmi", c2), NMI(c2.toCommunityStyle, lastTimeBest.toCommunityStyle)))

    val fr = for {
      mc1 <- fmc1
      mc2 <- fmc2
      nmic1 <- fnmic1
      nmic2 <- fnmic2
    } yield {
      if (mc2 > mc1 || nmic2 > nmic1) {
        false
      } else if (mc1 > mc2 || nmic1 > nmic2) {
        true
      } else {
        false
      }
    }
    Await.result(fr, 10 seconds)
  }
}