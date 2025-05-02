package Client.inventory;

import Database.DatabaseLoader.DatabaseConnectionEx;
import Net.server.MapleItemInformationProvider;
import Net.server.movement.LifeMovement;
import Net.server.movement.LifeMovementFragment;
import Net.server.movement.MovementNormal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MaplePet implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(MaplePet.class);
    private static final long serialVersionUID = 9179541993413738569L;
    private final int[] excluded = new int[10]; //寵物撿取過濾
    private String name;
    private int Fh = 0;
    private int stance = 0;
    private final int uniqueid;
    private final int petitemid;
    private int secondsLeft = 0;
    private int color = -1;
    private int addSkill = 0;
    private Point pos;
    private byte fullness = 100, level = 2, summoned = 0;
    private short inventoryPosition = 0, closeness = 0, flags = 0;
    private boolean changed = false;
    private boolean canPickup = true;
    private final int[] buffIds = new int[]{0, 0};

    public MaplePet(int petitemid, int uniqueid) {
        this.petitemid = petitemid;
        this.uniqueid = uniqueid;
        for (int i = 0; i < this.excluded.length; i++) {
            this.excluded[i] = 0;
        }
    }

    private MaplePet(int petitemid, int uniqueid, short inventorypos) {
        this.petitemid = petitemid;
        this.uniqueid = uniqueid;
        this.inventoryPosition = inventorypos;
        for (int i = 0; i < this.excluded.length; i++) {
            this.excluded[i] = 0;
        }
    }

    public static MaplePet loadFromDb(int itemid, int petid, short inventorypos) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            MaplePet ret = new MaplePet(itemid, petid, inventorypos);
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM pets WHERE petid = ?")) {
                ps.setInt(1, petid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        rs.close();
                        ps.close();
                        return null;
                    }
                    ret.setName(rs.getString("name"));
                    ret.setCloseness(rs.getShort("closeness"));
                    ret.setLevel(rs.getByte("level"));
                    ret.setFullness(rs.getByte("fullness"));
                    ret.setSecondsLeft(rs.getInt("seconds"));
                    ret.setFlags(rs.getShort("flags"));
                    ret.setColor(rs.getByte("color"));
                    ret.setAddSkill(rs.getInt("addSkill"));
                    String[] list = rs.getString("excluded").split(",");
                    for (int i = 0; i < ret.excluded.length; i++) {
                        ret.excluded[i] = Integer.parseInt(list[i]);
                    }
                    ret.changed = false;
                }
            }
            return ret;
        } catch (SQLException ex) {
            log.error("加載寵物訊息出錯", ex);
            return null;
        }
    }

    public static final MaplePet createPet(final int itemid) {
        return createPet(itemid, -1);
    }

    public static MaplePet createPet(int itemid, int uniqueid) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        return createPet(itemid, ii.getName(itemid), 1, 0, 100, uniqueid, ii.getLimitedLife(itemid), ii.getPetFlagInfo(itemid), -1);
    }

    public static MaplePet createPet(int itemid, String name, int level, int closeness, int fullness, int uniqueid, int secondsLeft, short flag, int color) {
        if (uniqueid <= -1) { //wah
            uniqueid = MapleInventoryIdentifier.getInstance();
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement pse = con.prepareStatement("INSERT INTO pets (petid, name, level, closeness, fullness, seconds, flags, color) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                pse.setInt(1, uniqueid);
                pse.setString(2, name);
                pse.setByte(3, (byte) level);
                pse.setShort(4, (short) closeness);
                pse.setByte(5, (byte) fullness);
                pse.setInt(6, secondsLeft);
                pse.setShort(7, flag);
                pse.setInt(8, color);
                pse.executeUpdate();
            }
        } catch (SQLException ex) {
            log.error("創建寵物訊息出錯", ex);
            return null;
        }
        MaplePet pet = new MaplePet(itemid, uniqueid);
        pet.setName(name);
        pet.setLevel(level);
        pet.setFullness(fullness);
        pet.setCloseness(closeness);
        pet.setFlags(flag);
        pet.setSecondsLeft(secondsLeft);

        return pet;
    }

    public void saveToDb() {
        if (!changed) {
            return;
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE pets SET name = ?, level = ?, closeness = ?, fullness = ?, seconds = ?, flags = ?, excluded = ?, color = ?, addSkill = ? WHERE petid = ?")) {
                ps.setString(1, name); // 寵物名字
                ps.setByte(2, getLevel()); // 寵物等級
                ps.setShort(3, closeness); // Set Closeness
                ps.setByte(4, fullness); // Set Fullness
                ps.setInt(5, secondsLeft);
                ps.setShort(6, flags);
                StringBuilder list = new StringBuilder();
                for (int anExcluded : excluded) {
                    list.append(anExcluded);
                    list.append(",");
                }
                String newlist = list.toString();
                ps.setString(7, newlist.substring(0, newlist.length() - 1)); //寵物撿取過濾
                ps.setInt(8, color);
                ps.setInt(9, addSkill);
                ps.setInt(10, uniqueid); // Set ID
                ps.executeUpdate(); // Execute statement
            } // 寵物名字
            changed = false;
        } catch (SQLException ex) {
            log.error("儲存寵物訊息出錯", ex);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.changed = true;
    }

    public boolean getSummoned() {
        return summoned > 0;
    }

    public void setSummoned(int summoned) {
        this.summoned = (byte) summoned;
    }

    public byte getSummonedValue() {
        return summoned;
    }

    public short getInventoryPosition() {
        return inventoryPosition;
    }

    public void setInventoryPosition(short inventorypos) {
        this.inventoryPosition = inventorypos;
    }

    public int getUniqueId() {
        return uniqueid;
    }

    public short getCloseness() {
        return closeness;
    }

    public void setCloseness(int closeness) {
        closeness = Math.max(1, closeness);
        this.closeness = (short) closeness;
        this.changed = true;
    }

    public byte getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = (byte) Math.max(1, level);
        this.changed = true;
    }

    public byte getFullness() {
        return fullness;
    }

    public void setFullness(int fullness) {
        this.fullness = (byte) fullness;
        this.changed = true;
    }

    public short getFlags() {
        return flags;
    }

    public void setFlags(int fffh) {
        this.flags = (short) fffh;
        this.changed = true;
    }

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

    public byte getType() {
        return 3;
    }

    public int getStance() {
        return stance;
    }

    public void setStance(int stance) {
        this.stance = stance;
    }

    public int getPetItemId() {
        return petitemid;
    }

    public boolean canConsume(int itemId) {
        MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
        for (int petId : mii.getItemEffect(itemId).getPetsCanConsume()) {
            if (petId == petitemid) {
                return true;
            }
        }
        return false;
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

    public int getSecondsLeft() {
        return secondsLeft;
    }

    public void setSecondsLeft(int sl) {
        this.secondsLeft = sl;
        this.changed = true;
    }

    public int[] getBuffSkills() {
        return buffIds;
    }

    public int getBuffSkill(int index) {
        return buffIds[index];
    }

    public void setBuffSkill(int index, int id) {
        buffIds[index] = id;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        this.changed = true;
    }

    public int getAddSkill() {
        return addSkill;
    }

    public void setAddSkill(int addSkill) {
        this.addSkill = addSkill;
        this.changed = true;
    }

    /*
     * 寵物撿取過濾功能
     */
    public void clearExcluded() {
        for (int i = 0; i < excluded.length; i++) {
            this.excluded[i] = 0;
        }
        this.changed = true;
    }

    public List<Integer> getExcluded() {
        List<Integer> list = new ArrayList<>();
        for (int anExcluded : excluded) {
            if (anExcluded > 0 && PetFlag.PET_IGNORE_PICKUP.check(flags)) {
                list.add(anExcluded);
            }
        }
        return list;
    }

    public void addExcluded(int i, int itemId) {
        if (i < excluded.length) {
            this.excluded[i] = itemId;
            this.changed = true;
        }
    }

    /*
     * 角色是否開啟關閉和激活寵物撿取道具功能
     */
    public boolean isCanPickup() {
        return canPickup;
    }

    public void setCanPickup(boolean can) {
        this.canPickup = can;
    }
}
