package com.sghaida.akka.streams

import java.util.Date

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, FanOutShape2, UniformFanInShape}
import akka.stream.scaladsl._

import scala.concurrent.ExecutionContextExecutor

object OpenGraph2 extends App {

  implicit val system: ActorSystem = ActorSystem("system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  /* take 3 numbers from 3 streams and get the max
   *  UniformFanInShape/UniformFanOutShape is used when all the inputs have the same Datatype
   * */

  val max3Graph = GraphDSL.create(){ implicit builder =>
    import GraphDSL.Implicits._

    val max1 = builder.add(ZipWith[Int, Int, Int]((a, b) => math.max(a, b)))
    val max2 = builder.add(ZipWith[Int, Int, Int]((a, b) => math.max(a, b)))

    max1.out ~> max2.in0
    UniformFanInShape(max2.out, max1.in0, max1.in1, max2.in1)
  }

  val source1 = Source(1 to 10)
  val source2 = Source(1 to 10).map(_ => 5)
  val source3 = Source((1 to 10).reverse)

  val sink = Sink.foreach[Int](println)

  val max3 = RunnableGraph.fromGraph(
    GraphDSL.create(){implicit builder =>
      import GraphDSL.Implicits._

      val max3Shape = builder.add(max3Graph)

      source1 ~> max3Shape.in(0)
      source2 ~> max3Shape.in(1)
      source3 ~> max3Shape.in(2)

      max3Shape ~> sink

      ClosedShape
    }
  )

  max3 run()


  /* non-unified fanout shapes*/

  /* process bank transactions and mark suspicious > 10000
  * outputs
  * 1. transaction go through, unmodified
  * 2. give suspicious transactions an id for further infestation
  * */


  case class Transaction(id: String, source: String, recipient: String, amount: Int, date: Date)


  val transactionSource = Source(
    List(
      Transaction("1", "a", "b", 100, new Date()),
      Transaction("2", "c", "a", 10001, new Date()),
      Transaction("3", "b", "c", 1000, new Date())
    )
  )

  val bankProcessor = Sink.foreach(println)

  val analysisService = Sink.foreach[String](txId => println(s"TXID: $txId"))


  val analysisGraph = GraphDSL.create(){ implicit builder =>
    import GraphDSL.Implicits._

    val broadcast = builder.add(Broadcast[Transaction](2))
    val suspeciousTFilter = builder.add(Flow[Transaction].filter(t => t.amount > 10000))
    val txIdExtractor = builder.add(Flow[Transaction].map(t => t.id))

    broadcast.out(0) ~> suspeciousTFilter ~> txIdExtractor

    new FanOutShape2(broadcast.in, broadcast.out(1), txIdExtractor.out)

  }

  val suspiciousTAnalyzer = RunnableGraph.fromGraph(
    GraphDSL.create(){implicit builder =>
      import GraphDSL.Implicits._

      val suspiciousShape = builder.add(analysisGraph)

      transactionSource ~> suspiciousShape.in

      suspiciousShape.out0 ~> bankProcessor
      suspiciousShape.out1 ~> analysisService

      ClosedShape
    }
  )

  suspiciousTAnalyzer run()
}

