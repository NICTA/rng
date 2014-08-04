import sbt._
import Keys._
import Tools.onVersion
import sbtrelease.ReleasePlugin._

object build extends Build {
  type Sett = Def.Setting[_]

  val base = Defaults.defaultSettings ++ ScalaSettings.all ++ Seq[Sett](
      name := "rng"
    , organization := "com.nicta"
    , version := "1.2.1"
  )

  val scalaz          = "org.scalaz"       %% "scalaz-core"               % "7.0.6"
  val scalazEffect    = "org.scalaz"       %% "scalaz-effect"             % "7.0.6"
  val scalazCheck     = "org.scalaz"       %% "scalaz-scalacheck-binding" % "7.0.6"    % "test"
  val specs2_1_12_4_1 = "org.specs2"       %% "specs2"                    % "1.12.4.1" % "test"
  val specs2_2_3_2    = "org.specs2"       %% "specs2"                    % "2.3.2"    % "test"
  val specs2_2_3_11   = "org.specs2"       %% "specs2"                    % "2.3.11"   % "test"
  val wartremover     = "org.brianmckenna" %% "wartremover"               % "0.7"      % "plugin->default(compile)"

  val rng = Project(
    id = "rng"
  , base = file(".")
  , settings = base ++ ScalaSettings.wartremoverSettings ++ ReplSettings.all ++ releaseSettings ++ PublishSettings.all ++ InfoSettings.all ++ Seq[Sett](
      name := "rng"
    , libraryDependencies <++= onVersion(
        all = Seq(scalaz, scalazEffect, scalazCheck)
      , on292 = Seq(specs2_1_12_4_1)
      , on210 = Seq(specs2_2_3_2, wartremover)
      , on211 = Seq(specs2_2_3_11)
      )
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
