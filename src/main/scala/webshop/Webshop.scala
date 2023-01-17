package webshop

import akka.actor.typed.ActorSystem
import webshop.Entity.{Item, Product, User}
import webshop.WebshopActor.{AddItemToBasket, MakeOrder}

object Webshop extends App {

	private val system = ActorSystem(WebshopActor(), "webshop")

	var user = User("1", "Mathias")
	system ! AddItemToBasket(user, Item(Product("Computer", BigDecimal("500")), 1))
	system ! AddItemToBasket(user, Item(Product("Underwear", BigDecimal("20")), 4))
	system ! AddItemToBasket(user, Item(Product("Coffee", BigDecimal("35")), 2))
	system ! MakeOrder(user)

	user = User("2", "Margarete")
	system ! AddItemToBasket(user, Item(Product("Computer", BigDecimal("500")), 5))
	system ! MakeOrder(user)
}
