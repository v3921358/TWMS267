package Net.auth.netty;

import Net.auth.AuthHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthClient extends Thread {
    private static final Logger log = LoggerFactory.getLogger(AuthClient.class);

    private static final AuthClient instance = new AuthClient();

    public static AuthClient getInstance(String script) {
        return instance;
    }

    static {
        instance.start();
    }

    private volatile int failCount = 0;

    private AuthClient() {
    }

    public void run() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 启动客户端
            String host = "127.0.0.1";
//            if (Config.isDevelop()) {
//                host = "127.0.0.1";
//            }
            int port = 9498;
            ChannelFuture future = new Bootstrap()
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("idleStateHandler", new IdleStateHandler(10, 10, 0))
                                    .addLast("decoder", new PacketDecoder())
                                    .addLast("encoder", new PacketEncoder())
                                    .addLast("handler", new AuthHandler());
                        }
                    }).connect(host, port).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("連接驗證伺服器失敗！", e);
//            if (++failCount >= 3) {
//                System.err.println("连续3次连接验证服务器失败，服务端自动关闭！");
//                Thread thread = new Thread(ShutdownServer.getInstance());
//                thread.start();
//                try {
//                    thread.join();
//                } catch (InterruptedException ex) {
//                    ex.printStackTrace();
//                }
//                System.exit(0);
//            }
//            if (!AuthHandler.succeed) {
//                System.exit(0);
//            }
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public void clearFailCount() {
        failCount = 0;
    }
}