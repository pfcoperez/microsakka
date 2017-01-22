package org.pfcoperez.microsakka.services.keystore

import java.util.UUID

import akka.actor.Actor

object KeyStore {

  type V = String
  type K = String

  // Message contract

  object Queries {

    trait Query {
      val id: UUID = UUID.randomUUID()
    }

    case class Get(k: K) extends Query
    case object GetAll extends Query
    case class Put(k: K, v: V) extends Query
    case class Delete(k: K) extends Query
    case object Clear extends Query
  }

  object Responses {

    trait Response {
      val id: UUID
    }

    case class Done(id: UUID) extends Response
    case object InvalidQuery

    trait ResultContent
    case class NotFound(k: K) extends ResultContent
    case class Entry(k: K, v: V) extends ResultContent
    case class Matches(entries: Seq[Entry]) extends ResultContent
    case class Result(id: UUID, content: ResultContent) extends Response
  }

}

trait KeyStore {
  self: Actor => // This interface is an actor interface

  import KeyStore._
  import Queries.Query
  import Responses.{InvalidQuery, Response}

  type State

  def initialState: State
  def stateTransition(st: State, query: Query): State

  def action(st: State, query: Query): Option[Response]

  final override def receive: Receive = receive(initialState)

  private def receive(st: State): Receive = {
    case q: Query =>
      val response = action(st, q) map { actionRes =>
        context.become(receive(stateTransition(st, q)))
        actionRes
      } getOrElse InvalidQuery
      sender() ! response
  }

}
