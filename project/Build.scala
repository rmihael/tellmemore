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
    libraryDependencies ++= Seq(spring, springJdbc, dbcp, h2, flyway, jodaTime, anorm),
    resolvers := Seq(springMilestoneRepo, typesafeRepo, localRepo)
  )

  val projectSettings = Defaults.defaultSettings ++ globalSettings
}

object Resolvers {
  val typesafeRepo = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  val springMilestoneRepo = "repo.springsource.org-milestone" at "https://repo.springsource.org/libs-milestone"
  val localRepo = Resolver.file("localRepo", file("localRepo"))(Resolver.ivyStylePatterns)
}

object Dependencies {
  val dbcp = "commons-dbcp" % "commons-dbcp" % "1.4"
  val spring = "org.springframework.scala" % "spring-scala" % "1.0.0.M2"
  val h2 = "com.h2database" % "h2" % "1.3.171"
  val springJdbc = "org.springframework" % "spring-jdbc" % "3.2.2.RELEASE"
  val flyway = "com.googlecode.flyway" % "flyway-core" % "2.1.1"
  val jodaTime = "org.scalaj" %% "scalaj-time" % "0.6"
  val anorm = "play" %% "anorm" % "2.1.1"
}

object ApplicationBuild extends Build {
  import Dependencies._
  import BuildSettings._

  override def settings = super.settings ++ globalSettings

  val appName         = "tellmemore"
  val appVersion      = "0.1-SNAPSHOT"

  lazy val main = Project("tellmemore", file("."))
}
