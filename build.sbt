lazy val V = _root_.scalafix.sbt.BuildInfo
inThisBuild(
  List(
    scalaVersion := V.scala212,
    crossScalaVersions := List(V.scala212, V.scala211),
    majorVersion := 0,
    addCompilerPlugin(scalafixSemanticdb),
    scalacOptions ++= List(
      "-Yrangepos",
      "-P:semanticdb:synthetics:on"
    )
  )
)

skip in publish := true

lazy val rules = project
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
  .settings(
    moduleName := "scalafix-http-verbs-v13",
    scalaVersion := V.scala212,
    crossScalaVersions := List(V.scala212, V.scala211),
    libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % V.scalafixVersion
  )

lazy val input = project.settings(
  skip in publish := true,
  libraryDependencies ++= Seq(
    "uk.gov.hmrc" %% "http-verbs-play-26" % "12.3.0"
  )
)

lazy val output = project.settings(
  skip in publish := true,
  libraryDependencies ++= Seq(
    "uk.gov.hmrc" %% "http-verbs-play-26" % "13.0.0-SNAPSHOT"
  )
)

lazy val tests = project
  .settings(
    skip in publish := true,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % V.scalafixVersion % Test cross CrossVersion.full,
    compile.in(Compile) :=
      compile.in(Compile).dependsOn(compile.in(input, Compile)).value,
    scalafixTestkitOutputSourceDirectories :=
      unmanagedSourceDirectories.in(output, Compile).value,
    scalafixTestkitInputSourceDirectories :=
      unmanagedSourceDirectories.in(input, Compile).value,
    scalafixTestkitInputClasspath :=
      fullClasspath.in(input, Compile).value,
  )
  .dependsOn(rules)
  .enablePlugins(ScalafixTestkitPlugin)
