package sieveEratosthenes

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Prime {

	case class PrimeMessage(counterRef: ActorRef[Counter.CounterMessage], values: List[Int])

	def apply(): Behavior[PrimeMessage] = {
		Behaviors.receive(
			(context, message) => {

				val head = message.values.head
				var newList: List[Int] = List()

				if (head == 1) {
					newList = message.values.tail
				}
				else {
					newList = message.values.filter(_ % head != 0)
				}

				if (newList.nonEmpty) {
					val nextRef: ActorRef[PrimeMessage] = context.spawn(Prime(), s"$head")
					nextRef ! PrimeMessage(message.counterRef, newList)
					message.counterRef ! Counter.Increment
				}
				else {
					message.counterRef ! Counter.Print
				}

				Behaviors.ignore
			}
		)
	}
}
