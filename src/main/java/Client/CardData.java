/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.Serializable;

/**
 * 角色卡系統
 *
 * @author PlayDK
 */
public class CardData implements Serializable {

    private static final long serialVersionUID = 2550550428979893978L;
    public final int chrId;
    public final short job;
    public final int level;

    public CardData(int cid, int level, short job) {
        this.chrId = cid;
        this.level = level;
        this.job = job;
    }

    @Override
    public String toString() {
        return "角色ID: " + chrId + " 職業ID: " + job + " 等級: " + level;
    }
}
