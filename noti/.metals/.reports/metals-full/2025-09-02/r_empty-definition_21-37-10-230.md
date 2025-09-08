error id: file:///C:/Users/Pky/Desktop/noti/build.sbt:`<error>`#`<error>`.
file:///C:/Users/Pky/Desktop/noti/build.sbt
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -dockerRepository.
	 -dockerRepository#
	 -dockerRepository().
	 -scala/Predef.dockerRepository.
	 -scala/Predef.dockerRepository#
	 -scala/Predef.dockerRepository().
offset: 587
uri: file:///C:/Users/Pky/Desktop/noti/build.sbt
text:
```scala
name := "notification-service"
version := "1.0-SNAPSHOT"
scalaVersion := "2.13.16"

lazy val root = (project in file(".")).enablePlugins(PlayScala, JavaAppPackaging, DockerPlugin)

libraryDependencies ++= Seq(
  guice,
  "com.typesafe.play" %% "play-slick" % "5.4.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.4.0",
  "mysql" % "mysql-connector-java" % "8.0.33",
  "io.grpc" % "grpc-netty" % "1.64.0",
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % "0.11.14"
)

dockerBaseImage := "openjdk:21-jdk-slim"
dockerExposedPorts := Seq(9001, 50052)
dockerRepo@@sitory := Some("Grpc-noti") 
dependencyOverrides += "org.scala-lang.modules" %% "scala-xml" % "2.2.0"

Compile / unmanagedSourceDirectories += baseDirectory.value / "src" / "main" / "scala"

```


#### Short summary: 

empty definition using pc, found symbol in pc: 