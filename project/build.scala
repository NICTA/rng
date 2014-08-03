import sbt._
import Keys._
import sbtrelease.ReleasePlugin._

object build extends Build {
  type Sett = Def.Setting[_]

  val base = Defaults.defaultSettings ++ ScalaSettings.all ++ Seq[Sett](
      name := "rng"
    , organization := "com.nicta"
    , version := "1.2.1"
  )

  val scalaz          = "org.scalaz"       %% "scalaz-core"               % "7.1.0"
  val scalazEffect    = "org.scalaz"       %% "scalaz-effect"             % "7.1.0"
  val scalazCheck     = "org.scalaz"       %% "scalaz-scalacheck-binding" % "7.1.0"    % "test"
  val specs2          = "org.specs2"       %% "specs2"                    % "2.4"      % "test"

  val rng = Project(
    id = "rng"
  , base = file(".")
  , settings = base ++ ReplSettings.all ++ releaseSettings ++ PublishSettings.all ++ InfoSettings.all ++ Seq[Sett](
      name := "rng"
    , libraryDependencies ++= Seq(scalaz, scalazEffect, scalazCheck, specs2)
    ) ++
    net.virtualvoid.sbt.graph.Plugin.graphSettings
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
