package com.sghaida.akka.actors.mailbox

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.dispatch.ControlMessage
import com.typesafe.config.ConfigFactory

object Mailboxes extends App {

  val system = ActorSystem("system", ConfigFactory.load().getConfig("mailboxesDemo"))

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case msg => log.info(msg.toString)
    }
  }

 val ticket = system.actorOf(Props[SimpleActor].withDispatcher("custom-mailbox-dispatcher"), "ticket-actor")

  /*ticket ! "normal message 1"
  ticket ! "normal message 2"
  ticket ! "normal message 3"
  ticket ! "3)maybe important message"
  ticket ! "3)maybe important message"
  ticket ! "1)very important message"
*/


  /* control aware mailbox: process some messages first regardless what is in the queue before
  * UnboundedControlAwareMailbox
  * 1. create ControlMessage
  * 2. make the actor attached to the mailbox
  * */

  case object ImportantMessage extends ControlMessage

  val controlAware = system.actorOf(Props[SimpleActor].withMailbox("control-mailbox"))

//  controlAware ! "3)maybe important message"
//  controlAware ! "1)very important message"
//  controlAware ! "1)very important message"
//  controlAware ! "1)very important message"
//  controlAware ! ImportantMessage

  val alternativeControlAware = system.actorOf(Props[SimpleActor], "alternative-control-aware")

  alternativeControlAware ! "3)maybe important message"
  alternativeControlAware ! "1)very important message"
  alternativeControlAware ! "1)very important message"
  alternativeControlAware ! "1)very important message"
  alternativeControlAware ! ImportantMessage




}
