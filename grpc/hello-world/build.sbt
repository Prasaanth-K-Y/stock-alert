name := "grpc-app"
version := "1.0-SNAPSHOT"
scalaVersion := "2.13.16"

lazy val root = (project in file("."))
  .enablePlugins(ProtocPlugin)

resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"

libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % "0.11.14",
  "io.grpc" % "grpc-netty" % "1.62.2"
)

Compile / PB.protoSources += baseDirectory.value / "src" / "main" / "protobuf"


Compile / PB.targets := Seq(
  scalapb.gen(grpc = true) -> (Compile / sourceManaged).value
)
