import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val buildOrganization = "io.github.chaosky"
  val appName = "loom"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    // "org.seleniumhq.selenium" % "selenium-firefox-driver" % "2.33.0" % "test",
    "com.googlecode.xmemcached" % "xmemcached" % "1.4.1",
    "com.alibaba" % "fastjson" % "1.1.32",
    jdbc,
    anorm
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    //.settings(defaultScalaSettings: _*)
    net.virtualvoid.sbt.graph.Plugin.graphSettings ++
      Seq(
        // Add your own project settings here
        resolvers ++= Seq("snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
          "releases" at "http://oss.sonatype.org/content/repositories/releases"),
        organization := buildOrganization,
        scalacOptions ++= Seq("-feature")

      ): _*

  )

}
