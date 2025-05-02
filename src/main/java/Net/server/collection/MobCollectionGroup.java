package Net.server.collection;

import java.util.HashMap;
import java.util.Map;

public class MobCollectionGroup {
    public int recordID;
    public int rewardID;
    public int exploraionCycle;
    public int exploraionReward;
    public final Map<Integer, MonsterCollection> mobCollections = new HashMap<>();
}
