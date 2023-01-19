package tollpersistence.actor.truck

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

import java.util.Date

object TruckActor {
	final case class Invoice(startedAt: String, exitedAt: String, km: Double, timestamp: Date)
	final case class TruckState(invoices: List[Invoice], start: String, km: Double)

	sealed trait TruckActorRequest
	case class RouteStart(position: String) extends TruckActorRequest
	case class RouteUsage(position: String, km: Double) extends TruckActorRequest
	case class RouteEnd(position: String, km: Double) extends TruckActorRequest

	sealed trait TruckActorEvent
	case class RouteStartEvent(position: String) extends TruckActorEvent
	case class RouteUsageEvent(position: String, km: Double) extends TruckActorEvent
	case class RouteEndEvent(position: String, km: Double) extends TruckActorEvent

	def apply(truckId: String): Behavior[TruckActorRequest] = {
		EventSourcedBehavior[TruckActorRequest, TruckActorEvent, TruckState] (
			persistenceId = PersistenceId.ofUniqueId(truckId),
			emptyState = TruckState(List(), "", 0.0),
			commandHandler = onReceiveCommand,
			eventHandler = onReceiveEvent
		).snapshotWhen {
			case (_, RouteEndEvent(_, _), _) => true
			case _ => false
		}
	}

	private def onReceiveCommand(state: TruckState, command: TruckActorRequest): Effect[TruckActorEvent, TruckState] = {
		command match {
			case RouteStart(pos) =>
				Effect.persist(RouteStartEvent(pos))
			case RouteUsage(pos, km) =>
				Effect.persist(RouteUsageEvent(pos, km))
			case RouteEnd(pos, km) =>
				Effect.persist(RouteEndEvent(pos, km)).thenRun(state => println(s"\n\n${state.invoices}\n\n"))
		}
	}

	private def onReceiveEvent(state: TruckState, event: TruckActorEvent): TruckState = {
		event match {
			case RouteStartEvent(pos) =>
				state.copy(start = pos)
			case RouteUsageEvent(_, kmDriven) =>
				state.copy(km = state.km + kmDriven)
			case RouteEndEvent(endPos, kmDriven) =>
				val invoice = Invoice(state.start, endPos, state.km + kmDriven, new Date())
				state.copy(invoices = state.invoices :+ invoice)
		}
	}
}
