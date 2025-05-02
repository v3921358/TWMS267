package Client.stat;

import java.io.Serializable;


public class MapleHyperStats implements Serializable {
    private int position;
    private int skillid;
    private int skilllevel;

    public MapleHyperStats(int pos, int skill, int level) {
        this.position = pos;
        this.skillid = skill;
        this.skilllevel = level;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getSkillid() {
        return skillid;
    }

    public void setSkillid(int skill) {
        this.skillid = skill;
    }

    public int getSkillLevel() {
        return skilllevel;
    }

    public void setSkillLevel(int level) {
        this.skilllevel = level;
    }
}
