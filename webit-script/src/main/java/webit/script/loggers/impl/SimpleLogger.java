// Copyright (c) 2013-2015, Webit Team. All Rights Reserved.
package webit.script.loggers.impl;

import webit.script.Init;
import webit.script.loggers.AbstractLogger;
import webit.script.util.StringUtil;

/**
 *
 * @author zqq90
 */
public final class SimpleLogger extends AbstractLogger {

    //settings
    protected String level = "info";

    private String prefix;
    private int levelNum;

    @Init
    public void init() {
        prefix = StringUtil.concat("[", name, "] ");

        String levelString = level.trim().toLowerCase();
        levelNum = "error".equals(levelString) ? LEVEL_ERROR
                : "warn".equals(levelString) ? LEVEL_WARN
                        : "info".equals(levelString) ? LEVEL_INFO
                                : "debug".equals(levelString) ? LEVEL_DEBUG
                                        : Integer.MAX_VALUE;
    }

    @Override
    public boolean isEnabled(int level) {
        return level >= this.levelNum;
    }

    @Override
    public void log(int level, String msg) {
        printLog(level, msg, null);
    }

    @Override
    public void log(int level, String msg, Throwable throwable) {
        printLog(level, msg, throwable);
    }

    protected void printLog(int level, String msg, Throwable throwable) {
        if (isEnabled(level)) {
            System.out.println(prefix == null ? msg : prefix.concat(msg != null ? msg : "null"));
            if (throwable != null) {
                throwable.printStackTrace(System.out);
            }
        }
    }
}
