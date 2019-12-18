scalaVersion := "2.12.10"

name := "Programming-Language-Theory"
version := "0.1"

val http4sVersion = "0.20.15"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % "test"
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion
)

assemblyJarName in assembly := "PLT-assembly.jar"
mainClass in assembly := Some("Main")