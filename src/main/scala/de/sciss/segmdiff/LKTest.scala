/*
 *  LKTest.scala
 *  (SegmDiff)
 *
 *  Copyright (c) 2020 Hanns Holger Rutz. All rights reserved.
 *
 *  This software is published under the GNU Affero General Public License v3+
 *
 *
 *  For further information, please contact Hanns Holger Rutz at
 *  contact@sciss.de
 */

package de.sciss.segmdiff

import de.sciss.segmdiff.CheckTriangleInequality.{mkEdgeCostMap, readGraph}
import de.sciss.tsp.LinKernighan

object LKTest {
  def main(args: Array[String]): Unit =
    run()

  def run(): Unit = {
    val (numVertices, edges) = readGraph()
    println(s"numVertices = $numVertices")  // 176
    val m: Map[Int, Map[Int, Double]] = mkEdgeCostMap(edges)
    val cost = Array.ofDim[Double](numVertices, numVertices)
    for (vi <- 0 until numVertices) {
      for (vj <- (vi + 1) until numVertices) {
        val c = m(vi)(vj)
        cost(vi)(vj) = c
        cost(vj)(vi) = c
      }
    }
    // randomization does not improve tour
    val tour0 = (0 until numVertices).toArray // 58.243789
//    val tour0 = util.Random.shuffle((0 until numVertices).toVector).toArray
    val lk    = LinKernighan(cost, tour0)
    println(s"Original cost: ${lk.tourCost}")
    val t0    = System.currentTimeMillis()
    lk.run()
    val t1    = System.currentTimeMillis()
    println(s"Optimization took ${t1-t0}ms.")
//    val tourOpt = lk.tour
    println(s"Optimized cost: ${lk.tourCost}")
    println(lk.tour.mkString(" => "))
  }
}
