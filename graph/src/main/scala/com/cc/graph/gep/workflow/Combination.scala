package com.cc.graph.gep.workflow

import scala.collection.mutable.ListBuffer
import com.cc.graph.gep.Chromosome
import com.cc.graph.gep.Gene
import com.cc.graph.base.Graph
import scala.annotation.tailrec

object Combination {

  def apply(s: Chromosome): List[Chromosome] = {
    val size = s.genes.size
    val geneList = s.genes.toList
    val buffer = ListBuffer[List[Int]]()
    applyInner(0, size, List.empty, buffer)
    val splits = buffer.toList
    val geneCombinations = for (split <- splits) yield mergeGene(split, geneList)
    geneCombinations.map(e => Chromosome(e: _*))
  }

  private def applyInner(current: Int, size: Int, result: List[Int], buffer: ListBuffer[List[Int]]): Unit = {
    val nextResult = current :: result
    if (current >= size) {
      buffer += nextResult.reverse
    } else {
      for (i <- current + 1 to size) {
        applyInner(i, size, nextResult, buffer)
      }
    }
  }

  private def mergeGene(indexList: List[Int], geneList: List[Gene]): List[Gene] = {
    val groupdGene = indexList.sliding(2).toList.map(e => geneList.slice(e.head, e.tail.head))
    groupdGene.map(group => group.reduce(Gene.merge))
  }

  def main(args: Array[String]): Unit = {
    val graph = Graph.load("src/main/resources/Dolphin.txt")
    val c = Chromosome.generate(graph)
    println(c)
    val l = apply(c)
    println(l)
  }

}