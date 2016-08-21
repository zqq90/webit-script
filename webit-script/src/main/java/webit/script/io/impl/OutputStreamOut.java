// Copyright (c) 2013-2015, Webit Team. All Rights Reserved.
package webit.script.io.impl;

import java.io.IOException;
import java.io.OutputStream;
import webit.script.Engine;
import webit.script.exceptions.ScriptRuntimeException;
import webit.script.io.Out;
import webit.script.io.charset.CoderFactory;
import webit.script.io.charset.Encoder;
import webit.script.util.InternedEncoding;

/**
 *
 * @author zqq90
 */
public final class OutputStreamOut implements Out {

    private final OutputStream outputStream;
    private final InternedEncoding encoding;
    private final Encoder encoder;

    private OutputStreamOut(OutputStream outputStream, InternedEncoding encoding, Encoder encoder) {
        this.outputStream = outputStream;
        this.encoding = encoding;
        this.encoder = encoder;
    }

    private OutputStreamOut(OutputStream outputStream, InternedEncoding encoding, CoderFactory coderFactory) {
        this(outputStream, encoding, coderFactory.newEncoder(encoding));
    }

    public OutputStreamOut(OutputStream outputStream, OutputStreamOut out) {
        this(outputStream, out.encoding, out.encoder);
    }

    public OutputStreamOut(OutputStream outputStream, Engine engine) {
        this(outputStream, engine.getEncoding(), engine.getCoderFactory());
    }

    public OutputStreamOut(OutputStream outputStream, InternedEncoding encoding, Engine engine) {
        this(outputStream, encoding != null ? encoding : engine.getEncoding(), engine.getCoderFactory());
    }

    @Override
    public void write(final byte[] bytes, final int offset, final int length) {
        try {
            this.outputStream.write(bytes, offset, length);
        } catch (IOException ex) {
            throw new ScriptRuntimeException(ex);
        }
    }

    @Override
    public void write(final byte[] bytes) {
        try {
            this.outputStream.write(bytes);
        } catch (IOException ex) {
            throw new ScriptRuntimeException(ex);
        }
    }

    @Override
    public void write(final char[] chars, final int offset, final int length) {
        try {
            this.encoder.write(chars, offset, length, this.outputStream);
        } catch (IOException ex) {
            throw new ScriptRuntimeException(ex);
        }
    }

    @Override
    public void write(final char[] chars) {
        write(chars, 0, chars.length);
    }

    @Override
    public void write(final String string, final int offset, final int length) {
        try {
            this.encoder.write(string, offset, length, this.outputStream);
        } catch (IOException ex) {
            throw new ScriptRuntimeException(ex);
        }
    }

    @Override
    public void write(final String string) {
        try {
            this.encoder.write(string, 0, string.length(), this.outputStream);
        } catch (IOException ex) {
            throw new ScriptRuntimeException(ex);
        }
    }

    @Override
    public InternedEncoding getEncoding() {
        return this.encoding;
    }

    @Override
    public boolean isByteStream() {
        return true;
    }
}
