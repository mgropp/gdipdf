package de.fau.cs.gdi.gdipdf.style;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;

/**
 * GdiPdf default pdf style (portrait).
 * @author Martin Gropp
 */
public class DefaultStylePortrait extends DefaultStyle {
	@Override
	public void setPageSize(Document document) {
		document.setPageSize(PageSize.A4);
		document.setMarginMirroring(false);
		float leftMargin = lineNumbers ? 24 : 36;
		document.setMargins(leftMargin, 36, 48, 36);
	}
	
	@Override
	public String toString() {
		return "Standard (Hochformat)";
	}
}
