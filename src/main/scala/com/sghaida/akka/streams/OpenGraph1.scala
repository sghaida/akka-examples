package com.sghaida.akka.streams

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, FlowShape, SinkShape, SourceShape}
import akka.stream.scaladsl._

import scala.concurrent.ExecutionContextExecutor

object OpenGraph1 extends App {

  implicit val system: ActorSystem = ActorSystem("system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  /* concatenate tow sources in graph */

  val firstSource = Source(1 to 10)
  val secondSource = Source(42 to 1000)

  val sourceGraph = Source.fromGraph(
    GraphDSL.create(){ implicit builder =>
      import GraphDSL.Implicits._

      val merge = builder.add(Concat[Int](2))

      firstSource ~> merge
      secondSource  ~> merge

      SourceShape(merge.out)
    }
  )

  //sourceGraph to Sink.foreach(println) run()


  /* complex sinks */
  val sink1 = Sink.foreach[Int](x =>println(s"[sink-1]: $x"))
  val sink2 = Sink.foreach[Int](x =>println(s"[sink-2]: $x"))

  val sinkGraph = Sink.fromGraph(
    GraphDSL.create(){implicit builder =>
      import GraphDSL.Implicits._
      val broadcast = builder.add(Broadcast[Int](2))
      broadcast ~> sink1
      broadcast ~> sink2

      SinkShape(broadcast.in)
    }
  )

  //firstSource to sinkGraph run()

  /* complex flows */
  val flow1 = Flow[Int].map(item => item + 1)
  val flow2 = Flow[Int].map(item => item * 2)

  val flowGraph = Flow.fromGraph(
    GraphDSL.create(){implicit builder =>
      import GraphDSL.Implicits._
      val flowshape1 = builder.add(flow1)
      val flowshape2 = builder.add(flow2)

      flowshape1 ~> flowshape2

      FlowShape(flowshape1.in, flowshape2.out)
    }
  )

  firstSource via flowGraph to Sink.foreach(println) run()


  def createFlowFromSourceAndSink[A, B](source: Source[A, _], sink: Sink[B, _]): Flow[B, A, _] = {
    Flow.fromGraph(
      GraphDSL.create(){ implicit builder =>
        val sourceShape = builder.add(source)
        val sinkShape = builder.add(sink)

        FlowShape(sinkShape.in, sourceShape.out)
      }
    )
  }

}
