package com.sghaida.akka.actors.patterns

import akka.actor.{ActorLogging, ActorRef, FSM}
import scala.concurrent.duration._

object AkkaFSM {

  case class RequestProduct(product: String)
  case class Instruction(msg: String)
  case class ReceiveMoney(amount: Int)
  case class Deliver(product: String)
  case class GivebackChange(amount: Int)
  case class VendingError(reason: String)

  trait VendingState
  case object Idle extends VendingState
  case object Operational extends VendingState
  case object WaitForMoney extends VendingState

  trait VendingData
  case object Uninitialized extends VendingData
  case class Initialized(inventory: Map[String, Int], prices: Map[String, Int]) extends VendingData

  case class WaitingForMoneyData(
                                  inventory: Map[String, Int], prices: Map[String, Int],
                                  product: String, money: Int, requester: ActorRef
                                ) extends VendingData

  class VendingMachineFSM extends FSM[VendingState, VendingData] with ActorLogging {

    startWith(Idle, Uninitialized)

    when(Idle) {
      case Event(Initialized(inventory, prices), Uninitialized) =>
        goto(Operational) using Initialized(inventory, prices)

      case _ =>
        sender() ! VendingError("machine not initialized")
        stay()

    }

    when(Operational){
      case Event(RequestProduct(product), Initialized(inventory, prices)) => inventory.get(product) match {
        case None | Some(0) =>
          sender() !VendingError("product not available")
          stay()
        case Some(amount) =>
          val price = prices(product)
          sender() ! Instruction(s"please insert price: $price")
          goto(WaitForMoney) using WaitingForMoneyData(inventory, prices, product, 0,sender())
      }

    }

    when(WaitForMoney, stateTimeout = 1 second){
      case Event(StateTimeout, WaitingForMoneyData(inventory, prices, product, money, requester)) =>
        requester ! VendingError("request time out: price hasn't been paid")
        if (money > 0) requester ! GivebackChange(money)
        goto(Operational) using Initialized(inventory, prices)

      case Event(ReceiveMoney(amount), WaitingForMoneyData(inventory, prices, product, money, requester)) =>
        val price = prices(product)

        if (money + amount >= price) {
          requester ! Deliver(product)
          if (money + amount > price) requester ! GivebackChange(money + amount - price)
          goto(Operational) using Initialized(inventory + (product -> (inventory(product) -1)), prices)
        } else {
          requester ! Instruction(s"please insert price: ${price - money - amount}")
          stay() using WaitingForMoneyData(inventory, prices, product, money+ amount, requester)
        }
    }

    whenUnhandled{
      case Event(_, _) =>
        sender() ! VendingError("command not found")
        stay()
    }

    onTransition{
      case st1-> st2 => log.info(s"moving for ${st1.toString} to ${st2.toString}")
    }

    initialize()
  }


}
