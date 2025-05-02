package Net.auth.netty;

import Config.$Crypto.MapleAESOFB;
import Net.auth.client.AuthServer;
import Net.auth.client.SimpleCryptStatus;
import Net.auth.packet.AuthPacket;
import Net.auth.packet.ServerOpcode;
import Net.auth.util.RSAUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.data.ByteArrayByteStream;
import tools.data.MaplePacketReader;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {
    public static final AttributeKey<DecoderState> DECODER_STATE_KEY = AttributeKey.newInstance("PacketDecoder");
    private static final Logger log = LoggerFactory.getLogger(PacketDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> message) throws Exception {
        AuthServer c = ctx.channel().attr(AuthServer.ATTRIBUTE_KEY).get();
        DecoderState decoderState = ctx.channel().attr(DECODER_STATE_KEY).get();
        if (decoderState == null) {
            decoderState = new DecoderState();
            ctx.channel().attr(DECODER_STATE_KEY).set(decoderState);
        }
        if (c == null) {
            if (in.readableBytes() >= 4 && decoderState.packetlength == -1) {
                byte[] header = new byte[4];
                in.readBytes(header);
                if (RSAUtil.getPacketLength(header) != 256) {
                    ctx.channel().close();
                    return;
                }
                decoderState.packetlength = 256;
            } else if (in.readableBytes() < 4 && decoderState.packetlength == -1) {
                return;
            }
            if (in.readableBytes() >= decoderState.packetlength) {
                byte[] packet = new byte[decoderState.packetlength];
                in.readBytes(packet);
                decoderState.packetlength = -1;
                byte[] data = RSAUtil.decryptByPublicKey(packet);
                MaplePacketReader slea = new MaplePacketReader(new ByteArrayByteStream(data));
                byte[] key = slea.read(slea.readInt());
                byte[] serverIv = slea.read(4);
                byte[] clientIv = slea.read(4);
                int seed1 = slea.readInt();
                int seed2 = slea.readInt();
                int seed3 = slea.readInt();
                c = new AuthServer(new SimpleCryptStatus(key, serverIv, clientIv, seed1, seed2, seed3), ctx.channel());
                ctx.channel().attr(AuthServer.ATTRIBUTE_KEY).set(c);
                c.announce(AuthPacket.connectionSuccess(c.getTick()));
                c.startPingSchedule();
            }
            return;
        }
        MapleAESOFB recvCrypto = c.getRecvCrypto();
        if (in.readableBytes() >= 4 && decoderState.packetlength == -1) {
            int packetHeader = in.readInt();
            if (decoderState.tempPacket != 0) {
                decoderState.packetlength = MapleAESOFB.getLongPacketLength(decoderState.tempPacket, packetHeader);
                decoderState.tempPacket = 0;
            } else {
                if (!recvCrypto.checkPacket(packetHeader)) {
                    System.err.println("wrong packet recieved");
                    ctx.channel().close();
                    return;
                }
                int len = MapleAESOFB.getPacketLength(packetHeader);
                if (len == 0xFF00) {
                    decoderState.tempPacket = packetHeader;
                } else {
                    decoderState.packetlength = len;
                }
            }
        } else if (in.readableBytes() < 4 && decoderState.packetlength == -1) {
            return;
        }
        if (in.readableBytes() >= decoderState.packetlength) {
            byte[] decryptedPacket = new byte[decoderState.packetlength];
            in.readBytes(decryptedPacket);
            decoderState.packetlength = -1;
            recvCrypto.crypt(decryptedPacket);
            message.add(decryptedPacket);
        }

    }

    private String lookupSend(int val) {
        for (ServerOpcode op : ServerOpcode.values()) {
            if (op.getValue() == val) {
                return op.name();
            }
        }
        return "UNKNOWN";
    }

    private short readFirstShort(byte[] arr) {
        return new MaplePacketReader(new ByteArrayByteStream(arr)).readShort();
    }


    private static class DecoderState {
        int packetlength = -1;
        int tempPacket = 0;
    }
}
