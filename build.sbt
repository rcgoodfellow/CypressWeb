name := "CypressWeb"

version := "1.0"

//fork := true
exportJars := true

lazy val `cypressweb` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( jdbc , anorm , cache , ws )

libraryDependencies += "org.scala-lang" % "scala-library" % "2.11.1"

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.11.1"

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.11.1"

libraryDependencies += "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  