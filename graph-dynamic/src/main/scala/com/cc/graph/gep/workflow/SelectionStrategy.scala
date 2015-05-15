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
case class SelectionResult(best: Option[Chromosome], selected: Vector[Chromosome], values: Vector[(Chromosome, Double)])

trait SelectionStrategy {
  def choose(chroms: Vector[Chromosome], graph: Graph, chooseNum: Int): SelectionResult

  def choose(chroms: Vector[Chromosome], graph: Graph): SelectionResult = choose(chroms, graph, chroms.size)
}

/**
 * selection by the modularity
 */
trait ModularitySelection extends SelectionStrategy {

  /**
   * given the chroms which the chromosome to choose from <br>
   * the graph which the chroms according to <br>
   * and the num of how many chroms to be choosed
   */
  def choose(chroms: Vector[Chromosome], graph: Graph, chooseNum: Int): SelectionResult = {
    if (chroms.size == 0) {
      SelectionResult(None, chroms, Vector.empty)
    } else {
      // compute modularity of each chrom
      val modularities = for (chrom <- chroms) yield {
        val communities = chrom.toCommunityStyle
        val modularities = Modularity.compute(communities, graph)
        modularities.sum
      }
      // sort the chroms by the modularity(descending order) the result is (chrom1,its modularity),(chrom2,its modularity)
      val values = (chroms zip modularities).sortBy(-1 * _._2)
      // get the best chrom(the first elements above
      val best = values(0)._1
      if (chooseNum <= 0) {
        SelectionResult(Some(best), Vector.empty, values)
      } else {
        val accModularities = modularities.foldLeft(ListBuffer[Double]()) {
          (r, e) =>
            val guard = if (e < 0) 0 else e
            if (r.size == 0) r += guard else r += r.last + guard
        }
        val sum = accModularities.last
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
        SelectionResult(Some(best), selected, values)
      }
    }
  }
}