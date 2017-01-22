package org.pfcoperez.microsakka.services.keystore.implementation

import akka.actor.Actor
import org.pfcoperez.microsakka.services.keystore.KeyStore

class SimpleInMemoryKS extends Actor with KeyStore {

  import KeyStore.{K, V}
  import KeyStore.Queries._
  import KeyStore.Responses._

  override type State = Map[K, V]

  override def initialState: State = Map("testkey_a" -> "1", "testkey_b" -> "2")

  override def action(content: State, query: Query): Option[Response] = {
    val rqId = query.id

    val response: PartialFunction[Query, Response] = {
      case GetAll =>
        val entries: Seq[Entry] = content.toSeq map { case (k: K, v: V) => Entry(k, v) }
        Result(rqId, Matches(entries))
      case Get(k) =>
        Result(rqId, content.get(k).map(v => Entry(k, v)).getOrElse(NotFound(k)))
      case Put(k: K, v: V) =>
        Done(rqId)
      case Delete(k: K) if content contains k =>
        Done(rqId)
      case Clear => Done(rqId)
    }

    response.lift(query)

  }

  override def stateTransition(st: State, query: Query): State = query match {
    case Put(k, v) => st + (k -> v)
    case Delete(k) => st - k
    case Clear => Map.empty
    case _ => st
  }

}
