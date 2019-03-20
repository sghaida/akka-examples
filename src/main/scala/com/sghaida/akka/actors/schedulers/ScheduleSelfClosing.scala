package com.sghaida.akka.actors.schedulers

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props}

import scala.concurrent.duration._

object ScheduleSelfClosing extends App{

  class SelfClosingActor extends Actor with ActorLogging {


    def selfTerminate(schedule: Cancellable): Receive = {
      case "timeout" =>
        log.info("stopping")
        context.stop(self)
      case message =>
        log.info(s"recieved $message: staying alive")
        schedule.cancel()
        context.become(selfTerminate(createTimeoutWindow))
    }

    override def receive: Receive = selfTerminate(createTimeoutWindow)

    def createTimeoutWindow: Cancellable = {
      context.system.scheduler.scheduleOnce(1 second){
        self ! "timeout"
      }(system.dispatcher)
    }
  }

  val system = ActorSystem("system")

  val actor = system.actorOf(Props[SelfClosingActor], "self-closing-actor")

  system.scheduler.scheduleOnce(500 millis){
    actor ! "ping"
  }(system.dispatcher)

  system.scheduler.scheduleOnce(1 second){
    actor ! "ping ping"
  }(system.dispatcher)

  system.scheduler.scheduleOnce(2 seconds){
    actor ! "ping ping ping"
  }(system.dispatcher)

  system.scheduler.scheduleOnce(3.5 seconds){
    actor ! "ping ping ping ping"
  }(system.dispatcher)

}
