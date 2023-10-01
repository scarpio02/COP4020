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
 -> ChannelSelector ::= : (red | green | blue)
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



	//ConditionalExpr ::=  ?  Expr  :  Expr  :  Expr
	//LogicalOrExpr ::= LogicalAndExpr (    (   |   |   ||   ) LogicalAndExpr)*
	//LogicalAndExpr ::=  ComparisonExpr ( (   &   |  &&   )  ComparisonExpr)*
	//ComparisonExpr ::= PowExpr ( (< | > | == | <= | >=) PowExpr)*

	// PowExpr ::= AdditiveExpr ** PowExpr |   AdditiveExpr
	// PowExpr ::= AdditiveExpr (** AdditiveExpr)*
	Expr PowExpr() throws PLCCompilerException {
		IToken firstToken = t;
		IToken op = null;
		Expr left = null;
		Expr right = null;
		left = AdditiveExpr();
		while (t.kind() == EXP) {
			op = t;
			consume();
			right = AdditiveExpr();
			left = new BinaryExpr(firstToken, left, op, right);
		}
		return left;
	}

	//AdditiveExpr ::= MultiplicativeExpr ( ( + | -  ) MultiplicativeExpr )*
	Expr AdditiveExpr() throws PLCCompilerException {
		IToken firstToken = t;
		IToken op = null;
		Expr left = null;
		Expr right = null;
		left = MultiplicativeExpr();
		while (t.kind() == PLUS || t.kind() == MINUS) {
			op = t;
			consume();
			right = MultiplicativeExpr();
			left = new BinaryExpr(firstToken, left, op, right);
		}
		return left;
	}

	// MultiplicativeExpr ::= UnaryExpr (( * |  /  |  % ) UnaryExpr)*
	Expr MultiplicativeExpr() throws PLCCompilerException {
		IToken firstToken = t;
		IToken op = null;
		Expr left = null;
		Expr right = null;
		left = UnaryExpr();
		while (t.kind() == TIMES || t.kind() == DIV || t.kind() == MOD) {
			op = t;
			consume();
			right = UnaryExpr();
			left = new BinaryExpr(firstToken, left, op, right);
		}
		return left;
	}

	// FIXME: IDK if this is correct. How should I return multiple ops and then the postfix???????
	// FIXME: LL(1)???
	// UnaryExpr ::=  ( ! | - | length | width) UnaryExpr  |  PostfixExpr
	// UnaryExpr ::=  ( ! | - | width | height)* PostfixExpr
	Expr UnaryExpr() throws PLCCompilerException {
		IToken firstToken = t;
		IToken op = null;
		Expr e = null;
		if (t.kind() == BANG || t.kind() == MINUS || t.kind() == RES_width || t.kind() == RES_height) {
			op = t;
			consume();
			e = UnaryExpr();
			e = new UnaryExpr(firstToken, op, e);
		}
		else {
			e = PostfixExpr();
		}
		return e;
	}

	//FIXME: how do I determine if after PrimaryExpr comes a PixelSelector or an ExpandedPixelExpr??????
	//FIXME: "A PostfixExpr could have both a PixelSelector and a ChannelSelector, but if neither of
	// those things is there, it will be some other type of Expr" ????????????????????????

	// PostfixExpr::= PrimaryExpr (PixelSelector | ε ) (ChannelSelector | ε )
	Expr PostfixExpr() throws PLCCompilerException {
		IToken firstToken = t;
		Expr pe = null;
		Expr e = null;
		PixelSelector ps = null;
		ChannelSelector cs = null;
		pe = PrimaryExpr();

		if (t.kind() == LSQUARE) {
			ps = PixelSelector();
			e = new PostfixExpr(firstToken, pe, ps, cs);
		}
		if (t.kind() == COLON) {
			cs = ChannelSelector();
			e = new PostfixExpr(firstToken, pe, ps, cs);
		}
		if (e != null) {
			return e;
		}
		else {
			return pe;
		}
	}

	// PrimaryExpr ::= STRING_LIT | NUM_LIT | BOOLEAN_LIT | IDENT | ( Expr ) | CONST | ExpandedPixelExpr
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
			e = ExpandedPixelExpr();
		}

		else {
			throw new SyntaxException("Error: expecting string literal, num literal, boolean literal, identifier, " +
					"( ), constant, or expanded pixel");
		}
        return e;
    }

	// ChannelSelector ::= : (red | green | blue)
	ChannelSelector ChannelSelector() throws PLCCompilerException {
		IToken firstToken = t;
		match(COLON);
		if (t.kind() == RES_red || t.kind() == RES_green || t.kind() == RES_blue) {
			IToken color = t;
			consume();
			return new ChannelSelector(firstToken, t);
		}
		else {
			throw new SyntaxException("Error: expecting reserved red, green, or blue token");
		}
	}

	//PixelSelector  ::= [ Expr , Expr ]
	PixelSelector PixelSelector() throws PLCCompilerException {
		IToken firstToken = t;
		Expr xExpr = null;
		Expr yExpr = null;
		match(LSQUARE);
		xExpr = expr();
		match(COMMA);
		yExpr = expr();
		match(RSQUARE);
		return new PixelSelector(firstToken, xExpr, yExpr);
	}

	// ExpandedPixelExpr ::= [ Expr , Expr , Expr ]
	Expr ExpandedPixelExpr() throws PLCCompilerException {
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
