package com.sghaida.akka.streams

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

object BackPressure extends App {

  implicit val system: ActorSystem = ActorSystem("system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  val fastSource = Source(1 to 1000)

  val slowSink = Sink.foreach[Int]{ item =>
    Thread.sleep(1000)
    println(s"Sink: $item")
  }

  val simpleFlow = Flow[Int].map{item =>
    println(s"incoming: $item")
    item + 1
  }

  // back-pressure
  //fastSource.async to slowSink run()

  //fastSource.async via simpleFlow.async to slowSink run()

  val bufferedFlow = simpleFlow.buffer(size=8, overflowStrategy = OverflowStrategy.dropHead)

  val throttledSource = fastSource.throttle(2, 1 seconds)

  //fastSource.async via bufferedFlow.async to slowSink run()

  throttledSource.async via simpleFlow.async to slowSink run()

}
