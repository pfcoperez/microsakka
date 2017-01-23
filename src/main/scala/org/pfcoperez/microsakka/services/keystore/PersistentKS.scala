package org.pfcoperez.microsakka.services.keystore

import java.util.UUID

import akka.persistence.PersistentActor
import org.pfcoperez.microsakka.services.keystore.KeyStore.{K, V}

object PersistentKS {

  val Commands = KeyStore.Queries

  private object Events {
    trait Event {
      val rqId: UUID
    }
    case class Saved(rqId: UUID, k: K, v: V) extends Event
    case class Deleted(rqId: UUID, k: K) extends Event
    case class DeletedBlock(rqId: UUID, keys: Set[K]) extends Event
  }

}

class PersistentKS extends PersistentActor {

  import PersistentKS.Events._
  import PersistentKS.Commands._
  import KeyStore.Responses._

  type State = Map[K, V]

  private var st: State = Map.empty


  def updateState(event: Event): Unit = {
    st = event match {
      case Saved(_, k, v) => st + (k -> v)
      case Deleted(_, k) => st - k
      case DeletedBlock(_, keys) => st -- keys
    }
  }

  override def receiveCommand: Receive = {
    // These commands do not change the state so they don't produce events
    case q @ Get(k) =>
      sender() ! Result(q.id, st.get(k).map(v => Entry(k, v)).getOrElse(NotFound(k)))
    case q: GetAll =>
      val entries: Seq[Entry] = st.toSeq map { case (k: K, v: V) => Entry(k, v) }
      sender() ! Result(q.id, Matches(entries))
    // The following commands do alter the KS state, therefore they are managed by events
    case q @ Put(k: K, v: V) =>
      val rqId = q.id
      val requester = sender()
      persist(Saved(rqId, k, v)) { event =>
        requester ! Done(rqId)
        updateState(event)
      }
    case q @ Delete(k: K) =>
      val rqId = q.id
      val requester = sender()
      persist(Deleted(rqId, k)) { event =>
        requester ! Done(rqId)
        updateState(event)
      }
    case q: Clear =>
      val rqId = q.id
      val requester = sender()
      persist(DeletedBlock(rqId, st.keySet)) { event =>
        requester ! Done(rqId)
        updateState(event)
      }
    case _ =>
      sender() ! InvalidQuery
  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
    // NO SNAPSHOT IN FIRST VERSION
    //case SnapshotOffer(_, snapshotState: State) =>
    //  st = snapshotState
  }

  override def persistenceId: String = "persistent-keystore-id"
}
