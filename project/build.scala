import sbt._
import Keys._
import com.typesafe.sbt.pgp.PgpKeys._
import Tools.onVersion

object build extends Build {
  type Sett = Project.Setting[_]

  val base = Defaults.defaultSettings ++ ScalaSettings.all ++ Seq[Sett](
      organization := "NICTA"
    , version := "1.0-SNAPSHOT"
  )

  val scalaz = "org.scalaz" %% "scalaz-core" % "7.0.0"
  val scalazEffect = "org.scalaz" %% "scalaz-effect" % "7.0.0"
  val scalacheck = "org.scalacheck" %% "scalacheck" % "1.10.0" % "test" cross(CrossVersion.full)
  val specs2_1_12_3 = "org.specs2" %% "specs2" % "1.12.3" % "test"
  val specs2_1_13 = "org.specs2" %% "specs2" % "1.13" % "test"

  val rng = Project(
    id = "rng"
  , base = file(".")
  , settings = base ++ ReplSettings.all ++ PublishSettings.all ++ InfoSettings.all ++ Seq[Sett](
      name := "rng"
    , libraryDependencies <++= onVersion(
        all = Seq(scalaz, scalazEffect, scalacheck)
      , on292 = Seq(specs2_1_12_3)
      , on210 = Seq(specs2_1_13)
      )
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

  publishMavenStyle := true

  publishArtifact in Test := false

  pomIncludeRepository := { _ => false }

  publishTo <<= version.apply(v => {
    val artifactory = "http://etd-packaging.research.nicta.com.au/artifactory/"
    val flavour = if (v.trim.endsWith("SNAPSHOT")) "libs-snapshot-local" else "libs-release-local"
    val url = artifactory + flavour
    val name = "etd-packaging.research.nicta.com.au"
    Some(Resolver.url(name, new URL(url)))
  })
}
