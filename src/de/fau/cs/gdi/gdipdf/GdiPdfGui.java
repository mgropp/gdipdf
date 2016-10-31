package de.fau.cs.gdi.gdipdf;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.itextpdf.text.DocumentException;

import de.fau.cs.gdi.gdipdf.style.PdfStyle;

/**
 * GdiPdfGui
 * @author Martin Gropp
 */
class GdiPdfGui extends JFrame {
	private static final long serialVersionUID = -2186292876072970159L;
	
	private JTextField inDirTextField;
	private JTextField outDirTextField;
	private JTextField outFileTextField;
	private JTextField assignmentNameTextField;
	private JCheckBox assignmentNameAutoCheckBox;
	private JTextArea logTextArea;
	private JTextArea submissionsTextArea;
	private JButton pdfButton;
	private JComboBox<PdfStyle> styleBox;
	private JCheckBox lineNumberCheckbox;
	private JCheckBox delEmptyCheckbox;
	private JCheckBox dontAskCheckbox;
	
	public GdiPdfGui(String inputDir, String outputDir) {
		setTitle("GdiPdf");
		setSize(600, 470);
		setLocationRelativeTo(null);
		
		getContentPane().setLayout(new GridBagLayout());
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setPreferredSize(getSize());
		tabbedPane.addTab("Grundeinstellungen", createBasicPanel(inputDir, outputDir));
		tabbedPane.addTab("Erweitert", createAdvancedPanel());
		
		logTextArea = new JTextArea();
		logTextArea.setLineWrap(true);
		logTextArea.setWrapStyleWord(true);
		logTextArea.setEditable(false);
		logTextArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		JScrollPane scrollPane = new JScrollPane(logTextArea); 
		scrollPane.setPreferredSize(getSize());

		pdfButton = new JButton(pdfAction);

		GridBagConstraints c = new GridBagConstraints(
			0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
			GridBagConstraints.NONE, new Insets(4, 4, 4, 4),
			0, 0
		);
		
		//----------------------------------
		c.gridx = 0;
		c.gridy++;
		
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.BOTH;
		getContentPane().add(tabbedPane, c);
		
		//----------------------------------
		c.gridx = 0;
		c.gridy++;
		
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		getContentPane().add(scrollPane, c);

		//----------------------------------
		c.gridx = 0;
		c.gridy++;
		
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.CENTER;
		getContentPane().add(pdfButton, c);

		setVisible(true);
		
		log("Version: " + GdiPdf.VERSION);
		
		loadSettings();
		assignmentNameListener.changedUpdate(null);
	}
	
