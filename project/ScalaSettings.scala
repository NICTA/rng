import sbt._
import Keys._
import Tools.onVersionTask

object ScalaSettings {
  type Sett = Project.Setting[_]

  lazy val all: Seq[Sett] = Seq(
    scalaVersion := "2.10.2"
  , crossScalaVersions := Seq("2.9.2", "2.9.3", "2.10")
  , scalacOptions <++= onVersionTask(
      all = Seq("-deprecation", "-unchecked", "-optimise", "-P:wartremover:traverser:org.brianmckenna.wartremover.warts.Unsafe")
    , on210 = Seq("-Yinline-warnings", "-feature", "-language:implicitConversions", "-language:higherKinds", "-language:postfixOps")
    )
  )
}
