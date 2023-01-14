ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "distributed-computing"
  )

val akkaVersion = "2.7.0"

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.4.5" % Runtime,
    "com.github.kiprobinson" % "bigfraction" % "1.1.0"
)

