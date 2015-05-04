package com.cc.graph.gep.workflow

import java.util.concurrent.{ ThreadLocalRandom => TLRandom }

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

import com.cc.graph.Conf
import com.cc.graph.algorithm.Modularity
import com.cc.graph.base.Graph
import com.cc.graph.gep.Chromosome
import com.cc.graph.gep.Gene
import com.cc.graph.gep.Population

case class Result(best: Chromosome, history: List[Population])
class WorkFlow {

  selection: SelectionStrategy =>

  import Conf.Gep._

  type History = List[Population]
  type InnerResult = (Population, History)

  /**
   * run the init population<br/>
   * the related props are from application.conf
   */
  def run(graph: Graph): Result = {
    val initPopulation = Population.generate(graph, populationSize)
    val inner = innerRun((initPopulation, List(initPopulation)), graph, generationNum)
    val lastPop = inner._1
    var best = selection.choose(lastPop.chromosomes, graph, 0).best.get
    var better: Chromosome = best
    println("do combination iteration")
    do {
      best = better
      val r = selection.choose(Combination(best), graph, 0)
      better = r.best.get
      r.values.foreach(re => {
        println(re._1)
        println(re._2)
      })
      println("better=" + better)
      println("best=" + best)
    } while (best != better)
    Result(best, inner._2)
  }

  @tailrec
  private def innerRun(lastResult: InnerResult, graph: Graph, maxGenerationNum: Int): InnerResult = {
    val lastPop = lastResult._1
    println(lastPop.generationNum + "/" + maxGenerationNum)
    if (lastPop.generationNum >= maxGenerationNum) {
      lastResult
    } else {
      // remain one positive for the best
      val choosedChroms = selection.choose(lastPop.chromosomes, graph, lastPop.chromosomes.size - 1)
      val mutatedChroms = for (chrom <- choosedChroms.selected) yield {
        operateChromosome(chrom, graph)
      }
      val theRemainedOne = choosedChroms.best.getOrElse(Chromosome.generate(graph))
      val newPopulation = Population(mutatedChroms :+ theRemainedOne, lastPop.generationNum + 1)
      innerRun((newPopulation, newPopulation :: lastResult._2), graph, maxGenerationNum)
    }
  }

  private def operateChromosome(chrom: Chromosome, graph: Graph): Chromosome = {
    val random = TLRandom.current()
    val ls = ListBuffer[Gene]()
    ls ++= chrom.genes
    if (random.nextDouble() < geneMove) {
      doGeneMove(ls)
    }
    if (random.nextDouble() < geneExchange) {
      doGeneExchange(ls)
    }
    if (random.nextDouble() < geneSplitoff) {
      doGeneSplitOff(ls)
    }
    if (random.nextDouble() < geneMerge) {
      doGeneMerge(ls, graph)
    }
    Chromosome(ls: _*)
  }

  private def doGeneMove(genes: ListBuffer[Gene]): Unit = {
    val random = TLRandom.current()
    if (genes.size > 1) {
      val g1 = genes.remove(random.nextInt(genes.size))
      val g2 = genes.remove(random.nextInt(genes.size))
      val moved = Gene.move(g1, g2)
      genes ++= moved
    }
  }

  private def doGeneExchange(genes: ListBuffer[Gene]): Unit = {
    val random = TLRandom.current()
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

  private def doGeneMerge(genes: ListBuffer[Gene], graph: Graph): Unit = {
    val random = TLRandom.current()
    val chrom = Chromosome(genes: _*)
    val g1 = genes.remove(random.nextInt(genes.size))
    val g2 = genes.remove(random.nextInt(genes.size))
    genes += Gene.merge(g1, g2)
  }
}

object WorkFlow extends App {
  val workFlow = new WorkFlow with ModularitySelection
  val graph = Graph.load("src/main/resources/Zachary.txt")
  val result = workFlow.run(graph)
  val best = result.best
  println(best)
  println(Modularity.compute(best.toCommunityStyle, graph).sum)
  graph.displayCommunity(best.toCommunityStyle)
}