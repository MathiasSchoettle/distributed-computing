package forkjoin

import java.util.concurrent.{ForkJoinTask, RecursiveAction}

class IterativeMutable(values: Array[Int], start: Int, end: Int, internal: Boolean = false) extends RecursiveAction {
	override def compute(): Unit = {
		if (internal) {
			(start until end).foreach(i => values(i) = values(i) * 2)
		}
		else {
			var tasks: List[ForkJoinTask[Void]] = List()
			(values.indices by 10).foreach(startIndex => {
				val endIndex = Math.min(startIndex + 10, values.length)
				tasks = tasks :+ new IterativeMutable(values, startIndex, endIndex, true).fork()
			})
			tasks.foreach(_.join)
		}
	}
}
