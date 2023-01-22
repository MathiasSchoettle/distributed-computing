package api

import akka.actor
import akka.actor.typed.{ActorSystem, Scheduler}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import api.Domain.Message
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import akka.actor.typed.scaladsl.AskPattern.Askable
import api.GroupActor.TestReply
import api.grpc.MessagingServiceHandler

import scala.util.{Failure, Success}

object StartNode extends App with SprayJsonSupport with DefaultJsonProtocol {

	val system: ActorSystem[GroupActor.GroupActorCommand] = ActorSystem(SystemBehaviour(), "api-system")
	implicit val executionContext: ExecutionContextExecutor = system.executionContext
	implicit val scheduler: Scheduler = system.scheduler
	implicit val timeout: Timeout = Timeout(5 seconds)

	implicit val messageJsonFormat: RootJsonFormat[Message] = jsonFormat3(Message)

	private val routes = concat (
		path("messages") {
			post {
				entity(as[Message]) {
					message => {
						val result: Future[TestReply] = system.ask(ref => GroupActor.Test(message, ref))
						onSuccess(result) {
							case TestReply(message) => complete(message)
							case _ => complete(StatusCodes.InternalServerError, "got wrong reply")
						}
					}
				}
			}
		}
	)

	implicit val materializer: actor.ActorSystem = system.classicSystem

	Http().newServerAt("localhost", 8000).bind(routes)

	val service: HttpRequest => Future[HttpResponse] = MessagingServiceHandler.withServerReflection(new MessagingServiceImpl(system))

	private val bound: Future[Http.ServerBinding] =
		Http(system)
		  .newServerAt("localhost", 9000)
		  .bind(service)
		  .map(_.addToCoordinatedShutdown(5 seconds))

	bound.onComplete {
		case Success(binding) =>
			val localAddress = binding.localAddress
			println(s"gRPC server running on ${localAddress.getHostName}:${localAddress.getPort}")
		case Failure(ex) =>
			println(s"gRPC server failed to bind with exception: ${ex.getMessage}")
			system.terminate()
	}
}
