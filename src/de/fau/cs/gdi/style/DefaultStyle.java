package de.fau.cs.gdi.style;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import de.fau.cs.gdi.gdipdf.JavaLexer;

/**
 * GdiPdf default pdf style (landscape).
 * @author Martin Gropp
 * @version $Built: 20160408 1029 gropp$
 */
public class DefaultStyle extends PdfPageEventHelper implements PdfStyle {
	private String student = "";
	private String file = "";
	private String assignment = "";
	
	private static Font normalFont = new Font(Font.FontFamily.COURIER, 10, Font.NORMAL);
	private static Font keywordFont = new Font(Font.FontFamily.COURIER, 10, Font.BOLD, new BaseColor(0x074185)); 
	private static Font typeFont = new Font(Font.FontFamily.COURIER, 10, Font.BOLD);
	private static Font commentFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(Color.GRAY));
	private static Font literalFont = new Font(Font.FontFamily.COURIER, 10, Font.NORMAL, new BaseColor(0x092C47));
	
	@Override
	public Rectangle getPageSize() {
		return PageSize.A4.rotate();
	}
	
	@Override
	public float getLeading() {
		return 12.0f;
	}
	
	@Override
	public Font getFont(byte style) {
		switch (style) {
		case JavaLexer.KEYWORD_STYLE:
			return keywordFont;
			
		case JavaLexer.OPERATOR_STYLE:
		case JavaLexer.TYPE_STYLE:
			return typeFont;

		case JavaLexer.JAVA_COMMENT_STYLE:
		case JavaLexer.JAVADOC_COMMENT_STYLE:
		case JavaLexer.JAVADOC_TAG_STYLE:
			return commentFont;

		case JavaLexer.LITERAL_STYLE:	
			return literalFont;
			
		case JavaLexer.PLAIN_STYLE:
		case JavaLexer.SEPARATOR_STYLE:
		default:
			return normalFont;
		}
	}

	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		PdfContentByte cb = writer.getDirectContent();
		cb.saveState();
		Rectangle pageSize = writer.getPageSize();
		
		int margin = 18;
		int marginLeft = margin;
		int marginRight = margin;
		int marginTop = 12;
		int marginBottom = 28;
		int headerSize = 25;
		
		Color borderColor = new Color(0x151515);
		Color headerColor = new Color(0xF8F8F8);
		Color textColor = Color.BLACK;
		
		RoundRectangle2D border = new RoundRectangle2D.Double(marginLeft, marginTop, pageSize.getWidth()-(marginLeft+marginRight), pageSize.getHeight()-(marginTop+marginBottom), 3, 3);
		Rectangle2D header = new Rectangle2D.Double(border.getX(), border.getY(), border.getWidth(), headerSize);
		Line2D headerLine = new Line2D.Double(header.getX(), header.getY() + header.getHeight(), header.getX() + header.getWidth(), header.getY() + header.getHeight());
		
		BaseFont headerFont = getBaseFont(BaseFont.HELVETICA);
		BaseFont headerFontBold = getBaseFont(BaseFont.HELVETICA_BOLD);
		
		Graphics2D g = cb.createGraphics(pageSize.getWidth(), pageSize.getHeight());
		try {
			g.setColor(headerColor);
			g.fill(header);
			
			g.setColor(borderColor);
			g.draw(headerLine);
			g.draw(border);
		}
		finally {
			g.dispose();
		}

		float textY = pageSize.getHeight() - (float)headerLine.getY1() + 7;
		cb.setColorFill(new BaseColor(textColor));
		
		cb.setFontAndSize(headerFont, 12);
		cb.showTextAlignedKerned(
			Element.ALIGN_LEFT,
			student,
			(float)border.getX() + 8,
			textY+1,
			0
		);			
		
		cb.setFontAndSize(headerFontBold, 14);
		cb.showTextAlignedKerned(
			Element.ALIGN_CENTER,
			file,
			pageSize.getWidth() / 2,
			textY,
			0
		);
		
		cb.setFontAndSize(headerFont, 12);
		cb.showTextAlignedKerned(
			Element.ALIGN_RIGHT,
			assignment,
			(float)(border.getX() + border.getWidth()) - 8,
			textY+1,
			0
		);
		
		cb.setFontAndSize(headerFont, 12);
		cb.showTextAlignedKerned(
			Element.ALIGN_RIGHT,
			"Seite " + writer.getPageNumber(),
			(float)(border.getX() + border.getWidth()),
			(float)(pageSize.getHeight() - (border.getY() + border.getHeight()) - 15),
			0
		);
		
		cb.restoreState();
	}

	private static BaseFont getBaseFont(String name) {
		try {
			return BaseFont.createFont(name, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
		}
		catch (DocumentException e) {
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void setStudentName(String student) {
		this.student = student;
	}

	@Override
	public void setFileName(String file) {
		this.file = file;
	}

	@Override
	public void setAssignmentName(String assignment) {
		this.assignment = assignment;
	}
	
	@Override
	public String toString() {
		return "Standard";
	}
}
