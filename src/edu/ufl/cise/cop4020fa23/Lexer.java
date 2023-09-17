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
import static edu.ufl.cise.cop4020fa23.Kind.NUM_LIT;

import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;


public class Lexer implements ILexer {

	String input;
	private int currLine = 1;
	private int currColumn = 1;
	private int pos = 0;
	private int startPos = 0;
	private enum State {START, IN_IDENT, IN_NUM, HAVE_POUND, IN_COMMENT,
						HAVE_EQ, HAVE_MINUS, HAVE_AND, HAVE_OR, HAVE_STAR, HAVE_LSQUARE, HAVE_LT, HAVE_GT, HAVE_COLON}
	private State state = State.START;
	private boolean validToken = true;

	private char[] inputArr;



	public Lexer(String input) {
		this.input = input;
		inputArr = input.toCharArray();;
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
					// letters and identifiers
					if ('A' <= ch && ch <= 'Z' || 'a' <= ch && ch <= 'z' || ch == '_') {
						state = State.IN_IDENT;
						pos++;
					}
					else {
						switch (ch) {
							// whitespace and newline
							case ' ', '\t', '\r' -> {
								pos++;
								currColumn++;
							}
							case '\n' -> {
								pos++;
								currLine++;
								currColumn = 1;
							}
							// op or separator
							case '+' -> { //handle all single char tokens like this
								// create token:kind = Kind.PLUS, position = startPos, length 1;
								Token token = new Token(Kind.PLUS, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
								pos++;
								currColumn++;
								return token;
							}
							case '/' -> {
								Token token = new Token(Kind.DIV, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
								pos++;
								currColumn++;
								return token;
							}
							case '%' -> {
								Token token = new Token(Kind.MOD, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
								pos++;
								currColumn++;
								return token;
							}
							case ',' -> {
								Token token = new Token(Kind.COMMA, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
								pos++;
								currColumn++;
								return token;
							}
							case ';' -> {
								Token token = new Token(Kind.SEMI, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
								pos++;
								currColumn++;
								return token;
							}
							case '?' -> {
								Token token = new Token(Kind.QUESTION, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
								pos++;
								currColumn++;
								return token;
							}
							case '(' -> {
								Token token = new Token(Kind.LPAREN, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
								pos++;
								currColumn++;
								return token;
							}
							case ')' -> {
								Token token = new Token(Kind.RPAREN, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
								pos++;
								currColumn++;
								return token;
							}
							case '!' -> {
								Token token = new Token(Kind.BANG, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
								pos++;
								currColumn++;
								return token;
							}
							case '^' -> {
								Token token = new Token(Kind.RETURN, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
								pos++;
								currColumn++;
								return token;
							}
							case ']' -> {
								Token token = new Token(Kind.RSQUARE, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
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

							// digit, nonzero_digit, num_lit,
							case '0' -> {
								Token token = new Token(Kind.NUM_LIT, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
								pos++;
								currColumn++;
								return token;
							}
							case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
								state = State.IN_NUM;
								pos++;
							}

							// comment
							case '#' -> {
								state = State.HAVE_POUND;
								pos++;
								currColumn++;
							}

							case 0 -> {
								//this is the end of the input, add an EOF token and return;
								//FIXME: idk if this is what I should do tbh
								validToken = false;
							}
							default -> {
								throw new LexicalException("Invalid character: " + ch);
							}
						}
					}

				}
				case IN_IDENT-> {
					if ('A' <= ch && ch <= 'Z' || 'a' <= ch && ch <= 'z' || ch == '_' || '0' <= ch && ch <= '9') {
						pos++;
					}
					else {
						String identifier = input.substring(startPos, pos);
						switch (identifier) {
							case "Z", "BLACK", "BLUE", "CYAN", "DARK_GRAY", "GRAY", "GREEN", "LIGHT_GRAY",
									"MAGENTA", "ORANGE", "PINK", "RED", "WHITE", "YELLOW" -> {
								Token token = new Token(Kind.CONST, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
								state = State.START;
								currColumn += pos-startPos;
								return token;
							}
							case "TRUE", "FALSE" -> {
								Token token = new Token(Kind.BOOLEAN_LIT, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
								state = State.START;
								currColumn += pos-startPos;
								return token;
							}
							case "image" -> {
								Token token = new Token(Kind.RES_image, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
								state = State.START;
								currColumn += pos-startPos;
								return token;
							}
							case "pixel" -> {
								Token token = new Token(Kind.RES_pixel, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
								state = State.START;
								currColumn += pos-startPos;
								return token;
							}
							case "int" -> {
								Token token = new Token(Kind.RES_int, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
								state = State.START;
								currColumn += pos-startPos;
								return token;
							}
							case "string" -> {
								Token token = new Token(Kind.RES_string, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
								state = State.START;
								currColumn += pos-startPos;
								return token;
							}
							case "void" -> {
								Token token = new Token(Kind.RES_void, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
								state = State.START;
								currColumn += pos-startPos;
								return token;
							}
							case "boolean" -> {
								Token token = new Token(Kind.RES_boolean, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
								state = State.START;
								currColumn += pos-startPos;
								return token;
							}
							case "write" -> {
								Token token = new Token(Kind.RES_write, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
								state = State.START;
								currColumn += pos-startPos;
								return token;
							}
							case "height" -> {
								Token token = new Token(Kind.RES_height, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
								state = State.START;
								currColumn += pos-startPos;
								return token;
							}
							case "width" -> {
								Token token = new Token(Kind.RES_width, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
								state = State.START;
								currColumn += pos-startPos;
								return token;
							}
							case "if" -> {
								Token token = new Token(Kind.RES_if, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
								state = State.START;
								currColumn += pos-startPos;
								return token;
							}
							case "fi" -> {
								Token token = new Token(Kind.RES_fi, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
								state = State.START;
								currColumn += pos-startPos;
								return token;
							}
							case "do" -> {
								Token token = new Token(Kind.RES_do, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
								state = State.START;
								currColumn += pos-startPos;
								return token;
							}
							case "od" -> {
								Token token = new Token(Kind.RES_od, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
								state = State.START;
								currColumn += pos-startPos;
								return token;
							}
							case "red" -> {
								Token token = new Token(Kind.RES_red, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
								state = State.START;
								currColumn += pos-startPos;
								return token;
							}
							case "green" -> {
								Token token = new Token(Kind.RES_green, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
								state = State.START;
								currColumn += pos-startPos;
								return token;
							}
							case "blue" -> {
								Token token = new Token(Kind.RES_blue, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
								state = State.START;
								currColumn += pos-startPos;
								return token;
							}
							default -> {
								Token token = new Token(Kind.IDENT, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
								state = State.START;
								currColumn += pos-startPos;
								return token;
							}
						}
					}
				}
				case IN_NUM -> {
					switch (ch) {
						case '0','1','2','3','4','5','6','7','8','9' -> {
							pos++;  //still in number,
							//increment pos to read next char
						}
						default -> {
							try {
								Integer.parseInt(input.substring(startPos, pos));
							}
							catch (Exception e){
								throw new LexicalException(new SourceLocation(currLine, currColumn), "Invalid NUM_LIT");
							}
							Token token = new Token(Kind.NUM_LIT, startPos, pos-startPos, inputArr, new SourceLocation(currLine, currColumn));
							state = State.START;
							currColumn += pos-startPos;
							return token;
						}

					}
				}
				case IN_COMMENT -> {
					if (32 <= ch && ch <= 126) {
						pos++;
						currColumn++;
					}
					else {
						state = State.START;
					}
				}
				case HAVE_POUND -> {
					switch (ch) {
						case '#' -> {
							state = State.IN_COMMENT;
							pos++;
							currColumn++;
						}
						default -> {
							throw new LexicalException(new SourceLocation(currLine, currColumn), "Invalid pound character");
						}
					}
				}
				case HAVE_EQ -> {
					switch (ch) {
						case '=' -> {
							Token token = new Token(Kind.EQ, startPos, 2, inputArr, new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						default -> {
							Token token = new Token(Kind.ASSIGN, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
							state = State.START;
							currColumn++;
							return token;
						}
					}
				}
				case HAVE_MINUS -> {
					switch (ch) {
						case '>' -> {
							Token token = new Token(Kind.RARROW, startPos, 2, inputArr, new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						default -> {
							Token token = new Token(Kind.MINUS, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
							state = State.START;
							currColumn++;
							return token;
						}
					}
				}
				case HAVE_AND -> {
					switch (ch) {
						case '&' -> {
							Token token = new Token(Kind.AND, startPos, 2, inputArr, new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						default -> {
							Token token = new Token(Kind.BITAND, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
							state = State.START;
							currColumn++;
							return token;
						}
					}
				}
				case HAVE_OR -> {
					switch (ch) {
						case '|' -> {
							Token token = new Token(Kind.OR, startPos, 2, inputArr, new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						default -> {
							Token token = new Token(Kind.BITOR, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
							state = State.START;
							currColumn++;
							return token;
						}
					}
				}
				case HAVE_STAR -> {
					switch (ch) {
						case '*' -> {
							Token token = new Token(Kind.EXP, startPos, 2, inputArr, new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						default -> {
							Token token = new Token(Kind.TIMES, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
							state = State.START;
							currColumn++;
							return token;
						}
					}
				}
				case HAVE_LSQUARE -> {
					switch (ch) {
						case ']' -> {
							Token token = new Token(Kind.BOX, startPos, 2, inputArr, new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						default -> {
							Token token = new Token(Kind.LSQUARE, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
							state = State.START;
							currColumn++;
							return token;
						}
					}
				}
				case HAVE_LT -> {
					switch (ch) {
						case '=' -> {
							Token token = new Token(Kind.LE, startPos, 2, inputArr, new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						case ':' -> {
							Token token = new Token(Kind.BLOCK_OPEN, startPos, 2, inputArr, new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						default -> {
							Token token = new Token(Kind.LT, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
							state = State.START;
							currColumn++;
							return token;
						}
					}
				}
				case HAVE_GT -> {
					switch (ch) {
						case '=' -> {
							Token token = new Token(Kind.GE, startPos, 2, inputArr, new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						default -> {
							Token token = new Token(Kind.GT, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
							state = State.START;
							currColumn++;
							return token;
						}
					}
				}
				case HAVE_COLON -> {
					switch (ch) {
						case '>' -> {
							Token token = new Token(Kind.BLOCK_CLOSE, startPos, 2, inputArr, new SourceLocation(currLine, currColumn));
							state = State.START;
							pos++;
							currColumn += 2;
							return token;
						}
						default -> {
							Token token = new Token(Kind.COLON, startPos, 1, inputArr, new SourceLocation(currLine, currColumn));
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
