package tollsupervision

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors

object Sensor {
	sealed trait SensorMessage
	case class SensorReading(string: String) extends SensorMessage
	case class PaymentListing(listing: Receptionist.Listing) extends SensorMessage
	def apply(name: String): Behavior[SensorMessage] = Behaviors.withStash[SensorMessage](100) {
		stash => {
			Behaviors.setup[SensorMessage] {
				context =>
					val adapter = context.messageAdapter[Receptionist.Listing](msg => PaymentListing(msg))
					context.system.receptionist ! Receptionist.Subscribe(PaymentActor.providerKey, adapter)

					Behaviors.receiveMessage {
						case PaymentListing(listing) =>
							stash.unstashAll {
								ready(name, listing.serviceInstances(PaymentActor.providerKey).head)
							}
						case msg =>
							stash.stash(msg)
							Behaviors.same
					}
			}
		}
	}

	private def ready(name: String, paymentActor: ActorRef[String]): Behavior[SensorMessage] =
	Behaviors.receiveMessage[SensorMessage] {
		case SensorReading(string) if string.startsWith("hello") =>
			println(s"$name received message: $string")
			Behaviors.same
		case SensorReading(string) =>
			paymentActor ! string
			Behaviors.same
	}
}
