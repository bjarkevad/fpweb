organization := "fpweb"
name := "fpweb"
version := "0.0.1-SNAPSHOT"
scalaVersion := "2.11.11"

val Http4sVersion = "0.15.11a"

libraryDependencies ++= Seq(
 "org.http4s"     %% "http4s-blaze-server" % Http4sVersion,
 "org.http4s"     %% "http4s-argonaut"     % Http4sVersion,
 "org.http4s"     %% "http4s-dsl"          % Http4sVersion,
 "ch.qos.logback" %  "logback-classic"     % "1.2.1",
 "org.tpolecat" %% "doobie-core"      % "0.4.4",
 "org.tpolecat" %% "doobie-hikari"      % "0.4.4",
 "org.xerial" % "sqlite-jdbc" % "3.20.0",

 "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)
