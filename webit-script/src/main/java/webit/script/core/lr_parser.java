// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.script.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import webit.script.Engine;
import webit.script.Template;
import webit.script.asm.AsmMethodCaller;
import webit.script.asm.AsmMethodCallerManager;
import webit.script.core.ast.BinaryOperator;
import webit.script.core.ast.ClassNameList;
import webit.script.core.ast.Expression;
import webit.script.core.ast.ResetableValueExpression;
import webit.script.core.ast.SelfOperator;
import webit.script.core.ast.TemplateAST;
import webit.script.core.ast.expressions.AndOperator;
import webit.script.core.ast.expressions.BitAndOperator;
import webit.script.core.ast.expressions.BitOrOperator;
import webit.script.core.ast.expressions.BitXorOperator;
import webit.script.core.ast.expressions.CommonMethodDeclareExpression;
import webit.script.core.ast.expressions.ContextValue;
import webit.script.core.ast.expressions.CurrentContextValue;
import webit.script.core.ast.expressions.DivOperator;
import webit.script.core.ast.expressions.EqualsOperator;
import webit.script.core.ast.expressions.GreaterEqualsOperator;
import webit.script.core.ast.expressions.GreaterOperator;
import webit.script.core.ast.expressions.IfOrOperator;
import webit.script.core.ast.expressions.IntStepOperator;
import webit.script.core.ast.expressions.LShiftOperator;
import webit.script.core.ast.expressions.LessEqualsOperator;
import webit.script.core.ast.expressions.LessOperator;
import webit.script.core.ast.expressions.MinusOperator;
import webit.script.core.ast.expressions.ModOperator;
import webit.script.core.ast.expressions.MultOperator;
import webit.script.core.ast.expressions.NotEqualsOperator;
import webit.script.core.ast.expressions.OrOperator;
import webit.script.core.ast.expressions.PlusOperator;
import webit.script.core.ast.expressions.RShiftOperator;
import webit.script.core.ast.expressions.SelfBitAndOperator;
import webit.script.core.ast.expressions.SelfBitOrOperator;
import webit.script.core.ast.expressions.SelfBitXorOperator;
import webit.script.core.ast.expressions.SelfDivOperator;
import webit.script.core.ast.expressions.SelfLShiftOperator;
import webit.script.core.ast.expressions.SelfMinusOperator;
import webit.script.core.ast.expressions.SelfModOperator;
import webit.script.core.ast.expressions.SelfMultOperator;
import webit.script.core.ast.expressions.SelfPlusOperator;
import webit.script.core.ast.expressions.SelfRShiftOperator;
import webit.script.core.ast.expressions.SelfURShiftOperator;
import webit.script.core.ast.expressions.URShiftOperator;
import webit.script.core.ast.method.AsmNativeMethodDeclare;
import webit.script.core.ast.method.NativeConstructorDeclare;
import webit.script.core.ast.method.NativeMethodDeclare;
import webit.script.core.ast.method.NativeNewArrayDeclare;
import webit.script.core.ast.statments.ForInStatmentPart;
import webit.script.core.ast.statments.ForMapStatmentPart;
import webit.script.core.ast.statments.PlaceHolderStatmentFactory;
import webit.script.core.text.TextStatmentFactory;
import webit.script.exceptions.ParseException;
import webit.script.loggers.Logger;
import webit.script.util.ClassLoaderUtil;
import webit.script.util.ClassUtil;
import webit.script.util.ExceptionUtil;
import webit.script.util.StatmentUtil;
import webit.script.util.StringUtil;
import webit.script.util.collection.ArrayStack;
import webit.script.util.collection.Stack;

