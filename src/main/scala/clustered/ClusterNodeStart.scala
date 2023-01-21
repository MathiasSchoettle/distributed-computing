package clustered

import akka.actor.typed.{ActorSystem, SupervisorStrategy}
import akka.actor.typed.scaladsl.{Behaviors, Routers}
import com.typesafe.config.ConfigFactory

object ClusterNodeStart extends App {
	private val config = ConfigFactory.load()
	private val port = config.getInt("akka.remote.artery.canonical.port")
	private val systemName = "my_system"

	println(s"starting actor system with port: $port")

	if (port == 2553) {
		val system = ActorSystem(Behaviors.supervise(Routers.group(NodeActor.serviceKey)).onFailure(SupervisorStrategy.restart), systemName)
		Thread.sleep(3000)

		println("started main system")
		for (i <- 0 to 20)
			system ! WorkerActor.WorkerMessage(s"MESSAGE-$i")

		for (i <- 0 to 10) {
			Thread.sleep(i * 1000)
			println(i)
		}

		for (i <- 0 to 20)
			system ! WorkerActor.WorkerMessage(s"MESSAGE-$i")
	}
	else {
		ActorSystem(Behaviors.supervise(NodeActor(port % 10 + 5)).onFailure(SupervisorStrategy.restart), systemName)
	}
}
