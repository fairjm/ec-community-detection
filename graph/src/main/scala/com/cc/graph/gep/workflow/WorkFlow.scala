package com.cc.graph.gep.workflow

import com.cc.graph.gep.Population
import java.util.concurrent.{ ThreadLocalRandom => TLRandom }
import com.cc.graph.Conf
import com.cc.graph.base.Graph
import com.cc.graph.gep.Chromosome
import scala.collection.mutable.ListBuffer
import com.cc.graph.gep.Gene
import scala.annotation.tailrec
import com.cc.graph.algorithm.Modularity
import com.cc.graph.base.MutableGraph
import com.cc.graph.base.Edge

class WorkFlow {
  selection: ChromosomeSelection =>

  import Conf.Gep._

  var generations = List[Population]()

  lazy val random = TLRandom.current()

  /**
   * run the init population<br/>
   * the related props are from application.conf
   */
  def run(graph: Graph): Population = {
    val initPopulation = Population.generate(graph, populationSize)
    innerRun(initPopulation, graph, generationNum)
  }

  @tailrec
  private def innerRun(current: Population, graph: Graph, maxGenerationNum: Int): Population = {
    println(current.generationNum + "/" + maxGenerationNum)
    generations = current :: generations
    if (current.generationNum >= maxGenerationNum) {
      current
    } else {
      // remain one positive for the best
      val choosedChroms = selection.choose(current.chromosomes, graph, current.chromosomes.size - 1)
      val mutatedChroms = for (chrom <- choosedChroms.selected) yield {
        operateChromosome(chrom)
      }
      val theRemainedOne = choosedChroms.best.getOrElse(Chromosome.generate(graph))
      innerRun(Population(mutatedChroms :+ theRemainedOne, current.generationNum + 1), graph, maxGenerationNum)
    }
  }

  private def operateChromosome(chrom: Chromosome): Chromosome = {
    val ls = ListBuffer[Gene]()
    ls ++= chrom.genes
    if (random.nextDouble() <= geneMove) {
      doGeneMove(ls)
    }
    if (random.nextDouble() <= geneExchange) {
      doGeneExchange(ls)
    }
    if (random.nextDouble() <= geneMerge) {
      doGeneMerge(ls)
    }
    if (random.nextDouble() <= geneSplitoff) {
      doGeneSplitOff(ls)
    }
    Chromosome(ls: _*)
  }

  private def doGeneMove(genes: ListBuffer[Gene]): Unit = {
    if (genes.size > 1) {
      val g1 = genes.remove(random.nextInt(genes.size))
      val g2 = genes.remove(random.nextInt(genes.size))
      genes ++= Gene.move(g1, g2)
    }
  }

  private def doGeneExchange(genes: ListBuffer[Gene]): Unit = {
    if (genes.size > 1) {
      val g1 = genes.remove(random.nextInt(genes.size))
      val g2 = genes.remove(random.nextInt(genes.size))
      genes ++= Gene.exchange(g1, g2)
    }
  }

  private def doGeneSplitOff(genes: ListBuffer[Gene]): Unit = {
    val geneOfMaxSize = genes.reduce((g1, g2) => if (g1.size > g2.size) g1 else g2)
    genes -= geneOfMaxSize ++= Gene.splitoff(geneOfMaxSize)
  }

  private def doGeneMerge(genes: ListBuffer[Gene]): Unit = {
    if (genes.size > 1) {
      val g1 = genes.remove(random.nextInt(genes.size))
      val g2 = genes.remove(random.nextInt(genes.size))
      genes += Gene.merge(g1, g2)
    }
  }
}

object WorkFlow extends App {
  val workFlow = new WorkFlow with ModularitySelection
  val graph = Graph.load("src/main/resources/Zachary.txt")
  val pop = workFlow.run(graph)
  val chromWithModularities = pop.chromosomes zip pop.chromosomes.map(c => Modularity.compute(c.genes.toList.map(_.nodes), graph).sum)
  val sorted = chromWithModularities.sortBy(-1 * _._2)
  val best = sorted.map(_._1).apply(0)
  println(best)
  println(sorted.map(_._2))
}