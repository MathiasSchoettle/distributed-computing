package tollpersistence.actor.highway

import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors

import scala.language.postfixOps

object RegionManagerActor {
	def apply(name: String): Behavior[String] = Behaviors.setup[String] {
		context => {
			// for test reasons only two highways here
			val a3 = context.spawn(supervise(HighwayManagerActor("A3")), name = "A3")
			val a93 = context.spawn(supervise(HighwayManagerActor("A93")), name = "A93")

			Behaviors.receiveMessagePartial[String] {
				case msg if msg.contains("A93") =>
					a93 ! msg
					Behaviors.same
				case msg =>
					a3 ! msg
					Behaviors.same
			}.receiveSignal {
				case (context, signal) =>
					println(s"Signal received: name=$name signal=$signal context.self=${context.self}")
					Behaviors.same
			}
		}
	}

	// wrap child behavior with (parent) supervisor behavior
	private def supervise[T](behavior: Behavior[T]): Behavior[T] = {
		Behaviors.supervise(behavior)
		  .onFailure(SupervisorStrategy
			.restart
			.withStopChildren(false)
			  //.withLimit(1, 1 seconds)
		  )
	}
}
