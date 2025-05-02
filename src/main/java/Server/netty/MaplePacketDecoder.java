package Server.netty;

import Client.MapleClient;
import Config.$Crypto.MapleAESOFB;
import Config.configs.Config;
import Config.configs.OpcodeConfig;
import Config.constants.ServerConstants;
import Opcode.Headler.InHeader;
import Server.ServerType;
import Server.login.handler.LoginPasswordHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.HexTool;
import tools.StringUtil;
import tools.data.ByteArrayByteStream;
import tools.data.MaplePacketReader;

import java.util.List;

import static Server.MapleServerHandler.AllPacketLog;

public class MaplePacketDecoder extends ByteToMessageDecoder {
    public static final AttributeKey<DecoderState> DECODER_STATE_KEY = AttributeKey.newInstance("MaplePacketDecoder");
    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger("DebugWindows");
    private final ServerType type;

    public MaplePacketDecoder(ServerType type) {
        this.type = type;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> message) throws Exception {
        final MapleClient client = ctx.channel().attr(MapleClient.CLIENT_KEY).get();
        DecoderState decoderState = ctx.channel().attr(DECODER_STATE_KEY).get();
        if (decoderState == null) {
            decoderState = new DecoderState();
            ctx.channel().attr(DECODER_STATE_KEY).set(decoderState);
        }

        boolean crypt = false;
        if (in.readableBytes() >= 4 && decoderState.packetlength == -1) {
            int packetHeader = in.readInt();
            if (decoderState.tempPacket != 0) {
                decoderState.packetlength = MapleAESOFB.getLongPacketLength(decoderState.tempPacket, packetHeader);
                decoderState.tempPacket = 0;
            } else {
                int packetid = (packetHeader >> 16) & 0xFFFF;
                if (type == ServerType.LoginServer && !client.isLoggedIn() && packetid == 0x6969) {
                    int packetlength = ((packetHeader & 0xFF) << 8) + (packetHeader >> 8 & 0xFF); // packetHeader & 0xFF
                    byte[] packet = new byte[packetlength];
                    in.readBytes(packet);
                    MaplePacketReader slea = new MaplePacketReader(new ByteArrayByteStream(packet));
                    LoginPasswordHandler.handlePacket(slea, client);
                    return;
                }
                if (packetHeader == 0xFFFF0000) {
                    crypt = true;
                }
                if (!client.getReceiveCrypto().checkPacket(packetHeader)) {
                    ctx.channel().disconnect();
                    return;
                }
                if (crypt) {
                    decoderState.packetlength = in.readableBytes();
                } else {
                    int len = MapleAESOFB.getPacketLength(packetHeader);
                    if (len == 0xFF00) {
                        decoderState.tempPacket = packetHeader;
                    } else {
                        decoderState.packetlength = len;
                    }
                }
            }
        } else if (in.readableBytes() < 4 && decoderState.packetlength == -1) {
            return;
        }
        if (in.readableBytes() >= decoderState.packetlength) {
            byte[] decryptedPacket = new byte[decoderState.packetlength];
            in.readBytes(decryptedPacket);
            decoderState.packetlength = -1;
            if (!crypt) {
                client.getReceiveCrypto().crypt(decryptedPacket);
            } else if (readFirstShort(decryptedPacket) != InHeader.CTX_ENTER_ACCOUNT.getValue()) {
                return;
            }
            client.decryptOpcode(decryptedPacket);
            message.add(decryptedPacket);
            if (Config.isDevelop() || ServerConstants.isLogPacket()) {
                int packetLen = decryptedPacket.length;
                short pHeader = readFirstShort(decryptedPacket);
                String pHeaderStr = Integer.toHexString(pHeader).toUpperCase();
                pHeaderStr = pHeader + "(0x" + StringUtil.getLeftPaddedStr(pHeaderStr, '0', 4) + ")";
                InHeader op = InHeader.valueOf(InHeader.getOpcodeName(pHeader));
                if (op == null) {
                    op = InHeader.UNKNOWN;
                }
                String tab = "";
                for (int i = 4; i > op.name().length() / 8; i--) {
                    tab += "\t";
                }
                StringBuilder recvString = new StringBuilder();
                String t = packetLen >= 10 ? packetLen >= 100 ? packetLen >= 1000 ? "" : " " : "  " : "   ";
                recvString.append("\r\n").append("[CP]\t").append(op.name()).append(tab).append("\t包頭:").append(pHeaderStr).append(t).append("[").append(packetLen).append("字元]");
                if (client.getPlayer() != null) {
                    recvString.append("角色名:").append(client.getPlayer().getName());
                }
                recvString.append("\r\n");
                recvString.append(HexTool.toString(decryptedPacket)).append("\r\n");
                recvString.append(HexTool.toStringFromAscii(decryptedPacket));

                if (ServerConstants.isLogPacket()) {
                    AllPacketLog.info(recvString.toString());
                }
                if (Config.isDevelop() && !OpcodeConfig.isblock(op.name(), false)) {
                    log.trace(recvString.toString());
                }
            }
        }
    }

    private short readFirstShort(byte[] arr) {
        return new MaplePacketReader(new ByteArrayByteStream(arr)).readShort();
    }

    private static class DecoderState {
        int packetlength = -1;
        int tempPacket = 0;
    }
}
