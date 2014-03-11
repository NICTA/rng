import sbt._
import Keys._
import Tools.onVersion
import sbtrelease.ReleasePlugin._

object build extends Build {
  type Sett = Project.Setting[_]

  val base = Defaults.defaultSettings ++ ScalaSettings.all ++ Seq[Sett](
      name := "rng"
    , organization := "com.nicta"
    , version := "1.2.1"
  )

  val scalaz          = "org.scalaz"       %% "scalaz-core"               % "7.0.6"
  val scalazEffect    = "org.scalaz"       %% "scalaz-effect"             % "7.0.6"
  val scalazCheck     = "org.scalaz"       %% "scalaz-scalacheck-binding" % "7.0.6"    % "test"
  val scalacheck      = "org.scalacheck"   %% "scalacheck"                % "1.11.0"   % "test"
  val specs2_1_12_4_1 = "org.specs2"       %% "specs2"                    % "1.12.4.1" % "test"
  val specs2_2_3_2    = "org.specs2"       %% "specs2"                    % "2.3.10"   % "test"
  val wartremover     = "org.brianmckenna" %% "wartremover"               % "0.7" 

  val rng = Project(
    id = "rng"
  , base = file(".")
  , settings = base ++ ReplSettings.all ++ releaseSettings ++ PublishSettings.all ++ InfoSettings.all ++ Seq[Sett](
      name := "rng"
    , libraryDependencies <++= onVersion(
        all = Seq(scalaz, scalazEffect, scalazCheck, scalacheck)
      , on292 = Seq(specs2_1_12_4_1)
      , on210 = Seq(specs2_2_3_2)
      )
    , addCompilerPlugin(wartremover)
    )
  )

  val examples = Project(
    id = "examples"
  , base = file("examples")
  , dependencies = Seq(rng)
  , settings = base ++ Seq[Sett](
      name := "rng-examples"
    , fork in run := true
    , libraryDependencies ++= Seq(scalaz, scalazEffect)
    , javaOptions in run <++= (fullClasspath in Runtime) map { cp => Seq("-cp", sbt.Build.data(cp).mkString(":")) }
    )
  )
}
