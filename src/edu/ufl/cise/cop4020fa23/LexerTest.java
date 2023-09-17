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

import static edu.ufl.cise.cop4020fa23.Kind.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;

/**
 * 
 */
class LexerTest {
	
	/** Switches on and off display of output via show */
	static final boolean VERBOSE = true;

	/** Output object to consol */
	void show(Object obj) {
		if (VERBOSE) {
			System.out.println(obj);
		}
	}
	
	/**
	 * Checks that IToken T has expected Kind
	 * 
	 * @param expectedKind
	 * @param t
	 */
	void checkToken(Kind expectedKind, IToken t) {
		assertEquals(expectedKind, t.kind());
	}
	
	/**
	 * Checks that IToken t has expected Kind and text
	 * 
	 * @param expectedKind
	 * @param expectedText
	 * @param t
	 */
	void checkToken(Kind expectedKind, String expectedText, IToken t) {
		assertEquals(expectedKind, t.kind());
		assertEquals(expectedText, t.text());
	}
	
	/**
	 * Checks that IToken t has expected Kind, text, and position
	 * 
	 * @param expectedKind
	 * @param expectedText
	 * @param expectedLine
	 * @param expectedColumn
	 * @param t
	 */
	void checkToken(Kind expectedKind, String expectedText, int expectedLine, int expectedColumn, IToken t) {
		assertEquals(expectedKind, t.kind());
		assertEquals(expectedText, t.text());
		SourceLocation loc = t.sourceLocation();
		assertEquals(expectedLine, loc.line());
		assertEquals(expectedColumn, loc.column());
		;
	}
	
	/*
	 * Checks that IToken t is an EOF Token
	 */
	void checkEOF(IToken t) {
		checkToken(EOF,t);
	}
	
	/**
	 * Checks that IToken t has kind STRING_LIT, and checks that the characters are as expected.
	 * 
	 * For convenience, stringValue is provided String without surrounding quotes (although they will have surrounding quotes in 
	 * the Java source code of the test.
	 * 
	 * The text of the token should be surrounded with quotes, so we check that the first and last
	 * characters are " and then compare the token text after removing the first and last characters
	 * with the given String.    
	 * 
	 * This is simply for convenience so that we can write "expected string" in
	 * tests rather than "\"expected string\"".
	 * 
	 * @param expectedStringValue
	 * @param t
	 */
	void checkString(String expectedStringValue, IToken t) {
		assertEquals(STRING_LIT, t.kind());
		String s = t.text();  
		assertEquals('\"', s.charAt(0));  //check that first char is "
		assertEquals('\"', s.charAt(s.length()-1));
		assertEquals(expectedStringValue, s.substring(1, s.length() - 1));
	}
	
	/**
	 * Checks that IToken t is a NUM_LIT with given 
	 * @param expectedNumlitText
	 * @param t
	 */
	void checkNumLit(String expectedNumlitText, IToken t) {
		assertEquals(NUM_LIT, t.kind());
		assertEquals(expectedNumlitText, t.text());
	}
	
	
	/**
	 * Checks that IToken t is a NUM_LIT with given int value
	 * 
	 * @param expectedNumLitValue
	 * @param t
	 */
	void checkNumLit(int expectedNumLitValue, IToken t) {
		checkNumLit(Integer.toString(expectedNumLitValue),t);
	}
	
	/**
	 * Checks that IToken t is a BOOLEAN_LIT with given value
	 * @param expectedBooleanValue
	 * @param t
	 */
	void checkBooleanLit(boolean expectedBooleanValue, IToken t) {
		assertEquals(BOOLEAN_LIT, t.kind());
		String text = t.text();
		String expectedText = expectedBooleanValue ? "TRUE" : "FALSE";
		assertEquals(expectedText, text);
	}
	
	/**
	 * checks that IToken t is an IDENT with given name
	 * 
	 * @param expectedText
	 * @param t
	 */
	private void checkIdent(String expectedText, IToken t) {
		assertEquals(IDENT, t.kind());
		assertEquals(expectedText, t.text());
	}
	
