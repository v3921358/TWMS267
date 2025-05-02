package Net.server.life;

import java.util.LinkedHashSet;
import java.util.Set;

public class MonsterDropEntry {

    public int id, itemId, chance, minimum, maximum, questid, period;
    public Set<Integer> channels = new LinkedHashSet<>();
    public String addFrom;
    public boolean onlySelf;

    public MonsterDropEntry(int id, int itemId, int chance, int Minimum, int Maximum, int questid, boolean onlySelf, int period, String addFrom) {
        this.id = id;
        this.itemId = itemId;
        this.chance = chance;
        this.questid = questid;
        this.minimum = Minimum;
        this.maximum = Maximum;
        this.onlySelf = onlySelf;
        this.period = period;
        this.addFrom = addFrom;
    }
}
