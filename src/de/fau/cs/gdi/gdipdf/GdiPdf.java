package de.fau.cs.gdi.gdipdf;

import java.io.File;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * GdiPdf
 * @author Martin Gropp
 * @version $buildinfo: 2016-11-13 11:12 martin$
 */
public class GdiPdf {
	private static final String BUILDINFO = "$buildinfo: 2016-11-13 11:12 martin$";
	public static final String VERSION = BUILDINFO.substring(BUILDINFO.indexOf(' ')+1, BUILDINFO.length()-1);
	
	public static class Options {
		@Option(name="--no-gui", usage="Keine grafische Benutzeroberfläche anzeigen")
		public boolean noGui;
		
		@Option(name="--assignment-name", usage="Namen der Aufgabe manuell festlegen (nur bei --no-gui, höchstens ein Aufgabenverzeichnis)")
		public String assignmentName = null;
		
		@Option(name="--pdf-style", usage="Klassenname des PDF-Stils (nur bei --no-gui)")
		public String pdfStyle = Common.pdfStyles.get(0).getClass().getCanonicalName();
		
		@Option(name="--del-empty-dirs", usage="Leere Verzeichnisse löschen (nur bei --no-gui)")
		public boolean delEmptyDirs;
		
		@Option(name="--overwrite", usage="Existierende Dateien ohne Nachfrage überschreiben (nur bei --no-gui)")
		public boolean overwrite;
		
		@Option(name="--line-numbers", usage="Zeilennummern (nur bei --no-gui)")
		public boolean lineNumbers;
		
		@Option(name="--help", usage="Hilfetext anzeigen")
		public boolean help;
		
		@Option(name="--output-dir", usage="Ausgabe-Verzeichnis (Standard: Temp-Verzeichnis)")
		public String outputDir = null;
		
		@Option(name="--output-file-pattern", usage="Ausgabe-Dateiname mit Variablen ${basename}, ${extension}, ${filename} (Standard: " + Common.DEFAULT_OUTPUT_FILENAME_PATTERN + ")")
		public String outputFilePattern;
		
		@Option(name="--submissions", usage="Nur Abgaben mit Submission-IDs aus dieser Datei (eine pro Zeile) konvertieren")
		public String submissions;
		
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
		}
		
		opt.outputDir = (opt.outputDir != null) ? opt.outputDir : System.getProperty("java.io.tmpdir");
		
		if (opt.noGui) {
			if (opt.assignmentDirs == null || opt.assignmentDirs.isEmpty()) {
				parser.printUsage(System.out);
				System.exit(3);
			}
			if (opt.assignmentDirs.size() > 1 && opt.assignmentName != null) {
				parser.printUsage(System.out);
				System.exit(4);
			}
			
			GdiPdfCli.main(opt);
		
		} else {
			GdiPdfGui frame = new GdiPdfGui(
				(opt.assignmentDirs == null || opt.assignmentDirs.isEmpty()) ?
				new File(".").getCanonicalPath() :
				opt.assignmentDirs.get(0),
				opt.outputDir != null ? opt.outputDir : System.getProperty("java.io.tmpdir")
			);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
	}
}
