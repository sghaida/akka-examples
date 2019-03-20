package com.sghaida.akka.actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.sghaida.akka.actors.CounterActor.Counter.{Decrement, Increment, Print, Reset}

object CounterActor extends App {

  val system = ActorSystem("actor-system")

  object Counter {
    case object Increment
    case object Decrement
    case object Reset
    case object Print
  }

  class Counter extends Actor {

    override def receive: Receive = countReceive(0)

    import Counter._

    def countReceive(count: Int): Receive = {
      case Increment => context.become(countReceive(count + 1))
      case Decrement => context.become(countReceive(count - 1))
      case Reset => context.become(countReceive(0))
      case Print => println(count)
    }

  }

  val counter = system.actorOf(Props[Counter])

  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 2).foreach(_ => counter ! Decrement)
  counter ! Print
  counter ! Reset
  counter ! Print

}