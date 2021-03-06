// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.wit.core.ast.operators;

import org.febit.wit.InternalContext;
import org.febit.wit.core.ast.Expression;
import org.febit.wit.core.ast.expressions.DirectValue;
import org.febit.wit.util.ExceptionUtil;
import org.febit.wit.util.StatementUtil;

import java.util.function.Function;

/**
 * @author zqq90
 */
public class ConstableOperator extends Expression {

    protected final Expression expr;
    protected final Function<Object, Object> op;

    public ConstableOperator(Expression expr, Function<Object, Object> op, int line, int column) {
        super(line, column);
        this.expr = expr;
        this.op = op;
    }

    @Override
    public Object execute(final InternalContext context) {
        try {
            return op.apply(expr.execute(context));
        } catch (Exception e) {
            throw ExceptionUtil.toScriptRuntimeException(e, this);
        }
    }

    @Override
    public Expression optimize() {
        if (StatementUtil.isImmutableDirectValue(expr)) {
            return new DirectValue(op.apply(((DirectValue) expr).value), line, column);
        }
        return this;
    }

    @Override
    public Object getConstValue() {
        return op.apply(StatementUtil.calcConst(expr));
    }
}
