// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.script.core.ast.statements;

import webit.script.Context;
import webit.script.core.ast.Expression;
import webit.script.core.ast.Statement;
import webit.script.core.ast.expressions.FunctionDeclare;
import webit.script.lang.Iter;
import webit.script.lang.iter.IterMethodFilter;
import webit.script.util.CollectionUtil;
import webit.script.util.StatementUtil;

/**
 *
 * @author Zqq
 */
public final class ForInNoLoops extends Statement {

    private final Expression collectionExpr;
    private final int indexer;
    private final Statement[] statements;
    private final Statement elseStatement;
    protected final FunctionDeclare functionDeclareExpr;
    protected final int iterIndex;
    protected final int itemIndex;

    public ForInNoLoops(FunctionDeclare functionDeclareExpr, Expression collectionExpr, int indexer, int iterIndex, int itemIndex, Statement[] statements, Statement elseStatement, int line, int column) {
        super(line, column);
        this.functionDeclareExpr = functionDeclareExpr;
        this.collectionExpr = collectionExpr;
        this.indexer = indexer;
        this.statements = statements;
        this.elseStatement = elseStatement;
        this.iterIndex = iterIndex;
        this.itemIndex = itemIndex;
    }

    public Object execute(final Context context) {
        Iter iter = CollectionUtil.toIter(collectionExpr.execute(context), this);
        if (iter != null && functionDeclareExpr != null) {
            iter = new IterMethodFilter(context, functionDeclareExpr.execute(context), iter);
        }
        if (iter != null
                && iter.hasNext()) {
        final int preIndex = context.indexer;
        context.indexer = indexer;
            final Statement[] statements = this.statements;
            final int itemIndex = this.itemIndex;
            final Object[] vars = context.vars;
            vars[iterIndex] = iter;
            do {
                vars[itemIndex] = iter.next();
                StatementUtil.executeInverted(statements, context);
            } while (iter.hasNext());
            context.indexer = preIndex;
            return null;
        } else if (elseStatement != null) {
            elseStatement.execute(context);
        }
        return null;
    }
}
