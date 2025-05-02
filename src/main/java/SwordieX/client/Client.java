package SwordieX.client;

import Client.MapleClient;
import SwordieX.client.character.Char;
import connection.NettyClient;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client extends NettyClient {
    @Getter
    @Setter
    private Char chr;
    @Getter
    @Setter
    private User user;
    @Getter
    private final Lock lock;
    @Getter
    @Setter
    private byte channel;
    @Getter
    @Setter
    private byte worldId;

    public Client(Channel channel, byte[] sendSeq, byte[] recvSeq) {
        super(channel, sendSeq, recvSeq);
        lock = new ReentrantLock(true);
    }

    public Client(MapleClient client) {
        this(client.getSession(), client.getSendCrypto().getIv(), client.getReceiveCrypto().getIv());
        this.channel = (byte) client.getChannel();
        this.worldId = (byte) client.getWorldId();
    }
}
