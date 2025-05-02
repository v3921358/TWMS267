package Client.hexa;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class HexaStatCoreEntry {
    private final int type;
    private final int maxLevel;
    private final String stat;
    private Map<Integer, Integer> levelAddStat = new HashMap<>();

    public HexaStatCoreEntry(int type, int maxLevel, String stat, Map<Integer, Integer> levelAddStat) {
        this.type = type;
        this.maxLevel = maxLevel;
        this.stat = stat;
        this.levelAddStat = levelAddStat;
    }
}
