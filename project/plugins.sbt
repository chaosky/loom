// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
//resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

//resolvers += Resolver.url("https://github.com/jrudolph/sbt-dependency-graph.git")

//resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"


// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % "2.1.2")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.4.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.3")
