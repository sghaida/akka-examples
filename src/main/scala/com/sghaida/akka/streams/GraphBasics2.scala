package com.sghaida.akka.streams


import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ClosedShape}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

object GraphBasics2 extends App {

  implicit val system: ActorSystem = ActorSystem("system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  val fastInput = Source(1 to 1000).throttle(10, 1 second)
  val slowInput = Source(1 to 1000).throttle(5, 1 second)

  val sink1 = Sink.foreach[Int](item => println(s"Sink-1: $item"))
  val sink2 = Sink.foreach[Int](item => println(s"Sink-2: $item"))

  val sinkfold1 = Sink.fold[Int, Int](0){(acc, _) => println(s"Sink-1 ${acc+1}"); acc+1}
  val sinkfold2 = Sink.fold[Int, Int](0){(acc, _) => println(s"Sink-2 ${acc+1}"); acc+1}

  /* create shape
  * builder is mutable and the definition inside the static graph is the mutation
  * graph becomes immutable after closing the shape
  * */

  val graphShape1 = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val merge = builder.add(Merge[Int](2))
    val balance = builder.add(Balance[Int](2))

    fastInput ~> merge
    slowInput ~> merge

    merge ~> balance ~> sink1
             balance ~> sink2

    ClosedShape

  }

  val graphShape2 = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val merge = builder.add(Merge[Int](2))
    val balance = builder.add(Balance[Int](2))

    fastInput ~> merge
    slowInput ~> merge

    merge ~> balance ~> sinkfold1
    balance ~> sinkfold2

    ClosedShape

  }

  //RunnableGraph.fromGraph(graphShape1).run()
  RunnableGraph.fromGraph(graphShape2).run()
}
