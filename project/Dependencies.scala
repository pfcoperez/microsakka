import sbt._

object Versions {
  val akka = "2.4.16"
  val akkaHttp = "10.0.1"
}

object Dependencies {

  val akka = Seq(
    "com.typesafe.akka" %% "akka-http" % Versions.akkaHttp,
    "com.typesafe.akka" %% "akka-persistence" % Versions.akka,
    "org.iq80.leveldb"  % "leveldb" % "0.7",
    "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
  )

  val json4sJackson4akka = Seq(
    "org.json4s" %% "json4s-jackson" % "3.5.0",
    "de.heikoseeberger" %% "akka-http-json4s" % "1.11.0"
  )

}