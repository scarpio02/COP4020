package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.CodeGenException;

import java.util.List;

public class CodeGenVisitor implements ASTVisitor {

    StringBuilder javaCode;

    boolean importConsoleIO;
    boolean importFileURLIO;
    boolean importImageOps;
    boolean importPixelOps;
    boolean importPLCRuntimeException;
    public CodeGenVisitor() {
        javaCode = new StringBuilder();
        importConsoleIO = false;
        importFileURLIO = false;
        importImageOps = false;
        importPixelOps = false;
        importPLCRuntimeException = false;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
//        _LValue_ = _Expr_

        assignmentStatement.getlValue().visit(this, arg);
        javaCode.append(" = ");
        assignmentStatement.getE().visit(this, arg);

        return javaCode.toString();
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException {
//        If Expr[leftExpr].type is string and op is EQ
//              _Expr[leftExpr]_ .equals( _Expr[rigthExpr]_ )
//        If op is EXP
//              ((int)Math.round(Math.pow( _Expr[leftExpr]_ , _Expr[rigthExpr]_ ))
//        Otherwise
//                (_Expr[leftExpr]_ _op_ _Expr[rigthExpr]_)

        if (binaryExpr.getLeftExpr().getType() == Type.STRING && binaryExpr.getOpKind() == Kind.EQ) {
            binaryExpr.getLeftExpr().visit(this, arg);
            javaCode.append(".equals(");
            binaryExpr.getRightExpr().visit(this, arg);
            javaCode.append(")");

        }
        else if (binaryExpr.getOpKind() == Kind.EXP) {
            javaCode.append("((int)Math.round(Math.pow(");
            binaryExpr.getLeftExpr().visit(this, arg);
            javaCode.append(",");
            binaryExpr.getRightExpr().visit(this, arg);
            javaCode.append(")))");
        }
        else {
            javaCode.append("(");
            binaryExpr.getLeftExpr().visit(this, arg);
            javaCode.append(" ").append(binaryExpr.getOp().text()).append(" ");
            binaryExpr.getRightExpr().visit(this, arg);
            javaCode.append(")");
        }

        return javaCode.toString();
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCCompilerException {
//        { _BlockElem*_ }
        javaCode.append("{\n");
        List<Block.BlockElem> blockElems = block.getElems();
        for (Block.BlockElem elem : blockElems) {
            javaCode.append("\t\t");
            elem.visit(this, arg);
            javaCode.append(";\n");
        }
        javaCode.append("\t}");

        return javaCode.toString();
    }

    @Override
    public Object visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException {
//        _Block_

        statementBlock.getBlock().visit(this, arg);

        return javaCode.toString();
    }

    @Override
    public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitChannelSelector not implemented in CodeGenVisitor");
        //return null;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCCompilerException {
//        ( _Expr[GuardExpr]_ ? _Expr[TrueExpr]_ : _Expr[FalseExpr]_ )

        javaCode.append("(");
        conditionalExpr.getGuardExpr().visit(this, arg);
        javaCode.append(" ? ");
        conditionalExpr.getTrueExpr().visit(this, arg);
        javaCode.append(" : ");
        conditionalExpr.getFalseExpr().visit(this, arg);
        javaCode.append(")");

        return javaCode.toString();
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
//        Declaration::= NameDef _NameDef_
//        Declaration::= NameDef Expr _NameDef_ = _Expr_

        if (declaration.getNameDef().getType() != Type.IMAGE) {
            declaration.getNameDef().visit(this, arg);

            if (declaration.getInitializer() != null) {
                javaCode.append(" = ");
                declaration.getInitializer().visit(this, arg);
            }
        }
        else {
            if (declaration.getInitializer() == null) {
                javaCode.append("final ");
                declaration.getNameDef().visit(this, arg);
                importImageOps = true;
                javaCode.append(" = ImageOps.makeImage(");
                if (declaration.getNameDef().getDimension() != null) {
                    declaration.getNameDef().getDimension().visit(this, arg);
                    javaCode.append(")");
                } else {
                    throw new CodeGenException("Missing Dimension object in image NameDef");
                }
            }
            else {
                declaration.getNameDef().visit(this, arg);

                if (declaration.getInitializer().getType() == Type.STRING) {
                    importFileURLIO = true;
                    javaCode.append("FileURLIO.readImage(");
                    declaration.getInitializer().visit(this, arg);
                    if (declaration.getNameDef().getDimension() != null) {
                        javaCode.append(", ");
                        declaration.getNameDef().getDimension().visit(this, arg);
                    }
                    javaCode.append(")");
                }
                else if (declaration.getInitializer().getType() == Type.IMAGE) {
                    importImageOps = true;
                    if (declaration.getNameDef().getDimension() == null) {
                        javaCode.append("ImageOps.cloneImage(");
                        declaration.getInitializer().visit(this, arg);
                        javaCode.append(")");
                    }
                    else {
                        javaCode.append("ImageOps.copyAndResize(");
                        declaration.getInitializer().visit(this, arg);
                        javaCode.append(", ");
                        declaration.getNameDef().getDimension().visit(this, arg);
                        javaCode.append(")");
                    }
                }

            }
        }

        return javaCode.toString();
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
        // Dimension ::= Exprwidth Exprheight __Exprwidth__ , __Exprheight __

        dimension.getWidth().visit(this, arg);
        javaCode.append(" , ");
        dimension.getHeight().visit(this, arg);

        return javaCode.toString();
    }

    @Override
    public Object visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitDoStatement not implemented in CodeGenVisitor");
        //return null;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
        // ExpandedPixelExpr ::= Exprred Exprgreen Exprblue PixelOps.pack(_Exprred_, _Exprgreen_, _Exprblue_)

        importPixelOps = true;
        javaCode.append("PixelOps.pack(");
        expandedPixelExpr.getRed().visit(this, arg);
        javaCode.append(", ");
        expandedPixelExpr.getGreen().visit(this, arg);
        javaCode.append(", ");
        expandedPixelExpr.getBlue().visit(this, arg);
        javaCode.append(")");

        return javaCode.toString();
    }

