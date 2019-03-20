package com.sghaida.akka.actors.schedulers

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object TimersSchedulers extends App {

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }


  }

  val system = ActorSystem("system")

  val simpleActor = system.actorOf(Props[SimpleActor], "simple-actor")

  system.log.info("scheduling reminder for simpleActor")

  implicit val executor: ExecutionContext = system.dispatcher

  system.scheduler.scheduleOnce(1 second){
    simpleActor ! "reminder"
  }

  val recurring: Cancellable = system.scheduler.schedule(1 second, 2 seconds) {
    simpleActor ! "recurring reminder"
  }

  system.scheduler.scheduleOnce(10 seconds){
    recurring.cancel()
  }

}
