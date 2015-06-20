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
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._
import com.cc.graph.algorithm.NMI

case class Result(bests: List[Chromosome])

class WorkFlow {

  selection: SelectionStrategy =>

  import Conf.Gep._

  /**
   * run the init population<br/>
   * the related props are from application.conf
   */
  def run(graphs: Graph*): Result = {

    val graphArray = graphs.toArray

    if (graphArray.isEmpty) {
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

      for (index <- 1 until graphArray.size) {
        println("timestamp-" + index)
        val current = runTimestampN(graphArray(index), generationNum, lastBest)
        chroms += current
        lastBest = current
      }
      Result(chroms.toList)
    }
  }

  private def runTimestamp0(graph: Graph, maxGenerationNum: Int): Chromosome = {
    val initPopulation = Population.generate(graph, populationSize)
    val lastPop = innerTimestamp0(initPopulation, graph, generationNum)
    // get the best result back
    selection.choose(lastPop.chromosomes, graph, 0).best.get
  }

  @tailrec
  private def innerTimestamp0(lastPopulation: Population, graph: Graph, maxGenerationNum: Int): Population = {
    println(lastPopulation.generationNum + "/" + maxGenerationNum)
    if (lastPopulation.generationNum >= maxGenerationNum) {
      lastPopulation
    } else {

      val mutatedChromsFutures = for (chrom <- lastPopulation.chromosomes) yield {
        // use Future for concurrency
        Future(operateChromosome(chrom, graph))
      }

      val mutatedChroms = mutatedChromsFutures.map(f => Await.result(f, 10 seconds))

      // remain one positive for the best
      val choosedChroms = selection.choose(lastPopulation.chromosomes ++ mutatedChroms, graph, lastPopulation.chromosomes.size - 1)

      // choose the best one
      val theRemainedOne = choosedChroms.best.getOrElse(Chromosome.generate(graph))
      println(Modularity.compute(theRemainedOne.toCommunityStyle, graph).sum)

      val newPopulation = Population(choosedChroms.selected :+ theRemainedOne, lastPopulation.generationNum + 1)
      innerTimestamp0(newPopulation, graph, maxGenerationNum)
    }
  }

  private def runTimestampN(graph: Graph, maxGenerationNum: Int, lastTimestampCommunities: Chromosome): Chromosome = {
    val initPopulation = Population.generate(graph, populationSize)
    val lastPop = innerTimestampN(initPopulation, graph, generationNum, lastTimestampCommunities)
    val levels = NSGAII.fastNondominatedSort(lastPop.chromosomes, graph, lastTimestampCommunities)
    val moCache = scala.collection.mutable.Map[Chromosome, Double]()
    val nmiCache = scala.collection.mutable.Map[Chromosome, Double]()
    val moSum = levels(0)._2.map(e => moCache.getOrElseUpdate(e, Modularity.compute(e.toCommunityStyle, graph).sum)).sum
    val nmiSum = levels(0)._2.map(e => nmiCache.getOrElseUpdate(e, NMI(e.toCommunityStyle, lastTimestampCommunities.toCommunityStyle))).sum
    levels(0)._2.maxBy(chrom => moCache.getOrElseUpdate(chrom, Modularity.compute(chrom.toCommunityStyle, graph).sum) / moSum + nmiCache.getOrElseUpdate(chrom, NMI(chrom.toCommunityStyle, lastTimestampCommunities.toCommunityStyle)) / nmiSum)
  }

  @tailrec
  private def innerTimestampN(lastPopulation: Population, graph: Graph, maxGenerationNum: Int, lastTimestampCommunities: Chromosome): Population = {
    println(lastPopulation.generationNum + "/" + maxGenerationNum)
    if (lastPopulation.generationNum >= maxGenerationNum) {
      lastPopulation
    } else {
      val p = lastPopulation.chromosomes
      val length = p.size
      val qFutures = for (chrom <- p) yield {
        // use Future for concurrency
        Future(operateChromosome(chrom, graph))
      }
      val q = qFutures.map(f => Await.result(f, 10 seconds))
      // get the mixed chroms(twice length of the original one)
      val mixed = p ++ q
      //      println("mixed size:" + mixed.size)
      val levels = NSGAII.fastNondominatedSort(mixed, graph, lastTimestampCommunities)
      //      println("levels count:" + levels.map(_._2.size).sum)
      //      println("levels length:" + levels.length)
      //      println("length:" + length)
      var rest = length
      var i = 0
      val buffer = ListBuffer[Chromosome]()
      while (rest > 0 && rest - levels(i)._2.size > 0) {
        val in = levels(i)._2
        buffer ++= in
        i += 1
        rest = rest - in.size
      }
      if (rest > 0) {
        val in = levels(i)._2
        val sorted = NSGAII.crowdingDistanceAssignment(in, graph, lastTimestampCommunities).sortBy(_._2).map(_._1)
        buffer ++= sorted.take(rest)
      }
      innerTimestampN(Population(buffer.toVector, lastPopulation.generationNum + 1), graph, maxGenerationNum, lastTimestampCommunities)
    }
  }

