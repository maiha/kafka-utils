import sbt.Keys._

scalaVersion := "2.11.7"

scalacOptions := Seq(
  "-encoding", "UTF-8", "-target:jvm-1.7", "-deprecation",
  "-feature", "-unchecked", "-language:implicitConversions", "-language:postfixOps")

xerial.sbt.Sonatype.sonatypeRootSettings

// Maven Publishing
// http://www.scala-sbt.org/0.13/docs/Using-Sonatype.html

publishMavenStyle := true
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

version := "0.1"
organization := "sc.ala"
name := "kafka-utils"
description := "kafka utils"
homepage := Some(url("https://github.com/maiha/kafka-utils"))
licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))

pomExtra := (
     <developers>
        <developer>
          <id>maiha</id>
          <name>Kazunori Nishi</name>
          <url>https://github.com/maiha</url>
        </developer>
      </developers>
      <scm>
        <url>https://github.com/maiha/kafka-utils</url>
        <connection>scm:git:git@github.com:maiha/kafka-utils.git</connection>
      </scm>
)

val kafkaVersion = "0.8.2.1"

libraryDependencies ++= Seq(
  "org.apache.kafka" %% "kafka" % kafkaVersion,
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  "sc.ala" %% "rubyist" % "0.2.6",
  "pl.project13.scala" %% "rainbow" % "0.2" exclude("org.scalatest", "scalatest_2.11"),
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

fork in run := true
fork in Test := true
connectInput in run := true
javaOptions in run ++= sys.process.javaVmArguments.filter(
  a => Seq("-Dlogger.file", "-Dlogback.configurationFile").exists(a.startsWith)
)
