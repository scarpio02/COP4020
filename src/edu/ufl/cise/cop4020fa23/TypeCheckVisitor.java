package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;

import java.util.List;

public class TypeCheckVisitor implements ASTVisitor {
    SymbolTable st;
    Program root;

    public TypeCheckVisitor() {
        st = new SymbolTable();
    }

    void check(boolean b, AST obj, String error) throws TypeCheckException {
        if (!b)
        {
            throw new TypeCheckException(obj.firstToken().sourceLocation(), error);
        }
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitAssignmentStatement invoked.");
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitBinaryExpr invoked.");
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCCompilerException {
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitBlock invoked.");

        /*
        Block ::= (Declaration | Statement )*
            symbolTable.enterScope()
            check children
            symbolTable.leaveScope()
        */

        st.enterScope();
        List<Block.BlockElem> blockElems = block.getElems();
        for (Block.BlockElem elem : blockElems) {
            elem.visit(this, arg);
        }
        st.leaveScope();
        return block;

    }

    @Override
    public Object visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitBlockStatement invoked.");
    }

    @Override
    public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitChannelSelector invoked.");
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitConditionalExpr invoked.");
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitDeclaration invoked.");
        /*
        Declaration::= NameDef Expr?
            Condition: Expr == null
            || Expr.type == NameDef.type
            || (Expr.type == STRING && NameDef.type == IMAGE)
            Declaration.type  NameDef.type
            Note: visit Expr before NameDef
        */


    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitDimension invoked.");

        /*
        Dimension  ::=  Expr[width] Expr[height]
            Condition:  Expr[width].type  == INT
            Condition:  Expr[height].type  == INT
        */

        Type typeW = (Type) dimension.getWidth().visit(this, arg);
        check(typeW == Type.INT, dimension, "image width must be int");
        Type typeH = (Type) dimension.getHeight().visit(this, arg);
        check(typeH == Type.INT, dimension, "image height must be int");
        return dimension;
    }

    @Override
    public Object visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitDoStatement invoked.");
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitExpandedPixelExpr invoked.");
    }

    @Override
    public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitGuardedBlock invoked.");
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitIdentExpr invoked.");
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitIfStatement invoked.");
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitLValue invoked.");
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitNameDef invoked.");
        /*
        NameDef ::= Type Dimension? IDENT
            Condition: if (Dimension != null) { type == IMAGE }
            else Type ∈ {INT, BOOLEAN, STRING, PIXEL, IMAGE}
            NameDef.type <- type
            symbolTable.insert(nameDef) is successful
        */
        Type type;
        if (nameDef.getDimension() != null) {
            type = Type.IMAGE;
        }
        else {
            type = nameDef.getType();
        }
        nameDef.setType(type);
        st.insert(nameDef);
        return type;
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitNumLitExpr invoked.");

        /*
        NumLitExpr
            NumLitExpr.type <- INT
        */

        Type type = Type.INT;
        numLitExpr.setType(type);
        return type;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitPixelSelector invoked.");
    }

    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitPostfixExpr invoked.");
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCCompilerException {
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitProgram invoked.");
        /*
        Program::= Type IDENT NameDef* Block
            Program.type <- Type
            symbolTable.enterScope()
            check children NameDef* and Block
            symbolTable.leave Scope()
            Note: there are no constraints involving IDENT—it is not entered into the symbol table
         */
        //FIXME: "Not completely correct. Returns type because it is usually convenient to return a type for any node that has one."
        root = program;
        Type type = Type.kind2type(program.getTypeToken().kind());
        program.setType(type);
        st.enterScope();
        List<NameDef> params = program.getParams();
        for (NameDef param : params) {
            param.visit(this, arg);
        }
        program.getBlock().visit(this, arg);
        st.leaveScope();
        return type;

    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitReturnStatement invoked.");
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitStringLitExpr invoked.");
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitUnaryExpr invoked.");
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitWriteStatement invoked.");

        /*
        WriteStatement ::= Expr[expr]
        */

        writeStatement.getExpr().visit(this, arg);
        return writeStatement;
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitBooleanLitExpr invoked.");
    }

    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitConstExpr invoked.");
    }
}
