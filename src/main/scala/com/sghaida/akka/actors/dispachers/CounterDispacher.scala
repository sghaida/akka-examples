package com.sghaida.akka.actors.dispachers

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.util.Random

object CounterDispacher extends App {

  class Counter extends Actor with ActorLogging {

    override def receive: Receive = incrementCount()

    def incrementCount(count: Int=0): Receive = {
      case msg =>
        log.info(s"[$count] ${msg.toString}")
        context.become(incrementCount(count+1))
    }
  }


  val system = ActorSystem("system")//, ConfigFactory.load().getConfig("dispatcher-example"))

  val actors = for (i <- 1 to 10) yield {
    system.actorOf(Props[Counter].withDispatcher("custom-dispatcher"), s"counter-$i")
  }

  val r = new Random()

//  for (i<- 1 to 1000) {
//    actors(r.nextInt(10)) ! s"$i"
//  }


  val system1 = ActorSystem("system1", ConfigFactory.load().getConfig("dispatcher-example"))
  val counter = system1.actorOf(Props[Counter], "counter-example")

  counter ! "hello"



}
