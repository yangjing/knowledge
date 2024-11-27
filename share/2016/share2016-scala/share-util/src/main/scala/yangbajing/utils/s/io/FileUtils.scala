package yangbajing.utils.s.io

import java.io.BufferedWriter
import java.nio.charset.{Charset, StandardCharsets}

import scala.collection.JavaConverters._
import java.nio.file.{Files, OpenOption, Path, StandardOpenOption}

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-04-11.
  */
object FileUtils {

  /**
    * 写文件
    *
    * @param path 文件路径
    * @param lines 数据序列
    * @param charset 字符集（默认UTF-8）
    * @tparam T 所有CharSequence的子类
    * @return 成功返回path
    */
  def write[T <: CharSequence](path: Path, lines: TraversableOnce[T], charset: Charset = StandardCharsets.UTF_8): Path = {
    Files.write(path, lines.toIterable.asJava, charset)
  }

  def writeWith[T <: CharSequence, R](path: Path, charset: Charset = StandardCharsets.UTF_8)(func: BufferedWriter => R): Unit = {
    val writer = Files.newBufferedWriter(path, charset)
    try {
      func(writer)
    } finally {
      writer.close()
    }
  }

  //  def write(path: Path, s: String, charset: Charset = DEFAULT_CHARSET, option: String = ""): Path = {
  //    Files.write(path, s.getBytes(charset))
  //  }

  //  def write(path: Path, bytes: Array[Byte], option: String = ""): Path = {
  //    Files.write(path, bytes)
  //  }

  def parseOpenOption(s: String): Seq[OpenOption] = {
    import StandardOpenOption._
    //    s.map {
    //      case 'r' => READ
    //      case 'w' => WRITE
    //      case 'b' =>
    //    }

    var idx = 0
    while (idx < s.length) {
      s.charAt(idx) match {
        case 'r' => READ
        case 'w' =>
          if (s.charAt(idx + 1) == '+') {
            idx += 1
            APPEND
          } else {
            WRITE
          }
      }

      idx += 1
    }

    Nil
  }
}

