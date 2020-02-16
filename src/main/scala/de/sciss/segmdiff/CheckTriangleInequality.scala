package de.sciss.segmdiff

import de.sciss.file._
import de.sciss.synth.io.AudioFile

import scala.collection.immutable.{IndexedSeq => Vec}

object CheckTriangleInequality {
  def main(args: Array[String]): Unit =
    run()

  final case class WEdge(source: Int, target: Int, weight: Double)

  def mkEdgeCostMap(edges: Seq[WEdge]): Map[Int, Map[Int, Double]] =
    edges.groupBy(_.source).map { case (key, value) =>
      (key, value.groupBy(_.target).map { case (key, value) => assert (value.size == 1); (key, value.head.weight) })
    }

  def run(): Unit = {
    val (numVertices, edges) = readGraph()
    val m: Map[Int, Map[Int, Double]] = mkEdgeCostMap(edges)
    println(s"numVertices = $numVertices")  // 176
//    println(s"LOCALE: ${java.util.Locale.getDefault}")
    var bad = 0
    var reallyBad = 0
    var count = 0
    for (a <- 0 until numVertices; b <- (a + 1) until numVertices; c <- (b + 1) until numVertices) {
      val ab = m(a)(b)
      val ac = m(a)(c)
      val bc = m(b)(c)
      val ok = ab + ac >= bc && ab + bc >= ac && ac + bc >= ab
      if (!ok) {
        bad += 1
        val df0 = (bc - (ab + ac)).max(0.0) / bc
        val df1 = (ac - (ab + bc)).max(0.0) / ac
        val df2 = (ab - (ac + bc)).max(0.0) / ab
        val df  = math.max(df0, math.max(df1, df2)) * 100
        if (df > 3) reallyBad += 1
        println(f"$ab%1.3f, $ac%1.3f, $bc%1.3f -- $df%1.2f%%")
      }
      count += 1
    }
    println(f"Done. Bad triangles: $bad (${bad * 100.0 / count}%1.2f%%). Really bad: $reallyBad (${reallyBad * 100.0 / count}%1.2f%%)")
  }

  implicit class Tuple2Ops[A](private val in: (A, A)) extends AnyVal {
    def sorted(implicit ord: Ordering[A]): (A, A) =
      if (ord.lteq(in._1, in._2)) in else in.swap
  }

//  def threeOpt(): Unit = {
//    // cf. https://en.wikipedia.org/wiki/3-opt
//
//    val (numVertices, edges) = readGraph()
//    val m: Map[Int, Map[Int, Double]] = mkEdgeCostMap(edges)
//    import de.sciss.kollflitz.RandomOps._
//    import de.sciss.kollflitz.Ops._
//
//    implicit val rnd: Random = new Random(0L)
//    val s0 = ((0 until numVertices): Vec[Int]).shuffle()
//    val c0 = s0.mapPairs { (v0, v1) =>
//      val (vSrc, vTgt) = (v0, v1).sorted
//      m(vSrc)(vTgt)
//    } .sum
//
//    println(f"Costs0: $c0%1.3f")
//
//
//  }

  def readGraph(): (Int, Vec[WEdge]) = {
    val baseDir   = file("/data") / "projects" / "Almat" / "events" / "graz2020" / "kunsthaus"
    val audioDir  = baseDir / "audio_work"
    val fIn       = audioDir / "KunsthausStaircaseOM1_200128_11h_Level1-spect-extractOrderCorr.aif"
    val afIn      = AudioFile.openRead(fIn)
    try {
      require (afIn.numChannels == 1)
      val numFrames = afIn.numFrames.toInt
      val numChunks = (0.5 + math.sqrt(0.25 + numFrames * 2)).round.toInt
      val buf       = afIn.buffer(numFrames)
      afIn.read(buf)
      var i = 0
      val b0 = buf(0)
      val edges = for (a <- 0 until numChunks; b <- (a + 1) until numChunks) yield {
        val w = 1.0-b0(i).toDouble // high correlation = low cost
        i += 1
        WEdge(a, b, w)
      }
      (numChunks, edges)

    } finally {
      afIn.close()
    }
  }
}
