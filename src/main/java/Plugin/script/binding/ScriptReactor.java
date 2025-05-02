package Plugin.script.binding;

import Client.MapleClient;
import Net.server.maps.MapleReactor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Slf4j
public class ScriptReactor extends PlayerScriptInteraction {

    @Getter
    private final MapleReactor reactor;

    public ScriptReactor(MapleClient client, MapleReactor reactor) {
        super(client.getPlayer());
        this.reactor = reactor;
    }


    public String getName() {
        return getReactor().getName();
    }

    public int getDataId() {
        return getReactor().getObjectId();
    }

    public Point getPosition() {
        return getReactor().getPosition();
    }




}
