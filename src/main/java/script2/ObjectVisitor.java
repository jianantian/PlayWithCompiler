package script2;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;

import script2.PlayScriptParser.ArgumentsContext;
import script2.PlayScriptParser.ArrayInitializerContext;
import script2.PlayScriptParser.BlockContext;
import script2.PlayScriptParser.BlockStatementContext;
import script2.PlayScriptParser.BlockStatementsContext;
import script2.PlayScriptParser.ClassBodyContext;
import script2.PlayScriptParser.ClassBodyDeclarationContext;
import script2.PlayScriptParser.ClassDeclarationContext;
import script2.PlayScriptParser.ConstructorDeclarationContext;
import script2.PlayScriptParser.CreatorContext;
import script2.PlayScriptParser.EnhancedForControlContext;
import script2.PlayScriptParser.ExpressionContext;
import script2.PlayScriptParser.ExpressionListContext;
import script2.PlayScriptParser.FieldDeclarationContext;
import script2.PlayScriptParser.FloatLiteralContext;
import script2.PlayScriptParser.ForControlContext;
import script2.PlayScriptParser.ForInitContext;
import script2.PlayScriptParser.FormalParameterContext;
import script2.PlayScriptParser.FormalParameterListContext;
import script2.PlayScriptParser.FormalParametersContext;
import script2.PlayScriptParser.IntegerLiteralContext;
import script2.PlayScriptParser.LiteralContext;
import script2.PlayScriptParser.MemberDeclarationContext;
import script2.PlayScriptParser.MethodBodyContext;
import script2.PlayScriptParser.MethodCallContext;
import script2.PlayScriptParser.MethodDeclarationContext;
import script2.PlayScriptParser.ParExpressionContext;
import script2.PlayScriptParser.PrimaryContext;
import script2.PlayScriptParser.PrimitiveTypeContext;
import script2.PlayScriptParser.ProgContext;
import script2.PlayScriptParser.QualifiedNameContext;
import script2.PlayScriptParser.QualifiedNameListContext;
import script2.PlayScriptParser.StatementContext;
import script2.PlayScriptParser.SuperSuffixContext;
import script2.PlayScriptParser.SwitchBlockStatementGroupContext;
import script2.PlayScriptParser.SwitchLabelContext;
import script2.PlayScriptParser.TypeArgumentContext;
import script2.PlayScriptParser.TypeListContext;
import script2.PlayScriptParser.TypeTypeContext;
import script2.PlayScriptParser.TypeTypeOrVoidContext;
import script2.PlayScriptParser.VariableDeclaratorContext;
import script2.PlayScriptParser.VariableDeclaratorIdContext;
import script2.PlayScriptParser.VariableDeclaratorsContext;
import script2.PlayScriptParser.VariableInitializerContext;
import script2.PlayScriptParser.VariableModifierContext;

public class ObjectVisitor extends PlayScriptBaseVisitor<Object> {
    private Map<ParserRuleContext, Symbol> id2Symbol = null;

    //AST节点对应的Scope，如for、函数调用会启动新的Scope
    protected Map<ParserRuleContext, Scope> node2Scope = null;

    private Scope scopeTree = null;

    private VMStack stack = new VMStack();
    // private Stack<ActivationRecord> activationRecordStack = new
    // Stack<ActivationRecord>();

    // private void setIdValue(String idName, ParserRuleContext ctx, Object value) {
    // Symbol symbol = id2Symbol.get(ctx);

    // //首先在当前的活动记录中查找

    // //之后，顺着scope链逐级网上查找
    // //todo 需要修改
    // boolean found = false;
    // for (ActivationRecord record : activationRecordStack) {
    // if (record.scope == symbol.scope) {
    // found = true;
    // record.variables.put(idName, value);
    // break;
    // }
    // }

    // if (!found) {
    // System.out.println("Unable to set variable value: " + idName);
    // }
    // }

    // private Object getIdValue(String idName, ParserRuleContext ctx) {
    // Object rtn = null;
    // Symbol symbol = id2Symbol.get(ctx);

