package com.sghaida.akka.actors.scheduler

import akka.actor.{Actor, ActorLogging, Props}
import com.sghaida.models.ConfigModel

object ConfigurationRecieverActor {
  val props: Props = Props[ConfigurationRecieverActor]
  case class UpdateConfig(configs: List[ConfigModel])
  case object Print
}

class ConfigurationRecieverActor extends Actor with ActorLogging{

  import ConfigurationRecieverActor._

  override def receive: Receive = recieveConfiguration(List())

  def recieveConfiguration(etaConfigs: List[ConfigModel]): Receive = {
    case UpdateConfig(configs: List[ConfigModel]) =>
      log.info(etaConfigs.toString())
      context.become(recieveConfiguration(configs))
    case Print => log.info(etaConfigs.toString())
  }

}
