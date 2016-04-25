package de.fau.cs.gdi.gdipdf.style;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;

public class SimpleStylePortrait extends SimpleStyle {
	@Override
	public void setPageSize(Document document) {
		document.setPageSize(PageSize.A4);
		document.setMarginMirroring(false);
		float leftMargin = lineNumbers ? 24 : 36;
		document.setMargins(leftMargin, 36, 48, 36);
	}
	
	@Override
	public String toString() {
		return "Einfach (Hochformat)";
	}
}
