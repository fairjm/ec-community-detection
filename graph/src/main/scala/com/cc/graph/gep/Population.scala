package com.cc.graph.gep

import com.cc.graph.base.Graph

case class Population(chromosomes: Vector[Chromosome], generationNum: Int = 0) {
  val size = chromosomes.size
  override def toString: String = {
    s"""
      |Population:     ${generationNum}
      |chromosome size:${size}
     """.stripMargin
  }
}

object Population {
  def generate(graph: Graph, populationNum: Int): Population = {
    Population(Vector.fill(populationNum)(Chromosome.generate(graph)))
  }
}