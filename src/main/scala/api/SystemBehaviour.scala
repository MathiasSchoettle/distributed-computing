package api

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object SystemBehaviour {
	def apply(): Behavior[GroupActor.GroupActorCommand] = Behaviors.setup {
		context =>
			val oneGroupForAll = context.spawn(GroupActor(), "one-for-all-group-actor")
			Behaviors.receiveMessage[GroupActor.GroupActorCommand] {
				message => {
					oneGroupForAll ! message
					Behaviors.same
				}
			}
	}
}
