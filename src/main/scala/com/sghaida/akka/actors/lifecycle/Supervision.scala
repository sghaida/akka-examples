package com.sghaida.akka.actors.lifecycle

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorLogging, AllForOneStrategy, OneForOneStrategy, Props, SupervisorStrategy}

import scala.util.Random

object Supervision {

  case object Print


  class Supervisor extends Actor {

    val rand = new Random()
    /* apply only for the actors that caused the failure*/
    override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy(){
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: RuntimeException => Resume
      case _: Exception => Escalate
    }


    override def receive: Receive = {
      case props: Props =>
        val childRef = context.actorOf(props, s"child-${rand.nextInt(5)}")
        sender() ! childRef
    }
  }

  class AllforOneSupervisor extends Supervisor{
    /* apply to all children irrelevant to who cause the failure*/
    override val supervisorStrategy: SupervisorStrategy = AllForOneStrategy(){
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: RuntimeException => Resume
      case _: Exception => Escalate
    }

  }

  class ShittyWordCounter extends Actor with ActorLogging {
    override def receive: Receive = countWords(0)

    def countWords(count:Int): Receive = {
      case "" => throw new NullPointerException("string is empty")
      case text: String => text match {
        case t if t.length > 20 => throw new RuntimeException("its too fucking big")
        case t if Character.isUpperCase(t(0)) => throw new IllegalArgumentException("fuck off: no CAPS")
        case t =>
          val newCount = count + t.split(" ").length
          context.become(countWords(newCount))
      }
      case Print =>
        sender() ! count
      case _ => throw new Exception("only strings you idiot")
    }
  }

}
