/*
rule = OptionalConfig
*/
package fix

import akka.actor.ActorSystem
import com.typesafe.config.Config
import play.api.Configuration
import uk.gov.hmrc.http.Retries

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
}
