package com.cc.graph

import org.scalatest.WordSpec
import org.scalatest.MustMatchers
import com.cc.graph.gep.Gene

class TestGene extends WordSpec with MustMatchers {

  "move gene" must {
    "has one result if the size of source is 1" in {
      val g1 = Gene("1")
      val g2 = Gene("4", "5", "6")
      val result = Gene.move(g1, g2)
      assert(result.size == 1)
      assert(result(0) == Gene("1", "5", "6", "4"))
    }
    "add element to target and remove element from source(source >1)" in {
      val g1 = Gene("1", "2", "3", "a")
      val g2 = Gene("4", "5", "6", "c")

      val sourceSize = g1.size
      val targetSize = g2.size

      val result = Gene.move(g1, g2)
      assert(result.size == 2)
      assert(result(0).size == sourceSize - 1)
      assert(result(1).size == targetSize + 1)
    }
  }

  "exchange genes" must {
    "has no result if both the size of genes is zero" in {
      val g1 = Gene()
      val g2 = Gene()
      val result = Gene.exchange(g1, g2)
      assert(result.size == 0)
    }
    "has one result if one the size of the genes is zero" in {
      val g1 = Gene()
      val g2 = Gene("4", "5", "6")
      val result = Gene.exchange(g1, g2)
      assert(result.size == 1)
      assert(result(0) == g2)
    }
    "has two result otherwise" in {
      val g1 = Gene("1")
      val g2 = Gene("2")
      val result = Gene.exchange(g1, g2)
      assert(result.size == 2)
      assert(result(0) + result(1) == g1 + g2)
      assert(result(0) == g2)
      assert(result(1) == g1)
    }
  }

  "merge gene" must {
    "return one gene contains all element" in {
      val g1 = Gene("1")
      val g2 = Gene("4", "5", "6")
      val result = Gene.merge(g1, g2)
      assert(result.size == g1.size + g2.size)
      assert(result == g1 + g2)
    }
  }

  "split gene" must {
    "return one gene if the size of the original is 1" in {
      val g = Gene("1")
      val result = Gene.splitoff(g)
      assert(result.size == 1)
      assert(result(0) == g)
    }

    "return two genes and sum of the lengths is equal to the original one" in {
      val g = Gene("1", "2", "3", "4", "5", "6")
      val result = Gene.splitoff(g)
      assert(result.size == 2)
      assert(result(0).size + result(1).size == g.size)
    }
  }
}