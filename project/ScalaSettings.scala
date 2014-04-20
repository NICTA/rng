import sbt._
import Keys._
import Tools.onVersionTask

object ScalaSettings {
  type Sett = Def.Setting[_]

  private val scala210or211 = Seq(
    "-Yinline-warnings"
  , "-feature"
  , "-language:implicitConversions"
  , "-language:higherKinds"
  , "-language:postfixOps"
  )

  val wartremoverSettings = Seq(
    scalacOptions <++= onVersionTask(
      on210 = Seq("-P:wartremover:traverser:org.brianmckenna.wartremover.warts.Unsafe")
    )
  )

  lazy val all: Seq[Sett] = Seq(
    scalaVersion := "2.10.3"
  , crossScalaVersions := Seq("2.11.0", "2.10.3")
  , scalacOptions in Compile <++= onVersionTask(
    all = Seq("-deprecation", "-unchecked", "-optimise")
    , on210 = scala210or211
    , on211 = scala210or211
    )
  , scalacOptions in Test <<= onVersionTask(
      all = Seq("-deprecation", "-unchecked", "-optimise")
    , on210 = scala210or211
    , on211 = scala210or211
    )
  )
}
