package Plugin.script.binding;

import Net.server.Timer;
import Plugin.script.ServerManipulator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.script.Invocable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
public class ScriptServer extends ScriptBase {

    @Getter
    private static final Timer timerInstance = new Timer();
    private final ConcurrentHashMap<String, ScheduledFuture<?>> timers;
    private Invocable globalScope;
    @Getter
    private final ServerManipulator hooks;

    private int channelID;

    public ScriptServer(ServerManipulator hooks, Invocable globalScope, int channelID) {
        this.hooks = hooks;
        timers = new ConcurrentHashMap<String, ScheduledFuture<?>>();
    }

    public void startTimer(final String key, int millisDelay) {
        synchronized (timers) {
            stopTimer(key);
            timers.put(key, getTimerInstance().schedule(() -> {
                synchronized (timers) {
                    timers.remove(key);
                }
                try {
                    hooks.timerExpired(key);
                } catch (Exception e) {
                    log.error("error: startTimer. {}", e.getMessage());
                }
            }, millisDelay));
        }

    }

    public void stopTimer(String key) {
        ScheduledFuture<?> future = timers.remove(key);
        if (future != null) {
            future.cancel(true);
        }
    }

    public void stopTimers() {
        for (ScheduledFuture<?> future : timers.values()) {
            future.cancel(true);
        }
    }

}
