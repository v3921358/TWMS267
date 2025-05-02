package Plugin.gui;

import Client.MapleCharacter;

public interface GuiListener {

    void playerRegistered(int chrId);

    void playerTalked(int type, int chrId, String message);

    void updatePlayer(MapleCharacter player);

    void onlineStatusChanged(int channel, int count);
}
