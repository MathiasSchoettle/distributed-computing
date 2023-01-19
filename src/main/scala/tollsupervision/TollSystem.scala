package tollsupervision

import akka.actor.typed.ActorSystem

object TollSystem extends App {

	val system = ActorSystem(TollSupervisor(), "toll-system")

	system ! "sensor-A9-2-R AB 123"
	system ! "hello-A9-1"
	system ! "hello-A3-2"

	// system ! "fail-A2"

	system ! "sensor-A9-2-R AB 123"
	system ! "sensor-A3-2-R AB 123"
}
