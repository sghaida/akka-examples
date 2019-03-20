package com.sghaida.akka.actors

import akka.actor.{Actor, ActorSystem, Props}

object BasicActor extends App {

  val system = ActorSystem("example-actor")

  class WordCount extends Actor{
    var totalWords = 0
    override def receive: Receive = {
      case msg: String =>
        totalWords += msg.split("\\S+").length
        print(totalWords)
      case _ => unhandled(_)
    }
  }

  val actor = system.actorOf(Props[WordCount],"word-counter")

  actor ! "hello its me saddam"
  actor ! 10

}
