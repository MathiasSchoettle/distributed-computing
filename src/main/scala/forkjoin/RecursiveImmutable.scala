package forkjoin

import java.util.concurrent.RecursiveTask

class RecursiveImmutable(values: List[Int]) extends RecursiveTask[List[Int]] {
  override def compute(): List[Int] = {
    if (values.length < 10) {
      values.map(v => v * 2)
    }
    else {
      val middle = values.length / 2;
      val first  = new RecursiveImmutable(values.slice(0, middle - 1)).fork()
      val second = new RecursiveImmutable(values.slice(middle, values.length)).fork()
      first.join() ++ second.join()
    }
  }
}
