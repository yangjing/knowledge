import example.implicits.Matrix

val a = new Matrix(Array(Array(6.0), Array(15.0)))
val b = new Matrix(Array(Array(6.0), Array(15.0)))

java.util.Arrays.deepEquals(a.repr.asInstanceOf[Array[AnyRef]], a.repr.asInstanceOf[Array[AnyRef]])