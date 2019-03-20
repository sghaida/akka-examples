package com.sghaida.akka.actors.routers

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.routing._
import scala.concurrent.duration._

object ManualRouter extends App {

  class Master extends Actor with ActorLogging {

    /* create slaves*/
    private val slaves = for (i <- 1 to 5) yield {
      val slave = context.actorOf(Props[Slave], s"slave-$i")
      context.watch(slave)
      ActorRefRoutee(slave)
    }

    /* routing logic
    1. RoundRobinRoutingLogic()
    2. RandomRoutingLogic()
    3. SmallestMailboxRoutingLogic()
    4. BroadcastRoutingLogic()
    5. ScatterGatherFirstCompletedRoutingLogic(1 second) broadcast get the first answer and drop the rest
    6. TailChoppingRoutingLogic() forward to each one sequentially until answer is recieved and the rest is discarded
    7. ConsistentHashingRoutingLogic() all messages with the same hash goes to the same actor
    * */
    private val router = Router(RoundRobinRoutingLogic(), slaves)

    override def receive: Receive = {
      case msg => router route (msg, sender())
      case Terminated(ref) =>
        router.removeRoutee(ref)
        val slave = context.actorOf(Props[Slave], s"slave-${ref.path.name.split("-")(1)}")
        context.watch(slave)
        router.addRoutee(ActorRefRoutee(slave))
    }
  }

  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case msg => log.info(msg.toString)
    }
  }


  val system = ActorSystem("system")

  val router = system.actorOf(Props[Master])

  for (i <- 1 to 20) router ! s"message $i"

}
