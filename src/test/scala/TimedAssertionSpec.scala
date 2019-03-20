import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.Matchers._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration.DurationLong
import scala.util.Random

class TimedAssertionSpec extends TestKit(ActorSystem("timed-assertion", ConfigFactory.load().getConfig("config1")))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll{

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)
  import TimedAssertionSpec._

  " worker actor" should {

    val worker = system.actorOf(Props[WorkerActor])

    "reply within timely manner" in {

      within(500 millis, 1 second) {
        worker ! "work"
        expectMsg(Result(10))
      }
    }

    "replay of rapped fire within timely manner" in {
      within(1 second) {

        worker ! "workSequence"

        val results = receiveWhile[Int](2 seconds, idle = 500 milli, messages = 10) {
          case Result(res) => res
        }

        results.length shouldEqual 10
        results.head shouldEqual 1

      }
    }

    "reply to TestProbe within timely manner" in {
      within(1 second){
        val probe = TestProbe("test-reply")
        probe.send(worker,"work")
        probe.expectMsg(Result(10)) //timeout 0.3 sec
      }
    }
  }
}

object TimedAssertionSpec {
  case class Result(res: Int)
  class WorkerActor extends Actor{
    override def receive: Receive = {
      case "work" =>
        Thread.sleep(500)
        sender() ! Result(10)
      case "workSequence" =>
        val r = new Random()
        for (i <- 1 to 10){
          Thread.sleep(r.nextInt(50))
          sender() ! Result(1)
        }
    }
  }
}
