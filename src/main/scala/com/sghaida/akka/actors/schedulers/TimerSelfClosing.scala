package com.sghaida.akka.actors.schedulers

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props, Timers}

import scala.concurrent.duration._

object TimerSelfClosing extends App{

  case object TimerKey
  case object Start
  case object Reminder
  case object Stop
  class SelfClosingActor extends Actor with ActorLogging with Timers{

    timers.startSingleTimer(TimerKey, Start, 1 millis)

    override def receive: Receive = {
      case Start =>
        log.info("bootstrapping")
        timers.startPeriodicTimer(TimerKey, Stop, 1 second)

      case Stop =>
        log.warning("stopping")
        timers.cancel(TimerKey)
        context.stop(self)
      case message =>
        log.info(s"alive: ${message.toString}")
        timers.startPeriodicTimer(TimerKey, Stop, 1 second)
    }


  }

  val system = ActorSystem("system")

  val actor = system.actorOf(Props[SelfClosingActor], "timed-closing-actor")

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
