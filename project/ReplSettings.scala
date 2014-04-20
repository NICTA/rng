import sbt._
import Keys._

object ReplSettings {
  type Sett = Def.Setting[_]

  lazy val all = Seq[Sett](
    initialCommands := """
                         |import com.nicta.rng._
                         |import Rng._
                         |import scalaz._
                         |import Scalaz._
                       """.stripMargin
  )
}
