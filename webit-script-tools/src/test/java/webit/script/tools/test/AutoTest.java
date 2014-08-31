// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.script.tools.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import webit.script.Context;
import webit.script.Template;
import webit.script.exceptions.ParseException;
import webit.script.exceptions.ResourceNotFoundException;
import webit.script.exceptions.ScriptRuntimeException;
import webit.script.io.Out;
import webit.script.io.impl.DiscardOut;
import webit.script.io.impl.OutputStreamOut;
import webit.script.tools.EngineManager;
import webit.script.util.ClassUtil;
import webit.script.util.FastByteArrayOutputStream;
import webit.script.util.FastByteBuffer;
import static org.junit.Assert.*;

/**
 *
 * @author Zqq
 */
public class AutoTest {

    private final static int BUFFER_SIZE = 1024;
    private final static String AUTO_TEST_PATH = "webit/script/tools/test/tmpls/auto/";

    private Map<String, String> collectAutoTestTemplates() {
        final Map<String, String> templates = new TreeMap<String, String>();

        ClassLoader classLoader = ClassUtil.getDefaultClassLoader();
        try {
            URL url = classLoader.getResource(AUTO_TEST_PATH);
            File file = new File(url.getFile());
            String[] files = file.list();
            for (int i = 0, len = files.length; i < len; i++) {
                String path = files[i];
                if (path.endsWith(".wit")) {
                    String outPath = path.concat(".out");
                    templates.put("/auto/".concat(path), new File(file, outPath).exists() ? outPath : null);
                }
            }
        } catch (Exception ignore) {
        }
        return templates;
    }

    @Test
    public void test() throws ResourceNotFoundException, IOException {

        Map<String, String> templates = collectAutoTestTemplates();
        ClassLoader classLoader = ClassUtil.getDefaultClassLoader();

        final FastByteBuffer bytesBuffer = new FastByteBuffer();
        final byte[] buffer = new byte[BUFFER_SIZE];

        try {

            FastByteArrayOutputStream out = new FastByteArrayOutputStream();
            for (Map.Entry<String, String> entry : templates.entrySet()) {
                String templatePath = entry.getKey();
                String outPath = entry.getValue();
                if (outPath != null) {
                    out.reset();
                    mergeTemplate(templatePath, out);

                    //read outNotNull 
                    InputStream in;
                    if ((in = classLoader.getResourceAsStream(AUTO_TEST_PATH.concat(outPath))) != null) {
                        int read;
                        bytesBuffer.clear();
                        while ((read = in.read(buffer, 0, BUFFER_SIZE)) >= 0) {
                            bytesBuffer.append(buffer, 0, read);
                        }
                        assertArrayEquals(bytesBuffer.toArray(), out.toByteArray());
                        System.out.println("\tresult match to: " + outPath);

                        bytesBuffer.clear();
                    }
                    out.reset();
                } else {
                    mergeTemplate(templatePath, new DiscardOut());
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            throw e;
        } catch (ScriptRuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void mergeTemplate(String templatePath, OutputStream out) throws ResourceNotFoundException {
        mergeTemplate(templatePath, new OutputStreamOut(out, EngineManager.getEngine()));
    }

    public void mergeTemplate(String templatePath, Out out) throws ResourceNotFoundException {
        System.out.println("AUTO RUN: " + templatePath);
        Template template = EngineManager.getEngine().getTemplate(templatePath);
        Context context = template.merge(out);
        System.out.println("\tassert count: " + context.getLocalVar(webit.script.tools.testunit.AssertGlobalRegister.ASSERT_COUNT_KEY));
    }
}
