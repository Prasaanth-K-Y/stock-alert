enablePlugins(PlayScala)

import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._

name := "stock-alert-app"
version := "1.0-SNAPSHOT"
scalaVersion := "2.13.16"

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test,
  "com.typesafe.play" %% "play-slick" % "5.4.0",
  "org.mockito" %% "mockito-scala-scalatest" % "1.17.27" % Test,// for mock or demo repo , this allow not ot populate the reaal repo,auto closes
  "com.typesafe.play" %% "play-slick-evolutions" % "5.4.0",
  "mysql" % "mysql-connector-java" % "8.0.33",// Myslq driver
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % "0.11.14",
  "grpc-app" %% "grpc-app" % "1.0-SNAPSHOT",// Stub file for grpc 
  "io.grpc" % "grpc-netty-shaded" % "1.64.0",// shaded is used instead of grpc netty for docker compatability
  "io.grpc" % "grpc-services" % "1.64.0"
)
