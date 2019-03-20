package com.sghaida.akka.actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

object LoggingActor extends App {

  val system = ActorSystem("logging")

  class ExplicitLogging extends Actor{
    val logger = Logging(context.system, this)
    override def receive: Receive = {
      case message => logger.info(message.toString)
    }
  }

  val actor1 = system.actorOf(Props[ExplicitLogging], "loggingActor1")
  actor1 ! "this is a test"


  class ActorWithLogging extends Actor with ActorLogging{

    override def receive: Receive = {
      case (a, b) =>
        log.info("{} -> {}", a, b)
      case message: String =>
        log.info(message.toString)
    }
  }

  val actor2 = system.actorOf(Props[ActorWithLogging], "loggingActor2")
  actor2 ! "this is a test"
  actor2 ! ("saddam", 38)


}
