package com.sghaida.akka.actors.patterns

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import scala.concurrent.duration._

object FSMPattern {

  /* map between product and cost*/
  case class Initialize(inventory: Map[String, Int], prices: Map[String, Int])
  case class RequestProduct(product: String)
  case class Instruction(msg: String)
  case class ReceiveMoney(amount: Int)
  case class Deliver(product: String)
  case class GivebackChange(amount: Int)
  case class VendingError(reason: String)
  case object ReceiveMoneyTimeout


  class VendingMachine extends Actor with ActorLogging {


    override def receive: Receive = idle

    def idle: Receive = {
      case Initialize(inventory, prices) => context.become(operational(inventory, prices))
      case _ => sender() ! VendingError("machine not initialized")
    }

    def operational(inventory: Map[String, Int], prices: Map[String, Int]): Receive = {
      case RequestProduct(product) => inventory.get(product) match {
        case None | Some(0) =>
          sender() !VendingError("product not available")
        case Some(amount) =>
          val price = prices(product)
          sender() ! Instruction(s"please insert price: $price")
          context.become(
            waitingForMoney(
              inventory, prices, product, 0, receiveMonyTimeoutSchedule , sender()
            )
          )
      }
    }

    def waitingForMoney(
             inventory: Map[String, Int], prices: Map[String, Int],
             product: String, money: Int, moneyTimeoutSchedule: Cancellable,
             requester: ActorRef
    ): Receive = {

      case ReceiveMoneyTimeout =>
        requester ! VendingError("request time out: price hasn't been paid")
        if (money > 0) requester ! GivebackChange(money)
        context.become(operational(inventory, prices))

      case ReceiveMoney(amount) =>
        moneyTimeoutSchedule.cancel()
        val price = prices(product)

        if (money + amount >= price) {
          requester ! Deliver(product)
          if (money + amount > price) requester ! GivebackChange(money + amount - price)

          context.become(operational(inventory + (product -> (inventory(product) -1)), prices))
        } else {
          requester ! Instruction(s"please insert price: ${price - money - amount}")
          context.become(
            waitingForMoney(inventory,prices, product, money + amount, receiveMonyTimeoutSchedule, requester )
          )
        }
    }

    def receiveMonyTimeoutSchedule: Cancellable = context.system.scheduler.scheduleOnce(1 second){
      self ! ReceiveMoneyTimeout
    }(context.dispatcher)
  }

}
