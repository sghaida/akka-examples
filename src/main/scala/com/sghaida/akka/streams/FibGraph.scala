package com.sghaida.akka.streams

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ClosedShape, OverflowStrategy, UniformFanInShape}

import scala.concurrent.ExecutionContextExecutor

object FibGraph extends App {



  implicit val system: ActorSystem = ActorSystem("system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher


  val fibStaticGraph = GraphDSL.create(){implicit builder =>
    import GraphDSL.Implicits._

    val zipperShape = builder.add(Zip[BigInt,BigInt]())
    val mergeShape = builder.add(MergePreferred[(BigInt, BigInt)](1))
    val fibFlow = builder.add(Flow[(BigInt, BigInt)].map{fib =>
      Thread.sleep(10)
      (fib._1 + fib._2, fib._1)
    })

    val broadcast = builder.add(Broadcast[(BigInt, BigInt)](2))
    val extraxt = builder.add(Flow[(BigInt, BigInt)].map(_._1))


    zipperShape.out ~>  mergeShape            ~> fibFlow ~> broadcast ~> extraxt
                        mergeShape.preferred  <~            broadcast

    UniformFanInShape(extraxt.out, zipperShape.in0, zipperShape.in1)
  }




  RunnableGraph.fromGraph(GraphDSL.create(){implicit builder =>
    import GraphDSL.Implicits._

    val source1 = builder.add(Source.single[BigInt](1))
    val source2 = builder.add(Source.single[BigInt](1))
    val sink = builder.add(Sink.foreach(println))
    val fib = builder.add(fibStaticGraph)

    source1 ~> fib.in(0)
    source2 ~> fib.in(1)

    fib.out ~> sink

    ClosedShape

  }).run()

}

