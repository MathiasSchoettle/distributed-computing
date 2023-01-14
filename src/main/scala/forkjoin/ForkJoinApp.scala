package forkjoin

import java.util.concurrent.ForkJoinPool
import scala.language.postfixOps

object ForkJoinApp extends App {
	private val arr: Array[Int] = 1 to 1000 toArray
	private val pool = new ForkJoinPool(Runtime.getRuntime.availableProcessors())

	pool.invoke(new RecursiveMutable(arr, 0, arr.length))
	arr.foreach(println)

	private val result = pool.invoke(new RecursiveImmutable(arr.toList))
	result.foreach(println)

	pool.invoke(new IterativeMutable(arr, 0, arr.length))
	arr.foreach(println)
}
