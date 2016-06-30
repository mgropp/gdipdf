package de.fau.cs.gdi.gdipdf.style;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Another pdf style (landscape).
 * @author Martin Gropp
 */
public class SimpleStyle extends DefaultStyle {
	public SimpleStyle() {
		this(false);
	}
	
	public SimpleStyle(boolean portrait) {
		super(portrait);
	}
	
	public SimpleStyle(SimpleStyle source, boolean portrait) {
		super(source, portrait);
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
		document.setMargins(leftMargin, 36, 36, 36);
	}
	
	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		PdfContentByte cb = writer.getDirectContent();
		cb.saveState();
		
		Rectangle pageSize = writer.getPageSize();

		Color borderColor = new Color(0xc0c0c0);
		Color emphColor = new Color(0x909090);
		Color textColor = new Color(0xc0c0c0);
		
		RoundRectangle2D border = new RoundRectangle2D.Double(
			20, 20,
			pageSize.getWidth() - 40,
			pageSize.getHeight() - 40,
			4, 4
		);

		Graphics2D g = new PdfGraphics2D(cb, pageSize.getWidth(), pageSize.getHeight());
		try {
			g.setColor(borderColor);
			//g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{ 2 }, 0));
			g.setStroke(new BasicStroke(0.7f));
			g.draw(border);
		}
		finally {
			g.dispose();
		}

		float titleTextX = (float)(border.getX() + border.getWidth()) - 8;
		float titleTextY = pageSize.getHeight() - (float)border.getY() - 16;
		float pageTextX = titleTextX; //(float)border.getX() + 6;
		float pageTextY = pageSize.getHeight() - (float)(border.getY() + border.getHeight()) + 8;
		
		float lead = 14.0f;
		
		cb.beginText();
		cb.setColorFill(new BaseColor(emphColor.getRed(), emphColor.getGreen(), emphColor.getBlue()));
		cb.setFontAndSize(headerFontBold, 11);
		cb.showTextAlignedKerned(
			PdfContentByte.ALIGN_RIGHT,
			student,
			titleTextX,
			titleTextY,
			0
		);
		cb.endText();
		
		cb.beginText();
		cb.setColorFill(new BaseColor(textColor.getRed(), textColor.getGreen(), textColor.getBlue()));
		cb.setFontAndSize(headerFont, 11);
		cb.showTextAlignedKerned(
			PdfContentByte.ALIGN_RIGHT,
			assignment,
			titleTextX,
			titleTextY - lead, 
			0
		);
		cb.endText();
		
		cb.beginText();
		cb.setColorFill(new BaseColor(textColor.getRed(), textColor.getGreen(), textColor.getBlue()));
		cb.setFontAndSize(headerFont, 11);
		cb.showTextAlignedKerned(
			PdfContentByte.ALIGN_RIGHT,
			file,
			titleTextX,
			titleTextY - 2*lead,
			0
		);
		cb.endText();
		
		cb.beginText();
		cb.setColorFill(new BaseColor(emphColor.getRed(), emphColor.getGreen(), emphColor.getBlue()));
		cb.setFontAndSize(headerFont, 11);
		cb.showTextAlignedKerned(
			PdfContentByte.ALIGN_RIGHT,
			"Seite " + writer.getPageNumber(),
			pageTextX,
			pageTextY,
			0
		);
		cb.endText();
		
		cb.restoreState();
	}
	
	@Override
	public SimpleStyle asPortrait() {
		if (portrait) {
			return this;
		} else {
			return new SimpleStyle(this, true);
		}
	}
	
	@Override
	public SimpleStyle asLandscape() {
		if (portrait) {
			return new SimpleStyle(this, false);
		} else {
			return this;
		}
	}
	
	@Override
	public String toString() {
		if (portrait) {
			return "Einfach (Hochformat)";
		} else {
			return "Einfach";
		}
	}
}
