import sbt.Keys._
import sbt._
import play.core.PlayVersion

object BuildSettings {

  def common = Seq(
    version := "1.0.0",
    scalaVersion := "2.11.8",
    scalacOptions ++= Seq(
      "-encoding", "utf8",
      "-unchecked",
      "-feature",
      "-deprecation"
    ),
    javacOptions ++= Seq(
      "-encoding", "utf8",
      "-Xlint:unchecked",
      "-Xlint:deprecation"
    ),
    fork := true,
    libraryDependencies ++= Seq(
      _guava,
      _bouncycastle,
      _commonsLang3,
      _commonsCodec,
      _scalaLogging,
      _akkaActor,
      _akkaSlf4j,
      _akkaStream,
      _scalaXml,
      _scalaParserCombinators,
      _scalatest
    ),
    libraryDependencies ~= {
      _ map {
        case m if m.organization == "com.typesafe.play" =>
          m.exclude("commons-logging", "commons-logging").
            exclude("com.typesafe.akka", "akka-actor").
            exclude("com.typesafe.akka", "akka-slf4j").
            //            exclude("org.scala-lang", "scala-library").
            //            exclude("org.scala-lang", "scala-compiler").
            //            exclude("org.scala-lang", "scala-reflect").
            exclude("org.scala-lang.modules", "scala-xml").
            exclude("com.google.guava", "guava").
            exclude("org.scala-lang.modules", "scala-parser-combinators").
            excludeAll()
        case m => m
      }
    }
  )

  val _scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.5"
  val _scalaParserCombinators = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"

  val _playJson = "com.typesafe.play" %% "play-json" % PlayVersion.current
  val _playWS = "com.typesafe.play" %% "play-ws" % PlayVersion.current
  val _play = "com.typesafe.play" %% "play" % PlayVersion.current % "provided"
  val _scalatestplusPlay = ("org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % "test").excludeAll(ExclusionRule("com.typesafe.play"))

  val verAkka = "2.4.4"
  val _akkaActor = "com.typesafe.akka" %% "akka-actor" % verAkka
  val _akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % verAkka
  val _akkaStream = "com.typesafe.akka" %% "akka-stream" % verAkka
  val _akkaHttp = "com.typesafe.akka" %% "akka-http-experimental" % verAkka

  val _scalatest = "org.scalatest" %% "scalatest" % "2.2.6" % "test"

  val _scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"

  val _redisclient = ("net.debasishg" %% "redisclient" % "3.1").excludeAll(ExclusionRule("com.typesafe.akka"))

  val verSlickPg = "0.14.0"
  val _slickPg = ("com.github.tminglei" %% "slick-pg" % verSlickPg).excludeAll(ExclusionRule("org.postgresql"))
  val _slickPgDate2 = ("com.github.tminglei" %% "slick-pg_date2" % verSlickPg).excludeAll(ExclusionRule("org.postgresql"))
  val _slickPgPlayJson = ("com.github.tminglei" %% "slick-pg_play-json" % verSlickPg).excludeAll(ExclusionRule("org.postgresql"), ExclusionRule("com.typesafe.play"))

  val verSlick = "3.1.1"
  val _slickHikariCP = ("com.typesafe.slick" %% "slick-hikaricp" % verSlick).exclude("com.zaxxer", "HikariCP-java6").exclude("org.scala-lang", "scala-library")
  val _slick = ("com.typesafe.slick" %% "slick" % verSlick).exclude("org.scala-lang", "scala-library")

  val _reactivemongo = ("org.reactivemongo" %% "reactivemongo" % "0.11.11").excludeAll(ExclusionRule("com.typesafe.akka"))
  val _play2Reactivemongo = ("org.reactivemongo" %% "play2-reactivemongo" % "0.11.11").excludeAll(ExclusionRule("com.typesafe.akka"))

  val _mongoScalaDriver = "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.1"

  val _hikariCP = "com.zaxxer" % "HikariCP" % "2.4.3"
  val _postgresql = "org.postgresql" % "postgresql" % "9.4.1208.jre7"

  val _config = "com.typesafe" % "config" % "1.3.0"
  val _commonsLang3 = "org.apache.commons" % "commons-lang3" % "3.4"
  val _commonsCodec = "commons-codec" % "commons-codec" % "1.10"
  val _bouncycastle = "org.bouncycastle" % "bcprov-jdk15on" % "1.54"
  val _commonsEmail = "org.apache.commons" % "commons-email" % "1.4"
  val _guava = "com.google.guava" % "guava" % "19.0"
  val _guice = ("com.google.inject" % "guice" % "4.0").exclude("com.google.guava", "guava")
  val _zxingJavase = "com.google.zxing" % "javase" % "3.2.1"
  val _patchca = "com.github.bingoohuang" % "patchca" % "0.0.1"
  val _logbackClassic = "ch.qos.logback" % "logback-classic" % "1.1.7"

  val verPoi = "3.14"
  val _poi = "org.apache.poi" % "poi" % verPoi
  val _poiOoxml = "org.apache.poi" % "poi-ooxml" % verPoi

}
