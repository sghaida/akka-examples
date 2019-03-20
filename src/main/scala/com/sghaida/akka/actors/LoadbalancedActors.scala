package com.sghaida.akka.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object LoadbalancedActors extends App {

  val system = ActorSystem("ChildActors")

  object WordCounterMaster {
    case class Initialize(children: Int)
    case class WordCountTask(id: Int, text: String)
    case class WordCountReply(id: Int, count: Int)
  }

  class WordCounterMaster extends Actor {
    import WordCounterMaster._
    override def receive: Receive = {
      case Initialize(count) =>
        val children = for(i <- 1 to count) yield context.actorOf(Props[WordCounterWorker],s"wcw_$i")
        context.become(withChildren(children, 0, 0, Map()))
    }

    def withChildren(children: Seq[ActorRef], currentIndex: Int, currentId: Int, requests: Map[Int, ActorRef]): Receive = {
      case text: String =>
        val child = children(currentIndex)
        val updatedRequests = requests + (currentId -> sender())
        val updatedIndex = (currentIndex +1) % children.length
        val updatedTaskId = currentId +1
        child ! WordCountTask(currentId, text)
        context.become(withChildren(children, updatedIndex, updatedTaskId, updatedRequests))
      case WordCountReply(id, count) =>
        val sender = requests(id)
        println(s"count received from ${context.sender().path} with the taskId: $id and the count=$count")
        sender ! count
        context.become(withChildren(children, currentIndex, currentId, requests - id))
    }
  }

  class WordCounterWorker extends Actor {
    import WordCounterMaster._
    override def receive: Receive = {
      case WordCountTask(id, text) =>
        println(s"${self.path}: message received from ${sender().path} with taskId: $id message: $text ")
        sender() ! WordCountReply(id, text.split(" ").length)
    }
  }

  object TestActor{
    case object Start
  }
  class TestActor extends Actor {
    import WordCounterMaster._
    import TestActor._
    override def receive: Receive = {
      case Start =>
        val master = context.actorOf(Props[WordCounterMaster], "master")
        master ! Initialize(3)
        val text = Seq("this is me testing words count using 10 workers", "yet another test", "yes yes", "no no no no")
        text.foreach(master ! _)
      case count: Int => println(count)
    }
  }

  import TestActor._
  val test = system.actorOf(Props[TestActor],"test")
  test ! Start

}
