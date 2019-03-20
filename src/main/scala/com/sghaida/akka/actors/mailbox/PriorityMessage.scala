package com.sghaida.akka.actors.mailbox

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config

/* priority mailbox*/
/* class is going to be instantiated at runtime by reflection so arguments should be exact types */
case class PriorityMessage(setting: ActorSystem.Settings, config: Config)
  extends UnboundedPriorityMailbox(PriorityGenerator{
    case msg: String if msg.startsWith("1)")=> 0 //higher priority
    case msg: String if msg.startsWith("2)")=> 1
    case msg: String if msg.startsWith("3)")=> 2
    case _ => 4

  })