	private JPanel createBasicPanel(String inputDir, String outputDir) {
		JPanel panel = new JPanel();
		panel.setPreferredSize(getSize());
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		
		JLabel inDirLabel = new JLabel("Aufgaben-Verzeichnis:");
		
		inDirTextField = new JTextField(inputDir);
		Dimension inDirSize = inDirTextField.getPreferredSize();
		inDirSize.width = 250;
		inDirTextField.setPreferredSize(inDirSize);
		
		inDirTextField.getDocument().addDocumentListener(assignmentNameListener);
		
		JButton inDirBrowseButton = new JButton(inDirBrowseAction);
		
		JLabel outDirLabel = new JLabel("Ausgabe-Verzeichnis:");
		
		outDirTextField = new JTextField(outputDir);
		Dimension outDirSize = inDirTextField.getPreferredSize();
		outDirSize.width = 250;
		outDirTextField.setPreferredSize(inDirSize);
		
		JButton outDirBrowseButton = new JButton(outDirBrowseAction);
		
		JLabel assignmentLabel = new JLabel("Name der Aufgabe:");
		assignmentNameTextField = new JTextField();
		assignmentNameTextField.setPreferredSize(inDirSize);
		
		assignmentNameAutoCheckBox = new JCheckBox("Auto", true);
		
		lineNumberCheckbox = new JCheckBox("Zeilennummern");
		lineNumberCheckbox.setMnemonic('Z');
		delEmptyCheckbox = new JCheckBox("Leere Verzeichnisse löschen");
		delEmptyCheckbox.setMnemonic('L');
		dontAskCheckbox = new JCheckBox("Existierende Dateien ohne Nachfrage überschreiben");
		dontAskCheckbox.setMnemonic('E');
		
		JLabel styleLabel = new JLabel("PDF-Stil:");
		Vector<PdfStyle> styles = new Vector<>();
		styles.addAll(Common.getPdfStyles());
		styleBox = new JComboBox<PdfStyle>(styles);
		
		JPanel stylePanel = new JPanel();
		stylePanel.add(styleBox);
		stylePanel.add(lineNumberCheckbox);
		
		GridBagConstraints c = new GridBagConstraints(
			0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
			GridBagConstraints.NONE, new Insets(4, 4, 4, 4),
			0, 0
		);
		
		//----------------------------------
		panel.add(inDirLabel, c);
		
		c.gridx++;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(inDirTextField, c);

		c.gridx++;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		panel.add(inDirBrowseButton, c);
		
		//----------------------------------
		c.gridx = 0;
		c.gridy++;
		
		panel.add(outDirLabel, c);
		
		c.gridx++;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(outDirTextField, c);

		c.gridx++;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		panel.add(outDirBrowseButton, c);
		
		//----------------------------------
		c.gridx = 0;
		c.gridy++;
		
		panel.add(assignmentLabel, c);
		
		c.gridx++;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(assignmentNameTextField, c);
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		
		//c.gridx++;
		//panel.add(assignmentNameAutoCheckBox, c);
		
		//----------------------------------
		c.gridx = 0;
		c.gridy++;
		
		c.gridwidth = 1;
		c.gridheight = 1;
		panel.add(styleLabel, c);

		c.gridx++;
		c.gridwidth = 2;
		panel.add(stylePanel, c);

		//----------------------------------
		c.gridx = 0;
		c.gridy++;
		
		c.gridwidth = 3;
		panel.add(delEmptyCheckbox, c);
		
		//----------------------------------
		c.gridx = 0;
		c.gridy++;
		
		c.gridwidth = 3;
		panel.add(dontAskCheckbox, c);
		
		return panel;
	}
	
	private JPanel createAdvancedPanel() {
		JPanel panel = new JPanel();
		panel.setPreferredSize(getSize());
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		
		JLabel outFileLabel = new JLabel("Name der Ausgabe-Datei:");
		outFileTextField = new JTextField();
		outFileTextField.setText(Common.DEFAULT_OUTPUT_FILENAME_PATTERN);
		Dimension outFileSize = outFileTextField.getPreferredSize();
		outFileSize.width = 250;
		outFileTextField.setPreferredSize(outFileSize);
		
		JButton outFileDefaultButton = new JButton(outFileDefaultAction);
		
		outFileLabel.setToolTipText("Variablen: ${basename}, ${extension}, ${filename}");
		outFileTextField.setToolTipText("Variablen: ${basename}, ${extension}, ${filename}");
		
		JLabel submissionsLabel = new JLabel("Nur folgende Abgaben konvertieren (eine Submission-ID pro Zeile):");
		
		submissionsTextArea = new JTextArea();
		submissionsTextArea.setLineWrap(true);
		submissionsTextArea.setWrapStyleWord(true);
		submissionsTextArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		JScrollPane scrollPane = new JScrollPane(submissionsTextArea); 
		scrollPane.setPreferredSize(getSize());
		
		GridBagConstraints c = new GridBagConstraints(
			0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
			GridBagConstraints.NONE, new Insets(4, 4, 4, 4),
			0, 0
		);
		
		//----------------------------------
		c.gridx = 0;
		c.gridy++;
		
		panel.add(outFileLabel, c);
		
		c.gridx++;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(outFileTextField, c);
		
		c.gridx++;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		c.weighty = 0.0;
		panel.add(outFileDefaultButton, c);
		
		//----------------------------------
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		
		panel.add(submissionsLabel, c);

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		panel.add(scrollPane, c);
		
		return panel;
	}
	
