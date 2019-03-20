package com.sghaida.akka.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object BetterVotingActor extends App {

  val system = ActorSystem("voting-actor")

  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])

  class Citizen extends Actor {
    //var candidate: Option[String] = None

    override def receive: Receive = {
      case Vote(name) => context.become(voted(Some(name))) //candidate = Some(name)
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }

    def voted(candidate: Option[String]): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(candidate)
    }
  }

  case class AggregateVotes(citizens: Set[ActorRef])

  class VoteAggregator extends Actor{

    //var pendingVoters: Set[ActorRef] = Set()
    //var currentStatus: Map[String, Int] = Map()

    override def receive: Receive = awaitingCommand

    def awaitingCommand: Receive = {
      case AggregateVotes(citizens) =>
        citizens.foreach(_ ! VoteStatusRequest)
        context.become(awaitingStatuses(citizens, Map()))
    }

    def awaitingStatuses(waiting: Set[ActorRef], currentStats: Map[String, Int]): Receive = {
      case VoteStatusReply(None) => // sender() ! VoteStatusRequest

      case VoteStatusReply(Some(candidate)) =>
        val pending = waiting -  sender()
        val currentVotes = currentStats.getOrElse(candidate, 0)
        val newStatus = currentStats + (candidate -> (currentVotes + 1))

        if (pending.isEmpty) println(newStatus)
        else context.become(awaitingStatuses(pending,newStatus))
    }
  }

  val a = system.actorOf(Props[Citizen])
  val b = system.actorOf(Props[Citizen])
  val c = system.actorOf(Props[Citizen])
  val d = system.actorOf(Props[Citizen])
  val e = system.actorOf(Props[Citizen])


  a ! Vote("martin")
  b ! Vote("Jonas")
  c ! Vote("Roland")
  d ! Vote("martin")
  e ! Vote("Roland")

  val aggregator = system.actorOf(Props[VoteAggregator])

  aggregator ! AggregateVotes(Set(a, b, c, d, e))
  /* print the status of the votes
  martin 1
  jonas 1
  roland 2
  * */

}
