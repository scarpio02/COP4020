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

import java.util.Arrays;

import edu.ufl.cise.cop4020fa23.ast.AST;
import edu.ufl.cise.cop4020fa23.ast.BinaryExpr;
import edu.ufl.cise.cop4020fa23.ast.BooleanLitExpr;
import edu.ufl.cise.cop4020fa23.ast.ChannelSelector;
import edu.ufl.cise.cop4020fa23.ast.ConditionalExpr;
import edu.ufl.cise.cop4020fa23.ast.ConstExpr;
import edu.ufl.cise.cop4020fa23.ast.ExpandedPixelExpr;
import edu.ufl.cise.cop4020fa23.ast.Expr;
import edu.ufl.cise.cop4020fa23.ast.IdentExpr;
import edu.ufl.cise.cop4020fa23.ast.NumLitExpr;
import edu.ufl.cise.cop4020fa23.ast.PixelSelector;
import edu.ufl.cise.cop4020fa23.ast.PostfixExpr;
import edu.ufl.cise.cop4020fa23.ast.StringLitExpr;
import edu.ufl.cise.cop4020fa23.ast.UnaryExpr;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;

import static edu.ufl.cise.cop4020fa23.Kind.*;

/**
Expr::=  ConditionalExpr | LogicalOrExpr    
ConditionalExpr ::=  ?  Expr  :  Expr  :  Expr 
LogicalOrExpr ::= LogicalAndExpr (    (   |   |   ||   ) LogicalAndExpr)*
LogicalAndExpr ::=  ComparisonExpr ( (   &   |  &&   )  ComparisonExpr)*
ComparisonExpr ::= PowExpr ( (< | > | == | <= | >=) PowExpr)*
PowExpr ::= AdditiveExpr ** PowExpr |   AdditiveExpr
AdditiveExpr ::= MultiplicativeExpr ( ( + | -  ) MultiplicativeExpr )*
MultiplicativeExpr ::= UnaryExpr (( * |  /  |  % ) UnaryExpr)*
UnaryExpr ::=  ( ! | - | length | width) UnaryExpr  |  UnaryExprPostfix
UnaryExprPostfix::= PrimaryExpr (PixelSelector | ε ) (ChannelSelector | ε )
PrimaryExpr ::=STRING_LIT | NUM_LIT | BOOLEAN_LIT | IDENT | ( Expr ) | Z
    ExpandedPixel  
ChannelSelector ::= : red | : green | : blue                           ***** NOT LL(1)!!!!!!!!! *****
PixelSelector  ::= [ Expr , Expr ]
ExpandedPixel ::= [ Expr , Expr , Expr ]
Dimension  ::=  [ Expr , Expr ]                         

 */

public class ExpressionParser implements IParser {
	
	final ILexer lexer;
	private IToken t;
	

	/**
	 * @param lexer
	 * @throws LexicalException 
	 */
	public ExpressionParser(ILexer lexer) throws LexicalException {
		super();
		this.lexer = lexer;
		t = lexer.next();
	}


	@Override
	public AST parse() throws PLCCompilerException {
		Expr e = expr();
		return e;
	}


	private Expr expr() throws PLCCompilerException {
		IToken firstToken = t;
		return PrimaryExpr();
		//throw new UnsupportedOperationException("THE PARSER HAS NOT BEEN IMPLEMENTED YET");
	}

	void match(Kind k) throws LexicalException, SyntaxException {
		if (t.kind() == k) {
			t = lexer.next();
		}
		else {
			throw new SyntaxException("Error: Expecting token of kind: " + k.toString());
		}
	}
	void consume() throws LexicalException {
		t = lexer.next();
	}

	// PrimaryExpr ::= STRING_LIT | NUM_LIT | BOOLEAN_LIT | IDENT | ( Expr ) | CONST | ExpandedPixel
    Expr PrimaryExpr() throws PLCCompilerException {
		IToken firstToken = t;
		Expr e = null;
		if (firstToken.kind() == STRING_LIT) {
			consume();
			e = new StringLitExpr(firstToken);
		}
		else if (firstToken.kind() == NUM_LIT) {
			consume();
			e = new NumLitExpr(firstToken);
		}
		else if (firstToken.kind() == BOOLEAN_LIT) {
			consume();
			e = new BooleanLitExpr(firstToken);
		}
		else if (firstToken.kind() == IDENT) {
			consume();
			e = new IdentExpr(firstToken);
		}
		else if (firstToken.kind() == CONST) {
			consume();
			e = new ConstExpr(firstToken);
		}
		else if (firstToken.kind() == LPAREN) {
			consume();
			e = expr();
			match(RPAREN);
		}
		else if (firstToken.kind() == LSQUARE) {
			e = ExpandedPixel();
		}

		else {
			throw new SyntaxException("Error: expecting string literal, num literal, boolean literal, identifier, " +
					"( ), constant, or expanded pixel ");
		}
        return e;
    }

	// ExpandedPixel ::= [ Expr , Expr , Expr ]
	Expr ExpandedPixel() throws PLCCompilerException {
		IToken firstToken = t;
		Expr red = null;
		Expr green = null;
		Expr blue = null;
		match(LSQUARE);
		red = expr();
		match(COMMA);
		green = expr();
		match(COMMA);
		blue = expr();
		match(RSQUARE);
		red = new ExpandedPixelExpr(firstToken, red, green, blue);
		return red;
	}

}