    // //首先在当前的活动记录中查找

    // //之后，顺着scope链逐级网上查找
    // //todo 需要修改

    // boolean found = false;
    // for (ActivationRecord record : activationRecordStack) {
    // if (record.scope == symbol.scope) {
    // found = true;
    // rtn = record.variables.get(idName);
    // break;
    // }
    // }

    // if (!found) {
    // System.out.println("Unable to get variable value: " + idName);
    // }
    // return rtn;
    // }

    public ObjectVisitor(Map<ParserRuleContext, Symbol> id2Symbol, Map<ParserRuleContext, Scope> node2Scope, Scope scopeTree) {
        this.id2Symbol = id2Symbol;
        this.node2Scope = node2Scope;
        this.scopeTree = scopeTree;
    }

    @Override
    public Object visitArguments(ArgumentsContext ctx) {
        return super.visitArguments(ctx);
    }

    @Override
    public Object visitArrayInitializer(ArrayInitializerContext ctx) {
        return super.visitArrayInitializer(ctx);
    }

    @Override
    public Object visitBlock(BlockContext ctx) {
        // // 添加ActivationRecord
        // Scope scope = scopeTree.findDescendantByContext(ctx);
        // if (scope != null) {
        // ActivationRecord record = new ActivationRecord(scope);
        // activationRecordStack.push(record);
        // }

        Object rtn = visitBlockStatements(ctx.blockStatements());

        // // 去掉ActivationRecord
        // if (scope != null){
        // activationRecordStack.pop();
        // }
        return rtn;
    }

    @Override
    public Object visitBlockStatement(BlockStatementContext ctx) {
        Object rtn = null;
        if (ctx.variableDeclarators() != null) {
            rtn = visitVariableDeclarators(ctx.variableDeclarators());
        } else if (ctx.statement() != null) {
            rtn = visitStatement(ctx.statement());
        }
        return rtn;
    }

    @Override
    public Object visitEnhancedForControl(EnhancedForControlContext ctx) {
        return super.visitEnhancedForControl(ctx);
    }

    @Override
    public Object visitExpression(ExpressionContext ctx) {
        Object rtn = null;
        if (ctx.bop != null) {
            Object leftObject = visitExpression(ctx.expression(0));
            Object rightObject = null;
            Integer left = 0;
            Integer right = 0;
            if (ctx.expression().size() >= 2) {
                rightObject = visitExpression(ctx.expression(1));
            }

            if (leftObject instanceof LValue) {
                left = (Integer) ((LValue) leftObject).getValue();
            } else if (leftObject instanceof Integer){
                left = (Integer) leftObject;
            }
            
            if (rightObject instanceof LValue) {
                right = (Integer) ((LValue) rightObject).getValue();
            }else if (rightObject instanceof Integer){
                right = (Integer) rightObject;
            }

            switch (ctx.bop.getType()) {
            case PlayScriptParser.ADD:
                rtn = left + right;
                break;
            case PlayScriptParser.SUB:
                rtn = left - right;
                break;
            case PlayScriptParser.MUL:
                rtn = left * right;
                break;
            case PlayScriptParser.DIV:
                rtn = left / right;
                break;
            case PlayScriptParser.EQUAL:
                rtn = left == right;
                break;
            case PlayScriptParser.LE:
                rtn = left <= right;
                break;
            case PlayScriptParser.LT:
                rtn = left < right;
                break;
            case PlayScriptParser.GE:
                rtn = left >= right;
                break;
            case PlayScriptParser.GT:
                rtn = left > right;
                break;
            case PlayScriptParser.ASSIGN:
                if (leftObject instanceof LValue) {
                    ((LValue) leftObject).setValue(right);
                    rtn = right;
                } else {
                    System.out.println("Unsupported feature during assignment");
                }
                break;
            case PlayScriptParser.DOT:
                if (leftObject instanceof LValue) {
                    Symbol symbol = id2Symbol.get(ctx);
                    if (symbol.type.typeType == TypeType.Class){
                        //找到类的成员
                    }
                } else {
                    System.out.println("Expecting an Object Reference");
                }
                break;
            default:
                break;
            }
        } else if (ctx.primary() != null) {
            rtn = visitPrimary(ctx.primary());
        } else if (ctx.postfix != null) { // i++ 或 i-- 类型的语法
            LValue lValue = (LValue)visitExpression(ctx.expression(0));
            Integer value = (Integer) lValue.getValue();
            switch (ctx.postfix.getType()) {
            case PlayScriptParser.INC:
                lValue.setValue(value + 1);
                rtn = value; // 返回值还是原值。如果++放在前面，返回值要加1。
                break;
            case PlayScriptParser.DEC:
                lValue.setValue(value - 1);
                rtn = value;
                break;
            default:
                break;
            }
        } else if (ctx.methodCall() != null) {// methodCall
            rtn = visitMethodCall(ctx.methodCall());
        }
        return rtn;
    }

