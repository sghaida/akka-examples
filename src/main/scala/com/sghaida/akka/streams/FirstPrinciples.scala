package com.sghaida.akka.streams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object FirstPrinciples extends App {

  implicit val system: ActorSystem = ActorSystem("system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val source = Source(1 to 10)
  val sink = Sink.foreach(println)

  val graph = source to sink
  //graph.run()

  // flows

  val flow = Flow[Int].map( _ + 1)

  val graph2 = source via flow to sink

  //graph2 run()

  val namesSource = Source(List("saddam", "marcus", "antonia", "can"))
  namesSource.filter(_.length > 5).take(2).runForeach(println)

}
