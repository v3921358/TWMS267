package Net.server.collection;

import tools.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobCollectionRecord {
    public int recordID;
    public final List<Pair<Integer, Integer>> rewards = new ArrayList<>();
    public final Map<Integer, MobCollectionReward> mobCollectionRewards = new HashMap<>();
}
