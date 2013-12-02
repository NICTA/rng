import sbt._
import Keys._
import com.typesafe.sbt._
import pgp.PgpKeys._

object PublishSettings {
  type Sett = Project.Setting[_]

  lazy val all = Seq[Sett](
    pom
  , publish
  , publishMavenStyle := true
  , publishArtifact in Test := false
  , pomIncludeRepository := { _ => false }
  , licenses := Seq("BSD-3-Clause" -> url("http://www.opensource.org/licenses/BSD-3-Clause"))
  , homepage := Some(url("https://github.com/NICTA/rng"))
  , useGpg := true
  , credentials := Seq(Credentials(Path.userHome / ".sbt" / "scoobi.credentials"))
  )

  lazy val pom: Sett =
    pomExtra := (
      <scm>
        <url>git@github.com:NICTA/rng.git</url>
        <connection>scm:git@github.com:NICTA/rng.git</connection>
      </scm>
      <developers>
        <developer>
          <id>tonymorris</id>
          <name>Tony Morris</name>
          <url>http://tmorris.net/</url>
        </developer>
        <developer>
          <id>markhibberd</id>
          <name>Mark Hibberd</name>
          <url>http://mth.io/</url>
        </developer>
      </developers>
      )

  lazy val publish: Sett =
    publishTo <<= version.apply(v => {
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    })
}
