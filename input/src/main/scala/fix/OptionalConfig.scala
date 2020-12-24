/*
rule = OptionalConfig
*/
package fix

import akka.actor.ActorSystem
import com.typesafe.config.Config
import play.api.Configuration
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.http.{HttpGet, Retries}
import uk.gov.hmrc.play.http.ws.WSGet

object OptionalConfig {
  val retries: Retries = new Retries {
    override protected val configuration: Option[Config] = None
    override val actorSystem: ActorSystem = ActorSystem("test-actor-system")
  }

  val retries2: Retries = new Retries {
    override protected val configuration: Option[Config] =
      Some(
        Configuration(
          "http-verbs.retries.intervals" -> "[ 100 ms, 200 ms, 1 s]",
        ).underlying
      )
    override val actorSystem: ActorSystem = ActorSystem("test-actor-system")
  }

  // same again, but with def instead of val
  val retries3: Retries = new Retries {
    override protected def configuration: Option[Config] = None
    override val actorSystem: ActorSystem = ActorSystem("test-actor-system")
  }

  val retries4: Retries = new Retries {
    override protected def configuration: Option[Config] =
      Some(
        Configuration(
          "http-verbs.retries.intervals" -> "[ 100 ms, 200 ms, 1 s]",
        ).underlying
      )
    override val actorSystem: ActorSystem = ActorSystem("test-actor-system")
  }

  // checking the WSRequestBuilder change too
  class MyClass(config: Configuration,
                val wsClient: WSClient,
                val actorSystem: ActorSystem) extends HttpGet with WSGet {
    override val hooks: Seq[HttpHook] = Seq.empty

    override protected def configuration: Option[Config] = Some(config.underlying)
  }
}
