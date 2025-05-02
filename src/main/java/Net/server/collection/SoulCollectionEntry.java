package Net.server.collection;

import java.util.HashMap;
import java.util.Map;

public class SoulCollectionEntry {
    private int soulSkill;
    private int soulSkillH;
    private final Map<Integer, Integer> items = new HashMap<>();

    public Map<Integer, Integer> getItems() {
        return items;
    }

    public int getSoulSkill() {
        return soulSkill;
    }

    public void setSoulSkill(int soulSkill) {
        this.soulSkill = soulSkill;
    }

    public int getSoulSkillH() {
        return soulSkillH;
    }

    public void setSoulSkillH(int soulSkillH) {
        this.soulSkillH = soulSkillH;
    }
}
