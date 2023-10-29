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

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;

import static edu.ufl.cise.cop4020fa23.Kind.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser implements IParser {
	
	final ILexer lexer;
	private IToken t;

	public Parser(ILexer lexer) throws LexicalException {
		super();
		this.lexer = lexer;
		t = lexer.next();
	}


	@Override
	public AST parse() throws PLCCompilerException {
		AST e = program();
		if (t.kind() != EOF) {
			throw new SyntaxException("Error: tokens remaining");
		}
		return e;
	}

	// Program::= Type IDENT ( ParamList ) Block
	private AST program() throws PLCCompilerException {
		IToken firstToken = t;
		IToken type = Type();
		IToken ident = null;
		List<NameDef> list = null;
		if (t.kind() == IDENT) {
			ident = t;
		}
		match(IDENT);
		match(LPAREN);
		list = ParamList();
		match(RPAREN);
		Block b = Block();
		return new Program(firstToken, type, ident, list, b);
	}

	// Block ::= <: (Declaration ; | Statement ;)* :>
	private Block Block() throws PLCCompilerException {
		IToken firstToken = t;
		match(BLOCK_OPEN);
		List<Block.BlockElem> elems = new ArrayList<Block.BlockElem>();
		Block.BlockElem el = null;

		if (t.kind() == RES_image || t.kind() == RES_pixel || t.kind() == RES_int
				|| t.kind() == RES_string || t.kind() == RES_void || t.kind() == RES_boolean
				|| t.kind() == IDENT || t.kind() == RES_write || t.kind() == RES_do
				|| t.kind() == RES_if || t.kind() == RETURN || t.kind() == BLOCK_OPEN) {

			while (t.kind() == RES_image || t.kind() == RES_pixel || t.kind() == RES_int
					|| t.kind() == RES_string || t.kind() == RES_void || t.kind() == RES_boolean
					|| t.kind() == IDENT || t.kind() == RES_write || t.kind() == RES_do
					|| t.kind() == RES_if || t.kind() == RETURN || t.kind() == BLOCK_OPEN) {
				if (t.kind() == RES_image || t.kind() == RES_pixel || t.kind() == RES_int
						|| t.kind() == RES_string || t.kind() == RES_void || t.kind() == RES_boolean) {
					el = Declaration();

				}
				else {
					el = Statement();
				}
				elems.add(el);
				match(SEMI);
			}
		}
		match(BLOCK_CLOSE);
		return new Block(firstToken, elems);
	}

	// ParamList ::= ε | NameDef ( , NameDef ) *
	private List<NameDef> ParamList() throws PLCCompilerException {
		List<NameDef> list = new ArrayList<NameDef>();
		if (t.kind() == RES_image || t.kind() == RES_pixel || t.kind() == RES_int 
				|| t.kind() == RES_string || t.kind() == RES_void || t.kind() == RES_boolean) {
			NameDef nd = NameDef();
			list.add(nd);
			while (t.kind() == COMMA) {
				consume();
				nd = NameDef();
				list.add(nd);
			}
		}
		return list;
	}

	// NameDef ::= Type IDENT | Type Dimension IDENT
	// NameDef ::= Type (Dimension | ε) IDENT
	private NameDef NameDef() throws PLCCompilerException {
		IToken firstToken = t;
		IToken type = Type();
		IToken ident = null;
		Dimension d = null;
		if (t.kind() == LSQUARE) {
			d = Dimension();
		}
		if (t.kind() == IDENT)
		{
			ident = t;
		}
		match(IDENT);
		return new NameDef(firstToken, type, d, ident);
	}

	// Type ::= image | pixel | int | string | void | boolean
	private IToken Type() throws LexicalException, SyntaxException {
		IToken firstToken = t;
        return switch (firstToken.kind()) {
            case RES_image, RES_pixel, RES_int, RES_string, RES_void, RES_boolean -> {
                consume();
                yield firstToken;
            }
            default -> throw new SyntaxException("Error: expecting image, pixel, int, string, void, or boolean");
        };
	}

	// Declaration::= NameDef | NameDef = Expr
	// Declaration::= NameDef (= Expr | ε)
	private Block.BlockElem Declaration() throws PLCCompilerException {
		IToken firstToken = t;
		NameDef nd = NameDef();
		Expr e = null;
		if (t.kind() == ASSIGN) {
			consume();
			e = expr();
		}
		return new Declaration(firstToken, nd, e);

	}


	// Expr ::=  ConditionalExpr | LogicalOrExpr
	private Expr expr() throws PLCCompilerException {
		IToken firstToken = t;
		Expr e = null;
		if (firstToken.kind() == QUESTION) {
			e = ConditionalExpr();
		}
		else {
			e = LogicalOrExpr();
		}
		return e;
	}

	private void match(Kind k) throws LexicalException, SyntaxException {
		if (t.kind() == k) {
			t = lexer.next();
		}
		else {
			throw new SyntaxException("Error: Expecting token of kind: " + k.toString());
		}
	}
	private void consume() throws LexicalException {
		t = lexer.next();
	}



	//ConditionalExpr ::=  ?  Expr  ->  Expr  ,  Expr
	private Expr ConditionalExpr () throws PLCCompilerException {
		IToken firstToken = t;
		Expr guard = null;
		Expr trueE = null;
		Expr falseE = null;
		match(QUESTION);
		guard = expr();
		match(RARROW);
		trueE = expr();
		match(COMMA);
		falseE = expr();
		return guard = new ConditionalExpr(firstToken, guard, trueE, falseE);

	}

	//LogicalOrExpr ::= LogicalAndExpr (    (   |   |   ||   ) LogicalAndExpr)*
	private Expr LogicalOrExpr() throws PLCCompilerException {
		IToken firstToken = t;
		IToken op = null;
		Expr left = null;
		Expr right = null;
		left = LogicalAndExpr();
		while (t.kind() == BITOR|| t.kind() == OR) {
			op = t;
			consume();
			right = LogicalAndExpr();
			left = new BinaryExpr(firstToken, left, op, right);
		}
		return left;
	}

	//LogicalAndExpr ::=  ComparisonExpr ( (   &   |  &&   )  ComparisonExpr)*
	private Expr LogicalAndExpr() throws PLCCompilerException {
		IToken firstToken = t;
		IToken op = null;
		Expr left = null;
		Expr right = null;
		left = ComparisonExpr();
		while (t.kind() == BITAND|| t.kind() == AND) {
			op = t;
			consume();
			right = ComparisonExpr();
			left = new BinaryExpr(firstToken, left, op, right);
		}
		return left;
	}

	//ComparisonExpr ::= PowExpr ( (< | > | == | <= | >=) PowExpr)*
	private Expr ComparisonExpr() throws PLCCompilerException {
		IToken firstToken = t;
		IToken op = null;
		Expr left = null;
		Expr right = null;
		left = PowExpr();
		while (t.kind() == LT || t.kind() == GT || t.kind() == EQ || t.kind() == LE || t.kind() == GE) {
			op = t;
			consume();
			right = PowExpr();
			left = new BinaryExpr(firstToken, left, op, right);
		}
		return left;
	}

	// PowExpr ::= AdditiveExpr ** PowExpr |   AdditiveExpr
	private Expr PowExpr() throws PLCCompilerException {
		IToken firstToken = t;
		IToken op = null;
		Expr left = null;
		Expr right = null;
		left = AdditiveExpr();
		while (t.kind() == EXP) {
			op = t;
			consume();
			right = PowExpr();
			left = new BinaryExpr(firstToken, left, op, right);
		}
		return left;
	}

	//AdditiveExpr ::= MultiplicativeExpr ( ( + | -  ) MultiplicativeExpr )*
	private Expr AdditiveExpr() throws PLCCompilerException {
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
	private Expr MultiplicativeExpr() throws PLCCompilerException {
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
	private Expr UnaryExpr() throws PLCCompilerException {
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
	private Expr PostfixExpr() throws PLCCompilerException {
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
	private Expr PrimaryExpr() throws PLCCompilerException {
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
	private ChannelSelector ChannelSelector() throws PLCCompilerException {
		IToken firstToken = t;
		match(COLON);
		if (t.kind() == RES_red || t.kind() == RES_green || t.kind() == RES_blue) {
			IToken color = t;
			consume();
			return new ChannelSelector(firstToken, color);
		}
		else {
			throw new SyntaxException("Error: expecting reserved red, green, or blue token");
		}
	}

	//PixelSelector  ::= [ Expr , Expr ]
	private PixelSelector PixelSelector() throws PLCCompilerException {
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
	private Expr ExpandedPixelExpr() throws PLCCompilerException {
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

	// FIXME: Dimension is same as PixelSelector. Is this LL(1)?

	// Dimension ::= [ Expr, Expr ]
	private Dimension Dimension() throws PLCCompilerException {
		IToken firstToken = t;
		Expr xExpr = null;
		Expr yExpr = null;
		match(LSQUARE);
		xExpr = expr();
		match(COMMA);
		yExpr = expr();
		match(RSQUARE);
		return new Dimension(firstToken, xExpr, yExpr);
	}

	// LValue ::= IDENT (PixelSelector | ε ) (ChannelSelector | ε )
	private LValue LValue() throws PLCCompilerException {
		IToken firstToken = t;
		IToken ident = null;
		if (t.kind() == IDENT) {
			ident = t;
		}
		match(IDENT);
		PixelSelector ps = null;
		ChannelSelector cs = null;

		if (t.kind() == LSQUARE) {
			ps = PixelSelector();
		}
		if (t.kind() == COLON) {
			cs = ChannelSelector();
		}
		return new LValue(firstToken, ident, ps, cs);

	}

	/*
	Statement::=
		LValue = Expr |
		write Expr |
		do GuardedBlock ( [] GuardedBlock) * od |
		if GuardedBlock ( [] GuardedBlock) * fi |
		^ Expr |
		BlockStatement
	 */
	private Block.BlockElem Statement() throws PLCCompilerException {
		IToken firstToken = t;
		Block.BlockElem s = null;

		if (firstToken.kind() == IDENT) {
			LValue l = LValue();
			match(ASSIGN);
			Expr e = expr();
			s = new AssignmentStatement(firstToken, l, e);
		}
		else if (firstToken.kind() == RES_write)
		{
			consume();
			Expr e = expr();
			s = new WriteStatement(firstToken, e);
		}
		else if (firstToken.kind() == RES_do)
		{
			consume();
			List<GuardedBlock> blocks = new ArrayList<GuardedBlock>();
			GuardedBlock gb = GuardedBlock();
			blocks.add(gb);
			while (t.kind() == BOX)
			{
				consume();
				gb = GuardedBlock();
				blocks.add(gb);
			}
			match(RES_od);
			return new DoStatement(firstToken, blocks);
		}
		else if (firstToken.kind() == RES_if)
		{
			consume();
			List<GuardedBlock> blocks = new ArrayList<GuardedBlock>();
			GuardedBlock gb = GuardedBlock();
			blocks.add(gb);
			while (t.kind() == BOX)
			{
				consume();
				gb = GuardedBlock();
				blocks.add(gb);
			}
			match(RES_fi);
			return new IfStatement(firstToken, blocks);

		}
		else if (firstToken.kind() == RETURN)
		{
			consume();
			Expr e = expr();
			s = new ReturnStatement(firstToken, e);
		}
		else if (firstToken.kind() == BLOCK_OPEN)
		{
			s = BlockStatement();
		}
		else {
			throw new SyntaxException("Error: expecting identifier, write, if, do, ^, or <:");
		}
		return s;
	}

	// GuardedBlock := Expr -> Block
	private GuardedBlock GuardedBlock() throws PLCCompilerException {
		IToken firstToken = t;
		Expr e = expr();
		match(RARROW);
		Block b = Block();
		return new GuardedBlock(firstToken, e, b);
	}

	// BlockStatement ::= Block
	private Block.BlockElem BlockStatement() throws PLCCompilerException {
		IToken firstToken = t;
		Block B = Block();
		return new StatementBlock(firstToken, B);
	}

}
