/*
 * Modified by Martin Gropp.
 * Copyright 2000-2006 Omnicore Software, Hans Kratz & Dennis Strein GbR,
 *                     Geert Bevin <gbevin[remove] at uwyn dot com>.
 * Distributed under the terms of either:
 * - the common development and distribution license (CDDL), v1.0; or
 * - the GNU Lesser General Public License, v2.1 or later
 * $Id$
 */
package de.fau.cs.gdi.gdipdf;

import java.io.Reader;
import java.io.IOException;

%%

%class PythonLexer
%extends Lexer

%unicode
%pack

%buffer 128

%public

%int

%{
	public static boolean ASSERT_IS_KEYWORD = true;

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

	public PythonLexer() {
	}
%}

/* main character classes */

WhiteSpace = [ \t\f]

/* identifiers */

ConstantIdentifier = {SimpleConstantIdentifier}
SimpleConstantIdentifier = [A-Z0-9_]+

Identifier = [:jletter:][:jletterdigit:]*

TypeIdentifier = {SimpleTypeIdentifier}
SimpleTypeIdentifier = [A-Z][:jletterdigit:]*

/* int literals */

DecLiteral = [0-9](_?[0-9]+)*
HexLiteral = 0 [xX] 0* [0-9a-fA-F](_?[0-9a-fA-F]+)*
OctLiteral = 0 [oO] [0-7](_?[0-7]+)*
BinLiteral = 0 [bB] [01](_?[01]+)*

/* float literals */

FloatLiteral  = ({FLit1}|{FLit2}|{FLit3}|{FLit4}) ([fF]|[dD])?

FLit1 = [0-9](_?[0-9]+)* \. [0-9]?(_?[0-9]+)* {Exponent}?
FLit2 = \. [0-9](_?[0-9]+)* {Exponent}?
FLit3 = [0-9](_?[0-9]+)* {Exponent}
FLit4 = [0-9](_?[0-9]+)* {Exponent}?

Exponent = [eE] [+\-]? [0-9_]+

%state IN_COMMENT, IN_JAVA_DOC_COMMENT, IN_TUTOR_COMMENT

%%

<YYINITIAL> {

  /* keywords */
  "and" |
  "as" |
  "assert" |
  "async" |
  "await" |
  "break" |
  "class" |
  "continue" |
  "def" |
  "del" |
  "elif" |
  "else" |
  "except" |
  "finally" |
  "for" |
  "from" |
  "global" |
  "if" |
  "import" |
  "in" |
  "is" |
  "lambda" |
  "nonlocal" |
  "not" |
  "or" |
  "pass" |
  "raise" |
  "return" |
  "try" |
  "while" |
  "with" |
  "yield" { return KEYWORD_STYLE; }

  "str" |
  "int" |
  "float" |
  "complex" |
  "list" |
  "tuple" |
  "range" |
  "dict" |
  "set" |
  "frozenset" |
  "bool" |
  "bytes" |
  "bytearray" |
  "memoryview" { return TYPE_STYLE; }

  /* literals */
  "True" |
  "false" |
  "None" |

  (\" ( [^\"\n\\] | \\[^\n] )* (\n | \\\n | \")) |
  (\' ( [^\'\n\\] | \\[^\n] )* (\n | \\\n | \')) |

  {DecLiteral} |
  {HexLiteral} |
  {OctLiteral} |
  {FloatLiteral} { return LITERAL_STYLE; }

  /* separators */
  "(" |
  ")" |
  "{" |
  "}" |
  "[" |
  "]" |
  ";" |
  "," |
  ":" |
  "." { return SEPARATOR_STYLE; }

  /* operators */
  "=" |
  ">" |
  "<" |
  "!" |
  "~" |
  "?" |
  ":" |
  "+" |
  "-" |
  "*" |
  "/" |
  "&" |
  "|" |
  "^" |
  "%"                      { return OPERATOR_STYLE; }

  {ConstantIdentifier}                    { return PLAIN_STYLE; }

  {TypeIdentifier}  { return TYPE_STYLE; }

  \n |
  {Identifier} |
  {WhiteSpace}                   { return PLAIN_STYLE; }


  // comments
  "#*" [^\n]* \n { return TUTOR_COMMENT_STYLE; }
  "#" [^*] [^\n]* \n { return COMMENT_STYLE; }
}

/* error fallback */
[^]|\n                             { return PLAIN_STYLE; }
