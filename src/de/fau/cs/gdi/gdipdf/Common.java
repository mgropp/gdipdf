package de.fau.cs.gdi.gdipdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.StrSubstitutor;

import de.fau.cs.gdi.gdipdf.style.ClassicStyle;
import de.fau.cs.gdi.gdipdf.style.ClassicStylePortrait;
import de.fau.cs.gdi.gdipdf.style.PdfStyle;
import de.fau.cs.gdi.gdipdf.style.SimpleStyle;
import de.fau.cs.gdi.gdipdf.style.SimpleStylePortrait;

/**
 * Common GdiPdf functions.
 * @author Martin Gropp
 */
public class Common {
	public static final String DEFAULT_OUTPUT_FILENAME_PATTERN = "Korrektur-${filename}.pdf";
	
	private static final Pattern assignmentDirPattern = Pattern.compile("([^_]+)_(.*)_([^_]+)");
	private static final Pattern intPattern = Pattern.compile("[0-9]+");
	
	public static final List<PdfStyle> pdfStyles;
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
		
		if (!ClassicStyle.class.equals(customClass)) {
			pdfStyles.add(new ClassicStyle());
		}
		if (!ClassicStylePortrait.class.equals(customClass)) {
			pdfStyles.add(new ClassicStylePortrait());
		}
		if (!SimpleStyle.class.equals(customClass)) {
			pdfStyles.add(new SimpleStyle());
		}
		if (!SimpleStylePortrait.class.equals(customClass)) {
			pdfStyles.add(new SimpleStylePortrait());
		}
	}
	
	public static List<PdfStyle> getPdfStyles() {
		return pdfStyles;
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
	
	public static boolean isValidStudentDir(File studentDir) {
		String[] tokens = studentDir.getName().split("_");
		return (
			tokens.length > 1 &&
			!tokens[tokens.length-1].trim().isEmpty()
		);
	}

	public static String getSubmissionId(File studentDir) {
		String[] tokens = studentDir.getName().split("_");
		return tokens[tokens.length-1];
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

	private static String getStudentDirNameFromInputFile(File inputFile) {
		return inputFile.getParentFile().getName();
	}
	
	private static String getAssignmentDirNameFromInputFile(File inputFile) {
		return inputFile.getParentFile().getParentFile().getName();
	}
	
	/*
	private static String sanitizeString(String s) {
		s = s.replaceAll("^[a-zA-Z0-9_.-]", "_");
		if (".".equals(s) || "..".equals(s)) {
			return "_";
		} else {
			return s;
		}
	}
	*/
	
	public static File getPdfFilename(File inputFile, File outputDir, String outputFilePattern) throws IOException {
		String studentName = getStudentDirNameFromInputFile(inputFile);
		String assignmentName = getAssignmentDirNameFromInputFile(inputFile);

		String filename = inputFile.getName();
		
		String basename = filename;
		String extension = "";
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex >= 0) {
			basename = filename.substring(0, dotIndex);
			extension = filename.substring(dotIndex+1);
		}
		
		Map<String,String> variables = new HashMap<>();
		variables.put("filename", filename);
		variables.put("basename", basename);
		variables.put("extension", extension);
		
		StrSubstitutor sub = new StrSubstitutor(variables);
		String outputFileName = sub.replace(outputFilePattern);
		
		File outputDirForFile = new File(new File(outputDir, assignmentName), studentName);
		if (!outputDirForFile.isDirectory()) {
			if (!outputDirForFile.mkdirs()) {
				throw new IOException(String.format("Ausgabe-Verzeichnis '%s' konnte nicht angelegt werden.", outputDirForFile.getCanonicalPath()));
			}
		}
		
		return new File(outputDirForFile, outputFileName);
	}
}
