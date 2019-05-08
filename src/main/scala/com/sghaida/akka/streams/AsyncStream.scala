package com.sghaida.akka.streams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._


import scala.concurrent.ExecutionContextExecutor

object AsyncStream extends App {
  implicit val system: ActorSystem = ActorSystem("system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  val simpleSource = Source(1 to 1000)
  val simpleFlow1 = Flow[Int].map(_ + 1)
  val simpleFlow2 = Flow[Int].map(_ * 2)
  val simpleSink = Sink.foreach(println)

  /* runs on the same actor*/
  val simpleGraph = simpleSource via simpleFlow1 via simpleFlow2 to simpleSink

  //simpleGraph.run()

  val complexFlow1 = Flow[Int].map{ x =>
    Thread.sleep(1000)
    x + 1
  }

  val complexFlow2 = Flow[Int].map{ x =>
    Thread.sleep(1000)
    x * 2
  }
  /* bad */
  // simpleSource via complexFlow1 via complexFlow2 to simpleSink run()

  /* good */
  /*(
    simpleSource via complexFlow1.async
                via complexFlow2.async
                to simpleSink run()
  )*/


  Source(1 to 10)
    .map(item => {println(s"flow a: $item"); item}).async
    .map(item => {println(s"flow b: $item"); item}).async
    .map(item => {println(s"flow c: $item"); item}).async
    .runWith(Sink.ignore)

}
