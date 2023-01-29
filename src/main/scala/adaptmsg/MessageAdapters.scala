package adaptmsg

import akka.actor.typed.ActorSystem

object MessageAdapters extends App {
	val system = ActorSystem(Requester(), "system")
	system ! Requester.Request()
}
