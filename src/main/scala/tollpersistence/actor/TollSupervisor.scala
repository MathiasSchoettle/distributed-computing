package tollpersistence.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import tollpersistence.actor.highway.RegionManagerActor
import tollpersistence.actor.truck.ChargingManagerActor

object TollSupervisor {
	def apply(): Behavior[String] = Behaviors.setup {
		context => {
			context.spawn(DeadLetterReaderActor(), "deadletter-reader")
			context.spawn(ChargingManagerActor(), "charging-manager")

			val regionManager = context.spawn(RegionManagerActor("region-manager"), "region-manager")
			Behaviors.receiveMessage {
				msg =>
					regionManager ! msg
					Behaviors.same
			}
		}
	}
}
