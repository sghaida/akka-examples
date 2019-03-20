import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class InterceptingLogsSpec
  extends TestKit(ActorSystem("intercepting-logs", ConfigFactory.load().getConfig("interceptingLogMessages")))
  with WordSpecLike
  with ImplicitSender
  with BeforeAndAfterAll{

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import InterceptingLogsSpec._

  val item = "some item"

  "a checkout flow" should {
    "correctly log dispatch of an order" in {

      EventFilter.info(pattern =  s"order [0-9]+ for $item has been dispatched", occurrences = 1) intercept{
        val checkoutActor = system.actorOf(Props[CheckoutActor])
        checkoutActor ! Checkout(item, "1011-0111-1101-0010")
      }
    }

    "fail if the payment is denied" in {
      EventFilter[RuntimeException](occurrences = 1) intercept{
        val checkoutActor = system.actorOf(Props[CheckoutActor])
        checkoutActor ! Checkout(item, "0111-0111-1101-0010")
      }
    }
  }

}

object InterceptingLogsSpec {

  case class Checkout(item: String, creditCard: String)
  case class AuthorizeCard(card: String)
  case class DispatchOrder(item: String)
  case object PaymentAccepted
  case object PaymentRejected
  case object DispatchConfirmed


  class CheckoutActor extends Actor with ActorLogging {

    private val paymentActor = context.actorOf(Props[PaymentManager], "payment-actor")
    private val fulfillmentActor = context.actorOf(Props[FulfillmentManager], "fulfillment-actor")

    override def receive: Receive = awaitingCheckout

    def awaitingCheckout: Receive = {
      case Checkout(item, card) =>
        paymentActor ! AuthorizeCard(card)
        context.become(pendingPayment(item))
    }

    def pendingPayment(item: String): Receive = {
      case PaymentAccepted =>
        fulfillmentActor ! DispatchOrder(item)
        context.become(pendingFulfillment(item))
      case PaymentRejected => throw new RuntimeException("can't handle payment")
    }

    def pendingFulfillment(item: String): Receive = {
      case DispatchConfirmed =>
        context.become(awaitingCheckout)
    }
  }

  class PaymentManager extends Actor{
    override def receive: Receive = {
      case AuthorizeCard(card) =>
        if (card.startsWith("0")) sender() ! PaymentRejected
        else sender() !PaymentAccepted
    }
  }

  class FulfillmentManager extends Actor with ActorLogging{
    var orderId = 0
    override def receive: Receive = {
      case DispatchOrder(item) =>
        Thread.sleep(4000)
        orderId +=1
        log.info(s"order $orderId for $item has been dispatched")

        sender() ! DispatchConfirmed
    }
  }
}