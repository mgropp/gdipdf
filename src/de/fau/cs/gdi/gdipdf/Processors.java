package de.fau.cs.gdi.gdipdf;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.icu.text.CharsetDetector;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import de.fau.cs.gdi.gdipdf.style.PdfStyle;

/**
 * Function for producing PDF files.
 * @author Martin Gropp
 */
public class Processors {
	private static final int TAB_WIDTH = 4;
	
	/**
	 * Convert a single Java file to PDF.
	 * 
	 * @param inFile
	 *   Input file (.java)
	 * @param outFile
	 *   Output file (.pdf)
	 * @param pdfStyle
	 *   A PdfStyle object, with student name, assignment
	 *   name, file name set.
	 *   
	 * @throws IOException
	 * @throws DocumentException
	 */
	public static void convertFileToPdf(File inFile, File outFile, PdfStyle pdfStyle) throws IOException, DocumentException {
		String lineNumberFormat = pdfStyle.getLineNumberFormat();
		
		CharsetDetector charsetDetector = new CharsetDetector();
		BufferedInputStream stream = new BufferedInputStream(new FileInputStream(inFile));
		Reader reader = charsetDetector.getReader(stream, null);
		if (reader == null) {
			reader = new InputStreamReader(stream);
		}
		
		StringReader trimmedReader;
		try {
			trimmedReader = getTrimmedReader(reader);
		}
		finally {
			reader.close();
		}
		try {
			JavaLexer lexer = new JavaLexer();
			lexer.setReader(trimmedReader);

			Document document = new Document();
			pdfStyle.setPageSize(document);
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outFile));
			writer.setPageEvent(pdfStyle);

			document.addTitle(inFile.getName());
			document.addSubject(inFile.getName());
			document.addAuthor(System.getProperty("user.name"));
			document.addCreator("GdiPdf (" + GdiPdf.VERSION + ")");
			document.addCreationDate();
			
			
			document.open();

