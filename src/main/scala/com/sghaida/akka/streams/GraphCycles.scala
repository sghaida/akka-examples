package com.sghaida.akka.streams

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ClosedShape, OverflowStrategy}

import scala.concurrent.ExecutionContextExecutor

object GraphCycles extends App {



  implicit val system: ActorSystem = ActorSystem("system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher


  val accelerator = GraphDSL.create(){implicit builder =>
    import GraphDSL.Implicits._

    val sourceShape = builder.add(Source(1 to 100))
    val mergeShape = builder.add(Merge[Int](2))
    val incrementerShape = builder.add(Flow[Int].map { x =>
      println(s"[ACCELERATING] $x")
      x + 1
    })

    sourceShape ~>  mergeShape ~> incrementerShape
                    mergeShape <~ incrementerShape
    ClosedShape
  }

  /*
  cycle deadlock due to back pressure in the incrementerShape that probagates to the sourceShape
  output: [ACCELERATING] 1
  */
  //RunnableGraph.fromGraph(accelerator).run()


  /* SOLUTION 1
  replace the merge with prefered merge
  if no preferred value take from the source else take from the preferred inlet
  */
  val accelerator1 = GraphDSL.create(){implicit builder =>
    import GraphDSL.Implicits._

    val sourceShape = builder.add(Source(1 to 100))
    val mergeShape = builder.add(MergePreferred[Int](1))
    val incrementerShape = builder.add(Flow[Int].map { x =>
      println(s"[ACCELERATING] $x")
      x + 1
    })

    sourceShape ~>  mergeShape ~> incrementerShape
    mergeShape.preferred <~ incrementerShape
    ClosedShape
  }


  //RunnableGraph.fromGraph(accelerator1).run()


  /* solution2
  break the deadlock that happened from back-pressure using buffering strategy
  * */

  val accelerator2 = GraphDSL.create(){implicit builder =>
    import GraphDSL.Implicits._

    val sourceShape = builder.add(Source(1 to 100))
    val mergeShape = builder.add(Merge[Int](2))
    val repeaterShape = builder.add(Flow[Int].buffer(10, OverflowStrategy.dropHead).map { x =>
      println(s"[ACCELERATING] $x")
      Thread.sleep(100)
      x
    })

    sourceShape ~>  mergeShape ~> repeaterShape
    mergeShape <~ repeaterShape
    ClosedShape
  }


  RunnableGraph.fromGraph(accelerator2).run()

}

