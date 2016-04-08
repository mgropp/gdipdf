package de.fau.cs.gdi.style;

import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPageEvent;

/**
 * Interface for formatting pdf files.
 * @author Martin Gropp
 * @version $Built: 20160408 1029 gropp$
 */
public interface PdfStyle extends PdfPageEvent {
	Rectangle getPageSize();
	float getLeading();
	Font getFont(byte style);
	void setStudentName(String student);
	void setFileName(String file);
	void setAssignmentName(String assignment);
}
