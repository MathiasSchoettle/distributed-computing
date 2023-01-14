package forkjoinperf

import java.math.BigInteger
import java.util.Random
import java.util.concurrent.ForkJoinPool
import scala.language.postfixOps

object ForkJoinAppPerf extends App {
	private val numberCount = 10000

	private val numbers = LazyList.continually((new BigInteger(150000, new Random()), new BigInteger(15000, new Random())))
	  .take(numberCount)
	  .toList

	testWithCommonPool(1)
	testWithCommonPool(10)
	testWithCommonPool(100)

	private val processorCount = Runtime.getRuntime.availableProcessors()
	testWithForkJoin(processorCount / 2, 1)
	testWithForkJoin(processorCount / 2, 10)
	testWithForkJoin(processorCount / 2, 100)

	testWithForkJoin(processorCount * 2, 1)
	testWithForkJoin(processorCount * 2, 10)
	testWithForkJoin(processorCount * 2, 100)

	testWithForkJoin(processorCount - 1, 1)
	testWithForkJoin(processorCount - 1, 10)
	testWithForkJoin(processorCount - 1, 100)

	private def testWithCommonPool(threshold: Int): Unit = {
		test(ForkJoinPool.commonPool(), "common", threshold)
	}

	private def testWithForkJoin(processorCount: Int, threshold: Int): Unit = {
		val availableProcessorCount = Runtime.getRuntime.availableProcessors()
		val description = s"fork-join($processorCount of $availableProcessorCount)"
		test(new ForkJoinPool(processorCount), description, threshold)
	}

	private def test(pool: ForkJoinPool, description: String, threshold: Int): Unit = {
		val task = new BigFractionTask(numbers, threshold)
		val startStealCount = pool.getStealCount

		System.gc()
		val startTime = System.nanoTime()

		pool.invoke(task)

		val endTime = System.nanoTime() - startTime
		val stealCount = pool.getStealCount - startStealCount

		printf("%8.3f | %-20s\t%-20s | %s\n", endTime / 1000000.0, description, s"threshold=$threshold", s"steal-count=$stealCount")
	}
}
