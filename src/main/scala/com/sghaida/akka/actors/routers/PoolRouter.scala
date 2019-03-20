package com.sghaida.akka.actors.routers

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.routing._
import com.typesafe.config.ConfigFactory

object PoolRouter extends App {


  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case msg => log.info(msg.toString)
    }
  }


  val system = ActorSystem("system", ConfigFactory.load().getConfig("router-example"))

  /* define router programmaticall y*/
  val router = system.actorOf(RoundRobinPool(5).props(Props[Slave]), "pool-router")

  for (i <- 1 to 10) router ! s"message $i"
  Thread.sleep(1000)
  println("")

  /* define router through config*/

  val poolRouter = system.actorOf(FromConfig.props(Props[Slave]), "pool-master")
  for (i <- 1 to 10) poolRouter ! s"message $i"
}
