/*
 *  BinarySimilarities.scala
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

import de.sciss.file.File
import de.sciss.fscape.Graph
import de.sciss.fscape.GE

object CompleteGraphTest {
  def any2stringadd: Any = ()

  def mkGraphCorr(a: File, b: File): Graph = {
    import de.sciss.fscape.graph.{AudioFileIn => _, AudioFileOut => _, _}
    import de.sciss.fscape.lucre.graph._
    import de.sciss.fscape.lucre.graph.Ops._
    import de.sciss.numbers.Implicits._

    Graph {
      def mkIn()    = AudioFileIn("in")
      val in0       = mkIn()
      val in1       = mkIn()
      val sr        = in0.sampleRate
      //val maxFrames   = (sr * (60 * 5)).floor
      val numFramesIn = in0.numFrames // .min(maxFrames)
      val chunkSize = 65536
      val numBins   = chunkSize >> 1
      val numChunks = (numFramesIn / chunkSize).floor

      val fileType      = "out-type"    .attr(0)
      val smpFmt        = "out-format"  .attr(2)
      //val gainType      = "gain-type"   .attr(1)
      //val gainDb        = "gain-db"     .attr(0.0)
      //val gainAmt       = gainDb.dbAmp

      def mkIns(): (GE, GE) = {
        val aFFT      = Real1FFT(in0, size = chunkSize, mode = 2)
        val bFFT      = Real1FFT(in1, size = chunkSize, mode = 2)
        val _aMag     = aFFT.complex.mag
        val _bMag     = bFFT.complex.mag

        def chunkSeq  = ArithmSeq(length = numChunks)
        val chunkA    = RepeatWindow(chunkSeq, 1        , numChunks)
        val chunkB    = RepeatWindow(chunkSeq, numChunks, numChunks)
        val guard     = chunkA < chunkB
        val startA    = FilterSeq(chunkA, guard) * numBins  // chunkSize
        val startB    = FilterSeq(chunkB, guard) * numBins  // chunkSize
        val spansA    = startA zip (startA + numBins)
        val spansB    = startB zip (startB + numBins)

        val _slicesA  = Slices(_aMag, spansA)
        val _slicesB  = Slices(_bMag, spansB)
        (_slicesA, _slicesB)
      }

      val (aBins, bBins)  = mkIns()

      val nyquist   = sr/2

      def aWeight(f: GE): GE = {
        val f2 = f .squared
        val f4 = f2.squared
        val nom = 12194.squared * f4
        val den = (f2 + 20.6.squared) * ((f2 + 107.7.squared) * (f2 + 737.9.squared)).sqrt *
          (f2 + 12194.squared)
        nom / den
      }

      //Length(aBins).poll("aBins.length")
      //Length(bBins).poll("bBins.length")

      val awt   = RepeatWindow(aWeight(Line(0.0, nyquist, numBins)), numBins, numChunks)

      val aW    = aBins * awt
      val bW    = bBins * awt

      //Length(aW).poll("aW.length")
      //Length(bW).poll("bW.length")

      val corr  = Pearson(aW, bW, numBins)

      Length(corr).poll("corr.length")

      var prog = List.empty[(GE, GE, String)]

      def mkProgress(x: GE, num: GE, label: String): Unit = {
        prog ::= ((x, num, label))
      }

      def flushProgress(): Unit = {
        //  val total = prog.map(_._2).reduce(_ ++ _)
        val n = prog.size
        prog.reverse.foreach { case (x, num, label) =>
          ProgressFrames(x, num * n, label)
        }
      }

      ////def applyGain(x: GE, num: GE) =
      ////  If (gainType sig_== 0) Then {
      ////    val xBuf      = BufferDisk(x)
      ////    val rMax      = RunningMax(Reduce.max(x.abs))
      ////    mkProgress(rMax, num, "analyze")
      ////    val maxAmp    = rMax.last
      ////    val div       = maxAmp + (maxAmp sig_== 0.0)
      ////    val gainAmtN  = gainAmt / div
      ////    xBuf * gainAmtN
      ////
      ////  } Else {
      ////    x * gainAmt
      ////  }
      //
      val numFramesOut = numChunks
      val sig       = corr // applyGain(ordered, numFramesOut)
      //val sig       = applyGain(conv, numFramesOut)
      val written   = AudioFileOut("out", sig, fileType = fileType,
        sampleFormat = smpFmt, sampleRate = sr) // sr/chunkSize)
      mkProgress(written, numFramesOut, "out")

      flushProgress()
    }
  }
}
