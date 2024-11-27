package yangbajing.utils.s.time

import java.sql.{Date => SQLDate, Timestamp => SQLTimestamp}
import java.time._
import java.time.format.DateTimeFormatter
import java.util.Date

import scala.util.Try

/**
  * DateTimeUtils
  * Created by yangjing on 15-11-6.
  */
object TimeUtils extends Serializable {

  val ZONE_OFFSET = ZoneOffset.ofHours(8)

  val formatterDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  val formatterMonth = DateTimeFormatter.ofPattern("yyyy-MM")
  val formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val formatterDateHours = DateTimeFormatter.ofPattern("yyyy-MM-dd HH")
  val formatterDateMinutes = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
  val formatterMinutes = DateTimeFormatter.ofPattern("HH:mm")
  val formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")

  def now() = LocalDateTime.now()

  def parseDate(date: String): LocalDate =
    Try(LocalDate.parse(date, formatterDate)) getOrElse {
      val (year, month, day) = date.split('-') match {
        case Array(y, m, d) =>
          (y.toInt, m.toInt, d.toInt)
        case Array(y, m) =>
          (y.toInt, m.toInt, 1)
        case Array(y) =>
          (y.toInt, 1, 1)
        case _ =>
          throw new RuntimeException(s"$date is invalid iso date format")
      }

      if (year < 0 || year > 9999)
        throw new RuntimeException(s"$date is invalid iso date format ($year)")

      LocalDate.of(year, month, day)
    }

  def parseTime(time: String): LocalTime =
    Try(LocalTime.parse(time, formatterTime)) getOrElse {
      val (hour, minute, second, nano) =
        time.split(':') match {
          case Array(h, m, s) =>
            s.split('.') match {
              case Array(sec, millis) =>
                (h.toInt, m.toInt, sec.toInt, millis.toInt * 1000 * 1000)
              case arr =>
                (h.toInt, m.toInt, arr(0).toInt, 0)
            }
          case Array(h, m) =>
            (h.toInt, m.toInt, 0, 0)
          case Array(h) =>
            (h.toInt, 0, 0, 0)
          case _ =>
            throw new RuntimeException(s"$time is invalid iso time format")
        }

      LocalTime.of(hour, minute, second, nano)
    }

  def parseDateTime(date: String, time: String): LocalDateTime = {
    val d = parseDate(date)
    val t = parseTime(time)
    LocalDateTime.of(d, t)
  }

  def parseDateTime(datetime: String): LocalDateTime =
    Try(LocalDateTime.parse(datetime, formatterDateTime)) getOrElse {
      datetime.split( """[ ]+""") match {
        case Array(date, time) =>
          parseDateTime(date, time)
        case _ =>
          throw new RuntimeException(s"$datetime is invalid iso datetime format")
      }
    }

  def parseDateTime(instant: Instant): LocalDateTime = {
    LocalDateTime.ofInstant(instant, ZONE_OFFSET)
  }

  def toLocalDateTime(date: Date): LocalDateTime = {
    parseDateTime(date.toInstant)
  }

  def toLocalDateTime(epochMilli: Long): LocalDateTime = {
    LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneOffset.UTC)
  }

  def toDate(ldt: LocalDateTime): Date = {
    new Date(toEpochMilli(ldt))
  }

  def toEpochMilli(dt: LocalDateTime): Long = {
    dt.toInstant(ZoneOffset.UTC).toEpochMilli
  }

  def currentTimeSeconds() = System.currentTimeMillis() / 1000

  def toSqlTimestamp(dt: LocalDateTime): SQLTimestamp = new SQLTimestamp(toEpochMilli(dt))

  def toSqlDate(date: LocalDate): SQLDate = new SQLDate(toEpochMilli(date.atStartOfDay()))

  /**
    * @return 一天的开始：
    */
  def nowBegin(): LocalDateTime = {
    LocalDate.now().atStartOfDay()
  }

  /**
    * @return 一天的结尾：
    */
  def nowEnd(): LocalDateTime = {
    nowBegin().plusDays(1).minusSeconds(1)
  }

}