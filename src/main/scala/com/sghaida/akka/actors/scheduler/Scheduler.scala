package com.sghaida.akka.actors.scheduler

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._


object Scheduler extends App {

  val system = ActorSystem("system")
  val configActor = system.actorOf(ConfigActor.props, "config-sync-actor")
  val configRecipientActor = system.actorOf(ConfigurationRecieverActor.props, "config-receiver-actor")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher


  import ConfigActor._

  val cancellable = system.scheduler.schedule(0 milliseconds, 10 seconds, configActor, UpdateEtaConfig)

}
