package de.fau.cs.gdi.gdipdf;

import java.io.Reader;
import java.io.IOException;

%%

%class PlainLexer
%extends Lexer

%unicode
%pack

%buffer 128

%public

%int

%{
	public byte getStartState() {
		return YYINITIAL+1;
	}

	public byte getCurrentState() {
		return (byte) (yystate()+1);
	}

	public void setState(byte newState) {
		yybegin(newState-1);
	}

	public byte getNextToken() throws IOException {
		return (byte) yylex();
	}

	public int getTokenLength() {
		return yylength();
	}

	public void setReader(Reader r) {
		this.zzReader = r;
		this.yyreset(r);
	}

	public PlainLexer() {
	}
%}

%%

<YYINITIAL> {
[^]|\n { return PLAIN_STYLE; }
}