package forkjoin

import java.util.concurrent.RecursiveAction

class RecursiveMutable(values: Array[Int], start: Int, end: Int) extends RecursiveAction {
  override def compute(): Unit = {
    if (end - start < 10) {
      for (i: Int <- Range(start, end)) {
        values(i) = values(i) * 2
      }
    }
    else {
      val middle = start + (end - start) / 2
      val first  = new RecursiveMutable(values, start, middle - 1).fork()
      val second = new RecursiveMutable(values, middle, end).fork()
      first.join()
      second.join()
    }
  }
}
