import sbt._
import Keys._
import play.Project._

object BuildSettings {
  import Dependencies._
  import Resolvers._

  val globalSettings = Seq(
    scalaVersion := "2.10.2",
    scalacOptions += "-deprecation",
    scalacOptions += "-feature",
    libraryDependencies ++= Seq(spring, springJdbc, dbcp, flyway, jodaTime, anorm, specs2Spring,
                                mockito, atomikos, playFramework, kiama, scalaz, postgresqlJdbc),
    resolvers := Seq(springMilestoneRepo, typesafeRepo, localRepo, sonatypeSnapshotsRepo)
  )

  val projectSettings = Defaults.defaultSettings ++ globalSettings
}

object Resolvers {
  val typesafeRepo = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  val springMilestoneRepo = "repo.springsource.org-milestone" at "https://repo.springsource.org/libs-milestone"
  val sonatypeSnapshotsRepo = "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
  val localRepo = Resolver.file("localRepo", file("localRepo"))(Resolver.ivyStylePatterns)
}

object Dependencies {
  val dbcp = "commons-dbcp" % "commons-dbcp" % "1.4"
  val spring = "org.springframework.scala" % "spring-scala" % "1.0.0.M2"
  val postgresqlJdbc = "org.postgresql" % "postgresql" % "9.2-1003-jdbc4"
  val springJdbc = "org.springframework" % "spring-jdbc" % "3.2.2.RELEASE"
  val flyway = "com.googlecode.flyway" % "flyway-core" % "2.1.1"
  val jodaTime = "org.scalaj" %% "scalaj-time" % "0.6"
  val anorm = "play" %% "anorm" % "2.1.1"
  val kiama = "com.googlecode.kiama" %% "kiama" % "1.5.0"
  val scalaz = "org.scalaz" %% "scalaz-core" % "7.0.0"
  val specs2Spring = "org.specs2" %% "spring" % "1.0.1-SNAPSHOT" % "it,test"
//  val specs2 = "org.specs2" %% "specs2" % "1.14" % "it,test"
  val mockito = "org.mockito" % "mockito-all" % "1.9.5" % "it,test"
  val atomikos = "com.atomikos" % "transactions-jdbc" % "3.8.0" % "it,test"
  val playFramework = "play" %% "play" % "2.1.1"
}

object ApplicationBuild extends Build {
  import Dependencies._
  import BuildSettings._

  override def settings = super.settings ++ globalSettings

  val appName         = "tellmemore"
  val appVersion      = "0.1-SNAPSHOT"

  lazy val main = Project("tellmemore", file("."))
    .configs(IntegrationTest)
    .settings(Defaults.itSettings : _*)
    .settings(parallelExecution in IntegrationTest := false)

  lazy val tellmemoreWeb = play.Project("tellmemore-web", appVersion, Seq(), path=file("tellmemore-web"))
    .configs(IntegrationTest).settings(Defaults.itSettings : _*)
    .settings(parallelExecution in IntegrationTest := false) dependsOn main
}