    @Override
    public Object visitExpressionList(ExpressionListContext ctx) {
        Object rtn = null;
        for (ExpressionContext child : ctx.expression()) {
            rtn = visitExpression(child);
        }
        return rtn;
    }

    @Override
    public Object visitFloatLiteral(FloatLiteralContext ctx) {
        return super.visitFloatLiteral(ctx);
    }

    @Override
    public Object visitForInit(ForInitContext ctx) {
        Object rtn = null;
        if (ctx.variableDeclarators() != null) {
            rtn = visitVariableDeclarators(ctx.variableDeclarators());
        } else if (ctx.expressionList() != null) {
            rtn = visitExpressionList(ctx.expressionList());
        }
        return rtn;
    }

    @Override
    public Object visitIntegerLiteral(IntegerLiteralContext ctx) {
        Object rtn = null;
        if (ctx.DECIMAL_LITERAL() != null) {
            rtn = Integer.valueOf(ctx.DECIMAL_LITERAL().getText());
        }
        return rtn;
    }

    @Override
    public Object visitLiteral(LiteralContext ctx) {
        Object rtn = null;
        if (ctx.integerLiteral() != null) {
            rtn = visitIntegerLiteral(ctx.integerLiteral());
        }
        return rtn;
    }

    @Override
    public Object visitParExpression(ParExpressionContext ctx) {
        return visitExpression(ctx.expression());
    }

    @Override
    public Object visitPrimary(PrimaryContext ctx) {
        Object rtn = null;
        if (ctx.literal() != null) {
            rtn = visitLiteral(ctx.literal());
        } else if (ctx.IDENTIFIER() != null) {
            Symbol symbol = id2Symbol.get(ctx);
            rtn = stack.getLValue(symbol);
        }
        return rtn;
    }

    @Override
    public Object visitPrimitiveType(PrimitiveTypeContext ctx) {
        Object rtn = null;
        if (ctx.INT() != null) {
            rtn = PlayScriptParser.INT;
        } else if (ctx.LONG() != null) {
            rtn = PlayScriptParser.LONG;
        } else if (ctx.FLOAT() != null) {
            rtn = PlayScriptParser.FLOAT;
        } else if (ctx.DOUBLE() != null) {
            rtn = PlayScriptParser.DOUBLE;
        } else if (ctx.BOOLEAN() != null) {
            rtn = PlayScriptParser.BOOLEAN;
        } else if (ctx.CHAR() != null) {
            rtn = PlayScriptParser.CHAR;
        } else if (ctx.SHORT() != null) {
            rtn = PlayScriptParser.SHORT;
        } else if (ctx.BYTE() != null) {
            rtn = PlayScriptParser.BYTE;
        }
        return rtn;
    }

