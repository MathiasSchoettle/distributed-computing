package webshop

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import webshop.Entity.{Invoice, Payment}

object PaymentActor {
	sealed trait PaymentActorRequest
	case class CollectPayment(payment: Payment, ref: ActorRef[PaymentActorResponse]) extends PaymentActorRequest

	sealed trait PaymentActorResponse
	case class PaymentSuccess(invoice: Invoice) extends PaymentActorResponse
	case class PaymentFailure(reason: String) extends PaymentActorResponse

	def apply(): Behavior[PaymentActorRequest] = Behaviors.receiveMessage {
		case CollectPayment(payment, ref) =>
			if (payment.amount > 1000) {
				ref ! PaymentFailure("Amount to large")
			}
			else {
				val invoice = Invoice(payment.reference, payment.amount)
				ref ! PaymentSuccess(invoice)
			}
			Behaviors.same
	}
}
