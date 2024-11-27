package example.implicits

import java.util.concurrent.{Callable, Executors}

import com.typesafe.scalalogging.StrictLogging

import scala.collection.mutable.ArrayBuffer

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-17.
  */
class Matrix(val repr: Array[Array[Double]]) {

  def row(idx: Int): Seq[Double] = repr(idx)

  def col(idx: Int): Seq[Double] = {
    repr.foldLeft(ArrayBuffer.empty[Double]) { (buffer, row) =>
      buffer.append(row(idx))
      buffer
    }.toArray
  }

  lazy val rowRank = repr.length
  lazy val colRank = if (rowRank > 0) repr(0).length else 0

  override def toString =
    getClass.getSimpleName + repr.foldLeft("") { (msg, row) =>
      msg + row.mkString("\n|", " | ", "|")
    }

  override def equals(obj: scala.Any): Boolean = {
    //    this == obj ||
    obj match {
      case other: Matrix =>
        (other eq this) ||
          java.util.Arrays.deepEquals(other.repr.asInstanceOf[Array[AnyRef]], this.repr.asInstanceOf[Array[AnyRef]])
      case _ => false
    }
  }
}

trait ThreadStrategy {
  def execute[A](func: () => A): () => A
}

object MatrixUtils {

  def multiply(a: Matrix, b: Matrix)(implicit threading: ThreadStrategy): Matrix = {
    require(a.colRank == b.rowRank)
    val buffer = Array.ofDim[Array[Double]](a.rowRank)
    (0 until a.rowRank).foreach { i =>
      buffer(i) = Array.ofDim[Double](b.colRank)
    }

    // 内部函数，用于执行运算
    def computeValue(row: Int, col: Int): Unit = {
      val pairwiseElements = a.row(row).zip(b.col(col))
      val products = pairwiseElements.map { case (x, y) => x * y }
      buffer(row)(col) = products.sum
    }

    // 生成执行操作序列，待所有并发操作都开始执行
    val computations = for {
      i <- 0 until a.rowRank
      j <- 0 until b.colRank
    } yield threading.execute(() => computeValue(i, j))

    // 获得执行序列保存的所有函数的结果
    computations.foreach(_.apply())

    new Matrix(buffer)
  }

}

/**
  * 在相同线程中执行矩阵运算
  */
object SameThreadStrategy extends ThreadStrategy {
  override def execute[A](func: () => A): () => A = func
}

/**
  * 在线程池中并发执行矩阵运算
  */
object ThreadPoolStrategy extends ThreadStrategy {
  val pool = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors())

  override def execute[A](func: () => A): () => A = {
    val future = pool.submit(new Callable[A] with StrictLogging {
      override def call(): A = {
        logger.info("Executing function on thread: " + Thread.currentThread().getName)
        func()
      }
    })

    // 此外返回函数内部使用阻塞操作不会影响并发。
    () => future.get()
  }

}