    @Override
    public Object visitStatement(StatementContext ctx) {
        Object rtn = null;
        if (ctx.statementExpression != null) {
            rtn = visitExpression(ctx.statementExpression);
        } else if (ctx.IF() != null) {
            Boolean condition = (Boolean) visitParExpression(ctx.parExpression());
            if (Boolean.TRUE == condition) {
                rtn = visitStatement(ctx.statement(0));
            } else if (ctx.ELSE() != null) {
                rtn = visitStatement(ctx.statement(1));
            }
        } else if (ctx.FOR() != null) {
            // 添加StackFrame
            Scope scope = node2Scope.get(ctx);
            StackFrame frame = new StackFrame(scope);
            frame.parentFrame = stack.peek();
            stack.push(frame);

            ForControlContext forControl = ctx.forControl();
            if (forControl.enhancedForControl() != null) {

            } else {
                // 初始化部分执行一次
                if (forControl.forInit() != null) {
                    rtn = visitForInit(forControl.forInit());
                }

                while (true) {
                    Boolean condition = true; // 如果没有条件判断部分，意味着一直循环
                    if (forControl.expression() != null) {
                        condition = (Boolean) visitExpression(forControl.expression());
                    }

                    if (condition) {
                        // 执行for的语句体
                        rtn = visitStatement(ctx.statement(0));

                        // 执行forUpdate，通常是“i++”这样的语句。这个执行顺序不能出错。
                        if (forControl.forUpdate != null) {
                            visitExpressionList(forControl.forUpdate);
                        }
                    } else {
                        break;
                    }
                }
            }

            // 去掉ActivationRecord
            stack.pop();
        } else if (ctx.blockLabel != null) {
            rtn = visitBlock(ctx.blockLabel);

        } else if (ctx.RETURN() != null) {
            if (ctx.expression() != null) {
                rtn = visitExpression(ctx.expression());
            }
        }
        return rtn;
    }

    @Override
    public Object visitSuperSuffix(SuperSuffixContext ctx) {
        return super.visitSuperSuffix(ctx);
    }

    @Override
    public Object visitSwitchBlockStatementGroup(SwitchBlockStatementGroupContext ctx) {
        return super.visitSwitchBlockStatementGroup(ctx);
    }

    @Override
    public Object visitSwitchLabel(SwitchLabelContext ctx) {
        return super.visitSwitchLabel(ctx);
    }

    @Override
    public Object visitTypeType(TypeTypeContext ctx) {
        return visitPrimitiveType(ctx.primitiveType());
    }

    @Override
    public Object visitVariableDeclarator(VariableDeclaratorContext ctx) {
        Object rtn = null;
        LValue lValue = (LValue)visitVariableDeclaratorId(ctx.variableDeclaratorId());
        if (ctx.variableInitializer() != null) {
            rtn = visitVariableInitializer(ctx.variableInitializer());
            lValue.setValue(rtn);
        }
        return rtn;
    }

    @Override
    public Object visitVariableDeclaratorId(VariableDeclaratorIdContext ctx) {
        Object rtn = null;
        Symbol symbol = id2Symbol.get(ctx);
        rtn = stack.getLValue(symbol);
        return rtn;
    }

    @Override
    public Object visitVariableDeclarators(VariableDeclaratorsContext ctx) {
        Object rtn = null;
        // Integer typeType = (Integer)visitTypeType(ctx.typeType()); //后面要利用这个类型信息
        for (VariableDeclaratorContext child : ctx.variableDeclarator()) {
            rtn = visitVariableDeclarator(child);
        }
        return rtn;
    }

    @Override
    public Object visitVariableInitializer(VariableInitializerContext ctx) {
        Object rtn = null;
        if (ctx.expression() != null) {
            rtn = visitExpression(ctx.expression());
        }
        return rtn;
    }

    @Override
    public Object visitBlockStatements(BlockStatementsContext ctx) {
        Object rtn = null;
        for (BlockStatementContext child : ctx.blockStatement()) {
            rtn = visitBlockStatement(child);
        }
        return rtn;
    }

    @Override
    public Object visitProg(ProgContext ctx) {
        Object rtn = null;
        stack.push(new StackFrame(scopeTree));

        rtn = visitBlockStatements(ctx.blockStatements());

        stack.pop();

        return rtn;
    }

    @Override
    public Object visitFormalParameter(FormalParameterContext ctx) {
        return super.visitFormalParameter(ctx);
    }

