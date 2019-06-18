package com.sghaida.akka.streams


import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, FlowShape, SinkShape}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Success

object GraphMaterializedValues extends App {

  implicit val system: ActorSystem = ActorSystem("system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  val source = Source(List("hello", "there", "how", "are", "you"))

  /* pass a list of words,
  1. filter less than 3 and print the metrialized count,
  2. filter if small and print
  */

  val printer = Sink.foreach(println)
  val counter = Sink.fold[Int, String](0){ (count, _) => count +1}

  val complexSink1 = Sink.fromGraph(GraphDSL.create(counter){ implicit builder => counterShape =>
    import GraphDSL.Implicits._

    val broadcast = builder.add(Broadcast[String](2))
    val lowercaseFilter = builder.add(Flow[String].filter(word => word == word.toLowerCase()))
    val shortstringFilter = builder.add(Flow[String].filter(item => item.length <= 3))
    broadcast ~> lowercaseFilter ~> printer
    broadcast ~> shortstringFilter ~> counterShape

    SinkShape(broadcast.in)
  })

  val complexSink2 = Sink.fromGraph(GraphDSL.create(counter, printer)((a, b) => a){
    implicit builder => (counterShape, printerShape) =>
      import GraphDSL.Implicits._

      val broadcast = builder.add(Broadcast[String](2))
      val lowercaseFilter = builder.add(Flow[String].filter(word => word == word.toLowerCase()))
      val shortstringFilter = builder.add(Flow[String].filter(item => item.length <= 3))
      broadcast ~> lowercaseFilter ~> printerShape
      broadcast ~> shortstringFilter ~> counterShape

      SinkShape(broadcast.in)
  })

  source.toMat(complexSink1)(Keep.right).run() onComplete{
    case Success(value) =>
      println(value)
    case _ =>
  }


  def enhancedFlow[A, B](flow: Flow[A, B, _]): Flow[A, B, Future[Int]] = {

    val counter = Sink.fold[Int, B](0){(acc, _) => acc +1}

    Flow.fromGraph(GraphDSL.create(counter){ implicit  builder => counterShape =>
      import GraphDSL.Implicits._
      val flowShape = builder.add(flow)
      val broadcast = builder.add(Broadcast[B](2))

      flowShape ~> broadcast ~> counterShape

      FlowShape(flowShape.in, broadcast.out(1))
    })

  }

  val simpleFlow = Flow[Int].map(x => x)
  val simpleSource = Source(1 to 100)

  val res = simpleSource.viaMat(enhancedFlow(simpleFlow))(Keep.right).toMat(Sink.ignore)(Keep.left).run()

  res.onComplete{
    case Success(value) => println(value)
  }
}

