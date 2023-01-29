package adaptmsg

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Requester {
	sealed trait Requesters
	final case class Request() extends Requesters
	final case class Response(name: String) extends Requesters
	def apply(): Behavior[Requesters] = Behaviors.setup {
		context =>
		  	// we pass first adapter, but behaviour of second adapter is executed
			val adapter: ActorRef[Responser.Response] = context.messageAdapter(rsp => Response(rsp.text + " adapted"))
			val adapter2: ActorRef[Responser.Response] = context.messageAdapter(rsp => Response(rsp.text + " intercepted"))

			val responser = context.spawn(Responser(), "responser")

			Behaviors.receiveMessage {
				case Request() =>
					responser ! Responser.Request(adapter)
					Behaviors.same
				case Response(name) =>
					println(name)
					Behaviors.same
			}
	}
}