    @Override
    public Object visitFormalParameterList(FormalParameterListContext ctx) {
        return super.visitFormalParameterList(ctx);
    }

    @Override
    public Object visitFormalParameters(FormalParametersContext ctx) {
        return super.visitFormalParameters(ctx);
    }

    @Override
    public Object visitMethodCall(MethodCallContext ctx) {
        Object rtn = null;
        Symbol symbol = id2Symbol.get(ctx);
        MethodDeclarationContext method = (MethodDeclarationContext) symbol.type.code;

        // 添加StackFrame
        Scope scope = node2Scope.get(method);
        StackFrame frame = new StackFrame(scope);

        //TODO 假设函数调用者跟函数处于同一层级，高于或低于都要调整
        frame.parentFrame = stack.peek();   

        stack.push(frame);

        // 往活动记录绑定形参和实参，它们要能够一一对应
        List<Object> realParams = new LinkedList<Object>();
        if (ctx.expressionList() != null) {
            for (ExpressionContext exp : ctx.expressionList().expression()) {
                Object value = visitExpression(exp);
                if (value instanceof LValue){
                    value = ((LValue)value).getValue();
                }
                realParams.add(value);
            }
        }

        if (method.formalParameters().formalParameterList() != null) {
            for (int i = 0; i < method.formalParameters().formalParameterList().formalParameter().size(); i++) {
                FormalParameterContext param = method.formalParameters().formalParameterList().formalParameter(i);
                LValue lValue= (LValue)visitVariableDeclaratorId(param.variableDeclaratorId());
                lValue.setValue(realParams.get(i));
            }
        }

        // 调用方法体
        rtn = visitMethodDeclaration(method);

        // 弹出StackFrame
        stack.pop();
        return rtn;
    }

    @Override
    public Object visitMethodDeclaration(MethodDeclarationContext ctx) {
        Object rtn = null;
        rtn = visitMethodBody(ctx.methodBody());
        return rtn;
    }

    @Override
    public Object visitMethodBody(MethodBodyContext ctx) {
        Object rtn = null;
        if (ctx.block() != null) {
            rtn = visitBlock(ctx.block());
        }
        return rtn;
    }

    @Override
    public Object visitQualifiedName(QualifiedNameContext ctx) {
        return super.visitQualifiedName(ctx);
    }

    @Override
    public Object visitQualifiedNameList(QualifiedNameListContext ctx) {
        return super.visitQualifiedNameList(ctx);
    }

    @Override
    public Object visitTypeTypeOrVoid(TypeTypeOrVoidContext ctx) {
        return super.visitTypeTypeOrVoid(ctx);
    }

    @Override
    public Object visitClassBody(ClassBodyContext ctx) {
        return super.visitClassBody(ctx);
    }

    @Override
    public Object visitClassBodyDeclaration(ClassBodyDeclarationContext ctx) {
        return super.visitClassBodyDeclaration(ctx);
    }

    @Override
    public Object visitClassDeclaration(ClassDeclarationContext ctx) {
        return super.visitClassDeclaration(ctx);
    }

    @Override
    public Object visitConstructorDeclaration(ConstructorDeclarationContext ctx) {
        return super.visitConstructorDeclaration(ctx);
    }

    @Override
    public Object visitCreator(CreatorContext ctx) {
        return super.visitCreator(ctx);
    }

    @Override
    public Object visitFieldDeclaration(FieldDeclarationContext ctx) {
        return super.visitFieldDeclaration(ctx);
    }

    @Override
    public Object visitMemberDeclaration(MemberDeclarationContext ctx) {
        return super.visitMemberDeclaration(ctx);
    }

    @Override
    public Object visitTypeArgument(TypeArgumentContext ctx) {
        return super.visitTypeArgument(ctx);
    }

    @Override
    public Object visitTypeList(TypeListContext ctx) {
        return super.visitTypeList(ctx);
    }

    @Override
    public Object visitVariableModifier(VariableModifierContext ctx) {
        return super.visitVariableModifier(ctx);
    }

}