import sbt._
import Keys._
import sbtrelease.ReleasePlugin._
import xerial.sbt.Sonatype._
import SonatypeKeys._

object build extends Build {
  type Sett = Def.Setting[_]

  val base = Defaults.defaultSettings ++ ScalaSettings.all ++ Seq[Sett](
      name := "rng"
    , organization := "com.nicta"
  )

  val scalaz          = "org.scalaz"       %% "scalaz-core"               % "7.2.15"
  val scalazEffect    = "org.scalaz"       %% "scalaz-effect"             % "7.2.15"
  val scalazCheck     = "org.scalaz"       %% "scalaz-scalacheck-binding" % "7.2.15"    % "test"
  val specs2          = "org.specs2"       %% "specs2-core"               % "3.9.5"      % "test"

  val rng = Project(
    id = "rng"
  , base = file(".")
  , settings = base ++ ReplSettings.all ++ releaseSettings ++ PublishSettings.all ++ InfoSettings.all ++ Seq[Sett](
      name := "rng"
    , libraryDependencies ++= Seq(scalaz, scalazEffect, scalazCheck, specs2)
    ) ++
    net.virtualvoid.sbt.graph.Plugin.graphSettings ++
    sonatypeSettings
  )

  val examples = Project(
    id = "examples"
  , base = file("examples")
  , dependencies = Seq(rng)
  , settings = base ++ Seq[Sett](
      name := "rng-examples"
    , fork in run := true
    , libraryDependencies ++= Seq(scalaz, scalazEffect)
    , javaOptions in run <++= (fullClasspath in Runtime) map { cp => Seq("-cp", sbt.Attributed.data(cp).mkString(":")) }
    , resolvers ++= Seq(Resolver.typesafeRepo("releases"))
    )
  )
}
