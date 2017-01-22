import sbt._

object Dependencies {

  val akka = Seq(
    "com.typesafe.akka" %% "akka-http" % "10.0.1"
  )

  val json4sJackson4akka = Seq(
    "org.json4s" %% "json4s-jackson" % "3.5.0",
    "de.heikoseeberger" %% "akka-http-json4s" % "1.11.0"
  )

}