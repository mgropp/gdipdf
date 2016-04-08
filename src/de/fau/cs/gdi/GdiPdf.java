package de.fau.cs.gdi;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.ibm.icu.text.CharsetDetector;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfWriter;

import de.fau.cs.gdi.gdipdf.JavaLexer;
import de.fau.cs.gdi.style.DefaultStyle;
import de.fau.cs.gdi.style.DefaultStylePortrait;
import de.fau.cs.gdi.style.PdfStyle;

/**
 * GdiPdf
 * @author Martin Gropp
 * @version $Built: 20160408 1029 gropp$
 */
public class GdiPdf extends JFrame {
	private static final long serialVersionUID = -2186292876072970159L;

	private static final String VERSION = "$Built: 20160408 1029 gropp$"; 

	private static final Pattern assignmentDirPattern = Pattern.compile("([^_]+)_(.*)_([^_]+)");
	private static final Pattern intPattern = Pattern.compile("[0-9]+");

	private static final List<PdfStyle> pdfStyles;

	static {
		pdfStyles = new ArrayList<PdfStyle>();

		String customClassName = System.getProperty("pdfstyle", null);
		Class<?> customClass = null;
		if (customClass != null) {
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
	}

	private final JTextField dirTextField;
	private final JTextArea logTextArea;
	private final JButton pdfButton;
	private final JComboBox styleBox;

	public GdiPdf(String assignmentDir) {
		setTitle("GdiPdf");
		setSize(600, 400);
		setLocationRelativeTo(null);

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setBorder(new EmptyBorder(4, 4, 4, 4));


		JLabel dirLabel = new JLabel("Aufgaben-Verzeichnis:");

		dirTextField = new JTextField(assignmentDir);
		Dimension size = dirTextField.getPreferredSize();
		size.width = 250;
		dirTextField.setPreferredSize(size);

		JButton browseButton = new JButton(browseAction);

		JLabel styleLabel = new JLabel("PDF-Stil:");
		styleBox = new JComboBox(pdfStyles.toArray());

		logTextArea = new JTextArea();
		logTextArea.setLineWrap(true);
		logTextArea.setWrapStyleWord(true);
		logTextArea.setEditable(false);
		logTextArea.setBorder(new EmptyBorder(4, 4, 4, 4));
		JScrollPane scrollPane = new JScrollPane(logTextArea); 
		scrollPane.setPreferredSize(getSize());

		pdfButton = new JButton(pdfAction);

		GridBagConstraints c = new GridBagConstraints(
				0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
				GridBagConstraints.NONE, new Insets(4, 4, 4, 4),
				0, 0
		);
		panel.add(dirLabel, c);

		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(dirTextField, c);

		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		panel.add(browseButton, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		panel.add(styleLabel, c);

		c.gridx = 1;
		panel.add(styleBox, c);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		panel.add(scrollPane, c);

		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;

		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.CENTER;
		panel.add(pdfButton, c);

		log("Version: " + VERSION);
	}

	private Action browseAction;
	{
		browseAction = new AbstractAction("Durchsuchen") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent event) {
				File dir = dirTextField.getText().isEmpty() ? new File(".") : new File(dirTextField.getText());
				if (!dir.exists() || !dir.isDirectory()) {
					dir = new File(".");
				}
				JFileChooser chooser = new JFileChooser(); 
				chooser.setCurrentDirectory(dir);
				chooser.setDialogTitle("Verzeichnis auswählen");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				if (chooser.showOpenDialog(GdiPdf.this) == JFileChooser.APPROVE_OPTION) {
					try {
						dirTextField.setText(chooser.getSelectedFile().getCanonicalPath());
					}
					catch (IOException e) {
						dirTextField.setText(chooser.getSelectedFile().getAbsolutePath());
					}
				}
			}
		};

		browseAction.putValue(Action.MNEMONIC_KEY, (int)'D');
	}