	private DocumentListener assignmentNameListener = new DocumentListener() {
		private void update() {
			if (assignmentNameAutoCheckBox.isSelected()) {
				String name = Common.getAssignmentName(new File(inDirTextField.getText()));
				if (name != null) {
					assignmentNameTextField.setText(name);
				}
			}
		}
		
		@Override
		public void changedUpdate(DocumentEvent event) {
			update();
		}

		@Override
		public void insertUpdate(DocumentEvent event) {
			update();
		}

		@Override
		public void removeUpdate(DocumentEvent event) {
			update();	
		}
	};
	
	private Action inDirBrowseAction;
	{
		inDirBrowseAction = new AbstractAction("Durchsuchen") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent event) {
				File dir = inDirTextField.getText().isEmpty() ? new File(".") : new File(inDirTextField.getText());
				if (!dir.exists() || !dir.isDirectory()) {
					dir = new File(".");
				}
				JFileChooser chooser = new JFileChooser(); 
				chooser.setCurrentDirectory(dir);
				chooser.setDialogTitle("Verzeichnis auswählen");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				if (chooser.showOpenDialog(GdiPdfGui.this) == JFileChooser.APPROVE_OPTION) {
					try {
						inDirTextField.setText(chooser.getSelectedFile().getCanonicalPath());
					}
					catch (IOException e) {
						inDirTextField.setText(chooser.getSelectedFile().getAbsolutePath());
					}
				}
			}
		};

		inDirBrowseAction.putValue(Action.MNEMONIC_KEY, (int)'D');
	}
	
	private Action outDirBrowseAction;
	{
		outDirBrowseAction = new AbstractAction("Durchsuchen") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent event) {
				File dir = outDirTextField.getText().isEmpty() ? new File(".") : new File(outDirTextField.getText());
				if (!dir.exists() || !dir.isDirectory()) {
					dir = new File(".");
				}
				JFileChooser chooser = new JFileChooser(); 
				chooser.setCurrentDirectory(dir);
				chooser.setDialogTitle("Verzeichnis auswählen");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				if (chooser.showOpenDialog(GdiPdfGui.this) == JFileChooser.APPROVE_OPTION) {
					try {
						outDirTextField.setText(chooser.getSelectedFile().getCanonicalPath());
					}
					catch (IOException e) {
						outDirTextField.setText(chooser.getSelectedFile().getAbsolutePath());
					}
				}
			}
		};

		inDirBrowseAction.putValue(Action.MNEMONIC_KEY, (int)'U');
	}
	
	private Action outFileDefaultAction;
	{
		outFileDefaultAction = new AbstractAction("Zurücksetzen") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent event) {
				outFileTextField.setText(Common.DEFAULT_OUTPUT_FILENAME_PATTERN);
			}
		};
		
		outFileDefaultAction.putValue(Action.MNEMONIC_KEY, (int)'Z');
	}

	private Action pdfAction; 
	{
		pdfAction = new AbstractAction("Nach PDF konvertieren") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent event) {
				pdfButton.setEnabled(false);
				saveSettings();
				
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							String inDirName = inDirTextField.getText().trim();
							if (inDirName.startsWith("~/")) {
								inDirName = System.getProperty("user.home") + inDirName.substring(1);
							}
							File inDir = new File(inDirName);
							
							String outDirName = outDirTextField.getText().trim();
							if (outDirName.startsWith("~/")) {
								outDirName = System.getProperty("user.home") + outDirName.substring(1);
							}
							File outDir = new File(outDirName);
							
							if (!outDir.isDirectory()) {
								if (!outDir.mkdirs()) {
									throw new IOException(String.format("Ausgabe-Verzeichnis konnte nicht angelegt werden: '%s'", outDir.getCanonicalPath()));
								}
							}
							
							String outFilePattern = outFileTextField.getText().trim();
							
							String assignmentNameAuto = Common.getAssignmentName(inDir);
							if (assignmentNameAuto == null) {
								throw new IllegalArgumentException(String.format("Der Verzeichnisname '%s' scheint kein EST-Verzeichnis zu sein. Erwartet wird etwas wie '1_Aufgabe_1_2_Foo_855'.", inDir.getCanonicalPath()));
							}
							
							String assignmentNameManual = assignmentNameTextField.getText().trim();
							String assignmentName = assignmentNameManual.isEmpty() ? assignmentNameAuto : assignmentNameManual;
							
							convertToPdf(inDir, outDir, outFilePattern, assignmentName, delEmptyCheckbox.isSelected(), dontAskCheckbox.isSelected());
						}
						catch (final Throwable e) {
							e.printStackTrace();
							SwingUtilities.invokeLater(
									new Runnable() {
										@Override
										public void run() {
											log(e);
											JOptionPane.showMessageDialog(
												GdiPdfGui.this,
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
		System.out.print(msgStr);

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

	private void convertToPdf(
		File assignmentDir,
		File outputDir,
		String outputFilePattern,
		String assignmentName,
		boolean delEmpty,
		boolean dontAsk
	) throws IOException, DocumentException {
		if (!assignmentDir.isDirectory()) {
			throw new IllegalArgumentException(String.format("'%s' ist kein Verzeichnis!", assignmentDir.toString()));
		}
		
		PdfStyle pdfStyle = (PdfStyle)styleBox.getSelectedItem();
		pdfStyle.setLineNumbers(lineNumberCheckbox.isSelected());
		
		log("Aufgabenname: " + assignmentName);
		log("PDF-Stil: " + pdfStyle);	

		int numConverted = 0;
		
		studentLoop:
		for (File studentDir : assignmentDir.listFiles()) {
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
						JOptionPane.showConfirmDialog(
							GdiPdfGui.this,
							String.format("Die Datei '%s' existiert bereits. Überschreiben?", outFile.getAbsoluteFile()),
							"Überschreiben?",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE
						)
					) {
					case JOptionPane.YES_OPTION:
						break;
						
					case JOptionPane.CANCEL_OPTION:
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

	private void loadSettings() {
		delEmptyCheckbox.setSelected(Preferences.userNodeForPackage(GdiPdf.class).getBoolean("delete_empty_directories", true));
		dontAskCheckbox.setSelected(!Preferences.userNodeForPackage(GdiPdf.class).getBoolean("confirm_overwrite", false));
		lineNumberCheckbox.setSelected(Preferences.userNodeForPackage(GdiPdf.class).getBoolean("line_numbers", false));
		String pdfStyle = Preferences.userNodeForPackage(GdiPdf.class).get("pdf_style", null);
		if (pdfStyle != null) {
			for (PdfStyle style : Common.getPdfStyles()) {
				if (pdfStyle.equals(style.getClass().getCanonicalName())) {
					styleBox.setSelectedItem(style);
					break;
				}
			}
		}
		outFileTextField.setText(Preferences.userNodeForPackage(GdiPdf.class).get("output_filename_pattern", Common.DEFAULT_OUTPUT_FILENAME_PATTERN));
	}
	
	private void saveSettings() {
		Preferences.userNodeForPackage(GdiPdf.class).putBoolean("delete_empty_directories", delEmptyCheckbox.isSelected());
		Preferences.userNodeForPackage(GdiPdf.class).putBoolean("confirm_overwrite", !dontAskCheckbox.isSelected());
		Preferences.userNodeForPackage(GdiPdf.class).putBoolean("line_numbers", lineNumberCheckbox.isSelected());
		Preferences.userNodeForPackage(GdiPdf.class).put("pdf_style", styleBox.getSelectedItem().getClass().getCanonicalName());
		Preferences.userNodeForPackage(GdiPdf.class).put("output_filename_pattern", outFileTextField.getText().trim());
	}
}
