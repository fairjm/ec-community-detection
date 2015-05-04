package com.cc.graph.gep.workflow

import scala.collection.mutable.ListBuffer
import com.cc.graph.gep.Chromosome
import com.cc.graph.gep.Gene
import com.cc.graph.base.Graph
import scala.annotation.tailrec

object Combination {

  def apply(s: Chromosome): Vector[Chromosome] = {
    val genes = s.genes
    val buffer = ListBuffer[Vector[Gene]]()
    applyInner(genes.toList, Vector.empty, buffer)
    buffer.map(v => Chromosome(v: _*)).toVector
  }

  private def applyInner(genes: List[Gene], result: Vector[Gene], buffer: ListBuffer[Vector[Gene]]): Unit = {
    if (genes.isEmpty) {
      buffer += result
    } else {
      for (i <- -1 until result.size) {
        if (i == -1) {
          applyInner(genes.tail, result :+ genes.head, buffer)
        } else {
          val g = result(i)
          applyInner(genes.tail, result.updated(i, Gene.merge(g, genes.head)), buffer)
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val chromosome = Chromosome(Gene("1", "a"), Gene("2", "b"), Gene("3", "c"))
    println(chromosome)
    Combination(chromosome).foreach(println)
  }

}