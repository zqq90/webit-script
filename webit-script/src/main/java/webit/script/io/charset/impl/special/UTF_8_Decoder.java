// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.script.io.charset.impl.special;

import java.io.IOException;
import java.io.Writer;
import webit.script.io.charset.Decoder;
import webit.script.io.Buffers;
import webit.script.util.charset.UTF_8;

/**
 *
 * @author Zqq
 */
public final class UTF_8_Decoder implements Decoder {
    private final Buffers buffers;

    public UTF_8_Decoder(Buffers buffers) {
        this.buffers = buffers;
    }

    public void write(final byte[] bytes, final int off, final int len, final Writer writer) throws IOException {
        if (bytes == null || len == 0) {
            return;
        }
        final char[] chars;
        int used = UTF_8.decode(bytes, off, len, 
                chars = this.buffers.getChars(len));
        writer.write(chars, 0, used);
    }
}
