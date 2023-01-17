package webshop

object Entity {
	final case class Product(name: String, price: BigDecimal)
	final case class Item(product: Product, quantity: Int)
	final case class User(id: String, name: String)
	final case class Payment(amount: BigDecimal, accountId: String, reference: String)
	final case class Invoice(recipient: String, amount: BigDecimal)
}
