package de.fau.cs.gdi.gdipdf;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Locale;

public abstract class Lexer {
	public static final byte PLAIN_STYLE = 1;
	public static final byte KEYWORD_STYLE = 2;
	public static final byte TYPE_STYLE = 3;
	public static final byte OPERATOR_STYLE = 4;
	public static final byte SEPARATOR_STYLE = 5;
	public static final byte LITERAL_STYLE = 6;
	public static final byte COMMENT_STYLE = 7;
	public static final byte JAVADOC_COMMENT_STYLE = 8;
	public static final byte JAVADOC_TAG_STYLE = 9;
	public static final byte TUTOR_COMMENT_STYLE = 10;

	private static Lexer javaLexer = null;
	private static Lexer pythonLexer = null;
	private static Lexer plainLexer = null;

	public abstract void setReader(Reader r);
	public abstract byte getNextToken() throws IOException;
	public abstract String yytext();

	public static Lexer getLexer(File file) {
		String name = file.getName().toLowerCase();
		if (name.endsWith(".java")) {
			if (javaLexer == null) {
				javaLexer = new JavaLexer();
			}
			return javaLexer;
		} else if (name.endsWith(".py")) {
			if (pythonLexer == null) {
				pythonLexer = new PythonLexer();
			}
			return pythonLexer;
		} else if (name.endsWith(".txt")) {
			if (plainLexer == null) {
				plainLexer = new PlainLexer();
			}
			return plainLexer;
		} else {
			return null;
		}
	}
}
