package de.fau.cs.gdi.gdipdf.style;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPageEvent;

import de.fau.cs.gdi.gdipdf.TokenStyle;

/**
 * Interface for formatting pdf files.
 * @author Martin Gropp
 */
public interface PdfStyle extends PdfPageEvent {
	void setPageSize(Document document);
	float getLeading();
	Font getFont(TokenStyle tokenClass);
	void setStudentName(String student);
	void setFileName(String file);
	void setAssignmentName(String assignment);
	void setLineNumbers(boolean lineNumbers);
	String getLineNumberFormat();
	PdfStyle asPortrait();
	PdfStyle asLandscape();
}
