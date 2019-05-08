package com.sghaida.akka.streams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object MatrializingStreams extends App {
  implicit val system: ActorSystem = ActorSystem("system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher


  val simpleGraph = Source(1 to 10) to Sink.foreach(println)

  //val matrerializedValue = simpleGraph.run()

  val source = Source[Int](1 to 10)
  val flow = Flow[Int].map(2 * _)
  val sink = Sink.reduce[Int](_ + _)

  val result: Future[Int] = source.via(flow).runWith(sink)

  result onComplete{
    case Success(value) => println(value)
    case Failure(ex) => throw ex
  }

  val simpleSource = Source[Int](1 to 10)
  val simpleFlow = Flow[Int].map(_ + 1)
  val simpleSink = Sink.fold[List[Int], Int](List[Int]()){
    (acc, b) => acc :+ b
  }

  val graph = simpleSource.viaMat(simpleFlow)(Keep.right).toMat(simpleSink)(Keep.right)

  graph.run() onComplete{
    case Success(value) => println(value)
    case Failure(ex) => throw ex
  }

  val res2 = Source(1 to 10).runWith(Sink.reduce[Int](_ + _))
  val res3 = Source(1 to 10).runReduce(_ + _)

  // connect flow to source and sink
  val res4 = Flow[Int].map(_ *2).runWith(simpleSource, simpleSink)

  val res5 = Source(1 to 10).runWith(Sink.last)

  val sentencesSource = Source(List(
    "hell there how are you",
    "im fine thanks man",
    "how is life treating you"
  ))

  val sentencesFlow = Flow[String].map(s => s.split(" ").length)

  val sentencesSink = Sink.fold[Int,Int](0){ (a,b) => a+b}

  val count = sentencesSource.viaMat(sentencesFlow)(Keep.right).toMat(sentencesSink)(Keep.right)

  count.run() onComplete{
    case Success(value) => println(value)
    case Failure(ex) => throw ex
  }

}
