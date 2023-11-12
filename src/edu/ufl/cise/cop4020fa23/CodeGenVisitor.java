package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.CodeGenException;

public class CodeGenVisitor implements ASTVisitor {


    public CodeGenVisitor() {

    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitAssignmentStatement not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitBinaryExpr not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitBlock not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitBlockStatement not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitChannelSelector not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitConditionalExpr not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitDeclaration not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitDimension not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitDoStatement not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitExpandedPixelExpr not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitGuardedBlock not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitIdentExpr not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitIfStatement not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitLValue not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitNameDef not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitNumLitExpr not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitPixelSelector not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitPostfixExpr not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitProgram not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitReturnStatement not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitStringLitExpr not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitUnaryExpr not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitWriteStatement not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitBooleanLitExpr not implemented in CodeGenVisitor");
        return null;
    }

    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("visitConstExpr not implemented in CodeGenVisitor");
        return null;
    }
}
