package csi.server.common.codec;

import java.io.IOException;
import java.io.Writer;

// writer that does nothing
public class NullWriter extends Writer {

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void close() throws IOException {

    }
}