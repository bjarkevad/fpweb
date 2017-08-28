organization := "fpweb"
name := "fpweb"
version := "0.0.1-SNAPSHOT"
scalaVersion := "2.11.11"

scalacOptions ++= Seq(
)

val Http4sVersion = "0.15.11a"
val DoobieVersion = "0.4.4"

libraryDependencies += "com.lihaoyi" % "ammonite" % "1.0.2" % "test" cross CrossVersion.full

sourceGenerators in Test += Def.task {
  val file = (sourceManaged in Test).value / "amm.scala"
  IO.write(file, """object amm extends App { ammonite.Main().run() }""")
  Seq(file)
}.taskValue

libraryDependencies ++= Seq(
 "org.http4s"     %% "http4s-blaze-server" % Http4sVersion,
 "org.http4s"     %% "http4s-argonaut"     % Http4sVersion,
 "org.http4s"     %% "http4s-dsl"          % Http4sVersion,
 "ch.qos.logback" %  "logback-classic"     % "1.2.1",
 "org.tpolecat"   %% "doobie-core"         % DoobieVersion,
 "org.tpolecat"   %% "doobie-postgres"     % DoobieVersion,
 "org.tpolecat"   %% "doobie-hikari"       % DoobieVersion,
 "org.mindrot"    %  "jbcrypt"             % "0.4",
 "org.postgresql" %  "postgresql"          % "42.1.3",

 "org.scalatest"  %% "scalatest"           % "3.0.1" % "test",
 "org.tpolecat"   %% "doobie-scalatest"    % DoobieVersion % "test"

)
