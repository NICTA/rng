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

  val scalazVersion = "7.2.15"
  val specs2Version = "3.9.5"

  val scalaz          = "org.scalaz"       %% "scalaz-core"               % scalazVersion
  val scalazEffect    = "org.scalaz"       %% "scalaz-effect"             % scalazVersion
  val scalazCheck     = "org.scalaz"       %% "scalaz-scalacheck-binding" % scalazVersion    % "test"
  val specs2          = "org.specs2"       %% "specs2-core"               % specs2Version    % "test"
  val specs2Check     = "org.specs2"       %% "specs2-scalacheck"         % specs2Version    % "test"
  val scalaCheck      = "org.scalacheck"   %% "scalacheck"                % "1.13.4"         % "test"

  val rng = Project(
    id = "rng"
  , base = file(".")
  , settings = base ++ ReplSettings.all ++ releaseSettings ++ PublishSettings.all ++ InfoSettings.all ++ Seq[Sett](
      name := "rng"
    , libraryDependencies ++= Seq(scalaz, scalazEffect, scalazCheck, specs2, specs2Check, scalaCheck)
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
