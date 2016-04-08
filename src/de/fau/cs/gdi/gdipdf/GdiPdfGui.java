package de.fau.cs.gdi.gdipdf;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.fau.cs.gdi.gdipdf.style.PdfStyle;

import com.itextpdf.text.DocumentException;

class GdiPdfGui extends JFrame {
	private static final long serialVersionUID = -2186292876072970159L;
	
	private final JTextField dirTextField;
	private final JTextField assignmentNameTextField;
	private final JCheckBox assignmentNameAutoCheckBox;
	private final JTextArea logTextArea;
	private final JButton pdfButton;
	private final JComboBox styleBox;
	private final JCheckBox lineNumberCheckbox;
	private final JCheckBox delEmptyCheckbox;
	private final JCheckBox dontAskCheckbox;
	
	public GdiPdfGui(String assignmentDir) {
		setTitle("GdiPdf");
		setSize(600, 470);
		setLocationRelativeTo(null);

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));


		JLabel dirLabel = new JLabel("Aufgaben-Verzeichnis:");

		dirTextField = new JTextField(assignmentDir);
		Dimension size = dirTextField.getPreferredSize();
		size.width = 250;
		dirTextField.setPreferredSize(size);
		
		dirTextField.getDocument().addDocumentListener(assignmentNameListener);
				
		JButton browseButton = new JButton(browseAction);

		JLabel assignmentLabel = new JLabel("Name der Aufgabe:");
		assignmentNameTextField = new JTextField();
		assignmentNameTextField.setPreferredSize(size);
		
		assignmentNameAutoCheckBox = new JCheckBox("Auto", true);
		
		JLabel styleLabel = new JLabel("PDF-Stil:");
		styleBox = new JComboBox(GdiPdf.getPdfStyles().toArray());

		lineNumberCheckbox = new JCheckBox("Zeilennummern");
		lineNumberCheckbox.setMnemonic('Z');
		delEmptyCheckbox = new JCheckBox("Leere Verzeichnisse löschen");
		delEmptyCheckbox.setMnemonic('L');
		dontAskCheckbox = new JCheckBox("Existierende Dateien ohne Nachfrage überschreiben");
		dontAskCheckbox.setMnemonic('E');
		
		logTextArea = new JTextArea();
		logTextArea.setLineWrap(true);
		logTextArea.setWrapStyleWord(true);
		logTextArea.setEditable(false);
		logTextArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		JScrollPane scrollPane = new JScrollPane(logTextArea); 
		scrollPane.setPreferredSize(getSize());

		pdfButton = new JButton(pdfAction);

		JPanel stylePanel = new JPanel();
		stylePanel.add(styleBox);
		stylePanel.add(lineNumberCheckbox);
			
		GridBagConstraints c = new GridBagConstraints(
				0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
				GridBagConstraints.NONE, new Insets(4, 4, 4, 4),
				0, 0
		);
		panel.add(dirLabel, c);

		c.gridx++;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(dirTextField, c);

		c.gridx++;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		panel.add(browseButton, c);

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
		
		//----------------------------------
		c.gridx = 0;
		c.gridy++;
		
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		panel.add(scrollPane, c);

		//----------------------------------
		c.gridx = 0;
		c.gridy++;
		
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.CENTER;
		panel.add(pdfButton, c);

		setVisible(true);
		
		log("Version: " + GdiPdf.VERSION);
		
		loadSettings();
		assignmentNameListener.changedUpdate(null);
	}

	private DocumentListener assignmentNameListener = new DocumentListener() {
		private void update() {
			if (assignmentNameAutoCheckBox.isSelected()) {
				String name = GdiPdf.getAssignmentName(new File(dirTextField.getText()));
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
				if (chooser.showOpenDialog(GdiPdfGui.this) == JFileChooser.APPROVE_OPTION) {
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
				saveSettings();
				
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							String dir = dirTextField.getText().trim();
							if (dir.startsWith("~/")) {
								dir = System.getProperty("user.home") + dir.substring(1);
							}

							File assignmentDir = new File(dir);
							String dirName = assignmentDir.getCanonicalFile().getName();
							String assignmentNameAuto = GdiPdf.getAssignmentName(assignmentDir);
							
							if (assignmentNameAuto == null) {
								throw new IllegalArgumentException(String.format("Der Verzeichnisname '%s' scheint kein EST-Verzeichnis zu sein. Erwartet wird etwas wie '1_Aufgabe_1_2_Foo_855'.", dirName));
							}

							String assignmentNameManual = assignmentNameTextField.getText().trim();
							String assignmentName = assignmentNameManual.isEmpty() ? assignmentNameAuto : assignmentNameManual;

							convertToPdf(assignmentDir, assignmentName, delEmptyCheckbox.isSelected(), dontAskCheckbox.isSelected());
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

	private void convertToPdf(File assignmentDir, String assignmentName, boolean delEmpty, boolean dontAsk) throws IOException, DocumentException {
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

			String studentName = GdiPdf.getStudentName(studentDir); 
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
				if (!inFile.getName().endsWith(".java")) {
					continue;
				}
				File outFile = GdiPdf.getPdfFilename(inFile);

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
				
				GdiPdf.convertFileToPdf(inFile, outFile, pdfStyle);
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
			for (PdfStyle style : GdiPdf.getPdfStyles()) {
				if (pdfStyle.equals(style.getClass().getCanonicalName())) {
					styleBox.setSelectedItem(style);
					break;
				}
			}
		}
	}
	
	private void saveSettings() {
		Preferences.userNodeForPackage(GdiPdf.class).putBoolean("delete_empty_directories", delEmptyCheckbox.isSelected());
		Preferences.userNodeForPackage(GdiPdf.class).putBoolean("confirm_overwrite", !dontAskCheckbox.isSelected());
		Preferences.userNodeForPackage(GdiPdf.class).putBoolean("line_numbers", lineNumberCheckbox.isSelected());
		Preferences.userNodeForPackage(GdiPdf.class).put("pdf_style", styleBox.getSelectedItem().getClass().getCanonicalName());
	}
}
