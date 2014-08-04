addSbtPlugin(("com.typesafe.sbt" % "sbt-pgp" % "0.8").cross(CrossVersion.full))

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.2")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.2.1")

scalacOptions += "-deprecation"

resolvers += Resolver.typesafeRepo("releases")
