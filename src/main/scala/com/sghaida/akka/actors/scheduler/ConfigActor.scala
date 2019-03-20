package com.sghaida.akka.actors.scheduler

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.sghaida.akka.actors.scheduler.traites.JsonSupport
import com.sghaida.models.ConfigModel
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object ConfigActor {
  private val modelManagementUri =
    ConfigFactory.load().getConfig("development").getString("modelManagement.uri")

  val props: Props = Props[ConfigActor]
  case object UpdateEtaConfig

}

class ConfigActor extends Actor with ActorLogging with JsonSupport{

  import ConfigActor._
  import ConfigurationRecieverActor._
  import akka.http.scaladsl.unmarshalling.Unmarshal


  implicit private val executionContext: ExecutionContextExecutor = context.system.dispatcher
  implicit private val actorSystem: ActorSystem = context.system
  final implicit val materializer: ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(context.system))

  override def receive: Receive = getConfiguration

  def getConfiguration: Receive = {
    case UpdateEtaConfig =>
      log.info(modelManagementUri)
      val responseFuture = Http().singleRequest(HttpRequest(uri = modelManagementUri))

      val config: Future[List[ConfigModel]] = responseFuture flatMap {
        res => Unmarshal(res).to[List[ConfigModel]]
      } recoverWith {
        case ex =>
          log.error(ex.getMessage)
          throw ex
      }

      config onComplete{
        case Success(conf) =>
            context.actorSelection("/user/config-receiver-actor") ! UpdateConfig(conf)
        case Failure(ex) =>
          log.error(ex.getMessage)
          throw ex
      }

  }

}
