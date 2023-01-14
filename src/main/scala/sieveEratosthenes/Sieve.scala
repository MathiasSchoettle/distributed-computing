package sieveEratosthenes

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

import scala.language.postfixOps

object Sieve extends App {
	ActorSystem(Sieve(1 to 1000 toList), "erastosthenes")
	def apply(startRange: List[Int]): Behavior[Any] = Behaviors.setup(
		context => {
			val counterRef: ActorRef[Counter.CounterMessage] = context.spawn(Counter(), "counter")
			val primeRef: ActorRef[Prime.PrimeMessage] = context.spawn(Prime(), "first")

			primeRef ! Prime.PrimeMessage(counterRef, startRange)

			Behaviors.ignore
		}
	)
}
