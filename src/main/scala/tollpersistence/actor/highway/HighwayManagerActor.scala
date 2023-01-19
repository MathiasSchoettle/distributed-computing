package tollpersistence.actor.highway

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object HighwayManagerActor {
	def apply(highway: String): Behavior[String] = Behaviors.receivePartial[String] {

		case (context, msg) if msg.contains("fail") =>
			println(s"throwing new IllegalStateException in ${context.self}")
			throw new IllegalStateException(s"something is wrong with this highway $highway")

		case (context, msg) =>
			val sensorId = msg.split("-")(2) // "sensor-A9-2-R AB 123" or "hello-A9-1"
			val sensorName = s"$highway-$sensorId"
			val sensorActor = context.child(sensorName) match {
				case Some(actorRef) => actorRef.unsafeUpcast[SensorActor.SensorRequest]
				case None => context.spawn(SensorActor(sensorName), name = sensorName)
			}
			sensorActor ! SensorActor.SensorMessage(msg)
			Behaviors.same

	}.receiveSignal {
		case (context, signal) =>
			println(s"Signal received: forHighway=$highway signal=$signal context.self=${context.self}")
			Behaviors.same
	}
}
