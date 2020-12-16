package fix

import scalafix.v1._
import scala.meta._

class Httpverbs extends SemanticRule("Httpverbs") {

  override def fix(implicit doc: SemanticDocument): Patch = {
    Patch.replaceSymbols(
      "uk.gov.hmrc.http.logging.Authorization" -> "uk.gov.hmrc.http.Authorization",
      "uk.gov.hmrc.http.logging.ForwardedFor" -> "uk.gov.hmrc.http.ForwardedFor",
      "uk.gov.hmrc.http.logging.RequestChain" -> "uk.gov.hmrc.http.RequestChain",
      "uk.gov.hmrc.http.logging.RequestId" -> "uk.gov.hmrc.http.RequestId",
      "uk.gov.hmrc.http.logging.SessionId" -> "uk.gov.hmrc.http.SessionId"
    )
  }

}