	/**
	 * Displays all the tokens generated for the given input String
	 * 
	 * @param input
	 * @throws LexicalException
	 */
	void showTokens(String input) throws LexicalException{
		ILexer lexer = ComponentFactory.makeLexer(input);
		IToken token = lexer.next();
		while (token.kind()!= EOF) {			
			show(token);
			token = lexer.next();
		}
		show(token);
	}

	void checkString(String expectedStringValue, int expectedLine, int expectedColumn, IToken t) {
		assertEquals(STRING_LIT, t.kind());
		String s = t.text();
		assertEquals('\"', s.charAt(0));  //check that first char is "
		assertEquals('\"', s.charAt(s.length()-1));
		assertEquals(expectedStringValue, s.substring(1, s.length() - 1));
		SourceLocation loc = t.sourceLocation();
		assertEquals(expectedLine, loc.line());
		assertEquals(expectedColumn, loc.column());
		;
	}

	void checkNumLit( String expectedNumlitText, int expectedLine, int expectedColumn, IToken t) {
		assertEquals(NUM_LIT, t.kind());
		assertEquals(expectedNumlitText, t.text());
		SourceLocation loc = t.sourceLocation();
		assertEquals(expectedLine, loc.line());
		assertEquals(expectedColumn, loc.column());
		;
	}

	void checkIdent( String expectedText, int expectedLine, int expectedColumn, IToken t) {
		assertEquals(IDENT, t.kind());
		assertEquals(expectedText, t.text());
		SourceLocation loc = t.sourceLocation();
		assertEquals(expectedLine, loc.line());
		assertEquals(expectedColumn, loc.column());
		;
	}

	/**
	 * Empty input is OK, should add EOF token
	 * 
	 * @throws LexicalException
	 */
	@Test
	void test0() throws LexicalException {
	String input = "";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkEOF(lexer.next());
	}

