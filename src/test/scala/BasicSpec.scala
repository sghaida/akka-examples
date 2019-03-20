import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._
import scala.util.Random

class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll{

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "a SimpleActor" should {
    "send back the same message" in {
      val echoActor = system.actorOf(Props[BasicSpec.SimpleActor])
      val msg = "hello test"
      echoActor ! msg

      expectMsg(msg) // akka.test.single-expect-default=3s

    }
  }

  "a Blackhole Actor" should {
    "send back some message" in {
      val backholeActor = system.actorOf(Props[BasicSpec.BalckHoleActor])
      val msg = "hello test"
      backholeActor ! msg

      expectNoMessage(1 second)
    }
  }

  "a LabTestActor" should {

    val labTestActor = system.actorOf(Props[BasicSpec.LabTestActor])

    "turn a string to upper" in {

      val msg = "hello test"
      labTestActor ! msg
      val reply = expectMsgType[String]
      reply shouldEqual msg.toUpperCase
    }

    "get 0 or 1" in {
      labTestActor ! 10
      expectMsgAnyOf[Int](0,1)
    }

    "get all favourites" in {
      labTestActor ! "favourite"

      expectMsgAllOf("scala", "akka")
    }

    "get all favourites with replies" in {
      labTestActor ! "favourite"
      val messages = receiveN(2)

      messages.length shouldEqual 2

    }

    "get all favourites with partials" in {
      labTestActor ! "favourite"

      expectMsgPF(){
        case "scala" =>
        case "akka" =>
      }

    }
  }

}

object BasicSpec {
  class SimpleActor extends Actor{
    override def receive: Receive = {
      case msg => sender() ! msg
    }
  }

  class BalckHoleActor extends Actor{
    override def receive: Receive = Actor.emptyBehavior
  }

  class LabTestActor extends Actor{
    val rand = new Random()
    override def receive: Receive = {
      case "favourite" =>
        sender() ! "scala"
        sender() ! "akka"
      case msg: String => sender() ! msg.toUpperCase
      case num: Int => sender() ! (if (rand.nextBoolean()) 1 else 0)
    }
  }

}
