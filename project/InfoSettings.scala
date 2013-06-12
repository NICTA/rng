import sbt._
import Keys._

object InfoSettings {
  type Sett = Project.Setting[_]

  def all = Seq[Sett](versioninfo)

  val versioninfo = sourceGenerators in Compile <+= (sourceManaged in Compile, version, name) map { (d, v, n) =>
    val file = d / "info.scala"
    IO.write(file, """package com.nicta
                      package rng
                     |object Info {
                     |  val version = "%s"
                     |  val name = "rng"
                     |}
                     |""".stripMargin.format(v))
    Seq(file)
  }


}