	private Action pdfAction; 
	{
		pdfAction = new AbstractAction("Nach PDF konvertieren") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent event) {
				pdfButton.setEnabled(false);

				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							convertToPdf(new File(dirTextField.getText()));
						}
						catch (final Throwable e) {
							e.printStackTrace();
							SwingUtilities.invokeLater(
									new Runnable() {
										@Override
										public void run() {
											log(e);
											JOptionPane.showMessageDialog(
													GdiPdf.this,
													e.getClass().getSimpleName() + ": " + e.getMessage(),
													e.getClass().getName(),
													JOptionPane.ERROR_MESSAGE
											);
										}
									}
							);
						}
						finally {
							SwingUtilities.invokeLater(
									new Runnable() {
										@Override
										public void run() {
											pdfButton.setEnabled(true);
										}
									}
							);
						}
					}
				});

				thread.start();
			}
		};

		pdfAction.putValue(Action.MNEMONIC_KEY, (int)'P');
	}

	private void log(Object msg) {
		Calendar now = Calendar.getInstance();
		final String msgStr = String.format(
				"[%2d:%02d:%02d.%03d] %s\n",
				now.get(Calendar.HOUR_OF_DAY),
				now.get(Calendar.MINUTE),
				now.get(Calendar.SECOND),
				now.get(Calendar.MILLISECOND),
				msg.toString()
		);

		SwingUtilities.invokeLater(
				new Runnable() {
					@Override
					public void run() {
						logTextArea.append(msgStr);
						logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
					}
				}
		);

	}

	private void convertToPdf(File assignmentDir) throws IOException, DocumentException {
		if (!assignmentDir.isDirectory()) {
			throw new IllegalArgumentException(String.format("'%s' ist kein Verzeichnis!", assignmentDir.toString()));
		}

		String dirName = assignmentDir.getCanonicalFile().getName();
		Matcher matcher = assignmentDirPattern.matcher(dirName);
		if (!matcher.matches()) {
			throw new IllegalArgumentException(String.format("Der Verzeichnisname '%s' scheint kein EST-Verzeichnis zu sein. Erwartet wird etwas wie '1_Aufgabe_1_2_Foo_855'.", dirName));
		}

		String assignmentNumber = matcher.group(1);
		String assignmentName = processAssignmentName(matcher.group(2));;
		log("Aufgabenblatt: " + assignmentNumber);
		log("Aufgabenname: " + assignmentName);

		log("PDF-Stil: " + getStyle());	

		for (File studentDir : assignmentDir.listFiles()) {
			if (!studentDir.isDirectory()) {
				continue;
			}

			String studentName = getStudentName(studentDir); 
			log(studentName);

			for (File inFile : studentDir.listFiles()) {
				if (!inFile.getName().endsWith(".java")) {
					continue;
				}
				File outFile = getPdfFilename(inFile);

				log("Umwandeln: " + inFile.getName() + " -> " + outFile.getName() + "...");
				convertFileToPdf(inFile, outFile, assignmentNumber, assignmentName, studentName);
			}
		}

		log("Fertig.");
	}

	private static String processAssignmentName(String name) {
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

	private static String getStudentName(File studentDir) {
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

	private static File getPdfFilename(File file) {
		String filename = file.getAbsolutePath();
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex >= 0) {
			filename = filename.substring(0, dotIndex);
		}

		return new File(filename + ".pdf");
	}

	private PdfStyle getStyle() {
		return (PdfStyle)styleBox.getSelectedItem();
	}

	private void convertFileToPdf(File inFile, File outFile, String assignmentNumber, String assignmentName, String studentName) throws IOException, DocumentException {
		PdfStyle pdfStyle = getStyle();
		pdfStyle.setStudentName(studentName);
		pdfStyle.setFileName(inFile.getName());
		pdfStyle.setAssignmentName(assignmentName);

		CharsetDetector charsetDetector = new CharsetDetector();
		BufferedInputStream stream = new BufferedInputStream(new FileInputStream(inFile));
		Reader reader = charsetDetector.getReader(stream, null);
		if (reader == null) {
			reader = new InputStreamReader(stream);
		}
		try {
			JavaLexer lexer = new JavaLexer();
			lexer.setReader(reader);

			Document document = new Document(pdfStyle.getPageSize());
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outFile));
			writer.setPageEvent(pdfStyle);

			document.open();

			float leading = pdfStyle.getLeading();
			try {
				Paragraph paragraph = new Paragraph();
				paragraph.setLeading(leading);
				Phrase phrase = new Phrase(leading);

				int lineLength = 0;

				for (;;) {
					byte style = lexer.getNextToken();
					if (style == JavaLexer.YYEOF) {
						if (phrase.size() > 0 || paragraph.size() > 0) {
							paragraph.add(phrase);
							document.add(paragraph);
						}
						break;
					}

					String text = lexer.yytext();

					if ("\r".equals(text)) {
						// ignore
					} else if ("\n".equals(text)) {
						phrase.add("\n");
						paragraph.add(phrase);
						phrase = new Phrase(leading);
						lineLength = 0;
					} else if ("\t".equals(text)) {
						int tabWidth = 4;
						int n = tabWidth - lineLength % tabWidth;
						StringBuilder sb = new StringBuilder();
						for (int i = 0; i < n; i++) {
							sb.append(' ');
						}
						phrase.add(new Chunk(sb.toString(), pdfStyle.getFont(JavaLexer.PLAIN_STYLE)));
						lineLength += n;
					} else {
						String[] tokens = text.split("\n|\r");
						for (int i = 0; i < tokens.length; i++) {
							if (i > 0) {
								phrase.add("\n");
								paragraph.add(phrase);
								phrase = new Phrase(leading);
								lineLength = 0;
							}

							phrase.add(new Chunk(tokens[i], pdfStyle.getFont(style)));
							lineLength += tokens[i].length();
						}

						if (text.endsWith("\n")) {
							phrase.add("\n");
							paragraph.add(phrase);
							phrase = new Phrase(leading);
							lineLength = 0;
						}
					}
				}
			}
			finally {
				document.close();
			}
		}
		finally {
			reader.close();
		}
	}

	public static void main(String[] args) throws IOException {
		GdiPdf frame = new GdiPdf(args.length == 0 ? new File(".").getCanonicalPath() : args[0]);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
