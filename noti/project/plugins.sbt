addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.8")


addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox" % "0.10.7")
addSbtPlugin("org.foundweekends.giter8" % "sbt-giter8-scaffold" % "0.17.0")

resolvers += Resolver.sbtPluginRepo("releases")


addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.10.0")