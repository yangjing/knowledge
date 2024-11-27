package yangbajing.utils.s.regex

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-02-13.
  */
object RegexUtils {
  val regexDigit = """[\d,]+""".r

  def parseDigit(s: CharSequence): Option[Int] =
    regexDigit.findFirstIn(s).map(_.replaceAll(",", "").toInt)

  def parseDigit(s: CharSequence, deft: => Int): Int =
    parseDigit(s).getOrElse(deft)

  def parseDigit(a: Any, deft: => Int): Int =
    parseDigit(a.toString, deft)

  def parseDigitAll(s: CharSequence): List[Int] = {
    val iter = regexDigit.findAllIn(s)
    var list = List.empty[Int]
    while (iter.hasNext) {
      list = iter.next().toInt :: list
    }
    list
  }

}
