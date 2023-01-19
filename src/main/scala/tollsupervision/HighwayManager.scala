package tollsupervision

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object HighwayManager {

	def apply(name: String): Behavior[String] = Behaviors.receivePartial[String]{
		case (_, message) if message.startsWith("fail") =>
			throw new RuntimeException(s"Wanted to fail for $name")
		case (context, message) =>
			val sensorId = message.split("-")(2)
			val sensorActor = context.child(sensorId) match {
				case Some(actor) => actor.unsafeUpcast[Sensor.SensorMessage]
				case None => context.spawn(Sensor(sensorId), sensorId)
			}
			sensorActor ! Sensor.SensorReading(message)
			Behaviors.same
	}.receiveSignal {
		case (context, signal) =>
			println(s"Signal received: forHighway=$name signal=$signal context.self=${context.self}")
			Behaviors.same
	}
}
