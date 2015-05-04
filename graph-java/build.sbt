name := """graph-java"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
    "org.graphstream" % "gs-core" % "1.2",
    "com.typesafe" % "config" % "1.2.1",
    "org.graphstream" % "gs-ui" % "1.2"
)
