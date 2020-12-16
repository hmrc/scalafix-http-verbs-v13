/*
rule = OptionalConfig
*/
package fix

import akka.actor.ActorSystem
import com.typesafe.config.Config
import uk.gov.hmrc.http.Retries

object OptionalConfig {
  val retries: Retries = new Retries {
    override protected val configuration: Option[Config] = None
    override val actorSystem: ActorSystem = ActorSystem("test-actor-system")
  }
}
