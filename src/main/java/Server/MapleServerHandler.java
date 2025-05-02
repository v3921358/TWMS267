package Server;

import Client.MapleClient;
import Config.$Crypto.MapleAESOFB;
import Config.configs.Config;
import Config.configs.ServerConfig;
import Config.constants.ServerConstants;
import Net.server.ShutdownServer;
import Opcode.Headler.InHeader;
import Packet.LoginPacket;
import Server.cashshop.CashShopServer;
import Server.channel.ChannelServer;
import Server.login.LoginServer;
import Server.netty.MaplePacketDecoder;
import Server.world.World;
import connection.netty.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;
import tools.Randomizer;
import tools.data.ByteArrayByteStream;
import tools.data.MaplePacketReader;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a handler for MapleStory servers.
 */
public final class MapleServerHandler extends ChannelInboundHandlerAdapter {
    public static final Map<String, Long> blockIPList = new HashMap<>();
    public static final Logger AllPacketLog = LoggerFactory.getLogger("AllPackets");
    public static final Logger BuffPacketLog = LoggerFactory.getLogger("BuffPackets");
    public static final ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static final boolean preventIpAttack = true;
    private static final Logger log = LoggerFactory.getLogger(MapleServerHandler.class);
    private static final Logger handlerLog = LoggerFactory.getLogger("HandlePacket");
    private static final Map<String, Pair<Long, Byte>> tracker = new ConcurrentHashMap<>();
    private static final Logger ExceptionLog = LoggerFactory.getLogger("Exceptions");
    private static final Set<Short> UNKNOWN_PACKET = new HashSet<>();
    private static final Map<ServerType, MaplePacketHandler[]> handlers = new LinkedHashMap<>();
    private static long lastTrackerClearTime = 0;
    private final int world;
    private final int channel;
    private final ServerType type;

