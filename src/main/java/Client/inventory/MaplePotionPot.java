/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.inventory;

import Database.DatabaseLoader.DatabaseConnectionEx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author PlayDK
 * 藥劑罐系統
 */
public class MaplePotionPot {

    private final int chrId; //角色ID
    private final int itemId; //道具ID
    private final int maxlimit = 10000000; //最大上限
    private final long startTime; //開始時間
    private final long endTime; //結束時間
    private int hp; //當前儲存的hp
    private int mp; //當前儲存的hp
    private int maxValue; //當前容量上限

    public MaplePotionPot(int chrId, int itemId, int hp, int mp, int maxValue, long start, long end) {
        this.chrId = chrId;
        this.itemId = itemId;
        this.hp = hp;
        this.mp = mp;
        this.maxValue = maxValue;
        this.startTime = start;
        this.endTime = end;
    }

    /*
     * 生成1個藥劑罐的數據
     */
    public static MaplePotionPot createPotionPot(int chrId, int itemId, long endTime) {
        if (itemId != 5820000) {
            return null;
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO `character_potionpots` (`characterid`, `itemId`, `hp`, `mp`, `maxValue`, `startDate`, `endDate`) VALUES (?, ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, chrId);
            ps.setInt(2, itemId);
            ps.setInt(3, 0);
            ps.setInt(4, 0);
            ps.setInt(5, 1000000);
            ps.setLong(6, System.currentTimeMillis());
            ps.setLong(7, endTime);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            System.err.println("創建藥劑罐信息出錯 " + ex);
            return null;
        }
        return new MaplePotionPot(chrId, itemId, 0, 0, 1000000, System.currentTimeMillis(), endTime);
    }

    /*
     * 保存藥劑罐數據
     */
    public void saveToDb(Connection con) {
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE `character_potionpots` SET `hp` = ?, `mp` = ?, `maxValue` = ? WHERE `characterid` = ?");
            ps.setInt(1, hp);
            ps.setInt(2, mp);
            ps.setInt(3, maxValue);
            ps.setInt(4, chrId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            System.err.println("保存藥劑罐信息出錯" + ex);
        }
    }

    /*
     * 角色ID
     */
    public int getChrId() {
        return chrId;
    }

    /*
     * 道具ID為： 5820000
     */
    public int getItmeId() {
        return itemId;
    }

    /*
     * 當前儲存的hp數量
     */
    public int getHp() {
        if (hp < 0) {
            hp = 0;
        } else if (hp > maxValue) {
            hp = maxValue;
        }
        return hp;
    }

    public void setHp(int value) {
        hp = value;
    }

    public void addHp(int value) {
        if (value <= 0) {
            return;
        }
        hp += value;
        if (hp > maxValue) {
            hp = maxValue;
        }
    }

    /*
     * 當前儲存的Mp數量
     */
    public int getMp() {
        if (mp < 0) {
            mp = 0;
        } else if (mp > maxValue) {
            mp = maxValue;
        }
        return mp;
    }

    public void setMp(int value) {
        mp = value;
    }

    public void addMp(int value) {
        if (value <= 0) {
            return;
        }
        mp += value;
        if (mp > maxValue) {
            mp = maxValue;
        }
    }

    /*
     * 默認的上限為: 1000000
     * 每次增加上限: 1000000
     * 最大容量上限: 10000000
     */
    public int getMaxValue() {
        if (maxValue > maxlimit) {
            maxValue = maxlimit;
        }
        return maxValue;
    }

    public void setMaxValue(int value) {
        maxValue = value;
    }

    public boolean addMaxValue() {
        if (maxValue + 1000000 > maxlimit) {
            return false;
        }
        maxValue += 1000000;
        return true;
    }

    /*
     * 開始時間 其實就是當前時間
     */
    public long getStartDate() {
        return startTime;
    }

    /*
     * 現在這個道具沒有時間限制
     */
    public long getEndDate() {
        return endTime;
    }

    /*
     * 是否已經充滿
     */
    public boolean isFullHp() {
        return getHp() >= getMaxValue();
    }

    public boolean isFullMp() {
        return getMp() >= getMaxValue();
    }

    public boolean isFull(int addHp, int addMp) {
        if (addHp > 0 && addMp > 0) {
            return isFullHp() && isFullMp();
        } else if (addHp > 0 && addMp == 0) {
            return isFullHp();
        } else if (addHp == 0 && addMp >= 0) {
            return isFullMp();
        }
        return true;
    }
}
