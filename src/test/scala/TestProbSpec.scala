import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration.DurationLong

class TestProbSpec extends TestKit(ActorSystem("TestProb")) with ImplicitSender with WordSpecLike with BeforeAndAfterAll{

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import TestProbSpec._

  "a master actor" should {

    "register a slave" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      /* test interaction between master and slave*/
      master ! Register(slave.ref)
      expectMsg(RegistrationAck)
    }

    "send a work to slave actor" in {

      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")

      master ! Register(slave.ref)
      expectMsg(RegistrationAck)

      master ! Work("thi is a test to check")

      slave.expectMsg(SlaveWork("thi is a test to check", testActor))

      slave.reply(WorkCompleted(3, testActor))

      expectMsg(Report(3))

    }

    "aggregate data correctly" in {

      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")

      master ! Register(slave.ref)
      expectMsg(RegistrationAck)
      val text = "thi is a test to check"
      master ! Work(text)
      master ! Work(text)

      slave.receiveWhile(){
        case SlaveWork(`text`, `testActor`) => slave.reply(WorkCompleted(3, testActor))
      }

      expectMsg(Report(3))
      expectMsg(Report(6))

    }
  }

}

object TestProbSpec {

  case class Register(slave: ActorRef)
  case class Work(text: String)
  case class Report(count: Int)
  case class SlaveWork(text: String, requester: ActorRef)
  case class WorkCompleted(count: Int, requester: ActorRef)
  case object RegistrationAck

  class Master extends Actor {
    override def receive: Receive = {
      case Register(slave) =>
        sender() ! RegistrationAck
        context.become(online(slave, 0))
      case _ =>
    }

    def online(ref: ActorRef, totalCount: Int): Receive = {
      case Work(text) => ref ! SlaveWork(text, sender())
      case WorkCompleted(count, requester) =>
        val newTotalWordsCount = totalCount + count
        requester ! Report(newTotalWordsCount)
        context.become(online(ref, newTotalWordsCount))
    }
  }

}