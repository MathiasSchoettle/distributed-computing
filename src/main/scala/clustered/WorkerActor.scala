package clustered

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object WorkerActor {
	case class WorkerMessage(msg: String)
	def apply(): Behavior[WorkerMessage] = Behaviors.receiveMessage {
		case WorkerMessage(msg) =>
			println(msg)
			Behaviors.same
	}
}