  /**
   * operate chromosome(mutation)<br>
   * thread safe
   */
  private def operateChromosome(chrom: Chromosome, graph: Graph): Chromosome = {
    val random = TLRandom.current()
    val ls = ListBuffer[Gene]()
    ls ++= chrom.genes
    if (ls.size == 1) {
      doGeneSplitOff(ls)
    }
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
    if (genes.size > 1) {
      val g1 = genes.remove(random.nextInt(genes.size))
      val g2 = genes.remove(random.nextInt(genes.size))
      genes += Gene.merge(g1, g2)
    }
  }
}

object WorkFlow extends App {

  //  showZacharyInit()
  //  runFootball1()
  //  run1(seperator = " ", file = "src/main/resources/football/edges.1.1.txt")

  //    runZachary()
  //  runDolphin
  runFootballSelf()

  //  val graph = Graph.load("src/main/resources/Zachary.txt", seperator = ",")
  //  val r = Modularity.compute(List(Set("9","10","15","16","19","21","23","24","25","26","27","28","29","30","31","32","33","34"),
  //    Set("1","2","3","4","5","6","7","8","11","12","13","14","17","18","20","22")), graph)
  //  println(r.sum)

  //    val graph = Graph.load("src/main/resources/football/edges.1.1.txt", seperator = " ")
  //    println(graph.getVertexes.toList.map(a => Integer.valueOf(a.id)).sorted)
  //    println(graph.getVertexes.size)

  def runFootballN() = {
    val files = List(
      "src/main/resources/football/edges.1.1.txt",
      "src/main/resources/football/edges.1.2.txt",
      "src/main/resources/football/edges.1.3.txt")
    runN(" ", files: _*)
  }

  def runFootballSelf() = {
    val files = List(
      "src/main/resources/football/edges.1.1.txt",
      "src/main/resources/football/edges.1.1.txt",
      "src/main/resources/football/edges.1.1.txt")
    runN(" ", files: _*)
  }

  def runFootball1() = {
    val r1 = run1(seperator = " ", file = "src/main/resources/football/edges.1.1.txt")
    val r2 = run1(seperator = " ", file = "src/main/resources/football/edges.1.2.txt")
    val r3 = run1(seperator = " ", file = "src/main/resources/football/edges.1.3.txt")
    println(NMI(r1.toCommunityStyle, r2.toCommunityStyle))
    println(NMI(r2.toCommunityStyle, r3.toCommunityStyle))
  }

  def runMoGraphN() = {
    val files = List(
      "src/main/resources/mo/real.t01.edges",
      "src/main/resources/mo/real.t02.edges",
      "src/main/resources/mo/real.t03.edges",
      "src/main/resources/mo/real.t04.edges",
      "src/main/resources/mo/real.t05.edges",
      "src/main/resources/mo/real.t06.edges",
      "src/main/resources/mo/real.t07.edges",
      "src/main/resources/mo/real.t08.edges",
      "src/main/resources/mo/real.t09.edges",
      "src/main/resources/mo/real.t010.edges")
    runN(" ", files: _*)
  }

  def runMoGraph1() = {
    run1(seperator = " ", file = "src/main/resources/mo/real.t01.edges")
  }

  def runZachary() = {
    val workFlow = new WorkFlow with ModularitySelection
    val graph = Graph.load("src/main/resources/Zachary.txt", seperator = ",")
    val result = workFlow.run(graph)
    val best = result.bests(0)
    println(best)
    println(Modularity.compute(best.toCommunityStyle, graph).sum)
    graph.displayCommunity(best.toCommunityStyle)
  }

  def runDolphin() = {
    val workFlow = new WorkFlow with ModularitySelection
    val graph = Graph.load("src/main/resources/Dolphin.txt", seperator = ",")
    val result = workFlow.run(graph)
    val best = result.bests(0)
    println(best)
    println(Modularity.compute(best.toCommunityStyle, graph).sum)
    graph.displayCommunity(best.toCommunityStyle)
  }

  def run1(seperator: String, file: String) = {
    val workFlow = new WorkFlow with ModularitySelection
    val graph = Graph.load(file, seperator = seperator)
    val result = workFlow.run(graph)
    val r = result.bests(0)
    println(r)
    println("node size:" + graph.getVertexes.size)
    println(Modularity.compute(r.toCommunityStyle, graph).sum)
    println(r.genes.size)
    r
  }

  def runN(seperator: String, files: String*) = {
    val workFlow = new WorkFlow with ModularitySelection
    val graphs = files.map(file => Graph.load(file, seperator = seperator))
    val result = workFlow.run(graphs: _*)
    val bests = result.bests zip graphs

    println("edge size:")
    graphs.foreach(g => println(g.getEdges.size))

    println("node size:")
    // print node size
    graphs.foreach(g => println(g.getVertexes.size))

    println("comm size:")
    // print comm size
    result.bests.foreach(e => println(e.genes.size))

    println("modurity:")
    // compute each modularity
    bests.foreach(e => println(Modularity.compute(e._1.toCommunityStyle, e._2).sum))

    println("NMI")
    // compute NMI(sliding is used to group neighbor result: List(1,2,3) => List(List(1,2),List(2,3))
    bests.map(_._1).sliding(2).foreach(es => println(NMI(es(0).toCommunityStyle, es(1).toCommunityStyle)))
  }
}