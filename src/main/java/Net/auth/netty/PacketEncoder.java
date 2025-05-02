package Net.auth.netty;

import Config.$Crypto.MapleAESOFB;
import Net.auth.client.AuthServer;
import Net.auth.packet.ClientOpcode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.data.ByteArrayByteStream;
import tools.data.MaplePacketReader;

public class PacketEncoder extends MessageToByteEncoder<Object> {

    private static final Logger log = LoggerFactory.getLogger(PacketEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Object message, ByteBuf buffer) throws Exception {
        AuthServer con = ctx.channel().attr(AuthServer.ATTRIBUTE_KEY).get();
        synchronized (this) {
            MapleAESOFB sendCrypto = con.getSendCrypto();
            byte[] input = ((byte[]) message);
//        if (Config.isDevelop()) {
//            int packetLen = input.length;
//            int pHeader = readFirstShort(input);
//            String pHeaderStr = Integer.toHexString(pHeader).toUpperCase();
//            pHeaderStr = StringUtil.getLeftPaddedStr(pHeaderStr, '0', 4);
//            String op = lookupRecv(pHeader);
//            String RecvTo = "发送封包 包头：" + op + "[0x" + pHeaderStr + "] (" + packetLen + ")\r\n" + HexTool.toString(input) + "\r\n" + HexTool.toStringFromAscii(input);
//            log.info(RecvTo);
//        }
            byte[] unencrypted = new byte[input.length];
            System.arraycopy(input, 0, unencrypted, 0, input.length);
            byte[] header = sendCrypto.getPacketHeader(unencrypted.length);
            sendCrypto.crypt(unencrypted);
            buffer.writeBytes(header);
            buffer.writeBytes(unencrypted);
        }
    }

    private String lookupRecv(int val) {
        for (ClientOpcode op : ClientOpcode.values()) {
            if (op.getValue() == val) {
                return op.name();
            }
        }
        return "UNKNOWN";
    }

    private int readFirstShort(byte[] arr) {
        return new MaplePacketReader(new ByteArrayByteStream(arr)).readShort();
    }
}