package com.sghaida.akka.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.sghaida.akka.actors.CounterActor.system

object VotingActor extends App {

  val system = ActorSystem("voting-actor")

  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])

  class Citizen extends Actor {
    var candidate: Option[String] = None

    override def receive: Receive = {
      case Vote(name) => candidate = Some(name)
      case VoteStatusRequest => sender() ! VoteStatusReply(candidate)
    }
  }

  case class AggregateVotes(citizens: Set[ActorRef])

  class VoteAggregator extends Actor{

    var pendingVoters: Set[ActorRef] = Set()
    var currentStatus: Map[String, Int] = Map()

    override def receive: Receive = {

      case AggregateVotes(citizens) =>
        pendingVoters = citizens
        citizens.foreach(_ ! VoteStatusRequest)

      case VoteStatusReply(None) => // sender() ! VoteStatusRequest

      case VoteStatusReply(Some(candidate)) =>
        pendingVoters -=  sender()
        val currentVotes = currentStatus.getOrElse(candidate, 0)
        currentStatus +=(candidate -> (currentVotes + 1))

        if (pendingVoters.isEmpty) println(currentStatus)


    }
  }

  val a = system.actorOf(Props[Citizen])
  val b = system.actorOf(Props[Citizen])
  val c = system.actorOf(Props[Citizen])
  val d = system.actorOf(Props[Citizen])

  a ! Vote("martin")
  b ! Vote("Jonas")
  c ! Vote("Roland")
  d ! Vote("martin")

  val aggregator = system.actorOf(Props[VoteAggregator])

  aggregator ! AggregateVotes(Set(a, b, c, d))
  /* print the status of the votes
  martin 1
  jonas 1
  roland 2
  * */

}
