package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.ast.Dimension;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.CodeGenException;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;

import java.awt.*;
import java.util.List;

public class CodeGenVisitor implements ASTVisitor {

    StringBuilder javaCode;

    boolean importConsoleIO;
    boolean importFileURLIO;
    boolean importImageOps;
    boolean importPixelOps;
    boolean importBufferedImage;
    public CodeGenVisitor() {
        javaCode = new StringBuilder();
        importConsoleIO = false;
        importFileURLIO = false;
        importImageOps = false;
        importPixelOps = false;
        importBufferedImage = false;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
        if (assignmentStatement.getlValue().getVarType() == Type.IMAGE) {
            if (assignmentStatement.getlValue().getPixelSelector() == null && assignmentStatement.getlValue().getChannelSelector() == null) {
                importImageOps = true;
                if (assignmentStatement.getE().getType() == Type.IMAGE) {
                    javaCode.append("ImageOps.copyInto(");
                    assignmentStatement.getE().visit(this, arg);
                    javaCode.append(", ");
                    assignmentStatement.getlValue().visit(this, arg);
                    javaCode.append(")");
                }
                else if (assignmentStatement.getE().getType() == Type.PIXEL) {
                    javaCode.append("ImageOps.setAllPixels(");
                    assignmentStatement.getlValue().visit(this, arg);
                    javaCode.append(", ");
                    assignmentStatement.getE().visit(this, arg);
                    javaCode.append(")");
                }
                else if (assignmentStatement.getE().getType() == Type.STRING) {
                    importFileURLIO = true;
                    javaCode.append("ImageOps.copyInto(FileURLIO.readImage(");
                    assignmentStatement.getE().visit(this, arg);
                    javaCode.append("), ");
                    assignmentStatement.getlValue().visit(this, arg);
                    javaCode.append(")");
                }
            }
            else if (assignmentStatement.getlValue().getChannelSelector() != null) {
                throw new UnsupportedOperationException("AssignState's lVal's channelSelector was not null");
            }
            else if (assignmentStatement.getlValue().getPixelSelector() != null && assignmentStatement.getlValue().getChannelSelector() == null) {
//                if (assignmentStatement.getlValue().getPixelSelector().xExpr() instanceof SyntheticNameDef) {
//                    javaCode.append("for (int ");
//                    assignmentStatement.getlValue().getPixelSelector().xExpr().visit(this, arg);
//                    javaCode.append("= 0; ");
//                    assignmentStatement.getlValue().getPixelSelector().xExpr().visit(this, arg);
//                    javaCode.append(" < ");
//                    assignmentStatement.getlValue().visit(this, arg);
//                    javaCode.append(".getWidth(); ");
//                    assignmentStatement.getlValue().getPixelSelector().xExpr().visit(this, arg);
//                    javaCode.append("++) {\n");
//                }
//                if (assignmentStatement.getlValue().getPixelSelector().yExpr() instanceof SyntheticNameDef) {
//                    javaCode.append("for (int ");
//                    assignmentStatement.getlValue().getPixelSelector().yExpr().visit(this, arg);
//                    javaCode.append("= 0; ");
//                    assignmentStatement.getlValue().getPixelSelector().yExpr().visit(this, arg);
//                    javaCode.append(" < ");
//                    assignmentStatement.getlValue().visit(this, arg);
//                    javaCode.append(".getHeight(); ");
//                    assignmentStatement.getlValue().getPixelSelector().yExpr().visit(this, arg);
//                    javaCode.append("++) {\n");
//                }
                //FIXME: I don't get this :/

            }

        }
        else if (assignmentStatement.getlValue().getVarType() == Type.PIXEL && assignmentStatement.getlValue().getChannelSelector() != null) {
            importPixelOps = true;
            //FIXME: determine which "set___"
            javaCode.append("PixelOps.setRed(");
            assignmentStatement.getlValue().visit(this, arg);
            javaCode.append(", ");
            assignmentStatement.getE().visit(this, arg);
            javaCode.append(")");
        }

        //FIXME: idk if this was the correct way to handle test case hw5_6
        else if (assignmentStatement.getlValue().getVarType() == Type.PIXEL && assignmentStatement.getE().getType() == Type.INT) {
            importPixelOps = true;
            assignmentStatement.getlValue().visit(this, arg);
            javaCode.append(" = PixelOps.pack(");
            assignmentStatement.getE().visit(this, arg);
            javaCode.append(", ");
            assignmentStatement.getE().visit(this, arg);
            javaCode.append(", ");
            assignmentStatement.getE().visit(this, arg);
            javaCode.append(")");
        }

        else {
            // _LValue_ = _Expr_
            assignmentStatement.getlValue().visit(this, arg);
            javaCode.append(" = ");
            assignmentStatement.getE().visit(this, arg);
        }

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

        else if (binaryExpr.getLeftExpr().getType() == Type.IMAGE && binaryExpr.getRightExpr().getType() == Type.IMAGE)
        {
            importImageOps = true;
            javaCode.append("(ImageOps.binaryImageImageOp(ImageOps.OP.");
            if (binaryExpr.getOpKind() == Kind.PLUS) {
                javaCode.append("PLUS");
            }
            else if (binaryExpr.getOpKind() == Kind.MINUS) {
                javaCode.append("MINUS");
            }
            else if (binaryExpr.getOpKind() == Kind.TIMES) {
                javaCode.append("TIMES");
            }
            else if (binaryExpr.getOpKind() == Kind.DIV) {
                javaCode.append("DIV");
            }
            else if (binaryExpr.getOpKind() == Kind.MOD) {
                javaCode.append("MOD");
            }
            javaCode.append(", ");
            binaryExpr.getLeftExpr().visit(this, arg);
            javaCode.append(", ");
            binaryExpr.getRightExpr().visit(this, arg);
            javaCode.append("))");
        }
        else if (binaryExpr.getLeftExpr().getType() == Type.IMAGE && binaryExpr.getRightExpr().getType() == Type.PIXEL)
        {
            importImageOps = true;
            javaCode.append("(ImageOps.binaryImagePixelOp(ImageOps.OP.");
            if (binaryExpr.getOpKind() == Kind.PLUS) {
                javaCode.append("PLUS");
            }
            else if (binaryExpr.getOpKind() == Kind.MINUS) {
                javaCode.append("MINUS");
            }
            else if (binaryExpr.getOpKind() == Kind.TIMES) {
                javaCode.append("TIMES");
            }
            else if (binaryExpr.getOpKind() == Kind.DIV) {
                javaCode.append("DIV");
            }
            else if (binaryExpr.getOpKind() == Kind.MOD) {
                javaCode.append("MOD");
            }
            javaCode.append(", ");
            binaryExpr.getLeftExpr().visit(this, arg);
            javaCode.append(", ");
            binaryExpr.getRightExpr().visit(this, arg);
            javaCode.append("))");
        }
        else if (binaryExpr.getLeftExpr().getType() == Type.IMAGE && binaryExpr.getRightExpr().getType() == Type.INT)
        {
            importImageOps = true;
            javaCode.append("(ImageOps.binaryImageScalarOp(ImageOps.OP.");
            if (binaryExpr.getOpKind() == Kind.PLUS) {
                javaCode.append("PLUS");
            }
            else if (binaryExpr.getOpKind() == Kind.MINUS) {
                javaCode.append("MINUS");
            }
            else if (binaryExpr.getOpKind() == Kind.TIMES) {
                javaCode.append("TIMES");
            }
            else if (binaryExpr.getOpKind() == Kind.DIV) {
                javaCode.append("DIV");
            }
            else if (binaryExpr.getOpKind() == Kind.MOD) {
                javaCode.append("MOD");
            }
            javaCode.append(", ");
            binaryExpr.getLeftExpr().visit(this, arg);
            javaCode.append(", ");
            binaryExpr.getRightExpr().visit(this, arg);
            javaCode.append("))");
        }
        else if (binaryExpr.getLeftExpr().getType() == Type.PIXEL && binaryExpr.getRightExpr().getType() == Type.PIXEL
                && binaryExpr.getOpKind() == Kind.EQ)
        {
            importImageOps = true;
            javaCode.append("(ImageOps.binaryPackedPixelBooleanOP(ImageOps.OP.");
            javaCode.append("EQUALS");
            javaCode.append(", ");
            binaryExpr.getLeftExpr().visit(this, arg);
            javaCode.append(", ");
            binaryExpr.getRightExpr().visit(this, arg);
            javaCode.append("))");
        }
        else if (binaryExpr.getLeftExpr().getType() == Type.PIXEL && binaryExpr.getRightExpr().getType() == Type.PIXEL)
        {
            importImageOps = true;
            javaCode.append("(ImageOps.binaryPackedPixelPixelOp(ImageOps.OP.");
            if (binaryExpr.getOpKind() == Kind.PLUS) {
                javaCode.append("PLUS");
            }
            else if (binaryExpr.getOpKind() == Kind.MINUS) {
                javaCode.append("MINUS");
            }
            else if (binaryExpr.getOpKind() == Kind.TIMES) {
                javaCode.append("TIMES");
            }
            else if (binaryExpr.getOpKind() == Kind.DIV) {
                javaCode.append("DIV");
            }
            else if (binaryExpr.getOpKind() == Kind.MOD) {
                javaCode.append("MOD");
            }
            javaCode.append(", ");
            binaryExpr.getLeftExpr().visit(this, arg);
            javaCode.append(", ");
            binaryExpr.getRightExpr().visit(this, arg);
            javaCode.append("))");
        }
        else if (binaryExpr.getLeftExpr().getType() == Type.PIXEL && binaryExpr.getRightExpr().getType() == Type.INT)
        {
            importImageOps = true;
            javaCode.append("(ImageOps.binaryPackedPixelIntOp(ImageOps.OP.");
            if (binaryExpr.getOpKind() == Kind.PLUS) {
                javaCode.append("PLUS");
            }
            else if (binaryExpr.getOpKind() == Kind.MINUS) {
                javaCode.append("MINUS");
            }
            else if (binaryExpr.getOpKind() == Kind.TIMES) {
                javaCode.append("TIMES");
            }
            else if (binaryExpr.getOpKind() == Kind.DIV) {
                javaCode.append("DIV");
            }
            else if (binaryExpr.getOpKind() == Kind.MOD) {
                javaCode.append("MOD");
            }
            javaCode.append(", ");
            binaryExpr.getLeftExpr().visit(this, arg);
            javaCode.append(", ");
            binaryExpr.getRightExpr().visit(this, arg);
            javaCode.append("))");
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
        if (arg instanceof PostfixExpr) {
            importPixelOps = true;
            javaCode.append("PixelOps.");
            if (channelSelector.color() == Kind.RES_red) {
                javaCode.append("red");
            }
            else if (channelSelector.color() == Kind.RES_green) {
                javaCode.append("green");
            }
            else if (channelSelector.color() == Kind.RES_blue) {
                javaCode.append("blue");
            }
        }
        else if (arg instanceof LValue)
        {
            throw new UnsupportedOperationException("visitChannelSelector called from LValue");
        }
        return javaCode.toString();
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
            javaCode.append("final ");
            declaration.getNameDef().visit(this, arg);
            javaCode.append(" = ");

            if (declaration.getInitializer() == null) {
                importImageOps = true;
                javaCode.append("ImageOps.makeImage(");
                if (declaration.getNameDef().getDimension() != null) {
                    declaration.getNameDef().getDimension().visit(this, arg);
                    javaCode.append(")");
                } else {
                    throw new CodeGenException("Missing Dimension object in image NameDef");
                }
            }
            else {
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

        List<GuardedBlock> gb = doStatement.getGuardedBlocks();
        javaCode.append("{boolean continue$0= false;\n\t\twhile(!continue$0){\n\t\tcontinue$0=true;\n");
        for (int i = 0; i < gb.size(); i++) {
            javaCode.append("if ");
            gb.get(i).visit(this, doStatement);
        }
        javaCode.append("}}");

        return javaCode.toString();
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

        if (arg instanceof IfStatement) {
            javaCode.append("(");
            guardedBlock.getGuard().visit(this, arg);
            javaCode.append(")");
            guardedBlock.getBlock().visit(this, arg);
        }
        else if (arg instanceof DoStatement) {
            javaCode.append("(");
            guardedBlock.getGuard().visit(this, arg);
            javaCode.append(") {continue$0 = false;");
            guardedBlock.getBlock().visit(this, arg);
            javaCode.append("}");
        }

        return javaCode.toString();
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
//        _IdentExpr_.getNameDef().getJavaName()

        javaCode.append(identExpr.getNameDef().getJavaName());

        return javaCode.toString();
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        List<GuardedBlock> gb = ifStatement.getGuardedBlocks();

        for (int i = 0; i < gb.size(); i++) {
            javaCode.append("if ");
            gb.get(i).visit(this, ifStatement);
            if (i < gb.size() - 1)
            {
                javaCode.append("\nelse ");
            }
        }

        //FIXME:FINISH THIS after GaurdedBlock
        return javaCode.toString();
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
            importBufferedImage = true;
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

//        If Expr.type is Pixel
//        _ChannelSelector_ ( _Expr_ )
        if (postfixExpr.primary().getType() == Type.PIXEL) {
            postfixExpr.channel().visit(this, postfixExpr);
            javaCode.append("(");
            postfixExpr.primary().visit(this, arg);
            javaCode.append(")");
        }

//        Otherwise it is an image
//        If PixelSelector != null && ChannelSelector ==null
//        Generate code to get the value of the pixel at the
//        indicated location.
//        ImageOps.getRGB( _Expr_ , _PixelSelector _ )
        else {
            importImageOps = true;
            if (postfixExpr.pixel() != null && postfixExpr.channel() == null) {
                javaCode.append("ImageOps.getRGB(");
                postfixExpr.primary().visit(this, arg);
                javaCode.append(", ");
                postfixExpr.pixel().visit(this, arg);
                javaCode.append(")");
            }

//        If PixelSelector != null && ChannelSelector != null,
//        generate code to get the value of the pixel at the
//        indicated location and to invoke PixelOps.red,
//        PixelOps.green, or PixelOps.blue. (You may want
//        to visit the ChannelSelector, passing info that this is
//        in the context of an expression as indicated here, or
//        you may want to just get the value from
//        visitPostfixExpr)
//        _ChannelSelector_ (ImageOps.getRGB( _Expr_ , _PixelSelector_ ))
            else if (postfixExpr.pixel() != null && postfixExpr.channel() != null) {
                //FIXME: double check this!
                throw new UnsupportedOperationException("postfix with pixel AND channel");
//                postfixExpr.channel().visit(this, postfixExpr);
//                javaCode.append("(ImageOps.getRGB(");
//                postfixExpr.primary().visit(this, arg);
//                javaCode.append(", ");
//                postfixExpr.pixel().visit(this, arg);
//                javaCode.append("))");
            }

//        If PixelSelector == null && ChannelSelector != null,
//        generate code to invoke the
//        ImageOPs.extractRed,extractGreen, or extractBlue
//        method to return a new image containing the
//        indicated color channel.
//        ImageOps.extractRed( _Expr_ )
//        (or extractBlue or extractGreen)
            else if (postfixExpr.pixel() == null && postfixExpr.channel() != null) {
                //FIXME: Finish this after implementing channelSelector and double check this method
                throw new UnsupportedOperationException("postfix with channel");
            }
        }
        return javaCode.toString();
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
        else if (type == Type.IMAGE) {
            importBufferedImage = true;
            javaCode.append("BufferedImage");
        }
        else if (type == Type.PIXEL) {
            javaCode.append("int");
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
        if (importFileURLIO)
        {
            javaCode.insert(0, "import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;\n");
        }
        if (importImageOps)
        {
            javaCode.insert(0, "import edu.ufl.cise.cop4020fa23.runtime.ImageOps;\n");
        }
        if (importPixelOps)
        {
            javaCode.insert(0, "import edu.ufl.cise.cop4020fa23.runtime.PixelOps;\n");
        }
        if (importBufferedImage)
        {
            javaCode.insert(0, "import java.awt.image.BufferedImage;\n");
        }
        if (arg != null && arg != "")
        {
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
        if (op == Kind.RES_height) {
            javaCode.append(".getHeight()");
        }
        else if (op == Kind.RES_width) {
            javaCode.append(".getWidth()");
        }
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
        /*
        If ConsExpr.name = Z then 255
        else get hex String literal representing the
        RGB representation of the corresponding
        java.awt.Color.

        Example:
        Let the PLC Lang constant be BLUE.
        This corresponds to the java Color constant
        java.awt.Color.BLUE.

        Get the packed pixel version of the color with
        getRGB()

        Convert to a String with Integer.toHexString

        Prepend “0x” to make it a Java hex literal.

        Putting it all together, you get
        "0x" +
        Integer.toHexString(Color.BLUE.getRGB())

        Which is
        0xff0000ff
         */

        if (constExpr.getName().equals("Z")) {
            javaCode.append("255");
        }
        else {
            javaCode.append("0x");
            if (constExpr.getName().equals("BLACK")) {
               javaCode.append(Integer.toHexString(Color.BLACK.getRGB()));
            }
            else if (constExpr.getName().equals("BLUE")) {
                javaCode.append(Integer.toHexString(Color.BLUE.getRGB()));
            }
            else if (constExpr.getName().equals("CYAN")) {
                javaCode.append(Integer.toHexString(Color.CYAN.getRGB()));
            }
            else if (constExpr.getName().equals("DARK_GRAY")) {
                javaCode.append(Integer.toHexString(Color.DARK_GRAY.getRGB()));
            }
            else if (constExpr.getName().equals("GRAY")) {
                javaCode.append(Integer.toHexString(Color.GRAY.getRGB()));
            }
            else if (constExpr.getName().equals("GREEN")) {
                javaCode.append(Integer.toHexString(Color.GREEN.getRGB()));
            }
            else if (constExpr.getName().equals("LIGHT_GRAY")) {
                javaCode.append(Integer.toHexString(Color.LIGHT_GRAY.getRGB()));
            }
            else if (constExpr.getName().equals("MAGENTA")) {
                javaCode.append(Integer.toHexString(Color.MAGENTA.getRGB()));
            }
            else if (constExpr.getName().equals("ORANGE")) {
                javaCode.append(Integer.toHexString(Color.ORANGE.getRGB()));
            }
            else if (constExpr.getName().equals("PINK")) {
                javaCode.append(Integer.toHexString(Color.PINK.getRGB()));
            }
            else if (constExpr.getName().equals("RED")) {
                javaCode.append(Integer.toHexString(Color.RED.getRGB()));
            }
            else if (constExpr.getName().equals("WHITE")) {
                javaCode.append(Integer.toHexString(Color.WHITE.getRGB()));
            }
            else if (constExpr.getName().equals("YELLOW")) {
                javaCode.append(Integer.toHexString(Color.YELLOW.getRGB()));
            }
            else {
                throw new CodeGenException("Invalid ConstExpr");
            }
        }
        return javaCode.toString();
    }
}
