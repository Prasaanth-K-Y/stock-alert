

name := "notification-service"
version := "1.0-SNAPSHOT"
scalaVersion := "2.13.16"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, JavaAppPackaging, DockerPlugin)// Docker plugin

libraryDependencies ++= Seq(
  guice,
  "io.grpc" % "grpc-services" % "1.64.0",// groc service builder
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test, // Test library
  "com.typesafe.play" %% "play-slick" % "5.4.0",// DB queries
  "grpc-app" %% "grpc-app" % "1.0-SNAPSHOT",// GRPC Stub file location
  "com.typesafe.play" %% "play-slick-evolutions" % "5.4.0",// DB queries
  "mysql" % "mysql-connector-java" % "8.0.33",//MySQL driver
  "io.grpc" % "grpc-netty-shaded" % "1.64.0",// netty cannot be used in docker , so netty-shaded is used 
  "org.apache.pekko" %% "pekko-grpc-runtime" % "1.0.2"// pekko grpc file
)
Compile / unmanagedSourceDirectories += baseDirectory.value / "src" / "main" / "scala" // Inorder to run via src as entry point instead of app


Compile / mainClass := Some("shared.notification.NotificationServer") // Docker entry file 

dockerBaseImage := "openjdk:21-jdk-slim"

dockerExposedPorts := Seq(9001, 50052) 
dockerRepository := Some("Grpc-noti")

