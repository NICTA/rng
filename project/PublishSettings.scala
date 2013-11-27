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
  , credentials ++= Seq(
      Credentials(Path.userHome / ".credentials")
    , Credentials(Path.userHome / ".sbt" / "scoobi.credentials")
    )
  )

  lazy val pom: Sett =
    pomExtra := (
      <url>https://github.com/NICTA/rng/</url>
      <licenses>
        <license>
          <name>BSD-style</name>
          <url>http://www.opensource.org/licenses/bsd-license.php</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
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
      </developers>
      <developers>
        <developer>
          <id>markhibberd</id>
          <name>Mark Hibberd</name>
          <url>http://mth.io/</url>
        </developer>
      </developers>
      )

  lazy val publish: Sett =
    publishTo <<= version { v =>
      val artifactory = "http://etd-packaging.research.nicta.com.au/artifactory/"
      val flavour = if (v.trim.endsWith("SNAPSHOT")) "libs-snapshot-local" else "libs-release-local"
      val url = artifactory + flavour
      val name = "etd-packaging.research.nicta.com.au"
      Some(Resolver.url(name, new URL(url)))
    }
}
