import sbt.Keys._
import sbt._
import com.typesafe.sbt.SbtNativePackager.Linux
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.debian.DebianPlugin
import play.sbt.Play.autoImport._
import play.sbt.PlayScala
import play.twirl.sbt.Import.TwirlKeys

object Build extends Build {

  import BuildSettings._

  override lazy val settings = super.settings :+ {
    shellPrompt := (s => Project.extract(s).currentProject.id + " > ")
  }

  lazy val parent = Project("share2016-scala-parent", file("."))
    .aggregate(
      startup, akkaExample,
      webPlay, webPlayAdmin, webPlaySdk, webPlayApi, webPlayVendor, webPlayCommon,
      webPlayBusiness, shareData, shareUtil
    )

  lazy val startup = Project("startup", file("startup"))
    .settings(common: _*)
    .settings(
      description := "startup",
      libraryDependencies ++= Seq(
        _akkaHttp,
        _logbackClassic
      )
    )

  lazy val akkaExample = Project("akka-example", file("akka-example"))
    .settings(common: _*)
    .settings(
      description := "akka-example",
      libraryDependencies ++= Seq(
        _guice,
        _akkaHttp,
        _logbackClassic
      )
    )

  lazy val webPlay = Project("web-play", file("web-play"))
    .enablePlugins(PlayScala, DebianPlugin)
    .dependsOn(webPlayAdmin, webPlaySdk, webPlayApi, webPlayVendor)
    .settings(common: _*)
    .settings(
      packageDescription := "Chongqing-share2016-scala-play",
      packageName := "share2016-scala-play",
      name in Linux := "share2016-scala-play",
      packageSummary in Linux := "Chongqing share2016 scala play",
      packageDescription := "Chongqing share2016 scala play",
      daemonUser in Linux := "devops",
      maintainer in Linux := "羊八井 <yangbajing@gmail.com>",
      linuxPackageMappings := {
        val mappings = linuxPackageMappings.value
        mappings.map { linuxPackage =>
          val filtered = linuxPackage.mappings map {
            case (file, name) => file -> name // altering stuff here
          } filter {
            case (file, name) => true // remove stuff from mappings
          }
          val fileData = linuxPackage.fileData.copy(
            user = "devops",
            group = "devops",
            config = "false",
            docs = false
          )
          linuxPackage.copy(mappings = filtered, fileData = fileData)
        }.filter { linuxPackage =>
          linuxPackage.mappings.nonEmpty
        }
      },
      bashScriptConfigLocation := Some("${app_home}/../conf/jvmopts"),
      sources in(Compile, doc) := Seq.empty,
      publishArtifact in(Compile, packageDoc) := false,
      publishArtifact in packageDoc := false,
      PlayKeys.playDefaultPort := 59000,
      TwirlKeys.templateImports ++= Seq("share.data.model._", "share.data.domain._"),
      libraryDependencies ++= Seq(
        _patchca,
        _logbackClassic,
        _scalatestplusPlay
      )
    )

  lazy val webPlayApi = Project("web-play-api", file("web-play-api"))
    .enablePlugins(PlayScala)
    .dependsOn(webPlayCommon)
    .settings(common: _*)

  lazy val webPlayAdmin = Project("web-play-admin", file("web-play-admin"))
    .enablePlugins(PlayScala)
    .dependsOn(webPlayCommon)
    .settings(common: _*)

  lazy val webPlaySdk = Project("web-play-sdk", file("web-play-sdk"))
    .enablePlugins(PlayScala)
    .dependsOn(webPlayCommon)
    .settings(common: _*)

  lazy val webPlayVendor = Project("web-play-vendor", file("web-play-vendor"))
    .enablePlugins(PlayScala)
    .dependsOn(webPlayCommon)
    .settings(common: _*)

  lazy val webPlayCommon = Project("web-play-common", file("web-play-common"))
    .enablePlugins(PlayScala)
    .dependsOn(webPlayBusiness)
    .settings(common: _*)

  lazy val webPlayBusiness = Project("web-play-business", file("web-play-business"))
    .dependsOn(shareData)
    .settings(common: _*)
    .settings(
      libraryDependencies ++= Seq(
        _redisclient,
        _playWS
      )
    )

  lazy val shareData = Project("share-data", file("share-data"))
    .dependsOn(shareUtil)
    .settings(common: _*)
    .settings(
      libraryDependencies ++= Seq(
        _play2Reactivemongo,
        _reactivemongo,
        _hikariCP,
        _slickHikariCP,
        _slickPg,
        _slickPgDate2,
        _slickPgPlayJson,
        _postgresql,
        _playJson,
        _play
      )
    )

  lazy val shareUtil = Project("share-util", file("share-util"))
    .settings(common: _*)
    .settings(
      libraryDependencies ++= Seq(
        _play,
        _poi,
        _poiOoxml,
        _zxingJavase
      )
    )

}
