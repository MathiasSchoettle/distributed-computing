package tollpersistence.actor.highway

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors
import tollpersistence.actor.truck.ChargingManagerActor

object SensorActor {
	sealed trait SensorRequest

	final case class SensorMessage(msg: String) extends SensorRequest

	final case class OnListing(listing: Receptionist.Listing) extends SensorRequest

	// initial behavior is only subscribing for charging-manager actor ref and
	// stashing (storing) all messages for later, once the charging-manager actor ref is available
	def apply(sensorName: String): Behavior[SensorRequest] = Behaviors.withStash[SensorRequest](100) {
		stashBuffer => {

			Behaviors.setup[SensorRequest] {
				context => {
					// Cf. pattern "adapted message": Receptionist replies with Listing message but has to be converted to own OnListing type
					val adapter = context.messageAdapter((listing: Receptionist.Listing) => OnListing(listing))

					// Subsribe for charging-manager actor ref
					context.system.receptionist ! Receptionist.Subscribe(ChargingManagerActor.ChargingManagerServiceKey, adapter)

					Behaviors.receiveMessagePartial[SensorRequest] {
						// OnListing is sent (via adapter) from Receptionist: listing contains set of actor ref(s) for charging-manager actor(s)
						case OnListing(listing) =>
							println(s"service references now available for $sensorName; unstashing stashed messages now: $listing")
							// unstash all previously stashed messages with following behavior (behavior returned here by private def "ready")
							stashBuffer.unstashAll(
								ready(sensorName, listing.serviceInstances(ChargingManagerActor.ChargingManagerServiceKey))
							)
						case other =>
							println(s"stashing message for later: msg=$other")
							stashBuffer.stash(other)
							Behaviors.same
					}
				}
			}

		}
	}

	// "ready" state/behavior; after charging-manager ref(s) is/are available
	def ready(sensorName: String, chargingActors: Set[ActorRef[ChargingManagerActor.RouteUsageEvent]]): Behavior[SensorRequest] =
		Behaviors.receivePartial[SensorRequest] {

			case (ctx, SensorMessage(msg)) if msg.contains("sensor") =>
				// looping here, is could be possible that more services with the same key were registered
				for (chargingActor <- chargingActors)
					chargingActor ! ChargingManagerActor.RouteUsageEvent(msg)
				println(s"Message received by SensorActor $sensorName: msg=$msg context.self=${ctx.self}")
				Behaviors.same

			case (ctx, SensorMessage(msg)) =>
				println(s"Message received by SensorActor $sensorName: msg=$msg context.self=${ctx.self}")
				Behaviors.same

			// case OnListing(listing) => ... // if you want to get updates, in case a new service registered

		}.receiveSignal {
			case (context, signal) =>
				println(s"Signal received: name=$sensorName signal=$signal context.self=${context.self}")
				Behaviors.same
		}
}
