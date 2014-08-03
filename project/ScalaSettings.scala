import sbt._
import Keys._

object ScalaSettings {
  type Sett = Def.Setting[_]

  private val scala210or211 = Seq(
    "-Yinline-warnings"
  , "-feature"
  , "-language:implicitConversions"
  , "-language:higherKinds"
  , "-language:postfixOps"
  )

  lazy val all: Seq[Sett] = Seq(
    scalaVersion := "2.11.2"
  , crossScalaVersions := Seq("2.11.2", "2.10.4")
  , scalacOptions in Compile ++=  Seq("-deprecation", "-unchecked", "-optimise") ++ scala210or211
  , scalacOptions in Test ++= Seq("-deprecation", "-unchecked", "-optimise") ++ scala210or211
  )
}
