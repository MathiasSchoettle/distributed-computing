package webshop

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import webshop.Entity.{Item, User}

object BasketActor {
	sealed trait BasketActorRequest
	case class AddItemToBasket(user: User, item: Item) extends BasketActorRequest
	case class GetAllItems(user: User, ref: ActorRef[BasketActorResponse]) extends BasketActorRequest

	sealed trait BasketActorResponse
	case class AllItemsForUser(items: List[Item], user: User) extends BasketActorResponse

	def apply(): Behavior[BasketActorRequest] = basketReceive(Map())

	private def basketReceive(baskets: Map[String, List[Item]]): Behavior[BasketActorRequest] = Behaviors.receiveMessage {
		case AddItemToBasket(user, item) =>
			val list = baskets.getOrElse(user.id, List())
			basketReceive(baskets + (user.id -> (list :+ item)))
		case GetAllItems(user, ref) =>
			val list = baskets.getOrElse(user.id, List())
			ref ! AllItemsForUser(list, user)
			basketReceive(baskets)
	}
}
