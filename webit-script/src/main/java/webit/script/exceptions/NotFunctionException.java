// Copyright (c) 2013-2015, Webit Team. All Rights Reserved.
package webit.script.exceptions;

import webit.script.util.StringUtil;

/**
 *
 * @author zqq90
 */
public class NotFunctionException extends RuntimeException {

    public NotFunctionException(Object real) {
        super(StringUtil.format("Not a function but a [{}].", real == null ? "null" : real.getClass()));
    }
}
