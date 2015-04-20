


lazy val dispatchV = "0.11.2"

lazy val commonSettings = Seq(
  organization := "net.deterlab",
  version := "0.1.0",
  scalaVersion := "2.11.1",
  exportJars := true,
  resolvers ++= Seq(
    "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
    "Cypress repository" at "http://cypress.deterlab.net:8081/nexus/content/repositories/releases/"
  ),
  scalacOptions in ThisBuild ++= Seq(
    "-feature", 
    "-language:implicitConversions",
    "-language:postfixOps",
    "-language:existentials"),
  EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE18)
)

lazy val commonDeps = Seq(
  "org.scala-lang" % "scala-library" % "2.11.1",
  "org.scalatest" % "scalatest_2.11" % "2.2.2" % "test",
  "org.slf4j" % "slf4j-simple" % "1.7.12"
)
    


lazy val model = (project in file("model"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= commonDeps ++ Seq(
      "com.typesafe.play" %% "play-json" % "2.3.8"
    )
  )

//import ScalaxbKeys._

lazy val io = (project in file("io"))
  .settings(commonSettings: _*)
  .settings(scalaxbSettings : _*)
  .settings(
    sourceGenerators in Compile += (ScalaxbKeys.scalaxb in Compile).taskValue,
    ScalaxbKeys.dispatchVersion in (Compile, ScalaxbKeys.scalaxb) := dispatchV,
    ScalaxbKeys.async in (Compile, ScalaxbKeys.scalaxb) := true,
    ScalaxbKeys.packageName in (Compile, ScalaxbKeys.scalaxb) := "generated",
    //ScalaxbKeys.paramPrefix in (Compile, ScalaxbKeys.scalaxb) := Some("x"),
    //ScalaxbKeys.classPrefix in (Compile, ScalaxbKeys.scalaxb) := Some("y"),
    //ScalaxbKeys.attributePrefix in (Compile, ScalaxbKeys.scalaxb) := Some("x"),
    libraryDependencies ++= commonDeps ++ Seq(
      "org.scala-lang" % "scala-compiler" % "2.11.1",
      "org.scala-lang" % "scala-reflect" % "2.11.1",
      "org.scala-lang.modules" %% "scala-xml" % "1.0.2",
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1",
      "net.databinder.dispatch" %% "dispatch-core" % dispatchV,
      "net.deterlab.isi" % "jabac" % "1.5"
    )
  )
  .dependsOn(model)

lazy val web = (project in file("web"))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(
    //autoScalaLibrary := false,
  )
  .settings(
    libraryDependencies ++= Seq(
      jdbc, cache, ws,
      "org.scala-lang" % "scala-library" % "2.11.1",
      "org.scala-lang" % "scala-compiler" % "2.11.1",
      "org.scala-lang" % "scala-reflect" % "2.11.1",
      "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23",
      "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23",
      "org.slf4j" % "slf4j-simple" % "1.7.12"
    ),
    EclipseKeys.skipParents in ThisBuild := false,
    EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed
  )
  .dependsOn(io, model)

