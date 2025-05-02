package tools.data;

import Config.constants.ServerConstants;
import tools.BitTools;

import java.nio.charset.Charset;

public class StringObfuscation {
    private static final Charset CHARSET = ServerConstants.MapleType.getByType(ServerConstants.MapleRegion).getCharset();

    public static String DeobfuscateRor(byte[] buf, int ror) {
        for (int i = 0; i < buf.length; i++) {
            buf[i] = BitTools.rollRight(buf[i], ror);
        }
        return new String(buf, CHARSET);
    }

}
