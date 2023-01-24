ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "CsvAdder"
  )

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.3.11",
  "com.github.fd4s" %% "fs2-kafka" % "2.4.0",
  "co.fs2" %% "fs2-io" % "3.5.0",
  "org.http4s" %% "http4s-core" % "0.23.18",
  "org.http4s" %% "http4s-blaze-server" % "0.23.13",
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.2.6",
  "org.http4s" %% "http4s-dsl" % "0.23.18",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % "1.4.5"
)

addCommandAlias("check", ";scalafmtCheckAll ;scalafmtSbtCheck ;scalafixAll --check; dependencyUpdates")
addCommandAlias("format", ";scalafmtAll ;scalafmtSbt; scalafixAll")