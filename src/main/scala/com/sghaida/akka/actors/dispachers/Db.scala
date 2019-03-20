package com.sghaida.akka.actors.dispachers

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

import scala.concurrent.Future

object Db extends App {

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case msg => log.info(msg.toString)
    }
  }

  class DbActor extends Actor with ActorLogging {

    override def receive: Receive = {
      case msg => Future {
        Thread.sleep(5000) //Big no because it leads to dispacher starvation
        log.info(s"Success: $msg")
      }(context.dispatcher)
    }
  }

  val system = ActorSystem("system")
  val blockingActor = system.actorOf(Props[DbActor], "blocking")
  val nonBlockingActor = system.actorOf(Props[SimpleActor], "non-blocking")

  blockingActor ! "select * from someTable"

  for (_ <- 1 to 1000) {
    blockingActor ! "blocking"
    nonBlockingActor ! "non-blocking"
  }

  /* for actors that deals with futures use dedicated dispatcher
  context.dispatchers.lookup("some-configured-dispatcher")
  * */


}