/**
 * This class implements a skeleton table driven LR parser. In general, LR
 * parsers are a form of bottom up shift-reduce parsers. Shift-reduce parsers
 * act by shifting input onto a parse _stack until the Symbols matching the
 * column hand side of a production appear on the top of the _stack. Once this
 * occurs, a reduce is performed. This involves removing the Symbols
 * corresponding to the column hand side of the production (the so called
 * "handle") and replacing them with the non-terminal from the line hand side of
 * the production.
 * <p>
 *
 * To control the decision of whether to shift or reduce at any given point, the
 * parser uses a state machine (the "viable prefix recognition machine" built by
 * the parser generator). The current state of the machine is placed on top of
 * the parse _stack (stored as part of a Symbol object representing a terminal
 * or non terminal). The parse action table is consulted (using the current
 * state and the current lookahead Symbol as indexes) to determine whether to
 * shift or to reduce. When the parser shifts, it changes to a new state by
 * pushing a new Symbol (containing a new state) onto the _stack. When the
 * parser reduces, it pops the handle (column hand side of a production) off the
 * _stack. This leaves the parser in the state it was in before any of those
 * Symbols were matched. Next the reduce-goto table is consulted (using the new
 * state and current lookahead Symbol as indexes) to determine a new state to go
 * to. The parser then shifts to this goto state by pushing the line hand side
 * Symbol of the production (also containing the new state) onto the _stack.<p>
 *
 * This class actually provides four LR parsers. The methods parse() and
 * debug_parse() provide two versions of the main parser (the only difference
 * being that debug_parse() emits debugging trace messages as it parses). In
 * addition to these main parsers, the error recovery mechanism uses two more.
 * One of these is used to simulate "parsing ahead" in the input without
 * carrying out actions (to verify that a potential error recovery has worked),
 * and the other is used to parse through buffered "parse ahead" input in order
 * to execute all actions and re-synchronize the actual parser configuration.<p>
 *
 * This is an abstract class which is normally filled out by a subclass
 * generated by the JavaCup parser generator. In addition to supplying the
 * actual parse tables, generated code also supplies methods which invoke
 * various pieces of user supplied code, provide access to certain special
 * Symbols (e.g., EOF and error), etc. Specifically, the following abstract
 * methods are normally supplied by generated code:
 * <dl compact>
 * <dt> Symbol do_action()
 * <dd> Executes a piece of user supplied action code. This always comes at the
 * point of a reduce in the parse, so this code also allocates and fills in the
 * line hand side non terminal Symbol object that is to be pushed onto the
 * _stack for the reduce.
 * <dt> void init_actions()
 * <dd> Code to initialize a special object that encapsulates user supplied
 * actions (this object is used by do_action() to actually carry out the
 * actions).
 * </dl>
 *
 * In addition to these routines that <i>must</i> be supplied by the generated
 * subclass there are also a series of routines that <i>may</i>
 * be supplied. These include:
 * <dl>
 * <dt> Symbol scan()
 * <dd> Used to get the next input Symbol from the scanner.
 * <dt> int error_sync_size()
 * <dd> This determines how many Symbols past the point of an error must be
 * parsed without error in order to consider a recovery to be valid. This
 * defaults to 3. Values less than 2 are not recommended.
 * <dt> void report_error(String message, Object info)
 * <dd> This method is called to report an error. The default implementation
 * simply prints a message to System.err and where the error occurred. This
 * method is often replaced in order to provide a more sophisticated error
 * reporting mechanism.
 * <dt> void report_fatal_error(String message, Object info)
 * <dd> This method is called when a fatal error that cannot be recovered from
 * is encountered. In the default implementation, it calls report_error() to
 * emit a message, then throws an exception.
 * <dt> void syntax_error(Symbol cur_token)
 * <dd> This method is called as soon as syntax error is detected (but before
 * recovery is attempted). In the default implementation it invokes:
 * report_error("Syntax error", null);
 * <dt> void unrecovered_syntax_error(Symbol cur_token)
 * <dd> This method is called if syntax error recovery fails. In the default
 * implementation it invokes:<br>
 * report_fatal_error("Couldn't repair and continue parse", null);
 * </dl>
 *
 * @version last updated: 7/3/96
 * @author Frank Flannery
 */
abstract class lr_parser {

    private final static int stackInitialCapacity = 24;