			float leading = pdfStyle.getLeading();
			try {
				Paragraph paragraph = new Paragraph();
				paragraph.setLeading(leading);
				Phrase phrase = new Phrase(leading);

				int lineLength = 0;
				int lineNumber = 1;
				
				boolean emptyDocument = true;
				boolean leadingSpaces = true;
				boolean lineStart = true;
				for (;;) {
					byte style = lexer.getNextToken();
					if (style == JavaLexer.YYEOF) {
						if (phrase.size() > 0 || paragraph.size() > 0) {
							paragraph.add(phrase);
							document.add(paragraph);
						}
						break;
					}

					emptyDocument = false;					
					String text = lexer.yytext();
					
					Chunk chunk = new Chunk();
					if (leadingSpaces) {
						chunk.setFont(pdfStyle.getFont(TokenStyle.PLAIN_STYLE));
					} else {
						chunk.setFont(pdfStyle.getFont(TokenStyle.fromNumber(style)));
					}
					
					char[] chars = text.toCharArray();
					for (int i = 0; i < chars.length; i++) {
						switch (chars[i]) {
						case '\r':
							// Ignore
							break;
						
						case '\n':
							if (lineStart && lineNumberFormat != null) {
								// Empty line
								phrase.add(new Chunk(String.format(lineNumberFormat, lineNumber++), pdfStyle.getFont(TokenStyle.LINE_NUMBER_STYLE)));
							}
							phrase.add(chunk);
							phrase.add("\n");
							paragraph.add(phrase);
							phrase = new Phrase(leading);
				
							chunk = new Chunk();
							chunk.setFont(pdfStyle.getFont(TokenStyle.PLAIN_STYLE));
							lineLength = 0;
							leadingSpaces = true;
							lineStart = true;
							
							break;
							
						case '\t':
							if (chunk.getContent().length() > 0) {
								phrase.add(chunk);
								chunk = new Chunk();
								chunk.setFont(pdfStyle.getFont(TokenStyle.PLAIN_STYLE));
							} else if (lineStart && lineNumberFormat != null) {
								phrase.add(new Chunk(String.format(lineNumberFormat, lineNumber++), pdfStyle.getFont(TokenStyle.LINE_NUMBER_STYLE)));
								lineStart = false;
							}
							
							int n = TAB_WIDTH - lineLength % TAB_WIDTH;
							for (int j = 0; j < n; j++) {
								chunk.append(" ");
							}
							
							while (i < chars.length-1 && chars[i+1] == '\t') {
								i++;
								for (int j = 0; j < TAB_WIDTH; j++) {
									chunk.append(" ");
								}
							}
							
							phrase.add(chunk);
							chunk = new Chunk();
							chunk.setFont(pdfStyle.getFont(TokenStyle.PLAIN_STYLE));
							lineLength = 0;
							
							break;
						
						case ' ':
							if (lineStart && lineNumberFormat != null) {
								phrase.add(new Chunk(String.format(lineNumberFormat, lineNumber++), pdfStyle.getFont(TokenStyle.LINE_NUMBER_STYLE)));
								lineStart = false;
							}
							chunk.append(" ");
							lineLength++;
							break;
							
						default:
							if (leadingSpaces && chunk.getContent().length() > 0) {
								// Add leading spaces with default font -> correct indentation for multiline comments
								phrase.add(chunk);
								chunk = new Chunk();
							} else if (lineStart && lineNumberFormat != null) {
								phrase.add(new Chunk(String.format(lineNumberFormat, lineNumber++), pdfStyle.getFont(TokenStyle.LINE_NUMBER_STYLE)));
								lineStart = false;
							}
							
							chunk.setFont(pdfStyle.getFont(TokenStyle.fromNumber(style)));
							
							leadingSpaces = false;
							chunk.append(Character.toString(chars[i]));
							lineLength++;
							//break;
							
						}
					}
					
					if (chunk.getContent().length() > 0) {
						phrase.add(chunk);
						chunk = null;
					}
				}
				
				if (emptyDocument) {
					paragraph = new Paragraph();
					phrase = new Phrase();
					phrase.add(new Chunk("(Leere Datei)", pdfStyle.getFont(TokenStyle.JAVA_COMMENT_STYLE)));
					paragraph.add(phrase);
					document.add(paragraph);
				}
			}
			finally {
				document.close();
			}
		}
		finally {
			trimmedReader.close();
		}
	}
	
	/**
	 * Decorate a single PDF file (i.e. add frame etc. as specified by the PdfStyle).
	 * 
	 * @param inFile
	 *   Input file (.pdf) 
	 * @param outFile
	 *   Output file (.pdf)
	 * @param portraitStyle
	 *   A PdfStyle object, with student name, assignment
	 *   name, file name set.
	 *   Used for source pages in portrait orientation,
	 *   or for all pages if landscapeStyle == null.
	 * @param landscapeStyle
	 *   A PdfStyle object, with student name, assignment
	 *   name, file name set.
	 *   Used for source pages in landscape orientation,
	 *   or for all pages if portraitStyle == null.
	 * 
	 * @throws DocumentException
	 * @throws IOException
	 */
	public static void decoratePdf(File inFile, File outFile, PdfStyle portraitStyle, PdfStyle landscapeStyle) throws DocumentException, IOException {
		if (portraitStyle == null && landscapeStyle == null) {
			throw new IllegalArgumentException("Must set at least one of portraitStyle, landscapeStyle!");
		}
		
		PdfReader reader = new PdfReader(inFile.getAbsolutePath());
		Document document = new Document();
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outFile));
		
		document.addTitle(inFile.getName());
		document.addSubject(inFile.getName());
		document.addAuthor(System.getProperty("user.name"));
		document.addCreator("GdiPdf (" + GdiPdf.VERSION + ")");
		document.addCreationDate();
		
		int pageCount = reader.getNumberOfPages();
		
		document.open();
		try {
			for (int i = 0; i < pageCount; i++) {
				Rectangle mediabox = reader.getPageSizeWithRotation(i + 1);
				float sourceWidth = mediabox.getWidth();
				float sourceHeight = mediabox.getHeight();
				
				float rotation = (float)((mediabox.getRotation() / -180.0) * Math.PI);
				PdfStyle pdfStyle;
				if (sourceWidth < sourceHeight) {
					// source is portrait
					if (portraitStyle != null) {
						pdfStyle = portraitStyle;
					} else {
						pdfStyle = landscapeStyle;
						rotation += (float)(0.5 * Math.PI);
						
						float tmp = sourceWidth;
						sourceWidth = sourceHeight;
						sourceHeight = tmp;
					}
				} else {
					// source is landscape
					if (landscapeStyle != null) {
						pdfStyle = landscapeStyle;
					} else {
						pdfStyle = portraitStyle;
						rotation += (float)(0.5 * Math.PI);
						
						float tmp = sourceWidth;
						sourceWidth = sourceHeight;
						sourceHeight = tmp;
					}
				}
				
				pdfStyle.setPageSize(document);
				document.newPage();
				
				// setPageEvent must not come before newPage etc.
				// setPageEvent(null) deletes old listeners
				writer.setPageEvent(null);
				writer.setPageEvent(pdfStyle);
				
				double targetWidth = document.right() - document.left();
				double targetHeight = document.top() - document.bottom();
				
				PdfImportedPage sourcePage = writer.getImportedPage(reader, i + 1);

				double scaleFactor = Math.min(
					1.0,
					Math.min(
						targetWidth / sourceWidth,
						targetHeight / sourceHeight
					)
				);

				Image image = Image.getInstance(sourcePage);
				image.setRotation(rotation);
				image.scalePercent((float)(100.0 * scaleFactor));
				
				document.add(image);
			}
		}
		finally {
			document.close();
		}
	}
	
	private static StringReader getTrimmedReader(Reader reader) throws IOException {
		StringBuffer sb = new StringBuffer();
		char[] buffer = new char[4096];
		int read;
		while ((read = reader.read(buffer)) > 0) {
			sb.append(buffer, 0, read);
		}
		
		String lines = sb.toString();
		Pattern emptyPatternStart = Pattern.compile("^\\s*\\n");
		Matcher m = emptyPatternStart.matcher(lines);
		if (m.find()) {
			lines = lines.substring(m.end());
		}
		
		Pattern emptyPatternEnd = Pattern.compile("\\n\\s*$");
		m = emptyPatternEnd.matcher(lines);
		if (m.find()) {
			lines = lines.substring(0, m.start());
		}
		
		return new StringReader(lines);
	}
}
