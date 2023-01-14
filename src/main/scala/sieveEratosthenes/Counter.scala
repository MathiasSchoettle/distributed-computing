package sieveEratosthenes

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Counter {
	sealed trait CounterMessage
	case object Increment extends CounterMessage
	case object Print extends CounterMessage

	def apply() : Behavior[CounterMessage] = createBehaviour(0)
	private def createBehaviour(count: Int): Behavior[CounterMessage] = {
		Behaviors.receiveMessage {
			case Increment => createBehaviour(count + 1)
			case Print =>
				println(count)
				Behaviors.same
		}
	}
}
