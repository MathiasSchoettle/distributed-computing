package tollsupervision

import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors

object RegionManager {

	def apply(name: String): Behavior[String] = Behaviors.setup[String] {
		context =>
			val a3Manager = context.spawn(Behaviors.supervise(HighwayManager("A3")).onFailure(SupervisorStrategy.restart.withStopChildren(false)), "A3")
			val a9Manager = context.spawn(Behaviors.supervise(HighwayManager("A9")).onFailure(SupervisorStrategy.restart.withStopChildren(false)), "A9")

			Behaviors.receiveMessagePartial[String] {
				case msg if msg.contains("A3") =>
					a3Manager ! msg
					Behaviors.same
				case msg if msg.contains("A9") =>
					a9Manager ! msg
					Behaviors.same
			}.receiveSignal {
				case (context, signal) =>
					println(s"Signal received: name=$name signal=$signal context.self=${context.self}")
					Behaviors.same
			}
	}
}
