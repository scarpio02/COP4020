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

    void AssignmentCompatible(Type typeL, Type typeE) throws TypeCheckException {
        if (typeL == typeE ||
                (typeL == Type.PIXEL && typeE == Type.INT) ||
                (typeL == Type.IMAGE && typeE == Type.PIXEL) ||
                (typeL == Type.IMAGE && typeE == Type.INT) ||
                (typeL == Type.IMAGE && typeE == Type.STRING)) {
            return;
        }

        throw new TypeCheckException("Assignment statement types are incompatible");

    }
    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitAssignmentStatement invoked.");
//        AssignmentStatement ::= LValue[lValue] Expr[e]
    //        symbolTable.enterScope()
    //        visit children to check condition
    //        Condition: AssignmentCompatible (LValue.type, Expr.type)
    //        symbolTable.leaveScope()

        st.enterScope();
        Type typeL = (Type) assignmentStatement.getlValue().visit(this, arg);
        Type typeE = (Type) assignmentStatement.getE().visit(this, arg);
        AssignmentCompatible(typeL, typeE);
        st.leaveScope();
        return assignmentStatement; //FIXME: I'm not sure what's best to return here
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
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitBlockStatement invoked.");
        //StatementBlock ::= Block[block]

        statementBlock.getBlock().visit(this, arg);
        return statementBlock; //FIXME: I'm not sure what's best to return here
    }

    @Override
    public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitChannelSelector invoked.");
        Kind c = channelSelector.color();
        if (c == Kind.RES_red || c == Kind.RES_green || c == Kind.RES_blue) {
            return channelSelector; //FIXME: I'm not sure what's best to return here
        }
        else {
            throw new TypeCheckException("ChannelSelector has invalid type");
        }
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
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitDoStatement invoked.");
        //DoStatement ::= GuardedBlock+
        List<GuardedBlock> guardedBlocks = doStatement.getGuardedBlocks();
        for (GuardedBlock gb : guardedBlocks) {
            gb.visit(this, arg);
        }
        return doStatement; //FIXME: I'm not sure what's best to return here
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitExpandedPixelExpr invoked.");
        /*
        ExpandedPixelExpr ::= Expr[red] Expr[green] Expr[blue]
            Condition: Expr[red].type == INT
            Condition: Expr[green].type == INT
            Condition: Expr[blue].type == INT
            ExpandedPixelExpr.type <- PIXEL
        */

        Type typeR = (Type) expandedPixelExpr.getRed().visit(this, arg);
        check(typeR == Type.INT, expandedPixelExpr, "Expanded pixel's red component must be int");
        Type typeG = (Type) expandedPixelExpr.getGreen().visit(this, arg);
        check(typeG == Type.INT, expandedPixelExpr, "Expanded pixel's green component must be int");
        Type typeB = (Type) expandedPixelExpr.getBlue().visit(this, arg);
        check(typeB == Type.INT, expandedPixelExpr, "Expanded pixel's blue component must be int");
        Type type = Type.PIXEL;
        expandedPixelExpr.setType(type);
        return type;
    }

    @Override
    public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitGuardedBlock invoked.");
        /*
        GuardedBlock := Expr[Guard] Block[block]
            Condition: Expr.type == BOOLEAN
        */
        Type typeE = (Type) guardedBlock.getGuard().visit(this, arg);
        check(typeE == Type.BOOLEAN, guardedBlock, "Guarded block guard must be boolean");
        guardedBlock.getBlock().visit(this, arg);
        return typeE; //FIXME: I'm not sure what's best to return here
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitIdentExpr invoked.");
//        IdentExpr
    //        Condition: symbolTable.lookup(IdentExpr.name) defined
    //        IdentExpr.nameDef <- symbolTable.lookup(IdentExpr.name)
    //        IdentExpr.type <- IdentExpr.nameDef.type
        identExpr.setNameDef(st.lookup(identExpr.getName()));
        Type type = (Type) identExpr.getNameDef().visit(this, arg);
        identExpr.setType(type);
        return type;

    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitIfStatement invoked.");
        //IfStatement ::= GuardedBlock+
        List<GuardedBlock> guardedBlocks = ifStatement.getGuardedBlocks();
        for (GuardedBlock gb : guardedBlocks) {
            gb.visit(this, arg);
        }
        return ifStatement; //FIXME: I'm not sure what's best to return here

    }

    Type inferLValueType(Type varType, PixelSelector ps, ChannelSelector cs) throws TypeCheckException {
        if (ps == null && cs == null) {
            return varType;
        }
        if (varType == Type.IMAGE) {
            if (ps != null && cs == null) {
                return Type.PIXEL;
            }
            else if (ps != null && cs != null) {
                return Type.INT;
            }
            else if (ps == null && cs != null) {
                return Type.IMAGE;
            }
        }
        if (varType == Type.PIXEL) {
            if (ps == null && cs != null) {
                return Type.INT;
            }
        }
        throw new TypeCheckException("invalid LValue var type");

    }
    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitLValue invoked.");
