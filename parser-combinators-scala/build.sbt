val scala3Version = "3.1.3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "parser-combinators-scala",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.typelevel" %% "cats-parse" % "0.3.8"
  )
