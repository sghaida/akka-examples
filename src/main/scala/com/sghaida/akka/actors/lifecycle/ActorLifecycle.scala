package com.sghaida.akka.actors.lifecycle

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

object ActorLifecycle extends App{
  val system = ActorSystem("system")

  case object StartChild
  case object FailChild
  case object CheckChild
  case object Fail
  case object Check

  class Parent extends Actor with ActorLogging{

    override def preStart(): Unit = log.info("Starting")
    override def postStop(): Unit = log.info("stopping")

    override def receive: Receive = childRelated(null)

    def childRelated(child: ActorRef): Receive = {
      case StartChild =>
        val child = context.actorOf(Props[Child], "child")
        context.become(childRelated(child))
      case FailChild =>
        if (child != null ) child ! Fail else throw new RuntimeException("no child available")
      case CheckChild =>
        log.info("checking child")
        child ! Check
    }
  }

  class Child extends Actor with ActorLogging {

    override def preStart(): Unit = log.info("Starting")
    override def postStop(): Unit = log.info("stopping")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit =
      log.info(s"restarting because of: ${reason.getMessage} ")

    override def postRestart(reason: Throwable): Unit = log.info("restarted")

    override def receive: Receive = {
      case Fail =>
        log.warning("child fail")
        throw new RuntimeException("failed")

      case Check =>
        log.info("im alive")
    }
  }

//  val parent = system.actorOf(Props[Parent], "parent")
//  parent ! StartChild
//  parent !PoisonPill
//  println("")
//  println("")

  val parent1 = system.actorOf(Props[Parent], "parent1")
  parent1 ! StartChild
  parent1 ! FailChild

  parent1 ! CheckChild

}
