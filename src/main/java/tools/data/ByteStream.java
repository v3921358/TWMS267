package tools.data;

import java.io.IOException;

public interface ByteStream {

    long getPosition();

    void seek(final long offset) throws IOException;

    long getBytesRead();

    int readByte();

    String toString();

    String toString(final boolean b);

    long available();

}
