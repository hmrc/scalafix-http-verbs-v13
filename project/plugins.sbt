resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.bintrayIvyRepo("hmrc", "sbt-plugin-releases")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.24")

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "2.12.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "2.2.0")
