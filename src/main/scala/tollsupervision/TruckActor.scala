package tollsupervision

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object TruckActor {

	def apply(nameplate: String): Behavior[String] = Behaviors.receiveMessage {
		message =>
			println(s"$nameplate received message $message")
			Behaviors.same
	}
}
