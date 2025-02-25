package com.bsp.fsccis.util;

import java.io.IOException;
import java.util.List;

import javax.faces.context.FacesContext;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

public class ExportPdfUtil {
	public static final String CALIBRI = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/resources/fonts/CALIBRI.TTF");
	public static final String CALIBRI_BOLD = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/resources/fonts/CALIBRIB.TTF");
	public static final String CALIBRI_BOLD_ITALIC = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/resources/fonts/CALIBRIZ.TTF");
	
	
	public static void createTableHeader(PdfPTable table, List<String> headerList, Integer align, String font) throws DocumentException, IOException{
		Integer headerFS = 9;	
		//header
		Integer headerCtr = 0;
		for (String h : headerList) {
			table.addCell(cell(h, headerFS, font, true, 0, align));
			headerCtr++;
		}	
	}
	
	public static void createTableDetails(PdfPTable table, List<String[]> list,
			List<String> headerList, Integer align, String font) throws DocumentException, IOException {
		Integer detailFS = 8;

		// details
		for (String[] x : list) {
			for (int i = 0; i < headerList.size(); i++) {
				table.addCell(cell(x[i], detailFS, font, true, 0,
						align));
			}

		}

		if (list.isEmpty())
			table.addCell(cell("No records found.", detailFS,
					font, true, headerList.size(), null));

		table.setWidthPercentage(100);
	}
	
	/**
	 * 
	 * @param document Pdf document
	 * @param list List of Details List<String[]>
	 * @param headerList List of Header List<String>
	 * @param autoResizeColumn Auto resize column flag
	 * @throws DocumentException
	 * @throws IOException 
	 */
	public static void pdfExport(Document document,
			List<String[]> list, List<String> headerList, boolean autoResizeColumn) throws DocumentException, IOException {
		Integer headerFS = 9;
		Integer detailFS = 8;
		PdfPTable table = new PdfPTable(headerList.size()); 

		
		//header
		float[] floatArray = new float[headerList.size()];
		Integer headerCtr = 0;
		for (String h : headerList) {
			if (!autoResizeColumn) {
				floatArray[headerCtr] = floatArray[headerCtr] > h.length() ? floatArray[headerCtr]
						: h.length();
			}
			table.addCell(cell(h, headerFS, ExportPdfUtil.CALIBRI_BOLD, true, 0, null));
			headerCtr++;
		}		
		
		// details
		for (String[] x : list) {
			for (int i = 0; i < headerList.size(); i++) {
				if (!autoResizeColumn) {
					floatArray[i] = floatArray[i] > (x[i] == null ? 0 :  x[i].length()) ? floatArray[i]
							: x[i].length();
				}
				table.addCell(cell(x[i], detailFS, ExportPdfUtil.CALIBRI, true, 0, null));
			}

		}

		if (list.isEmpty())
			table.addCell(cell("No records found.", detailFS, ExportPdfUtil.CALIBRI, true,
					headerList.size(), null));

		if (!autoResizeColumn)
			table.setWidths(floatArray);
		
		if(staticColumnSize)
			table.setWidths(columnSizeArr);

		table.setWidthPercentage(100);
		document.add(table);
		document.add(Chunk.NEWLINE);

	}

	public static Phrase header(String string, int i, String font) throws DocumentException, IOException {
		return new Paragraph(string, getFont(font, i));
	}
	
	/**
	 * 
	 * @param value String value to display in cell
	 * @param hSize Font Size
	 * @param font Font Family
	 * @param withBorder display cell with border
	 * @param colspan No of Column Span 
	 * @param align Alignment of cell <b>Note:</b> set Null for default alignent
	 * @return PdfPCell
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static PdfPCell cell(String value, Integer hSize, String font, boolean withBorder, int colspan, Integer align) throws DocumentException, IOException {
		PdfPCell cell = new PdfPCell(ExportPdfUtil.header(value, hSize, font));
		if(!withBorder){
			cell.setBorder(Rectangle.NO_BORDER);
		}
		if(colspan != 0){
			cell.setColspan(colspan);
		}
		if(align != null){
			cell.setHorizontalAlignment(align);
		}
		
		return cell;
	}

	public static Paragraph paragraph(String value, String font, Integer tSize) throws DocumentException, IOException {
		Paragraph preface = new Paragraph();
		preface.add(new Paragraph(value, getFont(font, tSize)));
		preface.setSpacingAfter(10);
		return preface;
	}

	public static Font getFont(String fFamily, Integer hSize) throws DocumentException, IOException {
	    BaseFont arialUnicodeMs = BaseFont.createFont(fFamily, BaseFont.WINANSI, BaseFont.EMBEDDED);
		return new Font(arialUnicodeMs, hSize);
	}
	
	public static void setColumnSizeArr(float[] val)  {
		columnSizeArr = val;
	}
	
	public static void setStaticColumnSize(Boolean val)  {
		staticColumnSize = val;
	}

	private static float[] columnSizeArr;
	private static Boolean staticColumnSize = false;
}
