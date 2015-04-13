package com.cc.graph.gep.workflow

import com.cc.graph.gep.Gene
import com.cc.graph.gep.Population
import com.cc.graph.gep.Chromosome
import com.cc.graph.algorithm.Modularity
import com.cc.graph.base.Graph
import java.util.concurrent.{ ThreadLocalRandom => TLRandom }
import scala.collection.mutable.ListBuffer

/**
 * select result</br>
 * best is the selected one who has the best score or None if there is no this one(selected is empty)
 */
case class SelectionResult(best: Option[Chromosome], selected: Vector[Chromosome])

trait ChromosomeSelection {
  def choose(chroms: Vector[Chromosome], graph: Graph, chooseNum: Int): SelectionResult

  def choose(chroms: Vector[Chromosome], graph: Graph): SelectionResult = choose(chroms, graph, chroms.size)
}

trait ModularitySelection extends ChromosomeSelection {
  val antiImPositive = 0.25
  def choose(chroms: Vector[Chromosome], graph: Graph, chooseNum: Int): SelectionResult = {
    if (chroms.size == 0) {
      SelectionResult(None, chroms)
    } else {
      val modularities = for (chrom <- chroms) yield {
        val communities = chrom.genes.toList.map(_.nodes)
        val modularities = Modularity.compute(communities, graph)
        modularities.map(_ + antiImPositive).sum
      }
      val best = chroms(modularities.indexOf(modularities.max))
      val sum = modularities.sum
      val accModularities = modularities.foldLeft(ListBuffer[Double]()) {
        (r, e) =>
          if (r.size == 0) r += e else r += r.last + e
      }
      val chromWithAccModularities = chroms zip accModularities
      val random = TLRandom.current()
      val countDown = (1 to chooseNum).toVector
      val selected = for (i <- countDown) yield {
        val chooseDouble = random.nextDouble(sum)
        val choosed = chromWithAccModularities.dropWhile(_._2 < chooseDouble)
        if (choosed.size == 0) {
          chromWithAccModularities(chromWithAccModularities.size - 1)._1
        } else {
          choosed(0)._1
        }
      }
      SelectionResult(Some(best), selected)
    }
  }
}