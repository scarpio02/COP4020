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

    Type inferBinaryType(Type l, Kind op, Type r) throws TypeCheckException {
        if (l == Type.PIXEL && r == Type.PIXEL && (op == Kind.BITAND || op == Kind.BITOR)) {
            return Type.PIXEL;
        }
        else if (l == Type.PIXEL && r == Type.INT && op == Kind.EXP) {
            return Type.PIXEL;
        }
        else if ((l == Type.PIXEL || l == Type.IMAGE) && r == Type.INT && (op== Kind.TIMES || op== Kind.DIV || op == Kind.MOD)) {
            return l;
        }
        else if (l == Type.BOOLEAN && r == Type.BOOLEAN && (op == Kind.AND || op == Kind.OR)) {
            return Type.BOOLEAN;
        }
        else if (l == Type.INT && r == Type.INT && (op == Kind.LT || op == Kind.GT || op == Kind.LE || op == Kind.GE)) {
            return Type.BOOLEAN;
        }
        else if (l == Type.INT && r == Type.INT && op == Kind.EXP) {
            return Type.INT;
        }
        else if ((l == Type.INT || l == Type.PIXEL || l == Type.IMAGE) && r == l && (op == Kind.MINUS || op == Kind.TIMES || op == Kind.DIV || op == Kind.MOD)) {
            return l;
        }
        else if (r == l && op == Kind.EQ) {
            return Type.BOOLEAN;
        }
        else if (r == l && op == Kind.PLUS) {
            return l;
        }

        throw new TypeCheckException("Invalid types for BinaryExpr");
    }
    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException {
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitBinaryExpr invoked.");
        /*
        BinaryExpr ::= ExprleftExpr op ExprrigthExpr ‘
            Condition inferBinaryType is defined
            BinaryExpr.type  inferBinaryType(ExprleftExpr.type, op, ExprrigthExpr .type)
        */
        Type typeL = (Type) binaryExpr.getLeftExpr().visit(this, arg);
        Type typeR = (Type) binaryExpr.getRightExpr().visit(this, arg);
        Type type = inferBinaryType(typeL, binaryExpr.getOpKind(), typeR);
        binaryExpr.setType(type);
        return type;
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
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitConditionalExpr invoked.");

        /*
        ConditionalExpr ::= Expr[guardExpr]
            Expr[trueExpr]
            Expr[falseExpr]
            Condition: Expr[guardExpr].type == BOOLEAN
            Condition: Expr[trueExpr].type == Expr[falseExpr].type
            ConditionalExpr.type <- trueExpr.type
        */
        Type typeG = (Type) conditionalExpr.getGuardExpr().visit(this, arg);
        check(typeG == Type.BOOLEAN, conditionalExpr, "conditional guard expression must be boolean");
        Type typeT = (Type) conditionalExpr.getTrueExpr().visit(this, arg);
        Type typeF = (Type) conditionalExpr.getFalseExpr().visit(this, arg);
        check(typeT == typeF, conditionalExpr, "conditional true and false expressions must be same type");
        conditionalExpr.setType(typeT);
        return typeT;

    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitDeclaration invoked.");
        /*
        Declaration::= NameDef Expr?
            Condition: Expr == null
            || Expr.type == NameDef.type
            || (Expr.type == STRING && NameDef.type == IMAGE)
            Declaration.type <- NameDef.type
            Note: visit Expr before NameDef
        */
        Type typeE = null;
        if (declaration.getInitializer() != null)
        {
            typeE = (Type) declaration.getInitializer().visit(this, arg);
        }
        Type typeN = (Type) declaration.getNameDef().visit(this, arg);
        if (declaration.getInitializer() == null || typeE == typeN || typeE == Type.STRING && typeN == Type.IMAGE) {
            return typeN;
        }
        throw new TypeCheckException("Invalid types for for declaration");

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
            Type temp = nameDef.getType();
            if (temp == Type.INT || temp == Type.BOOLEAN || temp == Type.STRING || temp == Type.PIXEL || temp == Type.IMAGE) {
                type = temp;
            }
            else {
                throw new TypeCheckException("Invalid type for NameDef");
            }
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

    Type inferUnaryExprType(Type t, Kind op) throws TypeCheckException {
        if (t == Type.BOOLEAN && op == Kind.BANG) {
            return Type.BOOLEAN;
        }
        else if (t == Type.INT && op == Kind.MINUS) {
            return Type.INT;
        }
        else if (t == Type.IMAGE && (op == Kind.RES_width || op == Kind.RES_height)) {
            return Type.INT;
        }

        throw new TypeCheckException("Invalid types for UnaryExpr");
    }
    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCCompilerException {
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitUnaryExpr invoked.");
        /*
        UnaryExpr ::= op Expr
            Condition: inferUnaryExpr is defined
            UnaryExpr.type <- inferUnaryExprType(Expr.type, op,)
        */
        Type typeE = (Type) unaryExpr.getExpr().visit(this, arg);
        Type type = inferUnaryExprType(typeE, unaryExpr.getOp());
        unaryExpr.setType(type);
        return type;
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
