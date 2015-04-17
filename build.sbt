import ScalaxbKeys._

name := "CypressWeb"

version := "1.0"

exportJars := true

scalaVersion := "2.11.1"

resolvers += "Cypress repository" at "http://cypress.deterlab.net:8081/nexus/content/repositories/releases/"

libraryDependencies ++= Seq( jdbc , anorm , cache , ws )

lazy val dispatchV = "0.11.2"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % "2.11.1",
  "org.scala-lang" % "scala-compiler" % "2.11.1",
  "org.scala-lang" % "scala-reflect" % "2.11.1",
  "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.2",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1",
  "net.databinder.dispatch" %% "dispatch-core" % dispatchV,
  "net.deterlab.isi" % "jabac" % "1.5"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

lazy val `cypressweb` = (project in file(".")).
  enablePlugins(PlayScala).
  settings(scalaxbSettings : _*).
  settings(
    sourceGenerators in Compile += (scalaxb in Compile).taskValue,
    dispatchVersion in (Compile, scalaxb) := dispatchV,
    async in (Compile, scalaxb) := true,
    packageName in (Compile, scalaxb) := "generated"
  )
