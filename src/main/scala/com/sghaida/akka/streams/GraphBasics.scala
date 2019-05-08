package com.sghaida.akka.streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, FanOutShape}
import akka.stream.scaladsl._

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Success

object GraphBasics extends App {

  implicit val system: ActorSystem = ActorSystem("system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  val input = Source(1 to 1000)

  val incrementer = Flow[Int].map(_ + 1)
  val multiplier = Flow[Int].map(_ * 10)

  val printSink = Sink.foreach[(Int, Int)](println)
  val output = Sink.fold[List[(Int, Int)], (Int, Int)](List()){
    (acc, a) => acc :+ a
  }

  /* create shape
  * builder is mutable and the definition inside the static graph is the mutation
  * graph becomes immutable after closing the shape
  * */
  val graphShapeWithOutput = GraphDSL.create(output){ implicit builder: GraphDSL.Builder[Future[List[(Int, Int)]]] =>sink =>
    import GraphDSL.Implicits._

    val broadcast = builder.add(Broadcast[Int](2))
    val zip = builder.add(Zip[Int, Int])

    input ~> broadcast

    broadcast.out(0) ~> incrementer ~> zip.in0
    broadcast.out(1) ~> multiplier ~> zip.in1

    zip.out ~> sink.in

    ClosedShape
  }

  val graphShapeWithoutOutput = GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._

    val broadcast = builder.add(Broadcast[Int](2))
    val zip = builder.add(Zip[Int, Int])

    input ~> broadcast

    broadcast.out(0) ~> incrementer ~> zip.in0
    broadcast.out(1) ~> multiplier ~> zip.in1

    zip.out ~> printSink
    ClosedShape

  }

  /* create graph from shape*/
  val graph1 = RunnableGraph.fromGraph(graphShapeWithOutput)
  val graph2 = RunnableGraph.fromGraph(graphShapeWithoutOutput)

  val result = graph1.run()
  graph2.run()

//  result.onComplete{
//    case Success(value) => value.foreach(println)
//  }
}
