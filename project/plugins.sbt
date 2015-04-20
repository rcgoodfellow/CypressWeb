resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += Resolver.sonatypeRepo("public")

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

//libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.1.1"

//libraryDependencies += "org.scalaz" %% "scalaz-effect" % "7.1.1"

//libraryDependencies += "org.scalaz" %% "scalaz-typelevel" % "7.1.1"

//libraryDependencies += "org.scalaz" %% "scalaz-scalacheck-binding" % "7.1.1" % "test"

//libraryDependencies += specs2 % Test

libraryDependencies += "org.specs2" %% "specs2-core" % "3.5" % "test"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" % "test"

resolvers += "Typesafe Snapshots" at "https://repo.typesafe.com/typesafe/snapshots/"


addSbtPlugin("org.scalaxb" % "sbt-scalaxb" % "1.3.0")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.8")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "3.0.0")

