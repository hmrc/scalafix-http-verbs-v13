package fix

import akka.actor.ActorSystem
import com.typesafe.config.Config
import uk.gov.hmrc.http.Retries
import com.typesafe.config.ConfigFactory

object OptionalConfig {
  val retries: Retries = new Retries {
    override protected val configuration: Config = ConfigFactory.load()
    override val actorSystem: ActorSystem = ActorSystem("test-actor-system")
  }
}
