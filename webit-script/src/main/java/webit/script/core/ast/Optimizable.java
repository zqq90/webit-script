// Copyright (c) 2013-2015, Webit Team. All Rights Reserved.
package webit.script.core.ast;

/**
 *
 * @author zqq90
 */
public interface Optimizable {

    Statement optimize() throws Exception;
}