    public MapleServerHandler(int world, int channel, ServerType type) {
        this.world = world;
        this.channel = channel;
        this.type = type;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (ShutdownServer.getInstance().isShutdown()) {
            ctx.channel().close();
            return;
        }

        // Start IP checking
        String address = ctx.channel().remoteAddress().toString().split(":")[0];
        if (blockIPList.containsKey(address)) {
            if (blockIPList.get(address) <= System.currentTimeMillis()) {
                ctx.channel().close();
                return;
            } else {
                blockIPList.remove(address);
            }
        }

        if (type == ServerType.LoginServer) {
            if (!address.equals("/127.0.0.1")) {
                checkLastTrackerClear();
                Pair<Long, Byte> track = tracker.get(address);
                byte count;
                if (track == null) {
                    count = 1;
                } else {
                    count = track.right;
                    long difference = System.currentTimeMillis() - track.left;
                    if (difference < 10000) {
                        count++;
                    }
                    if (preventIpAttack && count > 5) {
                        blockIPList.put(address, System.currentTimeMillis() + 10 * 60 * 1000);
                        tracker.remove(address);
                        ctx.channel().close();
                        return;
                    }
                }
                tracker.put(address, new Pair<>(System.currentTimeMillis(), count));
            }
        }

        // End IP checking

        if (serverIsShutdown()) {
            ctx.channel().close();
            return;
        }

        byte[] ivRecv = {
                (byte) (Math.random() * 255), (byte) (Math.random() * 255), (byte) (Math.random() * 255), (byte) (Math.random() * 255)
        };
        byte[] ivSend = {
                (byte) (Math.random() * 255), (byte) (Math.random() * 255), (byte) (Math.random() * 255), (byte) (Math.random() * 255)
        };

        MapleAESOFB sendCypher = new MapleAESOFB(ivSend, ServerConstants.MapleMajor, true);
        MapleAESOFB recvCypher = new MapleAESOFB(ivRecv, ServerConstants.MapleMajor, false);
        MapleClient client = new MapleClient(sendCypher, recvCypher, ctx.channel());
        client.setSessionId(Randomizer.nextLong());
        client.setChannel(channel);
        client.setWorldId(world);

        int encryptCode = Integer.decode(System.getProperty("ivEncryptCode", "0"));
        if (encryptCode != 0) {
            for (String host : ServerConfig.noEncryptHost_List) {
                if (address.replace("/", "").equalsIgnoreCase(ServerConstants.getHostAddress(host))) {
                    encryptCode = 0;
                }
            }
        }

        byte[] fakeIVRecv = new byte[ivRecv.length];
        System.arraycopy(ivRecv, 0, fakeIVRecv, 0, ivRecv.length);
        byte[] fakeIVSend = new byte[ivSend.length];
        System.arraycopy(ivSend, 0, fakeIVSend, 0, ivSend.length);

        if (encryptCode != 0) {
            for (int i = 0; i < fakeIVRecv.length; i++) {
                fakeIVRecv[i] ^= encryptCode;
            }
            for (int i = 0; i < fakeIVSend.length; i++) {
                fakeIVSend[i] ^= encryptCode;
            }
        }

        ctx.channel().writeAndFlush(LoginPacket.getHello(ServerConstants.MapleMajor, fakeIVSend, fakeIVRecv, type));
        if (channel > -1) {
            client.setSessionIdx(ChannelServer.getInstance(channel).getSessionIdx());
        } else {
            client.setSessionIdx(0);
        }

        ctx.channel().attr(MapleClient.CLIENT_KEY).set(client);

        StringBuilder sb = new StringBuilder();
        if (channel > -1) {
            sb.append(String.format("[Channel Server] Channel %d : ", channel));
        } else if (type == ServerType.CashShopServer) {
            sb.append("[Cash Server] ");
        } else if (type == ServerType.ChatServer) {
            sb.append("[Chat Server] ");
        } else {
            sb.append("[Login Server] ");
        }

        World.Client.addClient(client);
        sb.append(String.format("IoSession opened %s", ctx.channel().remoteAddress()));
        System.out.println(sb);

        //client.startPingSchedule(type);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        MapleClient client = ctx.channel().attr(MapleClient.CLIENT_KEY).get();
        try {
            if (client != null) {
                client.disconnect(true, ServerType.CashShopServer.equals(type));
                World.Client.removeClient(client);
            }
        } catch (Throwable t) {
            log.error("連接異常關閉", t);
        } finally {
            ctx.channel().attr(MapleClient.CLIENT_KEY).set(null);
            ctx.channel().attr(MaplePacketDecoder.DECODER_STATE_KEY).set(null);
            ctx.channel().close();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MaplePacketReader slea = new MaplePacketReader(new ByteArrayByteStream((byte[]) msg));
        if (slea.available() < 2) {
            return;
        }
        MapleClient client = ctx.channel().attr(MapleClient.CLIENT_KEY).get();
        if (ChannelHandler.handlePacket(this, slea, client)) {
            return;
        }
        handlePacket(slea, client, type);
    }

    private static final List<InHeader> alreadyLoggedOpcode = new LinkedList<>();

    private static void handlePacket(MaplePacketReader slea, MapleClient client, ServerType type) throws IOException {
        if (client == null || !client.isReceiving()) {
            return;
        }

        short packetId = slea.readShort();
        InHeader opcode = lookupRecv(packetId);
        if (opcode == null) {
            System.err.printf("[In] %d(0x%s)%n", packetId, Integer.toHexString(packetId));
        } else {
            PacketProcessor.getProcessor(opcode, slea, type, client);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // MapleClient Client.client = ctx.channel().attr(MapleClient.CLIENT_KEY).get();
        // if (Client.client != null && Client.isLoggedIn()) {
        // if (Config.isDevelop())
        // System.out.println("userEventTriggered:" + Client.getAccountName());
        // ctx.channel().close();
        // }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause.getMessage().contains("強制關閉來自異常的連線。")) {
            ctx.channel().close();
            return;
        }

        if (Config.isDevelop()) {
            MapleClient client = ctx.channel().attr(MapleClient.CLIENT_KEY).get();
            if (client != null) {
                if (client.getPlayer() != null) {
                    ExceptionLog.error("帳號：" + client.getAccountName() + " 角色：" + client.getPlayer().getName() + " 地圖：" + client.getPlayer().getMapId(), cause);
                } else {
                    ExceptionLog.error("帳號：" + client.getAccountName(), cause);
                }
            }
        }

        if (cause instanceof IOException || cause instanceof ClassCastException) {
            ctx.channel().close();
            return;
        }

        MapleClient client = ctx.channel().attr(MapleClient.CLIENT_KEY).get();
        if (client != null) {
            if (client.getPlayer() != null) {
                client.getPlayer().saveToDB(true, type == ServerType.CashShopServer);
                log.error("發現異常，角色：" + client.getPlayer().getName() + " 地圖：" + client.getPlayer().getMapId(), cause);
            } else {
                log.error("發現異常，帳號：" + client.getAccountName(), cause);
            }
        }

        ctx.channel().close();
    }

    private void checkLastTrackerClear() {
        if (System.currentTimeMillis() - lastTrackerClearTime >= 60 * 60 * 1000) {
            lastTrackerClearTime = System.currentTimeMillis();
            tracker.clear();
        }
    }

    private static InHeader lookupRecv(short header) {
        InHeader recv = InHeader.valueOf(InHeader.getOpcodeName(header));
        return recv == null ? InHeader.UNKNOWN : recv;
    }

    public static void reloadHandlers() {
        handlers.clear();
    }

    private boolean serverIsShutdown() {
        if (channel > -1) {
            return ChannelServer.getInstance(channel).isShutdown();
        } else if (type == ServerType.CashShopServer) {
            return CashShopServer.isShutdown();
        } else if (type == ServerType.LoginServer) {
            return LoginServer.isShutdown();
        }
        return false;
    }
}
