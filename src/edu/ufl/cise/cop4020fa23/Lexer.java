/*Copyright 2023 by Beverly A Sanders
* 
* This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
* University of Florida during the fall semester 2023 as part of the course project.  
* 
* No other use is authorized. 
* 
* This code may not be posted on a public web site either during or after the course.  
*/
package edu.ufl.cise.cop4020fa23;

import static edu.ufl.cise.cop4020fa23.Kind.EOF;

import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;


public class Lexer implements ILexer {

	String input;
	private int currLine = 1;
	private int currColumn = 1;
	private int pos = 0;
	private int startPos = 0;
	private enum State {START, IN_IDENT, HAVE_ZERO, HAVE_DOT, IN_FLOAT, IN_NUM, HAVE_EQ, HAVE_MINUS, HAVE_AND, HAVE_OR,
						HAVE_STAR, HAVE_LSQUARE, HAVE_LT, HAVE_GT, HAVE_COLON}
	State state = State.START;
	boolean validToken = true;


	public Lexer(String input) {
		this.input = input;
	}

	@Override
	public IToken next() throws LexicalException {

		char ch;
		while (validToken) {
			// FIXME: Is this how I handle EOF?
			if (pos < input.length())
				ch = input.charAt(pos);
			else {
				ch = 0;
			}
			switch (state) {
				case START -> {
					startPos = pos;  //save position of first char in token
					switch (ch) {
						case ' ', '\t', '\r' -> {
							pos++;
							currColumn++;
						}
						case '\n' -> {
							pos++;
							currLine++;
							currColumn = 1;
						}
						case '+' -> { //handle all single char tokens like this
							// create token:kind = Kind.PLUS, position = startPos, length 1;
							Token token = new Token(Kind.PLUS, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							pos++;
							currColumn++;
							return token;
						}
						case '/' -> {
							Token token = new Token(Kind.DIV, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							pos++;
							currColumn++;
							return token;
						}
						case '%' -> {
							Token token = new Token(Kind.MOD, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							pos++;
							currColumn++;
							return token;
						}
						case ',' -> {
							Token token = new Token(Kind.COMMA, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							pos++;
							currColumn++;
							return token;
						}
						case ';' -> {
							Token token = new Token(Kind.SEMI, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							pos++;
							currColumn++;
							return token;
						}
						case '?' -> {
							Token token = new Token(Kind.QUESTION, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							pos++;
							currColumn++;
							return token;
						}
						case '(' -> {
							Token token = new Token(Kind.LPAREN, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							pos++;
							currColumn++;
							return token;
						}
						case ')' -> {
							Token token = new Token(Kind.RPAREN, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							pos++;
							currColumn++;
							return token;
						}
						case '!' -> {
							Token token = new Token(Kind.BANG, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							pos++;
							currColumn++;
							return token;
						}
						case '^' -> {
							Token token = new Token(Kind.RETURN, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							pos++;
							currColumn++;
							return token;
						}
						case ']' -> {
							Token token = new Token(Kind.RSQUARE, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							pos++;
							currColumn++;
							return token;
						}

						case '-' -> {
							state = State.HAVE_MINUS;
							pos++;
						}
						case '=' -> {
							state = State.HAVE_EQ;
							pos++;
						}
						case '&' -> {
							state = State.HAVE_AND;
							pos++;
						}
						case '|' -> {
							state = State.HAVE_OR;
							pos++;
						}
						case '*' -> {
							state = State.HAVE_STAR;
							pos++;
						}
						case '[' -> {
							state = State.HAVE_LSQUARE;
							pos++;
						}
						case '<' -> {
							state = State.HAVE_LT;
							pos++;
						}
						case '>' -> {
							state = State.HAVE_GT;
							pos++;
						}
						case ':' -> {
							state = State.HAVE_COLON;
							pos++;
						}
						case 0 -> {
							//this is the end of the input, add an EOF token and return;
							//FIXME: idk if this is what I should do tbh
							validToken = false;
						}
					}

				}
				case IN_IDENT-> {
					throw new UnsupportedOperationException("IN_IDENT not implemented");
				}
				case HAVE_ZERO -> {
					throw new UnsupportedOperationException("HAVE_ZERO not implemented");
				}
				case HAVE_DOT -> {
					throw new UnsupportedOperationException("HAVE_DOT not implemented");
				}
				case IN_FLOAT -> {
					throw new UnsupportedOperationException("IN_FLOAT not implemented");
				}
				case IN_NUM -> {
					throw new UnsupportedOperationException("IN_NUM not implemented");
				}
				case HAVE_EQ -> {
					switch (ch) {
						case '=' -> {
							Token token = new Token(Kind.EQ, startPos, 2, input.toCharArray(), new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						default -> {
							Token token = new Token(Kind.ASSIGN, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							state = State.START;
							currColumn++;
							return token;
						}
					}
				}
				case HAVE_MINUS -> {
					switch (ch) {
						case '>' -> {
							Token token = new Token(Kind.RARROW, startPos, 2, input.toCharArray(), new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						default -> {
							Token token = new Token(Kind.MINUS, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							state = State.START;
							currColumn++;
							return token;
						}
					}
				}
				case HAVE_AND -> {
					switch (ch) {
						case '&' -> {
							Token token = new Token(Kind.AND, startPos, 2, input.toCharArray(), new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						default -> {
							Token token = new Token(Kind.BITAND, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							state = State.START;
							currColumn++;
							return token;
						}
					}
				}
				case HAVE_OR -> {
					switch (ch) {
						case '|' -> {
							Token token = new Token(Kind.OR, startPos, 2, input.toCharArray(), new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						default -> {
							Token token = new Token(Kind.BITOR, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							state = State.START;
							currColumn++;
							return token;
						}
					}
				}
				case HAVE_STAR -> {
					switch (ch) {
						case '*' -> {
							Token token = new Token(Kind.EXP, startPos, 2, input.toCharArray(), new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						default -> {
							Token token = new Token(Kind.TIMES, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							state = State.START;
							currColumn++;
							return token;
						}
					}
				}
				case HAVE_LSQUARE -> {
					switch (ch) {
						case ']' -> {
							Token token = new Token(Kind.BOX, startPos, 2, input.toCharArray(), new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						default -> {
							Token token = new Token(Kind.LSQUARE, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							state = State.START;
							currColumn++;
							return token;
						}
					}
				}
				case HAVE_LT -> {
					switch (ch) {
						case '=' -> {
							Token token = new Token(Kind.LE, startPos, 2, input.toCharArray(), new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						case ':' -> {
							Token token = new Token(Kind.BLOCK_OPEN, startPos, 2, input.toCharArray(), new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						default -> {
							Token token = new Token(Kind.LT, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							state = State.START;
							currColumn++;
							return token;
						}
					}
				}
				case HAVE_GT -> {
					switch (ch) {
						case '=' -> {
							Token token = new Token(Kind.GE, startPos, 2, input.toCharArray(), new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						default -> {
							Token token = new Token(Kind.GT, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							state = State.START;
							currColumn++;
							return token;
						}
					}
				}
				case HAVE_COLON -> {
					switch (ch) {
						case '>' -> {
							Token token = new Token(Kind.BLOCK_CLOSE, startPos, 2, input.toCharArray(), new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						default -> {
							Token token = new Token(Kind.COLON, startPos, 1, input.toCharArray(), new SourceLocation(currLine, currColumn));
							state = State.START;
							currColumn++;
							return token;
						}
					}
				}
				default -> {
					throw new IllegalStateException("lexer bug");
				}
			}
		}

		return new Token(EOF, 0, 0, null, new SourceLocation(1, 1));
	}




}
