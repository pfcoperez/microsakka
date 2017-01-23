package org.pfcoperez.microsakka

import scala.concurrent.duration._
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{DefaultFormats, jackson}
import org.pfcoperez.microsakka.services.keystore.KeyStore.Queries._
import org.pfcoperez.microsakka.services.keystore.KeyStore.Responses._
import org.pfcoperez.microsakka.services.keystore.PersistentKS
import org.pfcoperez.microsakka.services.keystore.implementation.SimpleInMemoryKS


object HttpServer extends App with Json4sSupport {

  implicit val system = ActorSystem("MicroService")
  implicit val materializer = ActorMaterializer()

  object ServicesProps {
    val simpleKS = Props(new SimpleInMemoryKS)
    val persistentKS = Props(new PersistentKS)
  }

  val ksActor = system.actorOf(ServicesProps.persistentKS)
  implicit val rqTimeout = Timeout(1 second)

  implicit val serialization = jackson.Serialization // or native.Serialization
  implicit val formats       = DefaultFormats

  val route =
    path("test") {
      get {
        complete("Hello world")
      }
    } ~ path("kv") {
      get {
        onSuccess(ksActor ? GetAll()) {
          case Result(_, Matches(entries)) =>
            complete(entries) //TODO
        }
      }
    } ~ path("kv" / Segment) { key =>
      get {
        onSuccess(ksActor ? Get(key)) {
          case Result(_, content) =>
            complete(content)
        }
      } ~ (post | put) {
        entity(as[String]) { value =>
          onSuccess(ksActor ? Put(key, value)) {
            case Done(_) => complete(StatusCodes.OK)
          }
        }
      } ~ delete {
        onSuccess(ksActor ? Delete(key)) {
          case Done(_) => complete(StatusCodes.OK)
        }
      }
    }


  Http().bindAndHandle(route, "0.0.0.0", 8080)

}
