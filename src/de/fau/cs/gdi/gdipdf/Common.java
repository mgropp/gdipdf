package de.fau.cs.gdi.gdipdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.fau.cs.gdi.gdipdf.style.DefaultStyle;
import de.fau.cs.gdi.gdipdf.style.DefaultStylePortrait;
import de.fau.cs.gdi.gdipdf.style.PdfStyle;
import de.fau.cs.gdi.gdipdf.style.SimpleStyle;
import de.fau.cs.gdi.gdipdf.style.SimpleStylePortrait;

public class Common {
	private static final Pattern assignmentDirPattern = Pattern.compile("([^_]+)_(.*)_([^_]+)");
	private static final Pattern intPattern = Pattern.compile("[0-9]+");
	
	public static final List<PdfStyle> pdfStylesLandscape;
	public static final List<PdfStyle> pdfStylesPortrait;
	public static final List<PdfStyle> pdfStyles;
	static {
		pdfStylesLandscape = new ArrayList<PdfStyle>();
		pdfStylesPortrait = new ArrayList<PdfStyle>();
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

		pdfStylesLandscape.add(new DefaultStyle());
		pdfStylesLandscape.add(new SimpleStyle());
		pdfStylesPortrait.add(new DefaultStylePortrait());
		pdfStylesPortrait.add(new SimpleStylePortrait());
		
		if (!DefaultStyle.class.equals(customClass)) {
			pdfStyles.add(new DefaultStyle());
		}
		if (!DefaultStylePortrait.class.equals(customClass)) {
			pdfStyles.add(new DefaultStylePortrait());
		}
		if (!SimpleStyle.class.equals(customClass)) {
			pdfStyles.add(new SimpleStyle());
		}
		if (!SimpleStylePortrait.class.equals(customClass)) {
			pdfStyles.add(new SimpleStyle());
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
	
	public static File getPdfFilename(File inputFile, File outputDir) throws IOException {
		String studentName = getStudentDirNameFromInputFile(inputFile);
		String assignmentName = getAssignmentDirNameFromInputFile(inputFile);
		
		String inputFileName = inputFile.getName();
		int dotIndex = inputFileName.lastIndexOf('.');
		if (dotIndex >= 0) {
			inputFileName = inputFileName.substring(0, dotIndex);
		}
		inputFileName += ".pdf";
		
		File outputDirForFile = new File(new File(outputDir, assignmentName), studentName);
		if (!outputDirForFile.isDirectory()) {
			if (!outputDirForFile.mkdirs()) {
				throw new IOException(String.format("Ausgabe-Verzeichnis '%s' konnte nicht angelegt werden.", outputDirForFile.getCanonicalPath()));
			}
		}
		
		return new File(outputDirForFile, inputFileName);
	}
}
