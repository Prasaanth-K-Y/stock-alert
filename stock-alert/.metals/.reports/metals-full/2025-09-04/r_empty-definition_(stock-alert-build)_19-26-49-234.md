error id: file:///C:/Users/Pky/Desktop/scala/scala-files/stock-alert/build.sbt:com/typesafe/sbt/packager/docker/DockerPlugin.
file:///C:/Users/Pky/Desktop/scala/scala-files/stock-alert/build.sbt
empty definition using pc, found symbol in pc: com/typesafe/sbt/packager/docker/DockerPlugin.
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -com/typesafe/sbt/packager/docker/DockerPlugin.autoImport.DockerPlugin.
	 -com/typesafe/sbt/packager/docker/DockerPlugin.autoImport.DockerPlugin#
	 -com/typesafe/sbt/packager/docker/DockerPlugin.autoImport.DockerPlugin().
	 -DockerPlugin.
	 -DockerPlugin#
	 -DockerPlugin().
	 -scala/Predef.DockerPlugin.
	 -scala/Predef.DockerPlugin#
	 -scala/Predef.DockerPlugin().
offset: 25
uri: file:///C:/Users/Pky/Desktop/scala/scala-files/stock-alert/build.sbt
text:
```scala
enablePlugins(PlayScala, @@DockerPlugin) // removed JavaAppPackaging

import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._

name := "stock-alert-app"
version := "1.0-SNAPSHOT"
scalaVersion := "2.13.16"

Compile / mainClass := Some("stock.StockServer")

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test,
  "com.typesafe.play" %% "play-slick" % "5.4.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.4.0",
  "mysql" % "mysql-connector-java" % "8.0.33",
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % "0.11.14",
  "grpc-app" %% "grpc-app" % "1.0-SNAPSHOT",
  "io.grpc" % "grpc-netty-shaded" % "1.64.0",
  "io.grpc" % "grpc-services" % "1.64.0",
  "io.javalin" % "javalin" % "5.6.1"

)


```


#### Short summary: 

empty definition using pc, found symbol in pc: com/typesafe/sbt/packager/docker/DockerPlugin.