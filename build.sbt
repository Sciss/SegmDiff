lazy val commonSettings = Seq(
  name               := "SegmDiff",
  version            := "0.1.0-SNAPSHOT",
  organization       := "de.sciss",
  scalaVersion       := "2.13.1",
  licenses           := Seq("GPL v3+" -> url("http://www.gnu.org/licenses/gpl-3.0.txt")),
  homepage           := Some(url(s"https://git.iem.at/sciss/${name.value}")),
  scalacOptions     ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xsource:2.13", "-Xlint:-stars-align,_"),
  scalacOptions      += "-Yrangepos",  // this is needed to extract source code
  updateOptions      := updateOptions.value.withLatestSnapshots(false)
)

lazy val deps = new {
  val fscape          = "2.33.2"
  val kollflitz       = "0.2.3"
  val linKernighan    = "0.1.0"
  val lucre           = "3.16.1"
  val soundProcesses  = "3.33.0"
  val topology        = "1.1.2"
}

lazy val root = project.in(file("."))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "de.sciss" %% "fscape-lucre"                % deps.fscape,
      "de.sciss" %% "fscape-macros"               % deps.fscape,
      "de.sciss" %% "kollflitz"                   % deps.kollflitz,
      "de.sciss" %% "linkernighantsp"             % deps.linKernighan,
      "de.sciss" %% "lucre-bdb"                   % deps.lucre,
      "de.sciss" %% "soundprocesses-core"         % deps.soundProcesses,
      "de.sciss" %% "topology"                    % deps.topology,
    ),
  )
