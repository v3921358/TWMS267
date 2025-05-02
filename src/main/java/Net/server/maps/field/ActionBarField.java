package Net.server.maps.field;

import Client.MapleCharacter;
import Config.constants.enums.ActionBarResultType;
import Net.server.maps.MapleMap;
import Opcode.Headler.OutHeader;
import Plugin.provider.MapleData;
import Plugin.provider.MapleDataProviderFactory;
import Plugin.provider.MapleDataTool;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.HashMap;
import java.util.Map;

public class ActionBarField extends MapleMap {

    public static void init() {
        for (final MapleData zj : MapleDataProviderFactory.getEtc().getData("ActionBar.img").getChildByPath("ActionBar")) {
            if (!"info".equals(zj.getName())) {
                final int b = MapleDataTool.getInt("info/fieldType", zj, 0);
                final int int1 = Integer.parseInt(zj.getName());
                final MapleFieldActionBar akq = new MapleFieldActionBar(int1, b);
                for (final MapleData zj6 : zj) {
                    if (!"info".equals(zj6.getName())) {
                        final int int2 = Integer.parseInt(zj6.getName());
                        akq.getSkills().put(int2, new AKR(int2, MapleDataTool.getInt("id", zj6, 0), MapleDataTool.getString("type", zj6, "event"), MapleDataTool.getInt("useOnce", zj6, 0) > 0, MapleDataTool.getInt("usableCount", zj6, -1)));
                    }
                }
                MapleFieldActionBar.infos.put(int1, akq);
            }
        }
    }

    public ActionBarField(int mapid, int channel, int returnMapId, float monsterRate) {
        super(mapid, channel, returnMapId, monsterRate);
    }

    @Override
    public void userEnterField(MapleCharacter chr) {
        super.userEnterField(chr);
        final MapleFieldActionBar bm;
        if ((bm = MapleFieldActionBar.createActionBar(22)) != null) {
            chr.setActionBar(bm);
            final MaplePacketLittleEndianWriter hh;
            (hh = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.LP_ActionBarResult);
            hh.writeInt(ActionBarResultType.Create_Result.getValue());
            hh.writeInt(chr.getActionBar().pq);
            chr.send(hh.getPacket());
        }
    }

    @Override
    public void userLeaveField(MapleCharacter chr) {
        super.userLeaveField(chr);
        final MaplePacketLittleEndianWriter hh;
        (hh = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.LP_ActionBarResult);
        hh.writeInt(ActionBarResultType.Remove_Result.getValue());
        hh.writeInt(chr.getActionBar().pq);
        chr.send(hh.getPacket());
        chr.setActionBar(null);
    }

    public static final class MapleFieldActionBar {
        public static final Map<Integer, MapleFieldActionBar> infos = new HashMap<>();
        public final int pq;
        private final int amt;
        private final Map<Integer, AKR> skills = new HashMap<>();

        public MapleFieldActionBar(final int pq, final int amt) {
            this.pq = pq;
            this.amt = amt;
        }

        public static MapleFieldActionBar createActionBar(final int n) {
            MapleFieldActionBar res = null;
            final MapleFieldActionBar actionBar = MapleFieldActionBar.infos.get(n);
            if (actionBar != null) {
                final MapleFieldActionBar akq4 = new MapleFieldActionBar(actionBar.pq, actionBar.amt);
                actionBar.skills.forEach((n2, akr) -> akq4.skills.put(n2, new AKR(akr.index, akr.id, akr.type, akr.useOnce, akr.usableCount)));
                res = akq4;
            }
            return res;
        }

        public Map<Integer, AKR> getSkills() {
            return this.skills;
        }
    }

    public static final class AKR {
        final int index;
        final int id;
        final String type;
        final boolean useOnce;
        int usableCount;

        public AKR(final int index, final int pq, final String type, final boolean amv, final int amw) {
            this.index = index;
            this.id = pq;
            this.type = type;
            this.useOnce = amv;
            this.usableCount = amw;
        }
    }

}
