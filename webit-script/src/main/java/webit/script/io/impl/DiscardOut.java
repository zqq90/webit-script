// Copyright (c) 2013-2015, Webit Team. All Rights Reserved.
package webit.script.io.impl;

import webit.script.io.Out;
import webit.script.util.InternedEncoding;

/**
 *
 * @author zqq90
 */
public class DiscardOut implements Out {

    private final InternedEncoding encoding;
    private final boolean isByteStream;

    public DiscardOut() {
        this("UTF-8", false);
    }

    public DiscardOut(String encoding, boolean isByteStream) {
        this(InternedEncoding.intern(encoding), isByteStream);
    }

    public DiscardOut(InternedEncoding encoding, boolean isByteStream) {
        this.encoding = encoding;
        this.isByteStream = isByteStream;
    }

    @Override
    public void write(byte[] bytes, int offset, int length) {
    }

    @Override
    public void write(byte[] bytes) {
    }

    @Override
    public void write(char[] chars, int offset, int length) {
    }

    @Override
    public void write(char[] chars) {
    }

    @Override
    public void write(String string, int offset, int length) {
    }

    @Override
    public void write(String string) {
    }

    @Override
    public InternedEncoding getEncoding() {
        return encoding;
    }

    @Override
    public boolean isByteStream() {
        return isByteStream;
    }

}
