package Plugin.gui;

import java.util.ArrayList;
import java.util.List;

public class GuiManager {
    private static final List<GuiListener> LISTENERS = new ArrayList<>();

    public static void registerListener(GuiListener listener) {
        LISTENERS.add(listener);
    }

    public static void deregisterListener(GuiListener listener) {
        LISTENERS.remove(listener);
    }

    public static void clear() {
        LISTENERS.clear();
    }

    public static void playerRegistered(int chrId) {
        LISTENERS.forEach(it -> it.playerRegistered(chrId));
    }

    public static void playerTalked(int type, int chrId, String message) {
        LISTENERS.forEach(it -> it.playerTalked(type, chrId, message));
    }

    public static void onlineStatusChanged(int channel, int count) {
        LISTENERS.forEach(it -> it.onlineStatusChanged(channel, count));
    }
}
