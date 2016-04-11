package de.fau.cs.gdi.gdipdf;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.ibm.icu.text.CharsetDetector;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfWriter;

import de.fau.cs.gdi.gdipdf.style.DefaultStyle;
import de.fau.cs.gdi.gdipdf.style.DefaultStylePortrait;
import de.fau.cs.gdi.gdipdf.style.PdfStyle;
import de.fau.cs.gdi.gdipdf.style.SimpleStyle;

/**
 * GdiPdf
 * @author Martin Gropp
 * @version $buildinfo: 2016-04-11 12:09 gropp$
 */
public class GdiPdf {
	private static final String BUILDINFO = "$buildinfo: 2016-04-11 12:09 gropp$";
	public static final String VERSION = BUILDINFO.substring(BUILDINFO.indexOf(' ')+1, BUILDINFO.length()-1);

	private static final Pattern assignmentDirPattern = Pattern.compile("([^_]+)_(.*)_([^_]+)");
	private static final Pattern intPattern = Pattern.compile("[0-9]+");

	private static final int TAB_WIDTH = 4;
	
	private static final List<PdfStyle> pdfStyles;	
	static {
		pdfStyles = new ArrayList<PdfStyle>();

		String customClassName = System.getProperty("gdipdf.style", null);
		Class<?> customClass = null;
		if (customClassName != null) {
			try {
				customClass = Class.forName(customClassName);
				pdfStyles.add((PdfStyle)customClass.newInstance());
			}
			catch (InstantiationException e) {
				throw new RuntimeException(e);
			}
			catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		if (!DefaultStyle.class.equals(customClass)) {
			pdfStyles.add(new DefaultStyle());
		}
		if (!DefaultStylePortrait.class.equals(customClass)) {
			pdfStyles.add(new DefaultStylePortrait());
		}
		if (!SimpleStyle.class.equals(customClass)) {
			pdfStyles.add(new SimpleStyle());
		}
	}
	
	public static List<PdfStyle> getPdfStyles() {
		return pdfStyles;
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

	public static String getAssignmentName(File assignmentDir) {
		String dirName;
		try {
			dirName = assignmentDir.getCanonicalFile().getName();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		Matcher matcher = assignmentDirPattern.matcher(dirName);
		if (!matcher.matches()) {
			return null;
		}
		
		String name = matcher.group(2);
		
		List<String> tokens = new ArrayList<String>(Arrays.asList(name.split("_")));
		if ("Aufgabe".equals(tokens.get(0))) {
			tokens.remove(0);
		}
	
		if (tokens.size() >= 2) {
			String major = tokens.get(0);
			String minor = tokens.get(1);
			if (intPattern.matcher(major).matches() && intPattern.matcher(minor).matches()) {
				tokens.remove(0);
				tokens.set(0, major + "." + minor);
			}
		}
	
		StringBuilder sb = new StringBuilder();
		for (String token : tokens) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(token);
		}
	
		return sb.toString();
	}

	public static String getStudentName(File studentDir) {
		List<String> tokens = new ArrayList<String>(Arrays.asList(studentDir.getName().split("_")));
		tokens.remove(tokens.size() - 1);
	
		StringBuilder sb = new StringBuilder();
		for (String token : tokens) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(token);
		}
	
		return sb.toString();
	}

	public static File getPdfFilename(File inFile) {
		String filename = inFile.getAbsolutePath();
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex >= 0) {
			filename = filename.substring(0, dotIndex);
		}
	
		return new File(filename + ".pdf");
	}

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
			document.addCreator("GdiPdf (" + VERSION + ")");
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
	
	public static class Options {
		@Option(name="--no-gui", usage="Keine grafische Benutzeroberfläche anzeigen")
		public boolean noGui;
		
		@Option(name="--assignment-name", usage="Namen der Aufgabe manuell festlegen (nur bei --no-gui, höchstens ein Aufgabenverzeichnis)")
		public String assignmentName = null;
		
		@Option(name="--pdf-style", usage="Klassenname des PDF-Stils (nur bei --no-gui)")
		public String pdfStyle = pdfStyles.get(0).getClass().getCanonicalName();
		
		@Option(name="--del-empty-dirs", usage="Leere Verzeichnisse löschen (nur bei --no-gui)")
		public boolean delEmptyDirs;
		
		@Option(name="--overwrite", usage="Existierende Dateien ohne Nachfrage überschreiben (nur bei --no-gui)")
		public boolean overwrite;
		
		@Option(name="--line-numbers", usage="Zeilennummern (nur bei --no-gui)")
		public boolean lineNumbers;
		
		@Option(name="--help", usage="Hilfetext anzeigen")
		public boolean help;
		
		@Argument
		public List<String> assignmentDirs = null;
	}
	
	public static void main(String[] args) throws Exception {
		Options opt = new Options();
		CmdLineParser parser = new CmdLineParser(opt);
		try {
			parser.parseArgument(args);
		}
		catch (CmdLineException e) {
			System.err.println(e.getMessage());
			if (!opt.noGui) {
				JOptionPane.showMessageDialog(
					null,
					e.getMessage(),
					e.getClass().getSimpleName(),
					JOptionPane.ERROR_MESSAGE
				);
			}
			System.exit(1);
		}
		
		if (opt.help) {
			parser.printUsage(System.out);
			System.exit(0);
		} else if (opt.noGui) {
			if (opt.assignmentDirs == null || opt.assignmentDirs.isEmpty()) {
				parser.printUsage(System.out);
				System.exit(2);
			}
			if (opt.assignmentDirs.size() > 1 && opt.assignmentName != null) {
				parser.printUsage(System.out);
				System.exit(3);
			}
			
			GdiPdfCli.main(opt);
		} else {
			/*
			try {
				String defaultLAF = UIManager.getSystemLookAndFeelClassName();
				if ("javax.swing.plaf.metal.MetalLookAndFeel".equals(defaultLAF)) {
					defaultLAF = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
				}
				UIManager.setLookAndFeel(System.getProperty("gdipdf.plaf", defaultLAF));
			}
			catch (Exception e) {
			}
			*/
				
			GdiPdfGui frame = new GdiPdfGui(
				(opt.assignmentDirs == null || opt.assignmentDirs.isEmpty()) ?
				new File(".").getCanonicalPath() :
				opt.assignmentDirs.get(0)
			);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
	}
}
