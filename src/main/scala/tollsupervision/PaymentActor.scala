package tollsupervision

import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors

object PaymentActor {
	val providerKey: ServiceKey[String] = ServiceKey[String]("payment-provider")
	def apply(): Behavior[String] = Behaviors.setup {
		context =>
		  	context.system.receptionist ! Receptionist.Register(providerKey, context.self)

			Behaviors.receiveMessage {
				message =>
				  	val nameplate = message.split("-")(3).replace(" ", "-")

					val truckRef = context.child(nameplate) match {
						case Some(actor) => actor.unsafeUpcast[String]
						case None => context.spawn(TruckActor(nameplate), nameplate)
					}
					truckRef ! message
					Behaviors.same
			}
	}
}
