package com.bsp.fsccis.util;

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;

public class ExportXlsUtil {
	public static void xlsExport(HSSFWorkbook wb, List<String[]> list,
			List<String> headerList) {

		CellStyle detailsBorder = detailsBorder(wb);
		CellStyle headerBoldBorder = headerBoldBorder(wb);

		Integer rowCtr = wb.getSheetAt(0).getPhysicalNumberOfRows();
		// set column header
		Integer hCounter = 0;
		Row rHeader = wb.getSheetAt(0).createRow(rowCtr++);
		for (String h : headerList) {
			rHeader.createCell(hCounter).setCellValue(h);
			rHeader.getCell(hCounter).setCellStyle(headerBoldBorder);
			hCounter++;
		}

		//set details
		for (String[] x : list) {
			Row rDetails = wb.getSheetAt(0).createRow(rowCtr);
			for (int i = 0; i <= headerList.size() - 1; i++) {
				rDetails.createCell(i).setCellValue(x[i]);
				rDetails.getCell(i).setCellStyle(detailsBorder);
			}
			rowCtr++;
		}
		
		
		// fit column content
		for (int c = 0; c < headerList.size(); c++) {
			wb.getSheetAt(0).autoSizeColumn(c);
		}

	}
	
	public static CellStyle detailsBorder(HSSFWorkbook wb) {
		CellStyle style = wb.createCellStyle();
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());

		Font font = wb.createFont();
		font.setFontName("Calibri");
		style.setFont(font);

		return style;
	}

	public static CellStyle titleBoldWeight(HSSFWorkbook wb) {
		CellStyle style = wb.createCellStyle();
		Font font = wb.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		font.setFontName("Calibri");
		style.setFont(font);
		return style;
	}
	
	public static CellStyle headerBoldBorder(HSSFWorkbook wb) {
		CellStyle style = wb.createCellStyle();
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		Font font = wb.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		font.setFontName("Calibri");
		style.setFont(font);
		return style;
	}

	public static CellStyle headerBoldItalicBorder(HSSFWorkbook wb) {
		CellStyle style = wb.createCellStyle();
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		Font font = wb.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		font.setFontName("Calibri");
		font.setItalic(true);
		style.setFont(font);
		return style;
	}
	
	@Deprecated
	public static void createHeader(HSSFWorkbook wb, List<String> headerList) {
		CellStyle headerBoldBorder = headerBoldBorder(wb);

		Integer rowCtr = wb.getSheetAt(0).getPhysicalNumberOfRows();
		// set column header
		Integer hCounter = 0;
		Row rHeader = wb.getSheetAt(0).createRow(rowCtr++);
		for (String h : headerList) {
			rHeader.createCell(hCounter).setCellValue(h);
			rHeader.getCell(hCounter).setCellStyle(headerBoldBorder);
			hCounter++;
		}

	}
	
	public static void createHeader(HSSFWorkbook wb, List<String> headerList, CellStyle headerBoldBorder) {

		Integer rowCtr = wb.getSheetAt(0).getPhysicalNumberOfRows();
		// set column header
		Integer hCounter = 0;
		Row rHeader = wb.getSheetAt(0).createRow(rowCtr++);
		for (String h : headerList) {
			rHeader.createCell(hCounter).setCellValue(h);
			rHeader.getCell(hCounter).setCellStyle(headerBoldBorder);
			hCounter++;
		}

	}

	public static void createDetails(HSSFWorkbook wb, List<String[]> list,
			List<String> headerList) {
		CellStyle detailsBorder = detailsBorder(wb);
		Integer rowCtr = wb.getSheetAt(0).getPhysicalNumberOfRows();
		// set details
		for (String[] x : list) {
			Row rDetails = wb.getSheetAt(0).createRow(rowCtr);
			for (int i = 0; i <= headerList.size() - 1; i++) {
				rDetails.createCell(i).setCellValue(x[i]);
				rDetails.getCell(i).setCellStyle(detailsBorder);
			}
			rowCtr++;
		}

	}
}