    lr_parser() {
        this._stack = new ArrayStack<Symbol>(stackInitialCapacity);
    }
    /**
     * The parse _stack itself.
     */
    final Stack<Symbol> _stack;
    /**
     * Internal flag to indicate when parser should quit.
     */
    boolean goonParse = false;

    //
    Engine engine;
    Template template;
    TextStatmentFactory textStatmentFactory;
    PlaceHolderStatmentFactory placeHolderStatmentFactory;
    Logger logger;
    boolean locateVarForce;
    NativeImportManager nativeImportMgr;
    VariantManager varmgr;
    Map<String, Integer> labelsIndexMap;
    int currentLabelIndex;

    /**
     *
     * @param in java.io.Reader
     * @param template Template
     * @return TemplateAST
     * @throws ParseException
     */
    public TemplateAST parseTemplate(java.io.Reader in, Template template) throws ParseException {
        Lexer lexer = null;
        try {
            lexer = new Lexer(in);
            this.template = template;
            final Engine _engine;
            this.engine = _engine = template.engine;
            lexer.setTrimCodeBlockBlankLine(_engine.isTrimCodeBlockBlankLine());
            this.logger = _engine.getLogger();
            TextStatmentFactory _textStatmentFactory;
            this.textStatmentFactory = _textStatmentFactory = _engine.getTextStatmentFactory();
            this.locateVarForce = !_engine.isLooseVar();
            this.placeHolderStatmentFactory = new PlaceHolderStatmentFactory(_engine.getFilter());
            //
            this.nativeImportMgr = new NativeImportManager();
            this.varmgr = new VariantManager();
            this.labelsIndexMap = new HashMap<String, Integer>();
            this.labelsIndexMap.put(null, 0);
            this.currentLabelIndex = 0;
            //
            _textStatmentFactory.startTemplateParser(template);
            Symbol sym = this.parse(lexer);
            _textStatmentFactory.finishTemplateParser(template);
            return (TemplateAST) sym.value;
        } catch (Exception e) {
            throw ExceptionUtil.castToParseException(e);
        } finally {
            try {
                if (lexer != null) {
                    lexer.yyclose();
                } else {
                    in.close();
                }
            } catch (IOException e) {
                //ignore
            }
        }
    }
    
