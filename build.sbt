import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.12"

lazy val microservice = Project("uknw-auth-checker-api", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
      // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalafmtOnCompile := true,
    scalacOptions += "-Wconf:cat=lint-eta-sam:s,src=routes/.*:s",
    PlayKeys.devSettings := Seq("play.server.http.port" -> "9070"),
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings *)
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    Test / unmanagedSourceDirectories := (Test / baseDirectory)(base => Seq(base / "test", base / "test-common")).value
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(scalafmtOnCompile := true)
  .settings(libraryDependencies ++= AppDependencies.it)

addCommandAlias("fmtAll", ";scalafmtAll;it/scalafmtAll")
addCommandAlias("runAllChecks", ";clean;compile;scalafmtCheckAll;coverage;test;it/test;scalastyle;coverageReport")
