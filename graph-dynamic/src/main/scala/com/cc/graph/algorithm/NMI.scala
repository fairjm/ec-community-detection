package com.cc.graph.algorithm

import com.cc.graph.base.Graph
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap

object NMI {

  /**
   * NMI(A,B)=2*H(A)/(H(A)+H(B))
   */
  def apply(a: Graph.Communities, b: Graph.Communities): Double = {
    applyInner(communityToList(a), communityToList(b))
  }

  private def communityToList(comms: Graph.Communities): List[Int] = {
    val list = ListBuffer[(String, Int)]()
    var communityNum = 1
    for (comm <- comms) {
      list ++= comm.map(a => (a, communityNum))
      communityNum += 1
    }
    list.sortBy(_._1).map(_._2).toList
  }

  private def applyInner(a: List[Int], b: List[Int]): Double = {
    val i = I(a, b)
    val ha = H(a)
    val hb = H(b)
    2 * i / (ha + hb)
  }

  /**
   * I(A,B)=H(A)-H(A|B）
   * sigma x,y p(x,y)* log2(p(x,y)/(p(x)*p(y))
   */
  def I(a: List[Int], b: List[Int]): Double = {
    val pBuffer = HashMap[(String, Int), Double]()
    val p2Buffer = HashMap[(Int, Int), Double]()
    val ua = a.toSet.toList
    val ub = b.toSet.toList
    val r = for {
      x <- ua
      y <- ub
    } yield {
      val pxy = p2Buffer.getOrElseUpdate((x, y), P(x, a, y, b))
      val px = pBuffer.getOrElseUpdate(("x", x), P(x, a))
      val py = pBuffer.getOrElseUpdate(("y", y), P(y, b))
      val r = pxy * (log2(pxy / (px * py)))
      if (r.isInfinite() || r.isNaN()) 0 else r
    }
    r.sum
  }

  /**
   * H(X) = -p(1)*log2(p(1)) -p(2)*log2(p(2)) -p(3)*log2(p(3)) and so on
   */
  def H(a: List[Int]): Double = {
    val ua = a.toSet.toList
    val r = for (i <- ua) yield {
      val pi = P(i, a)
      -1.0 * pi * log2(pi)
    }
    r.sum
  }

  def P(commNum: Int, comm: List[Int]) = {
    1.0 * comm.filter(_ == commNum).size / comm.size
  }

  def P(commNumA: Int, commA: List[Int], commNumB: Int, commB: List[Int]): Double = {
    val size = commA.size
    var count = 0
    for (i <- 0 until size) {
      if (commA(i) == commNumA && commB(i) == commNumB) {
        count += 1;
      }
    }
    count * 1.0 / size
  }

  def log2(i: Double): Double = Math.log(i) / Math.log(2)

  def main(args: Array[String]): Unit = {
    val a = communityToList(List(Set("1", "2", "3", "4", "5", "6"), Set("7", "8", "9", "10", "11", "12"), Set("13", "14", "15", "16", "17")))
    val b = communityToList(List(Set("2", "8", "9", "10", "11"), Set("1", "3", "4", "5", "6", "7", "13", "14"), Set("12", "15", "16", "17")))
    println("a")
    println(P(1, a))
    println(P(2, a))
    println(P(3, a))
    println("b")
    println(P(1, b))
    println(P(2, b))
    println(P(3, b))
    println("a - b")
    println(P(1, a, 1, b))
    println(P(1, a, 2, b))
    println(P(1, a, 3, b))
    println(P(2, a, 1, b))
    println(P(2, a, 2, b))
    println(P(2, a, 3, b))
    println(P(3, a, 1, b))
    println(P(3, a, 2, b))
    println(P(3, a, 3, b))

    println(applyInner(a, b))
    println(applyInner(b, a))
    println(applyInner(a, a))
    println(applyInner(b, b))
  }
}