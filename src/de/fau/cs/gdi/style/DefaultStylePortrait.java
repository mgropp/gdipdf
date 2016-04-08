package de.fau.cs.gdi.style;

import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;

/**
 * GdiPdf default pdf style (portrait).
 * @author Martin Gropp
 * @version $Built: 20160408 1029 gropp$
 */
public class DefaultStylePortrait extends DefaultStyle {
	@Override
	public Rectangle getPageSize() {
		return PageSize.A4;
	}
	
	@Override
	public String toString() {
		return "Standard (Hochformat)";
	}
}
