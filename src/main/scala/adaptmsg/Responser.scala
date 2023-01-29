package adaptmsg

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Responser {
	case class Request(ref: ActorRef[Response])
	case class Response(text: String)
	def apply(): Behavior[Request] = Behaviors.receiveMessage {
		case Request(ref) =>
			ref ! Response("hello")
			Behaviors.same
	}
}
