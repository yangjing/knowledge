package example.implicits

import org.scalatest.{MustMatchers, WordSpec}

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-17.
  */
class MatrixTest extends WordSpec with MustMatchers {

  "SameThreadStrategy" should {
    "execute" in {
      implicit val sameThreadStrategy = SameThreadStrategy
      execute() mustBe new Matrix(Array(Array(6.0), Array(15.0)))
    }
  }

  "ThreadPoolStrategy" should {
    "execute" in {
      implicit val threadPoolStrategy = ThreadPoolStrategy
      execute() mustBe new Matrix(Array(Array(6.0), Array(15.0)))
    }
  }

  private def execute()(implicit threadStrategy: ThreadStrategy): Matrix = {
    val x = new Matrix(Array(Array(1, 2, 3), Array(4, 5, 6)))
    println(s"x: $x")
    val y = new Matrix(Array(Array(1), Array(1), Array(1)))
    println(s"y: $y")

    val result = MatrixUtils.multiply(x, y)
    println(result)
    result
  }

}
