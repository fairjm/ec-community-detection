name := """graph"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.0" % "test",
    "org.graphstream" % "gs-core" % "1.2",
    "org.graphstream" % "gs-ui" % "1.2",
    "com.typesafe" % "config" % "1.2.1"
)
