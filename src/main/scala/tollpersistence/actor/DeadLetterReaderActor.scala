package tollpersistence.actor

import akka.actor.DeadLetter
import akka.actor.typed.Behavior
import akka.actor.typed.eventstream.EventStream
import akka.actor.typed.scaladsl.Behaviors

object DeadLetterReaderActor {
	def apply(): Behavior[DeadLetter] = Behaviors.setup[DeadLetter] {
		context => {
			context.system.eventStream ! EventStream.Subscribe(context.self)
			Behaviors.receiveMessage {
				msg => {
					println(msg)
					Behaviors.same
				}
			}
		}
	}
}
