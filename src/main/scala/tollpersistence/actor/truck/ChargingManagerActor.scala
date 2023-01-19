package tollpersistence.actor.truck

import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors

import scala.util.Random

object ChargingManagerActor {

	val ChargingManagerServiceKey: ServiceKey[RouteUsageEvent] = ServiceKey[RouteUsageEvent]("charging-manager")

	final case class RouteUsageEvent(sensorReading: String)

	def apply(): Behavior[RouteUsageEvent] = Behaviors.setup[RouteUsageEvent] {
		context => {

			// At startup, register this actor ("service") with Receptionist so other actors can subscribe and send messages
			context.system.receptionist ! Receptionist.Register(ChargingManagerServiceKey, context.self)
			println(s"ChargingManagerActor Register with Receptionist")

			Behaviors.receive[RouteUsageEvent] {

				case (context, RouteUsageEvent(sensorReading)) =>
					val msgParts = sensorReading.split("-") // example message: "sensor-A93-1-R AB 123-enter"
					val truckId = msgParts(3) // e.g. "R AB 123"
					val readingType = msgParts(4) // e.g. "enter" | "usage" | "exit"

					// check whether truck is already in list of own children (already spawned?)
					val truckActor = context.child(s"truck-$truckId") match {
						case Some(childRef) => childRef.unsafeUpcast[TruckActor.TruckActorRequest]
						case None => context.spawn(TruckActor(truckId), s"truck-$truckId")
					}

					// send message to dedicated truck actor
					// usage and end contain a random number of kilometers that was used for this stretch of the route (between 0.0 and 10.0)
					readingType match {
						case "enter" => truckActor ! TruckActor.RouteStart(sensorReading)
						case "usage" => truckActor ! TruckActor.RouteUsage(sensorReading, new Random().nextDouble() * 10.0)
						case "exit" => truckActor ! TruckActor.RouteEnd(sensorReading, new Random().nextDouble() * 10.0)
					}

					Behaviors.same
			}
		}
	}
}
