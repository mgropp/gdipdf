package de.fau.cs.gdi.gdipdf;

/**
 * TokenStyle
 * @author Martin Gropp
 */
public enum TokenStyle {
	PLAIN_STYLE,
	KEYWORD_STYLE,
	TYPE_STYLE,
	OPERATOR_STYLE,
	SEPARATOR_STYLE,
	LITERAL_STYLE,
	COMMENT_STYLE,
	JAVADOC_COMMENT_STYLE,
	JAVADOC_TAG_STYLE,
	LINE_NUMBER_STYLE,
	TUTOR_COMMENT_STYLE;

	public static TokenStyle fromNumber(byte number) {
		switch (number) {
		case Lexer.PLAIN_STYLE:
			return PLAIN_STYLE;
		case Lexer.KEYWORD_STYLE:
			return KEYWORD_STYLE;
		case Lexer.TYPE_STYLE:
			return TYPE_STYLE;
		case Lexer.OPERATOR_STYLE:
			return OPERATOR_STYLE;
		case Lexer.SEPARATOR_STYLE:
			return SEPARATOR_STYLE;
		case Lexer.LITERAL_STYLE:
			return LITERAL_STYLE;
		case Lexer.COMMENT_STYLE:
			return COMMENT_STYLE;
		case Lexer.JAVADOC_COMMENT_STYLE:
			return JAVADOC_COMMENT_STYLE;
		case Lexer.JAVADOC_TAG_STYLE:
			return JAVADOC_TAG_STYLE;
		case Lexer.TUTOR_COMMENT_STYLE:
			return TUTOR_COMMENT_STYLE;
		default:
			throw new IllegalArgumentException();
		}
	}
}
