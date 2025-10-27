enablePlugins(PlayScala)

import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._

name := "stock-alert-app"
version := "1.0-SNAPSHOT"
scalaVersion := "2.13.16"
resolvers ++= Seq(
  Resolver.mavenCentral,
  "pauldijou-releases" at "https://repo1.maven.org/maven2/"
)
libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test,// Test for scala
  "com.typesafe.play" %% "play-slick" % "5.4.0",// DB queries
  "com.github.jwt-scala" %% "jwt-core" % "9.4.4",// Jwt action builder
  "com.github.jwt-scala" %% "jwt-play-json" % "9.4.4",// Play library
  "org.mockito" %% "mockito-scala-scalatest" % "1.17.27" % Test,// for mock or demo repo , this allow not ot populate the reaal repo,auto closes
  "com.typesafe.play" %% "play-slick-evolutions" % "5.4.0",
  "mysql" % "mysql-connector-java" % "8.0.33",// Myslq driver
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % "0.11.14",// For proto files - not needed for now
  "grpc-app" %% "grpc-app" % "1.0-SNAPSHOT",// Stub file for grpc 
  "io.grpc" % "grpc-netty-shaded" % "1.64.0",// shaded is used instead of grpc netty for docker compatability
  "io.grpc" % "grpc-services" % "1.64.0"// Grpc class builder

)

libraryDependencies ++= Seq(
  guice,
  "com.softwaremill.sttp.client3" %% "core" % "3.9.6",
  "com.softwaremill.sttp.client3" %% "play-json" % "3.9.6"
)
libraryDependencies += "com.softwaremill.sttp.client3" %% "async-http-client-backend-future" % "3.9.0"



libraryDependencies ++= Seq(
  "com.razorpay" % "razorpay-java" % "1.4.4",
  "org.json" % "json" % "20230227"
)