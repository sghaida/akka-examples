import TestProbSpec.Report
import akka.actor.{ActorRef, ActorSystem, Props, Terminated}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import com.sghaida.akka.actors.lifecycle.Supervision
import org.scalatest.Matchers._

class SupervisionSpec extends TestKit(ActorSystem("supervision"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll{

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)
  import Supervision._


  "supervisor" should {
    "supervisor resume child in case of minor fault" in {
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[ShittyWordCounter]
      val child = expectMsgType[ActorRef]

      child ! "its boring, fuck it"
      child ! Print
      expectMsg(4)

      /* suspend -> supervisor -> resume child*/
      child ! "h h h h h h h h h h h h h h h h h h h h h h"
      child ! Print
      expectMsg(4)
    }

    "supervisor restart child with NullPointerException" in {
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[ShittyWordCounter]
      val child = expectMsgType[ActorRef]

      child ! "its boring, fuck it"
      child ! Print
      expectMsg(4)

      child ! ""
      child ! Print

      expectMsg(0)

    }

    "supervisor terminate child in major error" in {
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[ShittyWordCounter]
      val child = expectMsgType[ActorRef]

      watch(child)

      child ! "Stopping message"

      val terminated = expectMsgType[Terminated]

      terminated.actor shouldEqual child

    }

    "supervisor escalate if doens't know how to handle error" in {
      val supervisor = system.actorOf(Props[Supervisor], "supervisor")
      supervisor ! Props[ShittyWordCounter]
      val child = expectMsgType[ActorRef]

      watch(child)

      child ! 42

      val terminated = expectMsgType[Terminated]
      /* escalate supervisor will stop all children and then escalate to parent, which mean child is going to die
      * */
      terminated.actor shouldEqual child

    }
  }

  "all for one supervisor" should {
    "apply all-for-one strategy" in {
      val supervisor = system.actorOf(Props[AllforOneSupervisor],"all-for-one")
      supervisor ! Props[ShittyWordCounter]
      val child1 = expectMsgType[ActorRef]

      supervisor ! Props[ShittyWordCounter]
      val child2 = expectMsgType[ActorRef]

      child2 ! "hello there"
      child2 ! Print
      expectMsg(2)

      EventFilter[NullPointerException]() intercept{
        child1 ! ""
      }

      child2 ! Print
      expectMsg(0)





    }
  }

}

object SupervisionSpec
