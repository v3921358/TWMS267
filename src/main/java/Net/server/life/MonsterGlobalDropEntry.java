package Net.server.life;

import java.util.LinkedHashSet;
import java.util.Set;

public class MonsterGlobalDropEntry {

    public byte dropType;
    public int id, itemId, chance, Minimum, Maximum, continent, questid, minMobLevel, maxMobLevel, period;
    public boolean onlySelf;
    public Set<Integer> channels = new LinkedHashSet<>();
    public String addFrom;

    public MonsterGlobalDropEntry(int id, int itemId, int chance, int continent, byte dropType, int Minimum, int Maximum, int questid, int minMobLevel, int maxMobLevel, boolean onlySelf, int period, String addFrom) {
        this.id = id;
        this.itemId = itemId;
        this.chance = chance;
        this.dropType = dropType;
        this.continent = continent;
        this.questid = questid;
        this.Minimum = Minimum;
        this.Maximum = Maximum;
        this.minMobLevel = minMobLevel;
        this.maxMobLevel = maxMobLevel;
        this.onlySelf = onlySelf;
        this.period = period;
        this.addFrom = addFrom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MonsterGlobalDropEntry that = (MonsterGlobalDropEntry) o;

        return itemId == that.itemId;
    }

    @Override
    public int hashCode() {
        return itemId;
    }
}
