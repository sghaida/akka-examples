import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import scala.concurrent.duration._

class FSMSpec extends TestKit(ActorSystem("system"))with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import com.sghaida.akka.actors.patterns.FSMPattern._

  " vending machine" should {
    "error when not initialized" in {

      val machine = system.actorOf(Props[VendingMachine])
      machine ! RequestProduct("chips")
      expectMsg(VendingError("machine not initialized"))
    }

    "report product not available" in {
      val machine = system.actorOf(Props[VendingMachine])
      machine ! Initialize(Map("chips"-> 10), Map("chips"-> 10))
      machine ! RequestProduct("xxx")
      expectMsg(VendingError("product not available"))
    }

    "timeout if no money inserted" in {
      val machine = system.actorOf(Props[VendingMachine])
      machine ! Initialize(Map("chips"-> 10), Map("chips"-> 10))
      machine ! RequestProduct("chips")
      expectMsg(Instruction("please insert price: 10"))

      within(2 seconds){
        expectMsg(VendingError("request time out: price hasn't been paid"))
      }
    }

    "handle recipient of partial money" in {
      val machine = system.actorOf(Props[VendingMachine])
      machine ! Initialize(Map("xxx"-> 10), Map("xxx"-> 10))
      machine ! RequestProduct("xxx")
      expectMsg(Instruction("please insert price: 10"))

      machine ! ReceiveMoney(5)
      expectMsg(Instruction("please insert price: 5"))

      within(2 seconds){
        expectMsg(VendingError("request time out: price hasn't been paid"))
        expectMsg(GivebackChange(5))
      }
    }

    "deliver the product if insert all the money" in {

      val machine = system.actorOf(Props[VendingMachine])
      machine ! Initialize(Map("aaa"-> 10), Map("aaa"-> 10))
      machine ! RequestProduct("aaa")
      expectMsg(Instruction("please insert price: 10"))

      machine ! ReceiveMoney(10)
      expectMsg(Deliver("aaa"))
    }

    "deliver the product and give back change" in {

      val machine = system.actorOf(Props[VendingMachine])
      machine ! Initialize(Map("bbb"-> 10), Map("bbb"-> 10))
      machine ! RequestProduct("bbb")
      expectMsg(Instruction("please insert price: 10"))

      machine ! ReceiveMoney(11)
      expectMsg(Deliver("bbb"))
      expectMsg(GivebackChange(1))

      machine ! RequestProduct("bbb")
      expectMsg(Instruction("please insert price: 10"))

    }
  }

}
