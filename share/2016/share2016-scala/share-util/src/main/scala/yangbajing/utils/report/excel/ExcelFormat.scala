package yangbajing.utils.report.excel

import play.api.libs.json._

/**
  * Excel文档格式
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-11.
  */
object ExcelFormat extends Enumeration {
  /**
    * 2007+
    */
  val XSSF = Value

  /**
    * 1997 - 2003
    */
  val HSSF = Value

  implicit val jsonFormats = new Format[ExcelFormat.Value] {
    override def writes(o: ExcelFormat.Value): JsValue = JsString(o.toString)

    override def reads(json: JsValue): JsResult[ExcelFormat.Value] = json.validate[String].map(ExcelFormat.withName)
  }
}
