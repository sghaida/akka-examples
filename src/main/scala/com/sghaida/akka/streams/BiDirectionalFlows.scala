package com.sghaida.akka.streams

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, BidiShape, ClosedShape}

import scala.concurrent.ExecutionContextExecutor

object BiDirectionalFlows extends App {

  implicit val system: ActorSystem = ActorSystem("system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  def encrypt(number: Int)(data: String) = data.map(c => (c + number).toChar)
  def decrypt(number: Int)(data: String) = data.map(c => (c - number).toChar)

  val cryptoGraph = GraphDSL.create(){implicit builder =>

    val encFlow = builder.add(Flow[String].map(encrypt(3)))
    val decFlow = builder.add(Flow[String].map(decrypt(3)))

    //BidiShape(encFlow.in, encFlow.out, decFlow.in, decFlow.out)
    BidiShape.fromFlows(encFlow, decFlow)
  }

  val data = List("hello", "there", "testing", "BiDi", "flow")
  val unencryptedSource = Source(data)
  val encryptedSource = Source(data.map(encrypt(3)))


  RunnableGraph.fromGraph(
    GraphDSL.create(){implicit builder =>
      import GraphDSL.Implicits._
      val unencryptedSourceShape = builder.add(unencryptedSource)
      val encryptedSourceShape = builder.add(encryptedSource)

      val bidi = builder.add(cryptoGraph)

      val encryptedSinkShape = builder.add(Sink.foreach[String](text => println(s"[Encrypted]: $text")))
      val decryptedSinkShape = builder.add(Sink.foreach[String](text => println(s"[Decrypted]: $text")))

      unencryptedSourceShape ~> bidi.in1; bidi.out1 ~> encryptedSinkShape
      encryptedSourceShape ~> bidi.in2; bidi.out2 ~> decryptedSinkShape

      ClosedShape

    }
  ).run()
}

