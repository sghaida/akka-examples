package com.sghaida.akka.actors.routers

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.routing._
import com.typesafe.config.ConfigFactory

object GroupRouter extends App {


  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case msg => log.info(msg.toString)
    }
  }


  val system = ActorSystem("system", ConfigFactory.load().getConfig("router-example"))

  /* programmatically */
  val slaves = (1 to 5).map{ i => system.actorOf(Props[Slave], s"slave-$i")}.toList
  val slavePaths = slaves.map(_.path.toString)
  val groupRouter = system.actorOf(RoundRobinGroup(slavePaths).props())

  for (i <- 1 to 10) groupRouter ! s"message $i"
  Thread.sleep(1000)
  println("")

  /* from config */
  val configGroupRouter = system.actorOf(FromConfig.props(), "group-master")
  for (i <- 1 to 10) configGroupRouter ! s"message $i"

  Thread.sleep(1000)
  println("")

  configGroupRouter ! Broadcast("hello their")

}
