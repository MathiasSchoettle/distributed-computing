package api

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import api.Domain.Message

object GroupActor {

	sealed trait GroupActorCommand

	final case class SendMessage(message: Message) extends GroupActorCommand

	final case class Test(message: Message, replyTo: ActorRef[TestReply]) extends GroupActorCommand

	final case class AddUserAgent(forUsername: String, actorRef: ActorRef[GroupActorReply]) extends GroupActorCommand

	final case object UserAgentLoggedOff extends GroupActorCommand

	final case class UserAgentError(throwable: Throwable) extends GroupActorCommand

	sealed trait GroupActorReply

	final case class TestReply(message: Message) extends GroupActorReply

	final case class NewMessageAvailable(message: Message) extends GroupActorReply

	def apply(): Behavior[GroupActorCommand] = changeState(Nil, Nil)

	private def changeState(messages: List[Message], subscribers: List[ActorRef[GroupActorReply]]): Behavior[GroupActorCommand] = Behaviors.receiveMessage {
		case Test(message, replyTo) =>
			println(s"Test($message) --> will reply to ${replyTo.path}")
			replyTo ! TestReply(message.copy(text = s"Sent me: ${message.text}"))
			Behaviors.same
		case SendMessage(message) =>
			println(s"Sending message $message to all subscribers")
			for (subscriber <- subscribers) {
				subscriber ! NewMessageAvailable(message)
			}
			Behaviors.same
		case AddUserAgent(forUsername, actorRef) =>
			println(s"Add agent $forUsername")
			changeState(messages, subscribers :+ actorRef)
		case UserAgentLoggedOff =>
			println(s"Some user agent logged off -> if I knew who it is, I would remove the subscriber")
			Behaviors.same
		case UserAgentError(ex) =>
			println(s"Some user agent had a problem: ${ex.getMessage}")
			Behaviors.same
	}
}
