/**
 * @author fairjm
 */
package com.cc.graph.gep

case class Gene private (nodes: Set[String]) {

  val size = nodes.size

  def +(v: String) = {
    Gene(nodes + v)
  }

  def +(g: Gene) = {
    Gene(nodes ++ g.nodes)
  }

  def -(v: String) = {
    Gene(nodes - v)
  }

  override def toString(): String = {
    s"u{${nodes.mkString(",")}}"
  }
}

object Gene {

  import scala.util.Random
  import java.util.concurrent.{ ThreadLocalRandom => TLRandom }

  def apply(es: String*): Gene = {
    Gene(es.toSet)
  }
  /**
   * move single terminal symbol from source to target gene<br/>
   * source: u123 target u4 will result in (u12,u34)
   *
   * @param source
   *  the source gene to move terminal symbol from
   * @param target
   *  the target gene to move terminal symbol to
   * @return List[Gene]
   */
  def move(source: Gene, target: Gene): List[Gene] = {
    val sNodes = source.nodes.toArray
    sNodes match {
      case Array()         => List(target)
      case Array(v)        => List(target + v)
      case _ =>
        val random = TLRandom.current()
        val movedValue = sNodes(random.nextInt(sNodes.size))
        (source - movedValue) :: (target + movedValue) :: Nil
    }
  }

  def exchange(g1: Gene, g2: Gene): List[Gene] = {
    if (g1.size == 0) {
      if (g2.size == 0) {
        Nil
      } else {
        List(g2)
      }
    } else {
      if (g2.size == 0) {
        List(g1)
      } else {
        val g1Nodes = g1.nodes.toArray
        val g2Nodes = g2.nodes.toArray
        val random = TLRandom.current()
        val g1MovedValue = g1Nodes(random.nextInt(g1Nodes.size))
        val g2MovedValue = g2Nodes(random.nextInt(g2Nodes.size))
        List(g1 - g1MovedValue + g2MovedValue, g2 - g2MovedValue + g1MovedValue)
      }
    }
  }

  /**
   * merge two gene to one
   * @param g1
   * @param g2
   * @return g1 + g2
   */
  def merge(g1: Gene, g2: Gene): Gene = {
    g1 + g2
  }

  def splitoff(g: Gene): List[Gene] = {
    val nodeSize = g.size
    if (nodeSize <= 1) {
      List(g)
    } else {
      val position = TLRandom.current().nextInt(nodeSize / 3, nodeSize * 2 / 3)
      val splited = g.nodes.splitAt(position)
      List(Gene(splited._1.toSet), Gene(splited._2.toSet))
    }
  }
}