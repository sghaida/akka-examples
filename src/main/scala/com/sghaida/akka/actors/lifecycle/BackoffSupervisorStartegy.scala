package com.sghaida.akka.actors.lifecycle

import java.io.File

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props}
import akka.pattern.{Backoff, BackoffSupervisor}

import scala.concurrent.duration._
import scala.io.Source

object BackoffSupervisorStartegy extends App {

  case object ReadFile
  class FileBasedPersistent extends Actor with ActorLogging {

    var dataSource: Source = null

    override def preStart(): Unit = log.info("starting")
    override def postStop(): Unit = log.warning("stopped")
    override def preRestart(reason: Throwable, message: Option[Any]): Unit = log.warning("restarting")

    override def receive: Receive = {
      case ReadFile =>
        if (dataSource == null)
          dataSource = Source.fromFile(new File("src/main/resources/testfiles/text1.txt"))
        log.info("read data: " + dataSource.getLines().toList)
    }
  }

  class eagerDataLoader extends FileBasedPersistent {
    override def preStart(): Unit = {
      log.info("eager starting")
      dataSource = Source.fromFile(new File("src/main/resources/testfiles/text1.txt"))
    }
  }

  val system = ActorSystem("system")

//  val actor = system.actorOf(Props[FileBasedPersistent],"actor")
//  actor ! ReadFile

  val simpleSupervisorProps = BackoffSupervisor.props(
    Backoff.onFailure(
      Props[FileBasedPersistent],
      "backoff-actor",
      3 seconds,
      30 seconds,
    0.3
    )
  )

  val stopSupervisorProps = BackoffSupervisor.props(
    Backoff.onStop(
      Props[eagerDataLoader],
      "stop-backoff-actor",
      1 seconds,
      30 seconds,
      0.3
    )/*.withSupervisorStrategy(OneForOneStrategy(){
      case _ => Restart
    })*/
  )



  //val simpleBackoffSupervisor = system.actorOf(simpleSupervisorProps, "backoff-supervisor")
  val stopBackoffSupervisor = system.actorOf(stopSupervisorProps, "stop-backoff-supervisor")

  /* create
      parent -> backoff-supervisor
        - child  -> backoff-actor
      parent forward all messages to child with supervisor strategy (restart on everything) with backoff
   */


  //simpleBackoffSupervisor ! ReadFile

  //stopBackoffSupervisor ! ReadFile
}
