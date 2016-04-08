package de.fau.cs.gdi.gdipdf;

import de.fau.cs.gdi.gdipdf.JavaLexer;

public enum TokenStyle {
	PLAIN_STYLE,
	KEYWORD_STYLE,
	TYPE_STYLE,
	OPERATOR_STYLE,
	SEPARATOR_STYLE,
	LITERAL_STYLE,
	JAVA_COMMENT_STYLE,
	JAVADOC_COMMENT_STYLE,
	JAVADOC_TAG_STYLE,
	LINE_NUMBER_STYLE,
	TUTOR_COMMENT_STYLE;

	public static TokenStyle fromNumber(byte number) {
		switch (number) {
		case JavaLexer.PLAIN_STYLE:
			return PLAIN_STYLE;
		case JavaLexer.KEYWORD_STYLE:
			return KEYWORD_STYLE;
		case JavaLexer.TYPE_STYLE:
			return TYPE_STYLE;
		case JavaLexer.OPERATOR_STYLE:
			return OPERATOR_STYLE;
		case JavaLexer.SEPARATOR_STYLE:
			return SEPARATOR_STYLE;
		case JavaLexer.LITERAL_STYLE:
			return LITERAL_STYLE;
		case JavaLexer.JAVA_COMMENT_STYLE:
			return JAVA_COMMENT_STYLE;
		case JavaLexer.JAVADOC_COMMENT_STYLE:
			return JAVADOC_COMMENT_STYLE;
		case JavaLexer.JAVADOC_TAG_STYLE:
			return JAVADOC_TAG_STYLE;
		case JavaLexer.TUTOR_COMMENT_STYLE:
			return TUTOR_COMMENT_STYLE;
		default:
			throw new IllegalArgumentException();
		}
	}
}
