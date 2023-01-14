package forkjoinperf

import com.github.kiprobinson.bigfraction.BigFraction

import java.math.BigInteger
import java.util.concurrent.RecursiveAction

class BigFractionTask(values: List[(BigInteger, BigInteger)], threshold: Int) extends RecursiveAction {
	override def compute(): Unit = {
		if (values.length <= threshold) {
			values.map(v => BigFraction.valueOf(v._1, v._2))
		}
		else {
			val middle = values.length / 2
			val first = new BigFractionTask(values.slice(0, middle - 1), threshold).fork()
			val second = new BigFractionTask(values.slice(middle, values.length), threshold).fork()
			first.join()
			second.join()
		}
	}
}
