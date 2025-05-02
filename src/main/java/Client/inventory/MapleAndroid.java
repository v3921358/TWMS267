package Client.inventory;

import Client.MapleCharacter;
import Database.DatabaseLoader.DatabaseConnectionEx;
import Net.server.MapleItemInformationProvider;
import Net.server.StructAndroid;
import Net.server.movement.LifeMovement;
import Net.server.movement.LifeMovementFragment;
import Net.server.movement.MovementNormal;
import Packet.AndroidPacket;
import Packet.PacketHelper;
import connection.OutPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.io.Serializable;
import java.sql.*;
import java.util.List;

public class MapleAndroid implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(MapleAndroid.class);
    private static final long serialVersionUID = 9179541993413738569L;
    private final int uniqueid;
    private final int itemid;
    private int Fh = 0;
    private int stance = 0;
    private int skin;
    private int hair;
    private int face;
    private int gender;
    private int type;
    private String name;
    private boolean antennaUsed;
    private long shopTime;
    private Point pos = new Point(0, 0);
    private boolean changed = false;

    public MapleAndroid(int itemid, int uniqueid) {
        this.itemid = itemid;
        this.uniqueid = uniqueid;
    }

    /*
     * 加載機器人信息
     */
    public static MapleAndroid loadFromDb(int itemid, int uniqueid) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            MapleAndroid ret = new MapleAndroid(itemid, uniqueid);
            PreparedStatement ps = con.prepareStatement("SELECT * FROM androids WHERE uniqueid = ?");
            ps.setInt(1, uniqueid);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return null;
            }
            int type = rs.getInt("type");
            int gender = rs.getInt("gender");
            boolean fix = false;
            if (type < 1) { //修復以前錯誤的設置 機器人的外形ID默認是從1開始
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                type = ii.getAndroidType(itemid);
                StructAndroid aInfo = ii.getAndroidInfo(type);
                if (aInfo == null) { //如果沒有這個類型就返回空
                    return null;
                }
                gender = aInfo.gender;
                fix = true;
            }
            ret.setType(type);
            ret.setGender(gender);
            ret.setSkin(rs.getInt("skin"));
            ret.setHair(rs.getInt("hair"));
            ret.setFace(rs.getInt("face"));
            ret.setName(rs.getString("name"));
            ret.setAntennaUsed(rs.getByte("antennaUsed") == 1);
            ret.setShopTime(rs.getTimestamp("shopTime") == null ? -1 : rs.getTimestamp("shopTime").getTime());
            ret.changed = fix;
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException ex) {
            log.error("加載機器人信息出錯", ex);
            return null;
        }
    }

    /*
     * 創建1個機器人信息
     * 也就是使用1個新機器人的道具
     */
    public static MapleAndroid create(int itemid, int uniqueid) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int type = ii.getAndroidType(itemid);
        StructAndroid aInfo = ii.getAndroidInfo(type);
        if (aInfo == null) {
            return null;
        }
        int gender = aInfo.gender;
        int skin = aInfo.skin.get(Randomizer.nextInt(aInfo.skin.size()));
        int hair = aInfo.hair.get(Randomizer.nextInt(aInfo.hair.size()));
        int face = aInfo.face.get(Randomizer.nextInt(aInfo.face.size()));
        if (uniqueid <= -1) { //修復唯一ID小於-1的情況
            uniqueid = MapleInventoryIdentifier.getInstance();
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement pse = con.prepareStatement("INSERT INTO androids (uniqueid, type, gender, skin, hair, face, name) VALUES (?, ?, ?, ?, ?, ?, ?)");
            pse.setInt(1, uniqueid);
            pse.setInt(2, type);
            pse.setInt(3, gender);
            pse.setInt(4, skin);
            pse.setInt(5, hair);
            pse.setInt(6, face);
            pse.setString(7, "機器人");
            pse.executeUpdate();
            pse.close();
        } catch (SQLException ex) {
            log.error("創建機器人信息出錯", ex);
            return null;
        }
        MapleAndroid and = new MapleAndroid(itemid, uniqueid);
        and.setType(type);
        and.setGender(gender);
        and.setSkin(skin);
        and.setHair(hair);
        and.setFace(face);
        and.setName("機器人");
        and.setAntennaUsed(false);
        and.setShopTime(-1);
        return and;
    }

    /**
     * 保存機器人信息到SQL
     */
    public void saveToDb() {
        saveToDb(null);
    }

    /**
     * 保存機器人信息到SQL
     */
    public void saveToDb(Connection con) {
        if (!changed) {
            return;
        }
        boolean needclose = false;
        try {
            if (con == null) {
                needclose = true;
                con = DatabaseConnectionEx.getInstance().getConnection();
            }
            PreparedStatement ps = con.prepareStatement("UPDATE androids SET type = ?, gender = ?, skin = ?, hair = ?, face = ?, name = ?, antennaUsed = ?, shopTime = ? WHERE uniqueid = ?");
            ps.setInt(1, type); //外形ID
            ps.setInt(2, gender); //性別
            ps.setInt(3, skin); //皮膚
            ps.setInt(4, hair); //髮型
            ps.setInt(5, face); //臉型
            ps.setString(6, name); //名字
            ps.setByte(7, (byte) (antennaUsed ? 1 : 0)); // 透明機器人耳飾感應器
            if (shopTime >= 0) {
                ps.setTimestamp(8, new Timestamp(shopTime));
            } else {
                ps.setNull(8, Types.TIMESTAMP);
            }
            ps.setInt(9, uniqueid); //唯一的ID
            ps.executeUpdate();
            ps.close();
            changed = false;
        } catch (SQLException ex) {
            log.error("保存機器人信息出錯", ex);
        } finally {
            if (needclose) {
                try {
                    con.close();
                } catch (SQLException e) {
                    log.error("保存機器人信息出錯", e);
                }
            }
        }
    }

    public int getItemId() {
        return itemid;
    }

    public int getUniqueId() {
        return uniqueid;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
        this.changed = true;
    }

    public int getType() {
        return type;
    }

    public void setType(int t) {
        this.type = t;
        this.changed = true;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int g) {
        this.gender = g;
        this.changed = true;
    }

    public int getSkin() {
        return skin;
    }

    public void setSkin(int s) {
        this.skin = s;
        this.changed = true;
    }

    public int getHair() {
        return hair;
    }

    public void setHair(int h) {
        this.hair = h;
        this.changed = true;
    }

    public int getFace() {
        return face;
    }

    public void setFace(int f) {
        this.face = f;
        this.changed = true;
    }

    public boolean isAntennaUsed() {
        return antennaUsed;
    }

    public void setAntennaUsed(boolean a) {
        this.antennaUsed = a;
        this.changed = true;
    }

    public long getShopTime() {
        return shopTime;
    }

    public void setShopTime(long s) {
        this.shopTime = s;
        this.changed = true;
    }

    /*
     * 移動相關信息
     */
    public int getFh() {
        return Fh;
    }

    public void setFh(int Fh) {
        this.Fh = Fh;
    }

    public Point getPos() {
        return pos;
    }

    public void setPos(Point pos) {
        this.pos = pos;
    }

    public int getStance() {
        return stance;
    }

    public void setStance(int stance) {
        this.stance = stance;
    }

    public void updatePosition(List<LifeMovementFragment> movement) {
        for (LifeMovementFragment move : movement) {
            if (move instanceof LifeMovement) {
                if (move instanceof MovementNormal) {
                    setPos(((MovementNormal) move).getPosition());
                    setFh(((MovementNormal) move).getFH());
                }
                setStance(((LifeMovement) move).getMoveAction());
            }
        }
    }

    public void showEmotion(MapleCharacter chr, String emotion) {
        byte speak;
        byte animation = 0;
        switch (emotion) {
            case "hello":
                speak = 0;
                animation = (byte) Randomizer.rand(1, 17);
                break;
            case "levelup":
                speak = 1;
                animation = (byte) Randomizer.rand(1, 17);
                break;
            case "dead":
                speak = 2;
                animation = (byte) Randomizer.rand(1, 17);
                break;
            case "bye":
                speak = 3;
                break;
            case "job":
                speak = 4;
                break;
            case "alert":
                speak = 5;
                animation = (byte) (Randomizer.isSuccess(10) ? 0 : 1);
                if (animation == 1) {
                    return;
                }
                break;
            default:
                return;
        }
        chr.getMap().broadcastMessage(AndroidPacket.showAndroidEmotion(chr.getId(), speak, animation));
    }

    public void encodeAndroidLook(MaplePacketLittleEndianWriter mplew) {
        OutPacket outPacket = new OutPacket();
        encodeAndroidLook(outPacket);
        mplew.write(outPacket.getData());
    }

    public void encodeAndroidLook(OutPacket outPacket) {
        outPacket.encodeShort(getSkin() >= 2000 ? getSkin() - 2000 : getSkin());
        outPacket.encodeInt(getHair());
        outPacket.encodeInt(getFace());
        outPacket.encodeString(getName());
        outPacket.encodeInt(isAntennaUsed() ? 2892000 : 0);
        outPacket.encodeLong(PacketHelper.getTime(getShopTime() < 0 ? -2 : getShopTime()));
        outPacket.encodeInt(0);
    }
}
