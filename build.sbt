import scala.sys.process._

ThisBuild / resolvers ++= Seq(
  "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
  Resolver.mavenLocal
)

ThisBuild / resolvers ++= Seq(
  "Segment Flink repository" at "https://public-personas-flink-maven.s3.us-west-2.amazonaws.com/",
  Resolver.mavenLocal
)

ThisBuild / version := ("git rev-parse --short head" #|| "echo snapshot").!!.trim
ThisBuild / scalaVersion := "2.12.13"
ThisBuild / organization := "playground"

// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
ThisBuild / Compile / run / fork := true
Global / cancelable := true


// Must use java 8
//val oldFlinkVersion = "1.14.4-segment.2"
val oldFlinkVersion = "1.8.2"
//lazy val timestampTestingOldFlink = (project in file("."))
//  .settings(
//    name := "timestampTestingOldFlink",
//    libraryDependencies ++= Seq(
//
//      "org.apache.flink" %% "flink-scala" % oldFlinkVersion,
//      "org.apache.flink" %% "flink-statebackend-rocksdb" % oldFlinkVersion,
//      "org.slf4j" % "slf4j-log4j12" % "1.7.36",
//      "org.slf4j" % "slf4j-simple" % "1.7.36"
//    ),
//    Compile / run / mainClass := Some("playground.OldFlinkTimestampTesting"),
//
//    // make run command include the provided dependencies
//    Compile / run := Defaults
//      .runTask(Compile / fullClasspath, Compile / run / mainClass, Compile / run / runner)
//      .evaluated
//  )

// Must use java 11
val flinkVersion = "1.15.1-segment.1"
lazy val timestampTesting = (project in file("."))
  .settings(
    name := "timestampTesting",
    libraryDependencies ++= Seq(

      "org.apache.flink" %% "flink-scala" % flinkVersion,
      "org.apache.flink" % "flink-statebackend-rocksdb" % flinkVersion,
      "org.slf4j" % "slf4j-log4j12" % "1.7.36",
      "org.slf4j" % "slf4j-simple" % "1.7.36"
    ),
    Compile / run / mainClass := Some("playground.TimestampTesting"),

    // make run command include the provided dependencies
    Compile / run := Defaults
      .runTask(Compile / fullClasspath, Compile / run / mainClass, Compile / run / runner)
      .evaluated
  )
