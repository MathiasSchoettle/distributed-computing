package tollsupervision

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object TollSupervisor {
	def apply(): Behavior[String] = Behaviors.setup {
		context =>
		  	context.spawn(DeadLetterActor(), "dead-letter")
			context.spawn(PaymentActor(), "payment")
			val regionManager = context.spawn(RegionManager("south"), "south")

			Behaviors.receiveMessage {
				msg => regionManager ! msg
				Behaviors.same
			}
	}
}