    @Override
    public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitGuardedBlock not implemented in CodeGenVisitor");
        //return null;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
//        _IdentExpr_.getNameDef().getJavaName()

        javaCode.append(identExpr.getNameDef().getJavaName());

        return javaCode.toString();
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitIfStatement not implemented in CodeGenVisitor");
        //return null;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
//        _IdentExpr_.getNameDef().getJavaName()

        javaCode.append(lValue.getNameDef().getJavaName());

        return javaCode.toString();
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {
//        _Type_ _name_
//        Where _name_ is the Java name of the IDENT.

        Type type = nameDef.getType();
        if (type == Type.STRING) {
            javaCode.append("String");
        }
        else if (type == Type.IMAGE) {
            javaCode.append("BufferedImage");
        }
        else if (type == Type.PIXEL) {
            javaCode.append("int");
        }
        else {
            javaCode.append(type.toString().toLowerCase());
        }
        javaCode.append(" ");

        javaCode.append(nameDef.getJavaName());

        return javaCode.toString();
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
//        _NumLitExpr_.getText

        javaCode.append(numLitExpr.getText());

        return javaCode.toString();
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
        // PixelSelector ::= ExprxExpr ExpryExpr __ExprxExpr__ , __ExpryExpr__

        pixelSelector.xExpr().visit(this, arg);
        javaCode.append(" , ");
        pixelSelector.yExpr().visit(this, arg);

        return javaCode.toString();
    }

    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitPostfixExpr not implemented in CodeGenVisitor");
        //return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCCompilerException {
//        public class _IDENT_ {
//            public static _Type_ apply(
//                    _NameDef*_
//            ) _Block
//        }
//        Note: parameters from _NameDef*_ are separated by commas
        javaCode.append("public class ").append(program.getName()).append(" {\n");
        javaCode.append("\tpublic static ");
        Type type = program.getType();
        if (type == Type.STRING) {
            javaCode.append("String");
        }
        else {
            javaCode.append(type.toString().toLowerCase());
        }
        javaCode.append(" apply(");
        List<NameDef> params = program.getParams();
        for (int i = 0; i < params.size(); i++) {
            params.get(i).visit(this, arg);
            if (i < params.size() - 1)
            {
                javaCode.append(", ");
            }
        }
        javaCode.append(") ");
        program.getBlock().visit(this, arg);
        javaCode.append("\n}");
        if (importConsoleIO)
        {
            javaCode.insert(0, "import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;\n");
        }
        if (arg != null && arg != "")
        {
            //javaCode.append("package ").append(arg).append(";\n");
            javaCode.insert(0, "package " + arg + ";\n");
        }


        return javaCode.toString();
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException {
//        return _Expr_

        javaCode.append("return ");
        returnStatement.getE().visit(this, arg);

        return javaCode.toString();
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
//        _StringLitExpr_.getText
        javaCode.append(stringLitExpr.getText());

        return javaCode.toString();
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCCompilerException {
        // ( _op_ _Expr_ )

        javaCode.append("(");
        Kind op = unaryExpr.getOp();
        if (op == Kind.BANG) {
            javaCode.append("!");
        }
        else if (op == Kind.MINUS) {
            javaCode.append("-");
        }
        unaryExpr.getExpr().visit(this, arg);
        javaCode.append(")");

        return javaCode.toString();
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
//        ConsoleIO.write( _Expr_ )
//        Note: you will need to import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO
        //javaCode.insert(0, "import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;\n");

        importConsoleIO = true;
        if (writeStatement.getExpr().getType() == Type.PIXEL) {
            javaCode.append("ConsoleIO.writePixel(");
        }
        else {
            javaCode.append("ConsoleIO.write(");
        }
        writeStatement.getExpr().visit(this, arg);
        javaCode.append(")");

        return javaCode.toString();
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException {
//        true or false

        javaCode.append(booleanLitExpr.getText().toLowerCase());

        return javaCode.toString();
    }

    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitConstExpr not implemented in CodeGenVisitor");
        //return null;
    }
}
