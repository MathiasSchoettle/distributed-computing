package webshop

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import webshop.Entity.{Invoice, Item, Payment, User}
import webshop.PaymentActor.{PaymentFailure, PaymentSuccess}

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Success

object WebshopActor {
	sealed trait WorkshopRequest
	final case class AddItemToBasket(user: User, item: Item) extends WorkshopRequest
	final case class MakeOrder(user: User) extends WorkshopRequest
	final case class GetPayment(items: List[Item], user: User) extends WorkshopRequest
	final case class OrderFail(reason: String) extends WorkshopRequest
	final case class OrderSuccess(invoice: Invoice) extends WorkshopRequest


	implicit val timeout: Timeout = Timeout(5 seconds)

	def apply(): Behavior[WorkshopRequest] = Behaviors.setup {
		context =>
			val basketActor = context.spawn(BasketActor(), "basket-actor")
			val paymentActor = context.spawn(PaymentActor(), "payment-actor")

			Behaviors.receiveMessage[WorkshopRequest] {
				case AddItemToBasket(user, item) =>
					basketActor ! BasketActor.AddItemToBasket(user, item)
					Behaviors.same
				case MakeOrder(user) =>
					context.ask(basketActor, mediator => BasketActor.GetAllItems(user, mediator)) {
						case Success(BasketActor.AllItemsForUser(items, user)) =>
							GetPayment(items, user)
					}
					Behaviors.same
				case GetPayment(items, user) =>
					val amount = items.map(i => i.product.price * i.quantity).sum
					val payment = Payment(amount, user.id, user.name)
					context.ask(paymentActor, mediator => PaymentActor.CollectPayment(payment, mediator)) {
						case Success(PaymentSuccess(invoice)) =>
							OrderSuccess(invoice)
						case Success(PaymentFailure(reason)) =>
							OrderFail(reason)
					}
					Behaviors.same
				case OrderSuccess(invoice) =>
					println(s"${invoice.amount} - ${invoice.recipient}")
					Behaviors.same
				case OrderFail(reason) =>
					println(reason)
					Behaviors.same
			}
	}
}
