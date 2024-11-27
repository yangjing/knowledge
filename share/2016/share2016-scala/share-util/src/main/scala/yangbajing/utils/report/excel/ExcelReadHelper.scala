package yangbajing.utils.report.excel

import java.io.InputStream

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.{Cell, Row, Sheet, Workbook}
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import scala.collection.JavaConverters._

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-11.
  */
class ExcelReadHelper private(workbook: Workbook, sheetNumbers: Seq[Int]) {

  def readLines: Seq[(Sheet, Seq[Seq[Cell]])] = {
    val numberOfSheets = workbook.getNumberOfSheets
    val rangeNumberOfSheets = 0 until numberOfSheets
    val needSheetNumbers = if (sheetNumbers.isEmpty) rangeNumberOfSheets else rangeNumberOfSheets.intersect(sheetNumbers)
    needSheetNumbers.map { sheetNumber =>
      val sheet = workbook.getSheetAt(sheetNumber)
      (sheet, readSheet(sheet))
    }
  }

  def readSheet(sheet: Sheet): Seq[Seq[Cell]] = {
    (sheet.getFirstRowNum to sheet.getLastRowNum).map { rowNum =>
      readRow(sheet.getRow(rowNum))
    }
  }

  def readRow(row: Row): Seq[Cell] = {
    (row.getFirstCellNum.toInt until row.getLastCellNum).map { cellNum =>
      row.getCell(cellNum)
    }
  }

}

object ExcelReadHelper {

  /**
    *
    * @param workbook Excel文档对象
    * @return
    */
  def apply(workbook: Workbook): ExcelReadHelper = new ExcelReadHelper(workbook, Nil)

  /**
    *
    * @param is 需要自行管理fis资源
    * @return
    */
  def apply(is: InputStream, format: ExcelFormat.Value): ExcelReadHelper = {
    val workbook = format match {
      case ExcelFormat.HSSF => new HSSFWorkbook(is)
      case ExcelFormat.XSSF => new XSSFWorkbook(is)
    }
    apply(workbook)
  }

  def parseCell(cell: Cell) = {
    cell.getCellType match {
      case Cell.CELL_TYPE_NUMERIC => cell.getNumericCellValue
      case Cell.CELL_TYPE_STRING => cell.getStringCellValue
      case Cell.CELL_TYPE_FORMULA => cell.getCellFormula
      case Cell.CELL_TYPE_BLANK => cell.getStringCellValue
      case Cell.CELL_TYPE_BOOLEAN => cell.getBooleanCellValue
      case Cell.CELL_TYPE_ERROR => cell.getErrorCellValue.toString
    }
  }

}
