package Net.server.collection;

import java.util.HashMap;
import java.util.Map;

public class MobCollectionReward {
    public int recordID;
    public int rewardID;
    public int rewardCount;
    public final Map<Integer, MobCollectionGroup> rewardGroup = new HashMap<>();
}
