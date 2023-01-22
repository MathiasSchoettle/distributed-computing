package api

object Domain {
	final case class Message(id: String, text: String, senderId: String)
	final case class User(name: String, password: String)
}
