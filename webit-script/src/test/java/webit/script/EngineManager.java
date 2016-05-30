// Copyright (c) 2013-2015, Webit Team. All Rights Reserved.
package webit.script;

import webit.script.exceptions.ResourceNotFoundException;

/**
 *
 * @author zqq90
 */
public class EngineManager {

    private static final Engine engine;

    static {
        engine = Engine.create("/webit-script-test.wim", null);
    }

    public static Engine getEngine() {
        return engine;
    }

    public static Template getTemplate(String name) throws ResourceNotFoundException {
        return engine.getTemplate(name);
    }
}
