package de.fau.cs.gdi.gdipdf;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Calendar;

import com.itextpdf.text.DocumentException;

import de.fau.cs.gdi.gdipdf.style.ClassicStyle;
import de.fau.cs.gdi.gdipdf.style.PdfStyle;

/**
 * GdiPdfCli
 * @author Martin Gropp
 */
class GdiPdfCli {
	private static void log(Object msg) {
		Calendar now = Calendar.getInstance();
		final String msgStr = String.format(
			"[%2d:%02d:%02d.%03d] %s\n",
			now.get(Calendar.HOUR_OF_DAY),
			now.get(Calendar.MINUTE),
			now.get(Calendar.SECOND),
			now.get(Calendar.MILLISECOND),
			msg.toString()
		);
		System.out.print(msgStr);
	}
	
	private static char confirm(Object msg, String options) {
		System.out.println(msg);

		Reader reader = new InputStreamReader(System.in);
		int answer;
		do {
			System.out.print("[" + options + "]? ");
			try {
				while (reader.ready()) {
					reader.read();
				}
				answer = reader.read();
			}
			catch (IOException e) {
				return '\0';
			}
			System.out.println();
		} while (answer >= 0 && options.indexOf(answer) < 0); 
		
		return (answer < 0) ? '\0' : (char)answer;
	}
	
	private static void convertToPdf(
		File inputDir,
		File outputDir,
		String outputFilePattern,
		String assignmentName,
		PdfStyle pdfStyle,
		boolean delEmpty,
		boolean dontAsk
	) throws IOException, DocumentException {
		if (!inputDir.isDirectory()) {
			throw new IllegalArgumentException(String.format("'%s' ist kein Verzeichnis!", inputDir.toString()));
		}
		
		log("Aufgabenname: " + assignmentName);
		log("PDF-Stil: " + pdfStyle);	

		int numConverted = 0;
		
		studentLoop:
		for (File studentDir : inputDir.listFiles()) {
			if (!studentDir.isDirectory()) {
				continue;
			}

			String studentName = Common.getStudentName(studentDir); 
			log(studentName);

			File[] fileList = studentDir.listFiles();
			if (fileList.length == 0 && delEmpty) {
				log("Verzeichnis ist leer: Löschen.");
				try {
					studentDir.delete();
				}
				catch (Exception e) {
					log(e);
				}
			}
			
			for (File inFile : fileList) {
				if (
					!inFile.getName().endsWith(".java") &&
					!inFile.getName().endsWith(".pdf")
				) {
					continue;
				}
				File outFile = Common.getPdfFilename(inFile, outputDir, outputFilePattern);

				if (outFile.exists() && !dontAsk) {
					switch (
						Character.toLowerCase(
							confirm(
								String.format("Die Datei '%s' existiert bereits. Überschreiben ([J]a/[N]ein/[A]lle/[E]nde)?", outFile.getAbsoluteFile()),
								"jnae"
							)
						)
					) {
					case 'a':
						dontAsk = true;
					case 'j':
						break;
						
					case 'e':
						log("Abgebrochen.");
						break studentLoop;
						
					default:
						continue;
					}
				}
				
				log("Umwandeln: " + inFile.getName() + " -> " + outFile.getName() + "...");

				pdfStyle.setStudentName(studentName);
				pdfStyle.setFileName(inFile.getName());
				pdfStyle.setAssignmentName(assignmentName);
				
				if (inFile.getName().endsWith(".java")) {
					Processors.convertFileToPdf(inFile, outFile, pdfStyle);
				} else if (inFile.getName().endsWith(".pdf")) {
					Processors.decoratePdf(inFile, outFile, pdfStyle.asPortrait(), pdfStyle.asLandscape());
				} else {
					throw new AssertionError();
				}
				
				numConverted++;
			}
		}

		log(String.format("Fertig. Es wurden %d PDF-Dateien erzeugt.", numConverted));
	}
	
	public static void main(GdiPdf.Options opt) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, DocumentException {
		String pdfStyleClass = opt.pdfStyle;
		PdfStyle pdfStyle;
		for (;;) {
			try {
				pdfStyle = (PdfStyle)Class.forName(pdfStyleClass).newInstance();
			}
			catch (ClassNotFoundException e) {
				if (pdfStyleClass.indexOf('.') < 0) {
					pdfStyleClass = ClassicStyle.class.getPackage().getName() + "." + pdfStyleClass;
					continue;
				} else {
					throw e;
				}
			}
		
			break;
		}
		
		pdfStyle.setLineNumbers(opt.lineNumbers);
		
		File outputDir = new File(opt.outputDir);
		if (!outputDir.isDirectory()) {
			if (!outputDir.mkdirs()) {
				log("Ausgabe-Verzeichnis konnte nicht erstellt werden: " + outputDir);
				System.exit(10);
			}
		}
		
		for (String assignmentDirName : opt.assignmentDirs) {
			File assignmentDir = new File(assignmentDirName);
			
			String assignmentName = opt.assignmentName; 
			if (assignmentName == null) {
				assignmentName = Common.getAssignmentName(assignmentDir);
			}
			
			convertToPdf(assignmentDir, outputDir, opt.outputFilePattern, assignmentName, pdfStyle, opt.delEmptyDirs, opt.overwrite);
		}
	}
}
