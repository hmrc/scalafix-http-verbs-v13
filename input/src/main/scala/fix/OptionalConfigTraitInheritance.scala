/*
rule = OptionalConfig
*/
package fix

import akka.actor.ActorSystem
import com.typesafe.config.Config
import uk.gov.hmrc.http.Retries

object OptionalConfigTraitInheritance {
  trait MyTrait extends Retries
  trait MyOtherTrait

  val retries: MyTrait = new MyTrait {
    override val configuration: Option[Config] = None
    override val actorSystem: ActorSystem = ActorSystem("test-actor-system")
  }

  val notRetries = new MyOtherTrait {
    // this configuration should be untouched, as it doesn't extend from our modified traits.
    val configuration: Option[Config] = None
    println(configuration)
  }
}
