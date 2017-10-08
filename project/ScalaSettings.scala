import sbt._
import Keys._

object ScalaSettings {
  type Sett = Def.Setting[_]

  private def scalacOptionsVersion(scalaVersion: String): Seq[String] = {
    Seq(
      "-deprecation"
      , "-unchecked"
      , "-feature"
      , "-language:implicitConversions"
      , "-language:higherKinds"
      , "-language:postfixOps"
    ) ++ (CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor)) if scalaMajor <= 11 => Seq(
        "-Yinline-warnings"
        , "-optimise"
      )
      case _ => Seq.empty
    })
  }

  lazy val all: Seq[Sett] = Seq(
    scalaVersion := "2.12.3"
  , crossScalaVersions := Seq("2.12.3", "2.11.11", "2.10.6")
  , scalacOptions in Compile ++= scalacOptionsVersion(scalaVersion.value)
  , scalacOptions in Test ++= scalacOptionsVersion(scalaVersion.value)
  )
}
