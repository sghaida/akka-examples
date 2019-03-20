package com.sghaida.akka.actors.patterns

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}

object Stash extends App {

  case object Open
  case object Close
  case object Read
  case class Write(data: String)

  /*
  * start with close
  * close => open(become open)
  * open => read, write, close (become close)
  * if close read and write is postponed
  * */

  class ResourceActor extends Actor with ActorLogging with Stash{
    override def receive: Receive = closed
    var data = ""

    def opened: Receive = {
      case Close =>
        log.info("closing")
        unstashAll()
        context.become(closed)
      case Read =>
        log.info(s"reading data : $data")
      case Write(d) =>
        log.info(s"writing $d")
        data = d
      case message =>
        log.info(s"stashing ${message.toString}")
        stash()

    }

    def closed: Receive = {
      case Open =>
        log.info("opening")
        unstashAll() // prepend messages to the mailbox
        context.become(opened)
      case message =>
        log.info(s"stashing ${message.toString}")
        stash()

    }
  }

  val system = ActorSystem("system")

  val actor = system.actorOf(Props[ResourceActor],"resource-actor")

//  actor ! Write("this is a test")
//  actor ! Read
//  actor ! Write("yet another test")
//  actor ! Read
//  actor ! Open
//  actor ! Close

  actor ! Read
  actor ! Open
  actor ! Open
  actor ! Write("im writing")
  actor ! Close
  actor ! Read


}
