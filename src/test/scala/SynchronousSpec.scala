import SyncronousSpec.{Counter, Increment, Read}
import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{CallingThreadDispatcher, TestActorRef, TestProbe}
import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class SynchronousSpec extends WordSpecLike with BeforeAndAfterAll{

  implicit val system: ActorSystem = ActorSystem("synchronous-test")

  override def afterAll(): Unit = system.terminate()

  "counter" should {
    "synchronously increase counter" in {
      val counter = TestActorRef[Counter](Props[Counter])
      counter ! Increment

      counter.underlyingActor.counter shouldEqual 1
    }

    "synchronously increase counter at the call of the receive function" in {
      val counter = TestActorRef[Counter](Props[Counter])
      counter.receive(Increment)
      counter.underlyingActor.counter shouldEqual 1
    }

    "work on the calling thread dispatcher" in {
      val counter = system.actorOf(Props[Counter].withDispatcher(CallingThreadDispatcher.Id))
      val probe = TestProbe()

      probe.send(counter, Read)
      probe.expectMsg(0)
    }
  }
}

object SyncronousSpec{

  case object Increment
  case object Read

  class Counter extends Actor {
    var counter = 0
    override def receive: Receive = {
      case Increment => counter +=1
      case Read => sender() ! counter
    }
  }
}
