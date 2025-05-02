package Net.auth.client;

import Config.$Crypto.MapleAESOFB;
import Net.auth.netty.PacketDecoder;
import Net.server.Timer;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class AuthServer implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(AuthServer.class);
    public static final AttributeKey<AuthServer> ATTRIBUTE_KEY = AttributeKey.newInstance("AUTH");
    private MapleAESOFB sendCrypto;
    private MapleAESOFB recvCrypto;
    private transient final Channel channel;
    private final SimpleCryptStatus status;
    private ScheduledFuture<?> aliveCheckSchedule;
    private final AtomicInteger aliveCheckCount = new AtomicInteger(0);
    private static AuthServer instance;

    public static AuthServer getInstance() {
        return instance;
    }

    public AuthServer(SimpleCryptStatus status, Channel channel) {
        this.status = status;
        //this.sendCrypto = new MapleAESOFB(status.getKey(), status.getIvSend(), (short) (0xFFFF - 0x0530));
        //this.recvCrypto = new MapleAESOFB(status.getKey(), status.getIvRecv(), (short) 0x0530);
        this.channel = channel;
        instance = this;
    }

    public void announce(byte[] message) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(message);
        }
    }

    public MapleAESOFB getSendCrypto() {
        return sendCrypto;
    }

    public MapleAESOFB getRecvCrypto() {
        return recvCrypto;
    }

    public void disconnect() {
        cancelPingTimer();
        channel.attr(ATTRIBUTE_KEY).set(null);
        channel.attr(PacketDecoder.DECODER_STATE_KEY).set(null);
        channel.close();
    }

    public SimpleCryptStatus getStatus() {
        return status;
    }

    public void startPingSchedule() {
        if (this.aliveCheckSchedule != null) {
            aliveCheckSchedule.cancel(true);
            aliveCheckSchedule = null;
        }

        aliveCheckSchedule = Timer.PingTimer.getInstance().register(() -> {
            try {
                if (aliveCheckCount.incrementAndGet() > 10) {
                    boolean close = false;
                    if (channel != null && channel.isActive()) {
                        close = true;
                        channel.close();
                    }
                    log.info("Disconnected: ping timeout " + close);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }, 5000, 5000);
    }

    public void pongReceived() {
        aliveCheckCount.set(0);
    }

    private void cancelPingTimer() {
        if (aliveCheckSchedule != null) {
            aliveCheckSchedule.cancel(true);
            aliveCheckSchedule = null;
        }
    }

    public synchronized long getTick() {
        return status.getTick();
    }

}
