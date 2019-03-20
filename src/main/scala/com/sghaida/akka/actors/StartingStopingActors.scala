package com.sghaida.akka.actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Kill, PoisonPill, Props, Terminated}

object StartingStopingActors extends App {
  val system = ActorSystem("stoppingActor")

  object Parent {
    case class StartChild(name: String)
    case class StopChild(name: String)
    case object Stop
  }

  class Parent extends Actor with ActorLogging {
    import Parent._

    override def receive: Receive = withChildren(Map())

    def withChildren(children: Map[String, ActorRef]): Receive = {
      case StartChild(name) =>
        log.info(s"starting child $name")
        val newChildren= children + (name -> context.actorOf(Props[Child], name))
        context.become(withChildren(newChildren))

      case StopChild(name) =>
        log.info(s"stopping child $name")
        val child = children.get(name)
        if (child.isDefined) context.stop(child.get)

      case Stop =>
        log.info("stopping parent")
        context.stop(self)

      case message => log.info(message.toString)

    }
  }

  class Child extends Actor with ActorLogging{
    override def receive: Receive = {
      case message: String => log.info(message)
    }
  }

  import Parent._

  val parent = system.actorOf(Props[Parent],"parent")
  parent ! "are you still alive?"
  parent ! StartChild("child1")
  Thread.sleep(500)

  val child = system.actorSelection("/user/parent/child1")
  child ! "hello there"
  parent ! StopChild("child1")

  parent ! StartChild("child2")
  Thread.sleep(500)
  val child2 = system.actorSelection("/user/parent/child2")
  child2 ! "hello second"

  parent ! Stop
  for (i <- 1 to 10) parent ! "are you still alive?"
  for (i <- 1 to 100) child2 ! "hello second"

  val c = system.actorOf(Props[Child], "childActor")

  c ! "hello there"
  c ! PoisonPill
  c ! "are you alive"


  val c1 = system.actorOf(Props[Child], "killChild")

  c1 ! "hello there"
  c1 ! Kill
  c1 ! "are you alive"


  class Watcher extends Actor with ActorLogging {
    import Parent._
    override def receive: Receive = {
      case StartChild(name) =>
        val child = context.actorOf(Props[Child], name)
        log.info(s"started and watchinng child $name")
        context.watch(child)

      case Terminated(ref) =>
        log.info(s"${ref.path} has been terminated")

    }
  }

  val w = system.actorOf(Props[Watcher], "watcher")
  w ! StartChild("c1")
  val watched = system.actorSelection("/user/watcher/c1")
  Thread.sleep(500)

  watched ! PoisonPill



}
