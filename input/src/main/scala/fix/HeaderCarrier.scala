/*
rule = HeaderCarrier
*/
package fix

import javax.inject.Inject
import play.api.http.Status._
import play.api.libs.json._
import play.api.Logger
import play.api.mvc._
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import play.api.{Configuration, Logging}
import uk.gov.hmrc.auth.core.AuthorisationException
import uk.gov.hmrc.http.{HeaderCarrier, BadRequestException, Upstream5xxResponse, Upstream4xxResponse, JsValidationException, NotFoundException}
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.backend.http.JsonErrorHandler
import uk.gov.hmrc.play.bootstrap.config.HttpAuditEvent

import scala.concurrent.{ExecutionContext, Future}

case class ErrorResponse(code: String, message: String, details: Option[Map[String, String]] = None)
object ErrorResponse {
  implicit val writes: OWrites[ErrorResponse] = Json.writes[ErrorResponse]
}

class ErrorHandlerOverride @Inject() (
                                       configuration:  Configuration,
                                       auditConnector: AuditConnector,
                                       httpAuditEvent: HttpAuditEvent
                                     )(implicit ec: ExecutionContext) extends JsonErrorHandler(auditConnector, httpAuditEvent, configuration) with Logging {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {

    implicit val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSessionAndRequest(request.headers, request = Some(request))

    statusCode match {
      case play.mvc.Http.Status.NOT_FOUND =>
        auditConnector.sendEvent(httpAuditEvent.dataEvent("ResourceNotFound", "Resource Endpoint Not Found", request))
        Future.successful(
          NotFound(Json.toJson(ErrorResponse("NOT_FOUND", "URI not found", Some(Map("requestedUrl" → request.path)))))
        )
      case _ ⇒ super.onClientError(request, statusCode, message)
    }
  }

  override def onServerError(request: RequestHeader, ex: Throwable): Future[Result] = {
    implicit val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSessionAndRequest(/*headers =*/ request.headers, request = Some(request))

    logger.error(s"! Internal server error, for (${request.method}) [${request.uri}] -> ", ex)

    val code = ex match {
      case _: NotFoundException      => "ResourceNotFound"
      case _: AuthorisationException => "ClientError"
      case _: JsValidationException  => "ServerValidationError"
      case _                         => "ServerInternalError"
    }

    auditConnector.sendEvent(
      httpAuditEvent.dataEvent(code, "Unexpected error", request, Map("transactionFailureReason" -> ex.getMessage)))
    Future.successful(resolveError(ex))
  }

  private def resolveError(ex: Throwable): Result = {
    val (statusCode, code, message) = ex match {
      case Upstream4xxResponse(message, _, _, _) => (BAD_GATEWAY, "UPSTREAM_ERROR", message)
      case Upstream5xxResponse(message, _, _, _) => (BAD_GATEWAY, "UPSTREAM_ERROR", message)
      case e: Throwable                          => (INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", e.getMessage)
    }

    new Status(statusCode)(Json.toJson(ErrorResponse(code, message)))
  }
}

class BaseApiController @Inject()(cc: ControllerComponents)
  extends BackendController(cc) {

  protected val logger: Logger = play.api.Logger(this.getClass)

  override implicit def hc(implicit rh: RequestHeader): HeaderCarrier =
    HeaderCarrierConverter.fromHeadersAndSessionAndRequest(rh.headers, request = Some(rh))

  def withValidJson[T](f: T => Future[Result])(implicit ec: ExecutionContext,
                                               hc: HeaderCarrier,
                                               request: Request[JsValue],
                                               r: Reads[T]): Future[Result] =
    request.body.validate[T] match {
      case JsSuccess(t, _) => f(t)
      case JsError(errors) =>
        Future.failed(new BadRequestException(errors.toString()))
    }
}