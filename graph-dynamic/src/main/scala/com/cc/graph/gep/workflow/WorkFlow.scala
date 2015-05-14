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
import com.cc.graph.mo.NSGAII

case class Result(bests: List[Chromosome])
class WorkFlow {

  selection: SelectionStrategy =>

  import Conf.Gep._

  /**
   * run the init population<br/>
   * the related props are from application.conf
   */
  def run(graphs: Graph*): Result = {
    if (graphs.isEmpty) {
      Result(Nil)
    } else {
      val chroms = ListBuffer[Chromosome]()

      // when timestamp is 0
      val firstGraph = graphs.head
      println("timestamp-0")
      val best = runTimestamp0(graphs.head, generationNum)
      chroms += best

      //when timestamp is larger than 0
      val tail = graphs.tail
      var lastBest = best

      for (graph <- tail) {
        val current = runTimestampN(graph, generationNum, lastBest)
        chroms += current
        lastBest = current
      }

      Result(chroms.toList)
    }
  }

  private def runTimestamp0(graph: Graph, maxGenerationNum: Int): Chromosome = {
    val initPopulation = Population.generate(graph, populationSize)
    val lastPop = innerTimestamp0(initPopulation, graph, generationNum)
    selection.choose(lastPop.chromosomes, graph, 0).best.get
  }

  @tailrec
  private def innerTimestamp0(lastPopulation: Population, graph: Graph, maxGenerationNum: Int): Population = {
    println(lastPopulation.generationNum + "/" + maxGenerationNum)
    if (lastPopulation.generationNum >= maxGenerationNum) {
      lastPopulation
    } else {
      // remain one positive for the best
      val choosedChroms = selection.choose(lastPopulation.chromosomes, graph, lastPopulation.chromosomes.size - 1)
      val mutatedChroms = for (chrom <- choosedChroms.selected) yield {
        operateChromosome(chrom, graph)
      }
      val theRemainedOne = choosedChroms.best.getOrElse(Chromosome.generate(graph))
      val newPopulation = Population(mutatedChroms :+ theRemainedOne, lastPopulation.generationNum + 1)
      innerTimestamp0(newPopulation, graph, maxGenerationNum)
    }
  }

  private def runTimestampN(graph: Graph, maxGenerationNum: Int, lastTimestampCommunities: Chromosome): Chromosome = {
    val initPopulation = Population.generate(graph, populationSize)
    val lastPop = innerTimestampN(initPopulation, graph, generationNum, lastTimestampCommunities)
    val levels = NSGAII.fastNondominatedSort(lastPop.chromosomes, graph, lastTimestampCommunities)
    // choose the max modularity of the first level
    levels(0)._2.maxBy(chrom => Modularity.compute(chrom.toCommunityStyle, graph).sum)
  }

  @tailrec
  private def innerTimestampN(lastPopulation: Population, graph: Graph, maxGenerationNum: Int, lastTimestampCommunities: Chromosome): Population = {
    println(lastPopulation.generationNum + "/" + maxGenerationNum)
    if (lastPopulation.generationNum >= maxGenerationNum) {
      lastPopulation
    } else {
      val p = lastPopulation.chromosomes
      val length = p.size
      val q = for (chrom <- p) yield {
        operateChromosome(chrom, graph)
      }
      val mixed = p ++ q
      println("mixed size:" + mixed.size)
      val levels = NSGAII.fastNondominatedSort(mixed, graph, lastTimestampCommunities)
      println("levels count:" + levels.map(_._2.size).sum)
      println("levels length:" + levels.length)
      println("length:" + length)
      var rest = length
      var i = 0
      val buffer = ListBuffer[Chromosome]()
      while (rest > 0 && rest - levels(i)._2.size > 0) {
        val in = levels(i)._2
        buffer ++= in
        i += 1
        rest = rest - in.size
        println("inSize=" + in.size)
        println("rest=" + rest)
      }
      if (rest > 0) {
        val in = levels(i)._2
        val sorted = NSGAII.crowdingDistanceAssignment(in, graph, lastTimestampCommunities).sortBy(_._2).map(_._1)
        buffer ++= sorted.take(rest)
      }
      innerTimestampN(Population(buffer.toVector, lastPopulation.generationNum + 1), graph, maxGenerationNum, lastTimestampCommunities)
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

  //  val workFlow = new WorkFlow with ModularitySelection
  //  val graph = Graph.load("src/main/resources/test2.txt")
  //  val result = workFlow.run(graph)
  //  val best = result.bests(0)
  //  println(best)
  //  println(Modularity.compute(best.toCommunityStyle, graph).sum)
  //  graph.displayCommunity(best.toCommunityStyle)

  val workFlow = new WorkFlow with ModularitySelection
  val file = "src/main/resources/mo/real.t01.edges"
  val graph1 = Graph.load("src/main/resources/mo/real.t01.edges", seperator = " ")
  val graph2 = Graph.load("src/main/resources/mo/real.t02.edges", seperator = " ")
  println(graph1)
  val result = workFlow.run(graph1, graph2)
  val c1 = result.bests(0)
  val c2 = result.bests(1)
  println(c1)
  println(c2)
  println(Modularity.compute(c1.toCommunityStyle, graph1).sum)
  println(Modularity.compute(c2.toCommunityStyle, graph1).sum)
  //  graph1.displayCommunity(best.toCommunityStyle)

}