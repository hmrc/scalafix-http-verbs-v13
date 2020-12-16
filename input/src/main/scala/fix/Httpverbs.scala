/*
rule = Httpverbs
*/
package fix

import uk.gov.hmrc.http.logging.{Authorization, ForwardedFor, RequestChain, RequestId, SessionId}

object Httpverbs {

  def myMethod() = {
    val auth = Authorization("authorization")
    val forwardedFor = ForwardedFor("forwardedFor")
    val requestChain = RequestChain("requestChain")
    val requestId = RequestId("requestId")
    val sessionId = SessionId("sessionId")

    "" + auth + forwardedFor + requestChain + requestId + sessionId
  }

}
