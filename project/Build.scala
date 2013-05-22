import sbt._
import Keys._
import play.Project._

object BuildSettings {
  import Dependencies._
  import Resolvers._

  val globalSettings = Seq(
    scalaVersion := "2.10.1",
    scalacOptions += "-deprecation",
    scalacOptions += "-feature",
    libraryDependencies ++= Seq(),
    resolvers := Seq(typesafeRepo, projectRepo)
  )

  val projectSettings = Defaults.defaultSettings ++ globalSettings
}

object Resolvers {
  val typesafeRepo = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  val projectRepo = Resolver.file("project-repo", file("localrepo"))(Resolver.ivyStylePatterns)
}

object Dependencies {
}

object ApplicationBuild extends Build {
  import Dependencies._
  import BuildSettings._

  override def settings = super.settings ++ globalSettings

  val appName         = "tellmemore"
  val appVersion      = "0.1-SNAPSHOT"

  lazy val commons = Project("commons", file("commons"),
                        settings = projectSettings ++
                                   Seq(libraryDependencies ++= Seq(anorm))
  )

  lazy val server = play.Project("server", appVersion, Seq(), path=file("server")).settings(
  ) dependsOn(commons)

  lazy val main = Project(appName, file("."), settings = projectSettings) aggregate(commons, server)
}
