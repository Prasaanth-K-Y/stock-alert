

name := "notification-service"
version := "1.0-SNAPSHOT"
scalaVersion := "2.13.16"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, JavaAppPackaging, DockerPlugin)

libraryDependencies ++= Seq(
  guice,
  "io.grpc" % "grpc-services" % "1.64.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test,
  "com.typesafe.play" %% "play-slick" % "5.4.0",
  "grpc-app" %% "grpc-app" % "1.0-SNAPSHOT",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.4.0",
  "mysql" % "mysql-connector-java" % "8.0.33",
  "io.grpc" % "grpc-netty-shaded" % "1.64.0",
  "org.apache.pekko" %% "pekko-grpc-runtime" % "1.0.2"
)
Compile / unmanagedSourceDirectories += baseDirectory.value / "src" / "main" / "scala"


Compile / mainClass := Some("shared.notification.NotificationServer")

dockerBaseImage := "openjdk:21-jdk-slim"

dockerExposedPorts := Seq(9001, 50052) 
dockerRepository := Some("Grpc-noti")

