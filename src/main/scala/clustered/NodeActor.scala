package clustered

import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{Behaviors, Routers}

object NodeActor {
	val serviceKey: ServiceKey[WorkerActor.WorkerMessage] = ServiceKey[WorkerActor.WorkerMessage]("worker")
	def apply(amount: Int): Behavior[WorkerActor.WorkerMessage] = Behaviors.setup {
		context =>
		  	context.system.receptionist ! Receptionist.Register(serviceKey, context.self)
			println(s"Created Pool Worker Router with $amount workers (${context.self.path})")
			Routers.pool(amount)(WorkerActor()).withRoundRobinRouting()
	}
}
