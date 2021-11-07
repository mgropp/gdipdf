package de.fau.cs.gdi.gdipdf;

import de.fau.cs.gdi.gdipdf.style.ClassicStyle;
import de.fau.cs.gdi.gdipdf.style.PdfStyle;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Moodle {
	public static class Options {
		@Option(name="--assignment-name", usage="Namen der Aufgabe manuell festlegen")
		public String assignmentName = null;

		@Option(name="--pdf-style", usage="Klassenname des PDF-Stils")
		public String pdfStyle = Common.pdfStyles.get(0).getClass().getCanonicalName();

		@Option(name="--output-dir", usage="Ausgabe-Verzeichnis (Standard: Temp-Verzeichnis)")
		public String outputDir = null;

		@Argument(required=true)
		public File assignmentDir = null;
	}

	private static class MoodleSubmission {
		File file;
		String student;
		String assignment;

		@Override
		public String toString() {
			return String.format(
				"[file=%s, student=%s, assignment=%s]",
				file,
				student,
				assignment
			);
		}
	}

	private static List<MoodleSubmission> findMoodleSubmissions(
		File baseDir,
		String assignment
	) {
		List<MoodleSubmission> submissions = new ArrayList<>();
		for (File dir : baseDir.listFiles()) {
			if (!dir.isDirectory()) {
				continue;
			}

			String name = dir.getName();
			String[] parts = name.split("_");
			if (parts.length == 1) {
				System.err.println("Unexpected directory name format: " + name);
				continue;
			}
			String student = parts[0];

			for (File file : dir.listFiles()) {
				if (file.isDirectory()) {
					System.err.println("Unexpected directory found in assignment directory: " + file);
					continue;
				}

				String filename = file.getName();

				MoodleSubmission sub = new MoodleSubmission();
				sub.file = file;
				sub.student = student;
				sub.assignment = assignment;
				submissions.add(sub);
			}
		}

		return submissions;
	}

	private static File getPdfFile(File inputFile) {
		String name = inputFile.getName();
		int idx = name.lastIndexOf('.');
		String base;
		if (idx < 0 || name.endsWith(".pdf")) {
			base = name;
		} else {
			base = name.substring(0, idx);
		}

		return new File(inputFile.getParentFile(), base + ".pdf");
	}

	public static void main(String[] args) throws Exception {
		Options opt = new Options();
		CmdLineParser parser = new CmdLineParser(opt);
		try {
			parser.parseArgument(args);
		}
		catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

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

		pdfStyle.setLineNumbers(true);

		List<MoodleSubmission> submissions = findMoodleSubmissions(
			opt.assignmentDir,
			opt.assignmentName
		);

		for (MoodleSubmission submission : submissions) {
			System.out.println(String.format(
				"%s\n\t%s\n\t%s",
				submission.file,
				submission.student,
				submission.assignment
			));


			Lexer lexer = Lexer.getLexer(submission.file);
			if (lexer == null) {
				System.out.println("!!! Unsupported format, skipping.");
				continue;
			}

			pdfStyle.setAssignmentName(opt.assignmentName);
			pdfStyle.setStudentName(submission.student);
			pdfStyle.setFileName(submission.file.getName());

			Processors.convertFileToPdf(
				submission.file,
				getPdfFile(submission.file),
				pdfStyle,
				lexer
			);
		}
	}
}