//        LValue ::= IDENT[nameToken] PixelSelector? ChannelSelector?
//            LValue.nameDef <- symbolTable.lookup(name)
    //        LValue.varType <- LValue.nameDef.type
    //        Condition: if (PixelSelector != null) LValue.varType == IMAGE
    //        Condition: if (ChannelSelector != null) LValue.varType ∈ { PIXEL, IMAGE}
    //        Condition: inferLValueType is defined
    //        LValue.type <- inferLValueType
        NameDef nd = st.lookup(lValue.getName());
        nd.visit(this, arg);
        lValue.setNameDef(nd);
        if (lValue.getPixelSelector() != null) {
            check(lValue.getVarType() == Type.IMAGE, lValue, "LValue var type must be image");
            lValue.getPixelSelector().visit(this, lValue);
        }
        if (lValue.getChannelSelector() != null) {
            if(lValue.getVarType() != Type.IMAGE && lValue.getVarType() != Type.PIXEL) {
                throw new TypeCheckException("LValue var type must be image or pixel");
            }
            lValue.getChannelSelector().visit(this, arg);
        }
        Type type = inferLValueType(lValue.getVarType(), lValue.getPixelSelector(), lValue.getChannelSelector());
        lValue.setType(type);
        return type;
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
            nameDef.getDimension().visit(this, arg);
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
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitPixelSelector invoked.");
//        PixelSelector ::= Expr[xExpr] Expr[yExpr]
    //        If the PixelSelector’s parent is an LValue then
        //        Condition: Expr[xExpr] is an IdentExp or NumLitExpr
        //        Condition: Expr[yExpr] is an IdentExp or NumLitExpr
        //        If Expr[xExpr] is an IdentExp and symbolTable.lookup(Expr[xExpr].name == null)
            //        Insert a SyntheticNameDef with name Expr[xExpr].name
            //        and type INT into the symbol table
        //        end if`
        //        If Expr[yExpr] is an IdentExp and symbolTable.lookup(Expr[yExpr].name == null)
            //        Insert a SyntheticNameDef with name Expr[yExpr].name
            //        and type INT into the symbol table
        //        end if
    //        end if
    //        Condition: Expr[xExpr].type == INT
    //        Condition: Expr[yExpr].type == INT

        if (arg instanceof LValue) {
            if (pixelSelector.xExpr() instanceof IdentExpr || pixelSelector.xExpr() instanceof NumLitExpr)
            {
                if ((pixelSelector.xExpr() instanceof IdentExpr) && (st.lookup(((IdentExpr) pixelSelector.xExpr()).getName()) == null)) {
                    NameDef nd = new SyntheticNameDef(((IdentExpr) pixelSelector.xExpr()).getName());
                    nd.setType(Type.INT);
                    st.insert(nd);
                }
            }
            if (pixelSelector.yExpr() instanceof IdentExpr || pixelSelector.yExpr() instanceof NumLitExpr)
            {
                if ((pixelSelector.yExpr() instanceof IdentExpr) && (st.lookup(((IdentExpr) pixelSelector.yExpr()).getName()) == null)) {
                    NameDef nd = new SyntheticNameDef(((IdentExpr) pixelSelector.yExpr()).getName());
                    nd.setType(Type.INT);
                    st.insert(nd);
                }
            }
        }
        Type typeX = (Type) pixelSelector.xExpr().visit(this, arg);
        check(typeX == Type.INT, pixelSelector, "Pixel selector X must be int");
        Type typeY = (Type) pixelSelector.yExpr().visit(this, arg);
        check(typeY == Type.INT, pixelSelector, "Pixel selector Y must be int");
        return  pixelSelector; //FIXME: I'm not sure what's best to return here
    }

    Type inferPostfixExprType(Type e, PixelSelector ps, ChannelSelector cs) throws TypeCheckException {
        if (ps == null && cs == null) {
            return e;
        }
        else if (e == Type.IMAGE && ps != null && cs == null) {
            return Type.PIXEL;
        }
        else if (e == Type.IMAGE && ps != null && cs != null) {
            return Type.INT;
        }
        else if (e == Type.IMAGE && ps == null && cs != null) {
            return Type.IMAGE;
        }
        else if (e == Type.PIXEL && ps == null && cs != null) {
            return Type.INT;
        }

        throw new TypeCheckException("Invalid types for PostfixExpr");
    }
    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitPostfixExpr invoked.");
        /*
        PostfixExpr::= Expr PixelSelector? ChannelSelector?
            Condition: inferPostfixExprType is defined
            PostfixExpr.type  inferPostfixExprType(Epxr.type, PixelSelector, ChannelSelector)
        */
        Type typeE = (Type) postfixExpr.primary().visit(this, arg);
        Type type = inferPostfixExprType(typeE, postfixExpr.pixel(), postfixExpr.channel());
        postfixExpr.setType(type);
        return type;
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
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitReturnStatement invoked.");
//        ReturnStatement ::= Expr[e]
    //        Condition: Expr.type == Program.type (where Program is the enclosing program)
        Type type = (Type) returnStatement.getE().visit(this, arg);
        check(type == root.getType(), returnStatement, "Return statement and program types do no match");
        return type;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitStringLitExpr invoked.");
//        StringLitExpr
//          StringLitExpr.type <- STRING
        Type type = Type.STRING;
        stringLitExpr.setType(type);
        return type;
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
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitBooleanLitExpr invoked.");
//      BooleanLitExpr
//          BooleanLitExpr.type <- BOOLEAN
        Type type = Type.BOOLEAN;
        booleanLitExpr.setType(type);
        return type;
    }

    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        //throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitConstExpr invoked.");
//        ConstExpr
//          ConstExpr.type <- if (ConstExpr.name == ‘Z’) INT else PIXEL
        Type type = Type.PIXEL;
        if (constExpr.getName() == "Z") {
            type = Type.INT;
        }
        constExpr.setType(type);
        return type;
    }
}
