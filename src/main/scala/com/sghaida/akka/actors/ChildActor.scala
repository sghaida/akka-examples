package com.sghaida.akka.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.sghaida.akka.actors.ChildActor.Parent.{CreateChild, TellChild}

object ChildActor extends App {

  val system = ActorSystem("system")

  object Parent{
    case class CreateChild(name: String)
    case class TellChild(message: String)
  }


  class Parent extends Actor {
    import Parent._

    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path}: create a child")
        val childRef = context.actorOf(Props[Child], name)
        context.become(withChild(childRef))
    }

    def withChild(child: ActorRef): Receive = {
      case TellChild(message) =>
        if (child != null) child forward message

      case CreateChild(name) =>
        println(s"${self.path}: create a child")
        val childRef = context.actorOf(Props[Child], name)
        context.become(withChild(childRef))
    }
  }

  class Child extends Actor {
    override def receive: Receive = {
      case message =>
        println(s"${self.path}: i got $message from ${sender()}")
    }
  }


  val parent = system.actorOf(Props[Parent],"parent")


  parent ! CreateChild("child1")
  parent ! TellChild("hello child")

  parent ! CreateChild("child2")
  parent ! TellChild("hello child2")

  val actor = system.actorSelection("/user/parent/child1")
  actor ! "i got a direct message"
}
