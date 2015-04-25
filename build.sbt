name := """scala-runner"""

version := "0.1"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % "2.11.5",
  "org.scalatest" %% "scalatest" % "2.1.6" % "test",
  // Akka
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "com.typesafe.akka" %% "akka-remote" % "2.3.9",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.9"
)
