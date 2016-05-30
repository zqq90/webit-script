// Copyright (c) 2013-2015, Webit Team. All Rights Reserved.
package webit.script.core.ast.operators;

import webit.script.Context;
import webit.script.core.ast.BinaryOperator;
import webit.script.core.ast.Expression;
import webit.script.core.ast.Optimizable;
import webit.script.core.ast.expressions.DirectValue;
import webit.script.util.ALU;
import webit.script.util.StatementUtil;

/**
 *
 * @author zqq90
 */
public final class Minus extends BinaryOperator implements Optimizable {

    public Minus(Expression leftExpr, Expression rightExpr, int line, int column) {
        super(leftExpr, rightExpr, line, column);
    }

    @Override
    public Object execute(final Context context) {
        try {
            return ALU.minus(leftExpr.execute(context), rightExpr.execute(context));
        } catch (Exception e) {
            throw StatementUtil.castToScriptRuntimeException(e, this);
        }
    }

    @Override
    public Expression optimize() {
        return (leftExpr instanceof DirectValue && rightExpr instanceof DirectValue)
                ? new DirectValue(ALU.minus(((DirectValue) leftExpr).value, ((DirectValue) rightExpr).value), line, column)
                : this;
    }
}