    /**
     * Perform a bit of user supplied action code (supplied by generated
     * subclass). Actions are indexed by an internal action number assigned at
     * parser generation time.
     *
     * @param act_num the internal index of the action to be performed.
     * @return Object
     * @throws java.lang.Exception
     */
    abstract Object do_action(int act_num) throws ParseException;

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/
    /**
     * Fetch an action from the action table. The table is broken up into rows,
     * one per state (rows are indexed directly by state number). Within each
     * row, a list of index, value pairs are given (as sequential entries in the
     * table), and the list is terminated by a default entry (denoted with a
     * Symbol index of -1). To find the proper entry in a row we do a linear or
     * binary search (depending on the size of the row).
     *
     * @param row actionTable[state]
     * @param id the Symbol index of the action being accessed.
     */
    private short getAction(final short[] row, int sym) {
        short tag;
        int first, last, probe, row_len;
        //final short[] row = actionTable[state];

        /* linear search if we are < 10 entries */
        if ((row_len = row.length) < 20) {
            for (probe = 0; probe < row_len; probe++) {
                /* is this entry labeled with our Symbol or the default? */
                tag = row[probe++];
                if (tag == sym || tag == -1) {
                    /* return the next entry */
                    return row[probe];
                }
            }
        } else {
            /* otherwise binary search */
            first = 0;
            last = ((row_len - 1) >> 1) - 1;  /* leave out trailing default entry */

            int probe_2;
            while (first <= last) {
                probe = (first + last) >> 1;
                probe_2 = probe << 1;
                if (sym == row[probe_2]) {
                    return row[probe_2 + 1];
                } else if (sym > row[probe_2]) {
                    first = probe + 1;
                } else {
                    last = probe - 1;
                }
            }

            /* not found, use the default at the end */
            return row[row_len - 1];
        }

        /* shouldn't happened, but if we run off the end we return the 
         default (error == 0) */
        return 0;
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/
    /**
     * Fetch a state from the reduce-goto table. The table is broken up into
     * rows, one per state (rows are indexed directly by state number). Within
     * each row, a list of index, value pairs are given (as sequential entries
     * in the table), and the list is terminated by a default entry (denoted
     * with a Symbol index of -1). To find the proper entry in a row we do a
     * linear search.
     *
     * @param row reduceTable[state]
     * @param id the Symbol index of the entry being accessed.
     */
    private short getReduce(final short[] row, int sym) {
        int probe, len;
        short tag;
        for (probe = 0, len = row.length; probe < len; probe++) {
            /* is this entry labeled with our Symbol or the default? */
            if ((tag = row[probe++]) == sym || tag == -1) {
                /* return the next entry */
                return row[probe];
            }
        }
        /* if we run off the end we return the default (error == -1) */
        return -1;
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/
    /**
     * This method provides the main parsing routine. It returns only when
     * finishParsing() has been called (typically because the parser has
     * accepted, or a fatal error has been reported). See the header
     * documentation for the class regarding how shift/reduce parsers operate
     * and how the various tables are used.
     */
    private Symbol parse(final Lexer myLexer) throws Exception {
        /* the current action code */
        int act;
        Symbol cur_token;
        Symbol currentSymbol;
        final Stack<Symbol> stack;
        (stack = this._stack).clear();
        //stack.push(newSymbol("START", 0, start_state()));
        {
            Symbol START;
            (START = new Symbol(0, null)).state = Parser.START_STATE;
            stack.push(currentSymbol = START);
        }

        final short[][] actionTable = Parser.ACTION_TABLE;
        final short[][] reduceTable = Parser.REDUCE_TABLE;
        final short[][] productionTable = Parser.PRODUCTION_TABLE;
        //final Lexer myLexer = lexer;
        /* get the first token */
        cur_token = myLexer.nextToken();

        /* continue until we are told to stop */
        goonParse = true;
        do {

            /* look up action out of the current state with the current input */
            act = getAction(actionTable[currentSymbol.state], cur_token.id);

            /* decode the action -- > 0 encodes shift */
            if (act > 0) {
                /* shift to the encoded state by pushing it on the _stack */
                cur_token.state = act - 1;
                stack.push(currentSymbol = cur_token);

                /* advance to the next Symbol */
                cur_token = myLexer.nextToken();
            } else if (act < 0) {
                /* if its less than zero, then it encodes a reduce action */
                //reduceAction()
                act = (-act) - 1;
                final int symId, handleSize;
                final Object result = do_action(act);
                final short[] row;
                symId = (row = productionTable[act])[0];
                handleSize = row[1];
                if (handleSize == 0) {
                    currentSymbol = new Symbol(symId, result);
                } else {
                    currentSymbol = new Symbol(symId, result, stack.peek(handleSize - 1)); //position based on left
                        /* pops the handle off the _stack */
                    stack.pops(handleSize);
                }

                /* look up the state to go to from the one popped back to */
                /* shift to that state */
                currentSymbol.state = getReduce(reduceTable[stack.peek().state], symId);
                stack.push(currentSymbol);

            } else {//act == 0
                throw new ParseException(StringUtil.concat("Parser stop at here, ", Integer.toString(myLexer.getLine()), "(", Integer.toString(myLexer.getColumn()), ")"), myLexer.getLine(), myLexer.getColumn());
            }
        } while (goonParse);

        return stack.peek();//lhs_sym;
    }

    static short[][] loadFromDataFile(String name) {
        ObjectInputStream in = null;
        try {
            return (short[][]) (in = new ObjectInputStream(ClassLoaderUtil
                    .getDefaultClassLoader()
                    .getResourceAsStream(StringUtil.concat("webit/script/core/Parser$", name, ".data"))))
                    .readObject();
        } catch (IOException e) {
            throw new Error(e);
        } catch (ClassNotFoundException e) {
            throw new Error(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
