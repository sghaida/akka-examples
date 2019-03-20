package com.sghaida.akka.actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object AkkaConfig extends App {

  val configStr =
    """
      | akka {
      |   loglevel = WARNING
      | }
    """.stripMargin

  val config = ConfigFactory.parseString(configStr)

  val system = ActorSystem("actor-config", ConfigFactory.load(config))
  val defaultSystem = ActorSystem("actor-default-config")
  val configSystem = ActorSystem("actor-some-config", ConfigFactory.load().getConfig("config1"))

  class ActorWithLogging extends Actor with ActorLogging{

    override def receive: Receive = {
      case message: String =>
        log.info(message.toString)
    }
  }

  val actor = system.actorOf(Props[ActorWithLogging], "loggingActor")
  val actor1 = defaultSystem.actorOf(Props[ActorWithLogging], "loggingActor1")
  val actor2 = configSystem.actorOf(Props[ActorWithLogging], "loggingActor2")

  actor ! "this is a test"
  actor1 ! "this is another test"
  actor2 ! "this is for different config"

  val conf = ConfigFactory.load("secrets/secrets.conf")
  println(conf.getString("akka.loglevel"))


}
