
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import org.scalatest.Matchers._
import com.sghaida.akka.actors.patterns.AskPattern._

class AskSpec extends TestKit(ActorSystem("system"))with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import AuthManager._

  def authenticatorTest(props: Props): Unit = {

    val auth = system.actorOf(props)

    "fail to authenticate unregistered users" in {
      auth ! Authenticate("xxx", "xxxx")
      expectMsg(AuthFailure(USER_NOT_FOUND))

    }

    "fail to authenticate if invalid password" in {
      auth ! RegisterUser("xxx", "xxxx")
      auth ! Authenticate("xxx", "123")
      expectMsg(AuthFailure(PASS_INCORRECT))

    }

    "success to authenticate " in {
      auth ! Authenticate("xxx", "xxxx")
      expectMsg(AuthSuccess)

    }
  }

  "ask authenticator" should {
    authenticatorTest(Props[AuthManager])
  }

  "piped authenticator" should {
    authenticatorTest(Props[PipedAuthManager])
  }
}