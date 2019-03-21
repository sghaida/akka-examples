package com.sghaida.akka.actors.patterns

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object AskPattern {

  case class Read(key: String)
  case class Write(key: String, value: String )
  class KVActor extends Actor with ActorLogging {

    def online(kv: Map[String, String]): Receive = {
      case Read(key) =>
        log.info(s"reading value for key: $key")
        sender() ! kv.get(key)
      case Write(key, value) =>
        log.info(s"writing $key -> $value")
        context.become(online(kv + (key -> value)))
    }

    override def receive: Receive = online(Map())
  }

  case class RegisterUser(username: String, passwor: String)
  case class Authenticate(username: String, passwor: String)
  case class AuthFailure(msg: String)
  case object AuthSuccess


  object AuthManager {

    val props: Props = Props[KVActor]

    val USER_NOT_FOUND = "username not found"
    val PASS_INCORRECT = "password incorrect"
    val UNKNOWN_ERROR  = "unknown error"
  }

  class AuthManager extends Actor with ActorLogging {

    import AuthManager._
    protected val authStore: ActorRef = context.actorOf(props, "auth-store")
    implicit val timout: Timeout = 1 second
    implicit val excutionContext: ExecutionContextExecutor = context.dispatcher

    override def receive: Receive = {
      case RegisterUser(username, password) => authStore ! Write(username, password)
      case Authenticate(username, password) => handleAuth(username, password)



    }

    def handleAuth(username: String, password: String): Unit = {

      val orginalSender = sender()
      /* ask runs on seperate thread, and onComplete also runs on separate thread
      * also the sender() if it has been used for the onComplete is authStore not the original sender
      * which breaks Actor Encapsulation
      * NEVER call methods on the actor instance or access mutable state onComplete
      * */
      authStore ? Read(username) onComplete{
        case Success(None) => orginalSender ! AuthFailure(USER_NOT_FOUND)
        case Success(Some(pass)) =>
          if (pass == password) orginalSender ! AuthSuccess
          else orginalSender ! AuthFailure(PASS_INCORRECT)
        case Failure(ex) => orginalSender ! AuthFailure(UNKNOWN_ERROR)
      }
    }
  }

  /* cleaner without onComplete callbacks*/
  class PipedAuthManager extends AuthManager {
    import AuthManager._
    override def handleAuth(username: String, password: String): Unit = {
      (authStore ? Read(username) ).mapTo[Option[String]].map{
        case None => AuthFailure(USER_NOT_FOUND)
        case Some(pass) =>
          if (pass == password) AuthSuccess
          else AuthFailure(PASS_INCORRECT)
      }.pipeTo(sender())// Future[Any]

    }
  }

}
