package Server.netty;

import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ServerWorkQueue {

    private static final Logger log = LoggerFactory.getLogger(ServerWorkQueue.class);
    private volatile boolean running = true;
    private final LinkedList<Runnable> list = new LinkedList<>();
    private final Executor executor;

    private static final ServerWorkQueue instance = new ServerWorkQueue();

    public static ServerWorkQueue getInstance() {
        return instance;
    }

    private ServerWorkQueue() {
        executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new DefaultThreadFactory("Client"));
    }

    public void addLast(Runnable runnable) {
        synchronized (list) {
            list.addLast(runnable);
            list.notifyAll();
        }
    }

    public void shutdown() {
        running = false;
        synchronized (list) {
            list.notifyAll();
        }
    }

    public void start() {
        executor.execute(() -> {
            while (running) {
                synchronized (list) {
                    while (running && list.isEmpty()) {
                        try {
                            list.wait();
                        } catch (InterruptedException e) {
                            log.error("ServerWorkQueue", e);
                        }
                    }
                    if (!list.isEmpty()) {
                        list.removeFirst().run();
                    }
                }
            }
        });
    }
}