	@Test
	void testPLUS() throws LexicalException {
		String input = "+";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(PLUS, lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testHAVE_MINUS() throws LexicalException {
		String input = "->-";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(RARROW, lexer.next());
		checkToken(MINUS, lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testHAVE_EQ() throws LexicalException {
		String input = "= ==";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(ASSIGN, lexer.next());
		checkToken(EQ, lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testStrictSingleCharTokens() throws LexicalException {
		String input = "+/%,;?()!^";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(PLUS, lexer.next());
		checkToken(DIV, lexer.next());
		checkToken(MOD, lexer.next());
		checkToken(COMMA, lexer.next());
		checkToken(SEMI, lexer.next());
		checkToken(QUESTION, lexer.next());
		checkToken(LPAREN, lexer.next());
		checkToken(RPAREN, lexer.next());
		checkToken(BANG, lexer.next());
		checkToken(RETURN, lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testAllOpOrSep() throws LexicalException {
		String input = ",;?:()<>[ ]= ==<=>=!& &&| ||+-* **/%<::>^->[]";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(COMMA, lexer.next());
		checkToken(SEMI, lexer.next());
		checkToken(QUESTION, lexer.next());
		checkToken(COLON, lexer.next());
		checkToken(LPAREN, lexer.next());
		checkToken(RPAREN, lexer.next());
		checkToken(LT, lexer.next());
		checkToken(GT, lexer.next());
		checkToken(LSQUARE, lexer.next());
		checkToken(RSQUARE, lexer.next());
		checkToken(ASSIGN, lexer.next());
		checkToken(EQ, lexer.next());
		checkToken(LE, lexer.next());
		checkToken(GE, lexer.next());
		checkToken(BANG, lexer.next());
		checkToken(BITAND, lexer.next());
		checkToken(AND, lexer.next());
		checkToken(BITOR, lexer.next());
		checkToken(OR, lexer.next());
		checkToken(PLUS, lexer.next());
		checkToken(MINUS, lexer.next());
		checkToken(TIMES, lexer.next());
		checkToken(EXP, lexer.next());
		checkToken(DIV, lexer.next());
		checkToken(MOD, lexer.next());
		checkToken(BLOCK_OPEN, lexer.next());
		checkToken(BLOCK_CLOSE, lexer.next());
		checkToken(RETURN, lexer.next());
		checkToken(RARROW, lexer.next());
		checkToken(BOX, lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testBOOL() throws LexicalException {
		String input = "false FALSE";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(IDENT, lexer.next());
		boolean b = false;
		checkBooleanLit(false, lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void test1() throws LexicalException {
	String input = ",[   ]%+";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(COMMA, lexer.next());
		checkToken(LSQUARE, lexer.next());
		checkToken(RSQUARE, lexer.next());
		checkToken(MOD, lexer.next());
		checkToken(PLUS, lexer.next());
		checkEOF(lexer.next());
	}
		
	@Test
	void test1a() throws LexicalException {
	String input = ",[]%+";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(COMMA, lexer.next());
		checkToken(BOX, lexer.next());
		checkToken(MOD, lexer.next());
		checkToken(PLUS, lexer.next());
	}

	@Test
	void test4() throws LexicalException {
	String input = """
		     , [ ]
			##{ }.
			% + /
			? !;
			""";
	ILexer lexer = ComponentFactory.makeLexer(input);
	checkToken(COMMA,",",lexer.next());
	checkToken(LSQUARE,"[",lexer.next());
	checkToken(RSQUARE,"]",lexer.next());
	checkToken(MOD, "%", lexer.next());
	checkToken(PLUS,"+", lexer.next());
	checkToken(DIV,"/", lexer.next());
	checkToken(QUESTION,"?", lexer.next());
	checkToken(BANG,"!", lexer.next());
	checkToken(SEMI, ";", lexer.next());
 	checkEOF(lexer.next());
	checkEOF(lexer.next());
	}	
	
	@Test
	void test5() throws LexicalException {
		String input = """
				& && 
				&&& &&&&
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(BITAND, "&", 1,1, lexer.next());
		checkToken(AND, "&&", 1,3, lexer.next());
		checkToken(AND, "&&", 2,1, lexer.next());
		checkToken(BITAND, "&", 2,3, lexer.next());
		checkToken(AND, "&&", 2, 5, lexer.next());
		checkToken(AND, "&&", 2, 7, lexer.next());
		checkEOF(lexer.next());
	}
	
	@Test
	void test6() throws LexicalException {
		String input = """
				<< <= <: <<: <,
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);	
		checkToken(LT, lexer.next());
		checkToken(LT, lexer.next());
		checkToken(LE, lexer.next());
		checkToken(BLOCK_OPEN, lexer.next());
		checkToken(LT, lexer.next());
		checkToken(BLOCK_OPEN, lexer.next());
		checkToken(LT,lexer.next());
		checkToken(COMMA,lexer.next());
		checkEOF(lexer.next());
	}
	
	@Test
	void test7() throws LexicalException {
		String input = """
				+== = == === 
				====-> - > ->>
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);	
		checkToken(PLUS, lexer.next());
		checkToken(EQ, lexer.next());
		checkToken(ASSIGN, lexer.next());
		checkToken(EQ, lexer.next());
		checkToken(EQ, lexer.next());
		checkToken(ASSIGN, lexer.next());		
		checkToken(EQ, lexer.next());
		checkToken(EQ, lexer.next());
		checkToken(RARROW, lexer.next());
		checkToken(MINUS, lexer.next());
		checkToken(GT, lexer.next());
		checkToken(RARROW, lexer.next());
		checkToken(GT, lexer.next());
		checkEOF(lexer.next());
	}
	
	@Test
	void test8() throws LexicalException {
		String input = """
				a+b
				ccc def
				BLACK
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(IDENT,"a",lexer.next());
		checkToken(PLUS, lexer.next());
		checkToken(IDENT,"b", lexer.next());
		checkToken(IDENT,"ccc", lexer.next());
		checkToken(IDENT,"def", lexer.next());
		checkToken(CONST,"BLACK", lexer.next());
		checkEOF(lexer.next());
	}
	
	@Test
	void test8a() throws LexicalException {
		String input = """
				a
				ccc
				RED
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(IDENT,"a",lexer.next());
		checkToken(IDENT,"ccc", lexer.next());
		checkToken(CONST,"RED", lexer.next());
		checkEOF(lexer.next());
	}
	
//	@Test
//	void test9() throws LexicalException {
//		String input = """
//				if fi
//				od do
//				red blue green
//				nil
//				image int string pixel boolean
//				void 
//				width height
//				write
//				DARK_GRAY MAGENTA Z
//				""";
//		ILexer lexer = ComponentFactory.makeLexer(input);
//		checkToken(RES_if, "if", lexer.next());
//		checkToken(RES_fi, "fi", lexer.next());
//		checkToken(RES_od, "od", lexer.next());
//		checkToken(RES_do, "do", lexer.next());
//		checkToken(RES_red, "red", lexer.next());
//		checkToken(RES_blue, "blue", lexer.next());
//		checkToken(RES_green, "green", lexer.next());
//		checkToken(RES_nil, "nil", lexer.next());
//		checkToken(RES_image, "image", lexer.next());
//		checkToken(RES_int, "int", lexer.next());
//		checkToken(RES_string, "string", lexer.next());
//		checkToken(RES_pixel, "pixel", lexer.next());
//		checkToken(RES_boolean, "boolean", lexer.next());
//		checkToken(RES_void, "void", lexer.next());
//		checkToken(RES_width, "width", lexer.next());
//		checkToken(RES_height, "height", lexer.next());
//		checkToken(RES_write, "write", lexer.next());
//		checkToken(CONST, "DARK_GRAY", lexer.next());
//		checkToken(CONST, "MAGENTA", lexer.next());
//		checkToken(CONST, "Z", lexer.next());
//	}
//	
//
//	
    @Test
    void test10() throws LexicalException {
    	String input = """
    			01 010 
    			""";
     	ILexer lexer = ComponentFactory.makeLexer(input);
    	checkNumLit("0",lexer.next());
    	checkNumLit("1",lexer.next());
       	checkNumLit("0",lexer.next());
       	checkNumLit("10",lexer.next());      	
    }
    
    
    /** 
     * This test shows how to write a test that will pass only if a LexicalExcption is thrown.
     * In this case, the number 9999999999999999999999999999999999999999 is too big.
     * 
     * Note that correct tokens before the token with the error should be returned normally. 
     * 
     * @throws LexicalException
     */
    @Test
    void test11() throws LexicalException {
    	String input = """
    			23 9999999999999999999999999999999999999999
    			""";
    	ILexer lexer = ComponentFactory.makeLexer(input);
    	checkNumLit("23",lexer.next());
		assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
    }
    
    @Test
    void test12() throws LexicalException {
    	String input = """
    			"hello" 
    			"abc"
    			"abcde@#$%"
    			""";
    	ILexer lexer = ComponentFactory.makeLexer(input);
    	checkString("hello", lexer.next());
    	checkString("abc", lexer.next());
    	checkString("abcde@#$%", lexer.next());
    	checkEOF(lexer.next());    			
    }
    
    @Test
    void test13() throws LexicalException {
    	String input = "\n\r\n";
       	ILexer lexer = ComponentFactory.makeLexer(input);
       	checkEOF(lexer.next());
       	checkEOF(lexer.next());
    }
    
    @Test
    void test14() throws LexicalException {
    	String input = """
    			abc ##hello there !@#$#%;
    			123
    			abc123
    			""";
     	ILexer lexer = ComponentFactory.makeLexer(input);
     	checkIdent("abc", lexer.next());
     	checkNumLit("123", lexer.next());
     	checkIdent("abc123", lexer.next());
     	checkEOF(lexer.next());
    }
    
    @Test
    void test15() throws LexicalException {
    	String input = """
    			abc123+123abc##1233435
    			"abc123+123abc##1233435"
    			""";
    	ILexer lexer = ComponentFactory.makeLexer(input);
    	checkIdent("abc123",lexer.next());
    	checkToken(PLUS, lexer.next());
    	checkNumLit("123", lexer.next());
    	checkIdent("abc", lexer.next());
    	checkString("abc123+123abc##1233435", lexer.next());
    	checkEOF(lexer.next());
    	checkEOF(lexer.next());
    	
    }

    @Test
    void test16() throws LexicalException {
    	String input = """
    			a[b,c]
    			""";
       	ILexer lexer = ComponentFactory.makeLexer(input);
    	checkIdent("a",lexer.next());
    	checkToken(LSQUARE,lexer.next());   	
    	checkIdent("b",lexer.next());
       	checkToken(COMMA,lexer.next());   	
       	checkIdent("c",lexer.next());
       	checkToken(RSQUARE,lexer.next());   	
    }
    //throws exception
    @Test
    void test17() throws Exception {
    	String input = """
    			555 #
    			""";
    	ILexer lexer = ComponentFactory.makeLexer(input);
    	checkNumLit("555",lexer.next());
		LexicalException e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
		show("Error message from test17: " + e.getMessage());
    }
    
    //throws exception
    @Test
    void test18() throws Exception {
    	String input = """
    			555 @
    			""";
    	ILexer lexer = ComponentFactory.makeLexer(input);
    	checkNumLit("555",lexer.next());
		LexicalException e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
		show("Error message from test18: " + e.getMessage());
    }
    
    //throws exception
    @Test
    void test19() throws Exception {
    	String input = """
    			"@"
    			## @ is legal in a comment
    			@
    			""";
    	ILexer lexer = ComponentFactory.makeLexer(input);
    	checkString("@",lexer.next());
		LexicalException e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
		show("Error message from test19: " + e.getMessage());
    }
    
    @Test
    void test20() throws Exception {
    	String input = """
    			FALSE TRUE
    			True false true
    			""";
       	ILexer lexer = ComponentFactory.makeLexer(input);
       	checkToken(BOOLEAN_LIT, "FALSE", lexer.next());
     	checkToken(BOOLEAN_LIT, "TRUE", lexer.next());
     	checkIdent("True",lexer.next());
     	checkIdent("false",lexer.next());
     	checkIdent("true",lexer.next());     	
    }

	// All tests below are based on test cases written and shared by classmates
	@Test
	void testPool1() throws Exception {
		String input = """
         "
         a
         a
         "
         """;
		ILexer lexer = ComponentFactory.makeLexer(input);
		assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
	}

	@Test
	void testPool2() throws Exception {
		String input = """
         "this"
          "test"
           "case"
            "should*($)%*)"
         """;
		ILexer lexer = ComponentFactory.makeLexer(input);
		//quotes escaped because quotation marks must be stored
		checkToken(STRING_LIT,"\"this\"", 1, 1, lexer.next());
		checkToken(STRING_LIT,"\"test\"", 2, 2, lexer.next());
		checkToken(STRING_LIT,"\"case\"", 3, 3, lexer.next());
		checkToken(STRING_LIT,"\"should*($)%*)\"", 4, 4, lexer.next());
		checkToken(EOF, lexer.next());
	}

	@Test
	void testPool3() throws Exception {
		String input = """
    			FALSETRUE
    			Truefalse TRUE
    			""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkIdent("FALSETRUE",lexer.next());
		checkIdent("Truefalse",lexer.next());
		checkToken(BOOLEAN_LIT, "TRUE", lexer.next());
	}

	@Test
	void testPool4() throws Exception {
		String input = """
				"RED"Redred
				REDred red
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkString("RED", lexer.next()); // checkString instead of checkToken as given tests (ex. test 12) show
		checkIdent("Redred", lexer.next()); // previous version had REDred check twice, although that does not match the input
		checkIdent("REDred", lexer.next());
		checkToken(RES_red, "red", lexer.next()); // red is not a CONST, although RED is
	}

	@Test
	void testPool5() throws Exception {
		String input = """
    			"asdjhfb"12389123 123/
    			12[]*&&%AB1290080_
    			""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkString("asdjhfb", lexer.next());
		checkNumLit("12389123", lexer.next());
		checkNumLit("123", lexer.next());
		checkToken(DIV, lexer.next());
		checkNumLit("12", lexer.next());
		checkToken(BOX, lexer.next());
		checkToken(TIMES, lexer.next());
		checkToken(AND, lexer.next());
		checkToken(MOD, lexer.next());
		checkIdent("AB1290080_",lexer.next());
	}

	@Test
	void testPool6() throws LexicalException {
		String input = """
         
      """;
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkEOF(lexer.next());
	}

	@Test
	void testPool7() throws LexicalException {
		String input = ",";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(COMMA, ",", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool8() throws LexicalException {
		String input = ";";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(SEMI, ";", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool9() throws LexicalException {
		String input = "?";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(QUESTION, "?", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool10() throws LexicalException {
		String input = "(";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(LPAREN, "(", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool11() throws LexicalException {
		String input = ")";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(RPAREN, ")", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool12() throws LexicalException {
		String input = "]";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(RSQUARE, "]", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool13() throws LexicalException {
		String input = "!";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(BANG, "!", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool14() throws LexicalException {
		String input = "+";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(PLUS, "+", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool15() throws LexicalException {
		String input = "/";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(DIV, "/", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool16() throws LexicalException {
		String input = "%";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(MOD, "%", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool17() throws LexicalException {
		String input = "^";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(RETURN, "^", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool18() throws LexicalException {
		String input = "==";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(EQ, "==", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool19() throws LexicalException {
		String input = "=";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(ASSIGN, "=", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool20() throws LexicalException {
		String input = "<=";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(LE, "<=", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool21() throws LexicalException {
		String input = "<:";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(BLOCK_OPEN, "<:", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool22() throws LexicalException {
		String input = "<";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(LT, "<", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool23() throws LexicalException {
		String input = ">=";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(GE, ">=", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool24() throws LexicalException {
		String input = ">";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(GT, ">", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool25() throws LexicalException {
		String input = "&&";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(AND, "&&", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool26() throws LexicalException {
		String input = "&";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(BITAND, "&", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool27() throws LexicalException {
		String input = "||";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(OR, "||", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool28() throws LexicalException {
		String input = "|";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(BITOR, "|", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool29() throws LexicalException {
		String input = "**";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(EXP, "**", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool30() throws LexicalException {
		String input = "*";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(TIMES, "*", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool31() throws LexicalException {
		String input = ",\n;";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(COMMA, ",", 1, 1, lexer.next());
		checkToken(SEMI, ";", 2, 1, lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool32() throws LexicalException {
		String input = ",;";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(COMMA, ",", 1, 1, lexer.next());
		checkToken(SEMI, ";", 1, 2, lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool33() throws LexicalException {
		String input = ",";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(COMMA, ",", 1, 1, lexer.next());
		checkEOF(lexer.next());
		checkEOF(lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void testPool34() throws LexicalException {
		String input = "\"\"";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkString("", lexer.next());
	}

	@Test
	void testPool35() {
		String input = "\"value\n\"";
		ILexer lexer = ComponentFactory.makeLexer(input);
		LexicalException e = assertThrows(LexicalException.class, () -> lexer.next());
		show("Error message from unitTestMultiLineString: " + e.getMessage());
	}

	@Test
	void testPool36() {
		String input = "\"31";
		ILexer lexer = ComponentFactory.makeLexer(input);
		LexicalException e = assertThrows(LexicalException.class, () -> lexer.next());
		show("Error message from unitTestInvalidString: " + e.getMessage());
	}

	@Test
	void testPool37() {
		String input = "2147483649";
		ILexer lexer = ComponentFactory.makeLexer(input);
		LexicalException e = assertThrows(LexicalException.class, () -> lexer.next());
		show("Error message from unitTestInvalidInteger: " + e.getMessage());
	}

	@Test
	void test0New() throws LexicalException {
		String input = "";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkEOF(lexer.next());
	}

	@Test
	void test1New() throws LexicalException {
		String input = ",[   ]%+";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(COMMA,",",1,1, lexer.next());
		checkToken(LSQUARE,"[",1,2, lexer.next());
		checkToken(RSQUARE,"]",1,6, lexer.next());
		checkToken(MOD,"%",1,7, lexer.next());
		checkToken(PLUS,"+",1,8, lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void test1aNew() throws LexicalException {
		String input = ",[]%+";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(COMMA,",",1,1, lexer.next());
		checkToken(BOX,"[]",1,2, lexer.next());
		checkToken(MOD,"%",1,4, lexer.next());
		checkToken(PLUS,"+",1,5, lexer.next());
	}

	@Test
	void test4New() throws LexicalException {
		String input = """
          , [ ]
      ##{ }.
      % + /
      ? !;
      """;
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(COMMA,",",1,5,lexer.next());
		checkToken(LSQUARE,"[",1,7,lexer.next());
		checkToken(RSQUARE,"]",1,9,lexer.next());
		checkToken(MOD, "%",3,1, lexer.next());
		checkToken(PLUS,"+",3,3, lexer.next());
		checkToken(DIV,"/",3,5, lexer.next());
		checkToken(QUESTION,"?",4,1, lexer.next());
		checkToken(BANG,"!",4,3, lexer.next());
		checkToken(SEMI, ";",4,4, lexer.next());
		checkEOF(lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void test5New() throws LexicalException {
		String input = """
            & &&
            &&& &&&&
            """;
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(BITAND, "&", 1,1, lexer.next());
		checkToken(AND, "&&", 1,3, lexer.next());
		checkToken(AND, "&&", 2,1, lexer.next());
		checkToken(BITAND, "&", 2,3, lexer.next());
		checkToken(AND, "&&", 2, 5, lexer.next());
		checkToken(AND, "&&", 2, 7, lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void test6New() throws LexicalException {
		String input = """
            << <= <: <<: <,
            """;
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(LT,"<", 1,1, lexer.next());
		checkToken(LT,"<", 1,2, lexer.next());
		checkToken(LE,"<=", 1,4, lexer.next());
		checkToken(BLOCK_OPEN,"<:", 1,7, lexer.next());
		checkToken(LT,"<", 1,10, lexer.next());
		checkToken(BLOCK_OPEN,"<:", 1,11, lexer.next());
		checkToken(LT,"<", 1,14,lexer.next());
		checkToken(COMMA,",", 1,15,lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void test7New() throws LexicalException {
		String input = """
            +== = == ===
            ====-> - > ->>
            """;
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(PLUS,"+", 1,1, lexer.next());
		checkToken(EQ,"==", 1,2, lexer.next());
		checkToken(ASSIGN,"=", 1,5, lexer.next());
		checkToken(EQ,"==", 1,7, lexer.next());
		checkToken(EQ,"==", 1,10, lexer.next());
		checkToken(ASSIGN,"=", 1,12, lexer.next());
		checkToken(EQ,"==", 2,1, lexer.next());
		checkToken(EQ,"==", 2,3, lexer.next());
		checkToken(RARROW,"->", 2,5, lexer.next());
		checkToken(MINUS,"-", 2,8, lexer.next());
		checkToken(GT,">", 2,10, lexer.next());
		checkToken(RARROW,"->", 2,12, lexer.next());
		checkToken(GT,">", 2,14, lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void test8New() throws LexicalException {
		String input = """
            a+b
            ccc def
            BLACK
            """;
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(IDENT,"a",1,1, lexer.next());
		checkToken(PLUS,"+",1,2, lexer.next());
		checkToken(IDENT,"b",1,3,  lexer.next());
		checkToken(IDENT,"ccc",2,1,  lexer.next());
		checkToken(IDENT,"def",2,5,  lexer.next());
		checkToken(CONST,"BLACK",3,1, lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void test8aNew() throws LexicalException {
		String input = """
            a
            ccc
            RED
            """;
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(IDENT,"a",1,1,lexer.next());
		checkToken(IDENT,"ccc",2,1, lexer.next());
		checkToken(CONST,"RED",3,1, lexer.next());
		checkEOF(lexer.next());
	}


	@Test
	void test10New() throws LexicalException {
		String input = """
             01 010
             """;
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkNumLit("0",1,1,lexer.next());
		checkNumLit("1",1,2,lexer.next());
		checkNumLit("0",1,4,lexer.next());
		checkNumLit("10",1,5,lexer.next());
	}


	/**
	 * This test shows how to write a test that will pass only if a LexicalExcption is thrown.
	 * In this case, the number 9999999999999999999999999999999999999999 is too big.
	 *
	 * Note that correct tokens before the token with the error should be returned normally.
	 *
	 * @throws LexicalException
	 */
	@Test
	void test11New() throws LexicalException {
		String input = """
             23 9999999999999999999999999999999999999999
             """;
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkNumLit("23",1,1,lexer.next());
		assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
	}

	@Test
	void test12New() throws LexicalException {
		String input = """
             "hello"
             "abc"
             "abcde@#$%"
             """;
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkString("hello",1,1, lexer.next());
		checkString("abc",2,1, lexer.next());
		checkString("abcde@#$%",3,1, lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void test13New() throws LexicalException {
		String input = "\n\r\n";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkEOF(lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void test14New() throws LexicalException {
		String input = """
             abc ##hello there !@#$#%;
             123
             abc123
             """;
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkIdent("abc",1,1, lexer.next());
		checkNumLit("123",2,1, lexer.next());
		checkIdent("abc123",3,1, lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void test15New() throws LexicalException {
		String input = """
             abc123+123abc##1233435
             "abc123+123abc##1233435"
             """;
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkIdent("abc123",1,1,lexer.next());
		checkToken(PLUS, lexer.next());
		checkNumLit("123",1,8, lexer.next());
		checkIdent("abc",1,11, lexer.next());
		checkString("abc123+123abc##1233435",2,1, lexer.next());
		checkEOF(lexer.next());
		checkEOF(lexer.next());

	}


	@Test
	void test16New() throws LexicalException {
		String input = """
             a[b,c]
             """;
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkIdent("a",1,1,lexer.next());
		checkToken(LSQUARE,"[",1,2,lexer.next());
		checkIdent("b",1,3,lexer.next());
		checkToken(COMMA,",",1,4,lexer.next());
		checkIdent("c",1,5,lexer.next());
		checkToken(RSQUARE,"]",1,6,lexer.next());
	}
	//throws exception
	@Test
	void test17New() throws Exception {
		String input = """
             555 #
             """;
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkNumLit("555",1,1,lexer.next());
		LexicalException e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
		show("Error message from test17: " + e.getMessage());
	}

	//throws exception
	@Test
	void test18New() throws Exception {
		String input = """
             555 @
             """;
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkNumLit("555",1,1,lexer.next());
		LexicalException e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
		show("Error message from test18: " + e.getMessage());
	}

	//throws exception
	@Test
	void test19New() throws Exception {
		String input = """
             "@"
             ## @ is legal in a comment
             @
             """;
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkString("@",1,1,lexer.next());
		LexicalException e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
		show("Error message from test19: " + e.getMessage());
	}

	@Test
	void test20New() throws Exception {
		String input = """
             FALSE TRUE
             True false true
             """;
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(BOOLEAN_LIT, "FALSE",1,1, lexer.next());
		checkToken(BOOLEAN_LIT, "TRUE",1,7, lexer.next());
		checkIdent("True",2,1,lexer.next());
		checkIdent("false",2,6,lexer.next());
		checkIdent("true",2,12,lexer.next());
	}


}
