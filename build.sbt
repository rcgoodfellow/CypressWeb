
lazy val scalaV = "2.11.6"

lazy val dispatchV = "0.11.2"


lazy val commonSettings = Seq(
  organization := "net.deterlab",
  version := "0.1.0",
  scalaVersion := scalaV,
  exportJars := true,
  resolvers ++= Seq(
    "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
    "Cypress repository" at "http://cypress.deterlab.net:8081/nexus/content/repositories/releases/"
  ),
  scalacOptions in ThisBuild ++= Seq(
    "-feature", 
    "-language:implicitConversions",
    "-language:postfixOps",
    "-language:existentials")
)

lazy val commonDeps = Seq(
  "org.scala-lang" % "scala-library" % scalaV,
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

lazy val io = (project in file("io"))
  .settings(commonSettings: _*)
  .settings(scalaxbSettings : _*)
  .settings(
    sourceGenerators in Compile += (ScalaxbKeys.scalaxb in Compile).taskValue,
    ScalaxbKeys.dispatchVersion in (Compile, ScalaxbKeys.scalaxb) := dispatchV,
    ScalaxbKeys.async in (Compile, ScalaxbKeys.scalaxb) := true,
    ScalaxbKeys.packageName in (Compile, ScalaxbKeys.scalaxb) := "generated",
    libraryDependencies ++= commonDeps ++ Seq(
      "org.scala-lang" % "scala-compiler" % scalaV,
      "org.scala-lang" % "scala-reflect" % scalaV,
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
    libraryDependencies ++= Seq(
      jdbc, cache, ws, anorm,
      "org.scala-lang" % "scala-library" % scalaV,
      "org.scala-lang" % "scala-compiler" % scalaV,
      "org.scala-lang" % "scala-reflect" % scalaV,
      "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23",
      "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23",
      "org.slf4j" % "slf4j-simple" % "1.7.12",
      "commons-io" % "commons-io" % "2.4"
    )
  )
  .dependsOn(io, model)

lazy val control = (project in file("control"))
  .settings(commonSettings: _*)
  .settings(
    resolvers ++= Seq(
      "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.4-SNAPSHOT",
      "org.scala-lang.modules" %% "scala-pickling" % "0.10.0"
    )
  )

