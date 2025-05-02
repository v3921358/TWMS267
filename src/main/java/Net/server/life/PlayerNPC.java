package Net.server.life;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.inventory.Item;
import Client.inventory.MapleInventoryType;
import Client.inventory.MaplePet;
import Database.DatabaseLoader.DatabaseConnectionEx;
import Net.server.maps.MapleMap;
import Packet.NPCPacket;
import Server.channel.ChannelServer;
import Server.world.WorldFindService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class PlayerNPC extends MapleNPC {

    private static final Logger log = LoggerFactory.getLogger(PlayerNPC.class);
    private final int[] pets = new int[3];
    private Map<Byte, Integer> equips = new HashMap<>();
    private final int mapid;
    private int face;
    private int hair;
    private final int charId;
    private byte skin, gender;

    public PlayerNPC(ResultSet rs) throws Exception {
        super(rs.getInt("ScriptId"), rs.getString("name"), rs.getInt("map"));
        hair = rs.getInt("hair");
        face = rs.getInt("face");
        mapid = rs.getInt("map");
        skin = rs.getByte("skin");
        charId = rs.getInt("charid");
        gender = rs.getByte("gender");
        setCoords(rs.getInt("x"), rs.getInt("y"), rs.getInt("dir"), rs.getInt("Foothold"));
        String[] pet = rs.getString("pets").split(",");
        for (int i = 0; i < 3; i++) {
            if (pet[i] != null) {
                pets[i] = Integer.parseInt(pet[i]);
            } else {
                pets[i] = 0;
            }
        }

        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs_equip WHERE NpcId = ?");
            ps.setInt(1, getId());
            ResultSet rs2 = ps.executeQuery();
            while (rs2.next()) {
                equips.put(rs2.getByte("equippos"), rs2.getInt("equipid"));
            }
            rs2.close();
            ps.close();
        }
    }

    public PlayerNPC(MapleCharacter cid, int npc, MapleMap map, MapleCharacter base) throws SQLException {
        super(npc, cid.getName(), map.getId());
        this.charId = cid.getId();
        this.mapid = map.getId();
        setCoords(base.getPosition().x, base.getPosition().y, 0, base.getFH()); //0 = facing dir? no idea, but 1 dosnt work
        update(cid);
    }

    public static void loadAll() {
        List<PlayerNPC> toAdd = new ArrayList<>();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                toAdd.add(new PlayerNPC(rs));
            }
            rs.close();
            ps.close();
        } catch (Exception se) {
            se.printStackTrace();
        }
        for (PlayerNPC npc : toAdd) {
            npc.addToServer();
        }
    }

    public static void updateByCharId(Connection con, MapleCharacter chr) {
        if (WorldFindService.getInstance().findChannel(chr.getId()) > 0) { //if character is in cserv
            for (PlayerNPC npc : ChannelServer.getInstance(WorldFindService.getInstance().findChannel(chr.getId())).getAllPlayerNPC()) {
                npc.update(con, chr);
            }
        }
    }

    public void setCoords(int x, int y, int f, int fh) {
        setPosition(new Point(x, y));
        setCy(y);
        setRx0(x - 50);
        setRx1(x + 50);
        setF(f);
        setCurrentFh(fh);
    }

    public void addToServer() {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            cserv.addPlayerNPC(this);
        }
    }

    public void removeFromServer() {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            cserv.removePlayerNPC(this);
        }
    }

    public void update(MapleCharacter chr) {
        update(null, chr);
    }

    public void update(Connection con, MapleCharacter chr) {
        if (chr == null || charId != chr.getId()) {
            return; //cant use name as it mightve been change actually..
        }
        setName(chr.getName());
        setHair(chr.getHair());
        setFace(chr.getFace());
        setSkin((chr.getSkinColor()));
        setGender(chr.getGender());
        //setPets(chr.getPets());

        equips = new HashMap<>();
        for (Item item : chr.getInventory(MapleInventoryType.EQUIPPED).newList()) {
            if (item.getPosition() < -127) {
                continue;
            }
            equips.put((byte) item.getPosition(), item.getItemId());
        }
        saveToDB(con);
    }

    public void destroy(Connection con) {
        destroy(con, false); //just sql
    }

    public void destroy(Connection con, boolean remove) {
        boolean needclose = false;
        try {
            if (con == null) {
                needclose = true;
                con = DatabaseConnectionEx.getInstance().getConnection();
            }
            PreparedStatement ps = con.prepareStatement("DELETE FROM playernpcs WHERE scriptid = ?");
            ps.setInt(1, getId());
            ps.executeUpdate();
            ps.close();

            ps = con.prepareStatement("DELETE FROM playernpcs_equip WHERE npcid = ?");
            ps.setInt(1, getId());
            ps.executeUpdate();
            ps.close();
            if (remove) {
                removeFromServer();
            }
        } catch (Exception se) {
            log.error("", se);
        } finally {
            if (needclose) {
                try {
                    con.close();
                } catch (SQLException e) {
                    log.error("", e);
                }
            }
        }
    }

    public void saveToDB(Connection con) {
        boolean needClose = false;
        try {
            if (getNPCFromWZ() == null) {
                destroy(con, true);
                return;
            }
            destroy(con);
            if (con == null) {
                needClose = true;
                con = DatabaseConnectionEx.getInstance().getConnection();
            }
            PreparedStatement ps = con.prepareStatement("INSERT INTO playernpcs(name, hair, face, skin, x, y, map, charid, scriptid, foothold, dir, gender, pets) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, getName());
            ps.setInt(2, getHair());
            ps.setInt(3, getFace());
            ps.setInt(4, getSkin());
            ps.setInt(5, getPosition().x);
            ps.setInt(6, getPosition().y);
            ps.setInt(7, getMapId());
            ps.setInt(8, getCharId());
            ps.setInt(9, getId());
            ps.setInt(10, getCurrentFH());
            ps.setInt(11, getF());
            ps.setInt(12, getGender());
            String[] pet = {"0", "0", "0"};
            for (int i = 0; i < 3; i++) {
                if (pets[i] > 0) {
                    pet[i] = String.valueOf(pets[i]);
                }
            }
            ps.setString(13, pet[0] + "," + pet[1] + "," + pet[2]);
            ps.executeUpdate();
            ps.close();

            ps = con.prepareStatement("INSERT INTO playernpcs_equip(npcid, charid, equipid, equippos) VALUES (?, ?, ?, ?)");
            ps.setInt(1, getId());
            ps.setInt(2, getCharId());
            for (Entry<Byte, Integer> equip : equips.entrySet()) {
                ps.setInt(3, equip.getValue());
                ps.setInt(4, equip.getKey());
                ps.executeUpdate();
            }
            ps.close();
        } catch (Exception se) {
            log.error("", se);
        } finally {
            if (needClose) {
                try {
                    con.close();
                } catch (SQLException e) {
                    log.error("", e);
                }
            }
        }
    }

    public MapleCharacter getPlayer() {
        return MapleCharacter.getCharacterById(charId);
    }

    public Map<Byte, Integer> getEquips() {
        return equips;
    }

    public byte getSkin() {
        return skin;
    }

    public void setSkin(byte s) {
        this.skin = s;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int g) {
        this.gender = (byte) g;
    }

    public int getFace() {
        return face;
    }

    public void setFace(int f) {
        this.face = f;
    }

    public int getHair() {
        return hair;
    }

    public void setHair(int h) {
        this.hair = h;
    }

    public int getCharId() {
        return charId;
    }

    public int getMapId() {
        return mapid;
    }

    public int getPet(int i) {
        return pets[i] > 0 ? pets[i] : 0;
    }

    public void setPets(List<MaplePet> p) {
        for (int i = 0; i < 3; i++) {
            if (p != null && p.size() > i && p.get(i) != null) {
                this.pets[i] = p.get(i).getPetItemId();
            } else {
                this.pets[i] = 0;
            }
        }
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.announce(NPCPacket.spawnNPC(this));
        client.announce(NPCPacket.spawnPlayerNPC(this));
        client.announce(NPCPacket.spawnNPCRequestController(this, true));

    }

    public MapleNPC getNPCFromWZ() {
        MapleNPC npc = MapleLifeFactory.getNPC(getId(), getMapId());
        if (npc != null) {
            npc.setName(getName());
        }
        return npc;
    }
}
