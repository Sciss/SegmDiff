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
    import de.sciss.fscape.graph._
    import de.sciss.numbers.Implicits._

    Graph {
      val aIn       = AudioFileIn(a, numChannels = 1)
      val bIn       = AudioFileIn(b, numChannels = 1)
      val chunkSize = 65536
      val numBins   = chunkSize >> 1
      val aFFT      = Real1FFT(aIn, size = chunkSize, mode = 2)
      val bFFT      = Real1FFT(bIn, size = chunkSize, mode = 2)
      val sr        = 44100.0
      val nyquist   = sr/2

      def aWeight(f: GE): GE = {
        val f2 = f .squared
        val f4 = f2.squared
        val nom = 12194.squared * f4
        val den = (f2 + 20.6.squared) * ((f2 + 107.7.squared) * (f2 + 737.9.squared)).sqrt *
          (f2 + 12194.squared)
        nom / den
      }

//      val freqSaw = ResizeWindow(GenWindow(numBins * 2, GenWindow.Triangle),
//        numBins * 2, stop = -numBins).linLin(0, 1, freqLo, freqHi).take(binLenA)

      val aBins = aFFT.complex.mag
      val bBins = bFFT.complex.mag
      val awt   = RepeatWindow(aWeight(Line(0.0, nyquist, numBins)), numBins, ??? /*numSteps*/)

      val aW    = aBins * awt
      val bW    = bBins * awt

      Pearson(aW, bW, numBins)
    }
  }
}
