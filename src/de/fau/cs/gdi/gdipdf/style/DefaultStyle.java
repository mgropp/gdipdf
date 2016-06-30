package de.fau.cs.gdi.gdipdf.style;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import de.fau.cs.gdi.gdipdf.TokenStyle;

/**
 * GdiPdf default pdf style (landscape).
 * @author Martin Gropp
 */
public class DefaultStyle extends PdfPageEventHelper implements PdfStyle {
	protected final boolean portrait;
	
	protected String student = "";
	protected String file = "";
	protected String assignment = "";
	protected boolean lineNumbers = false;
	
	protected Font normalFont = new Font(Font.FontFamily.COURIER, 10, Font.NORMAL);
	protected Font keywordFont = new Font(Font.FontFamily.COURIER, 10, Font.BOLD, new BaseColor(0xFF074185)); 
	protected Font typeFont = new Font(Font.FontFamily.COURIER, 10, Font.BOLD);
	protected Font commentFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(Color.GRAY.getRed(), Color.GRAY.getGreen(), Color.GRAY.getBlue()));
	protected Font literalFont = new Font(Font.FontFamily.COURIER, 10, Font.NORMAL, new BaseColor(0xFF092C47));
	protected Font lineNumberFont = new Font(Font.FontFamily.COURIER, 8, Font.NORMAL, new BaseColor(Color.GRAY.getRed(), Color.GRAY.getGreen(), Color.GRAY.getBlue()));
	protected Font tutorCommentFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(0xFFCC1111));
	
	protected BaseFont headerFont;
	protected BaseFont headerFontBold;
	{
		try {
			headerFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			headerFontBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
		}
		catch (DocumentException e) {
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public DefaultStyle() {
		this(false);
	}
	
	public DefaultStyle(boolean portrait) {
		this.portrait = portrait;
	}
	
	public DefaultStyle(DefaultStyle source, boolean portrait) {
		this.student = source.student;
		this.file = source.file;
		this.assignment = source.assignment;
		this.lineNumbers = source.lineNumbers;
		this.portrait = portrait;
	}
	
	@Override
	public void setPageSize(Document document) {
		if (portrait) {
			document.setPageSize(PageSize.A4);
		} else {
			document.setPageSize(PageSize.A4.rotate());
		}
		
		document.setMarginMirroring(false);
		float leftMargin = lineNumbers ? 24 : 36;
		document.setMargins(leftMargin, 36, 48, 36);
	}
	
	@Override
	public float getLeading() {
		return 12.0f;
	}
	
	@Override
	public Font getFont(TokenStyle style) {
		switch (style) {
		case LINE_NUMBER_STYLE:
			return lineNumberFont;
			
		case KEYWORD_STYLE:
			return keywordFont;
			
		case TYPE_STYLE:
			return typeFont;

		case JAVA_COMMENT_STYLE:
		case JAVADOC_COMMENT_STYLE:
		case JAVADOC_TAG_STYLE:
			return commentFont;

		case LITERAL_STYLE:	
			return literalFont;
			
		case TUTOR_COMMENT_STYLE:
			return tutorCommentFont;
			
		case PLAIN_STYLE:
		case OPERATOR_STYLE:
		case SEPARATOR_STYLE:
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
		
		Graphics2D g = new PdfGraphics2D(cb, pageSize.getWidth(), pageSize.getHeight());
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
		
		cb.beginText();
		cb.setColorFill(new BaseColor(textColor.getRed(), textColor.getGreen(), textColor.getBlue()));
		cb.setFontAndSize(headerFont, 12);
		cb.showTextAlignedKerned(
			PdfContentByte.ALIGN_LEFT,
			student,
			(float)border.getX() + 8,
			textY+1,
			0
		);
		cb.endText();
		
		cb.beginText();
		cb.setFontAndSize(headerFontBold, 14);
		cb.showTextAlignedKerned(
			PdfContentByte.ALIGN_CENTER,
			file,
			pageSize.getWidth() / 2,
			textY,
			0
		);
		cb.endText();
		
		cb.beginText();
		cb.setFontAndSize(headerFont, 12);
		cb.showTextAlignedKerned(
			PdfContentByte.ALIGN_RIGHT,
			assignment,
			(float)(border.getX() + border.getWidth()) - 8,
			textY+1,
			0
		);
		cb.endText();
		
		cb.beginText();
		cb.setFontAndSize(headerFont, 12);
		cb.showTextAlignedKerned(
			PdfContentByte.ALIGN_RIGHT,
			"Seite " + writer.getPageNumber(),
			(float)(border.getX() + border.getWidth()),
			(float)(pageSize.getHeight() - (border.getY() + border.getHeight()) - 15),
			0
		);
		cb.endText();
		
		cb.restoreState();
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
	public String getLineNumberFormat() {
		return lineNumbers ? "%3d " : null;
	}
	
	@Override
	public void setLineNumbers(boolean lineNumbers) {
		this.lineNumbers = lineNumbers;
	}
	
	@Override
	public DefaultStyle asPortrait() {
		if (portrait) {
			return this;
		} else {
			return new DefaultStyle(this, true);
		}
	}
	
	@Override
	public DefaultStyle asLandscape() {
		if (portrait) {
			return new DefaultStyle(this, false);
		} else {
			return this;
		}
	}
	
	@Override
	public String toString() {
		if (portrait) {
			return "Standard (Hochformat)";
		} else {
			return "Standard";
		}
	}
}
