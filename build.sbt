ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "distributed-computing"
  )

libraryDependencies ++= Seq(
    "com.github.kiprobinson" % "bigfraction" % "1.1.0"
)

