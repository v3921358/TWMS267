package SwordieX.client.character.skills;

import Net.server.MapleStatInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class Skill {
    private final int id;
    private boolean invisible;
    private int masterLevel;
    private int fixLevel;
    private boolean notRemoved;
    private boolean canNotStealableSkill;
    private boolean isPetAutoBuff;
    private int hyper;
    private String elemAttr = "";
    private Map<MapleStatInfo, String> common = new HashMap<>();
    private Map<MapleStatInfo, String> pVPcommon = new HashMap<>();
    private Map<String, String> info = new HashMap<>();
    private Map<String, String> info2 = new HashMap<>();
    private boolean psd = false;
    private Set<Integer> psdSkills = new HashSet<>();

    public Skill(int id) {
        this.id = id;
    }
}
