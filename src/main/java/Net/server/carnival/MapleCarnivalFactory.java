package Net.server.carnival;

import Net.server.life.MobSkill;
import Net.server.life.MobSkillFactory;
import Plugin.provider.MapleData;
import Plugin.provider.MapleDataProvider;
import Plugin.provider.MapleDataProviderFactory;
import Plugin.provider.MapleDataTool;

import java.util.HashMap;
import java.util.Map;

public class MapleCarnivalFactory {

    private final static MapleCarnivalFactory instance = new MapleCarnivalFactory();
    private final Map<Integer, MCSkill> skills = new HashMap<>();
    private final Map<Integer, MCSkill> guardians = new HashMap<>();
    private final MapleDataProvider dataRoot = MapleDataProviderFactory.getSkill();

    public MapleCarnivalFactory() {
        //whoosh
        initialize();
    }

    public static MapleCarnivalFactory getInstance() {
        return instance;
    }

    private void initialize() {
        if (!skills.isEmpty()) {
            return;
        }
       for (MapleData z : dataRoot.getData("MCSkill.img")) {
           new MCSkill(MapleDataTool.getInt("spendCP", z, 0), MapleDataTool.getInt("mobSkillID", z, 0), MapleDataTool.getInt("level", z, 0), MapleDataTool.getInt("target", z, 1) > 1);
       }
       for (MapleData z : dataRoot.getData("MCGuardian.img")) {
           new MCSkill(MapleDataTool.getInt("spendCP", z, 0), MapleDataTool.getInt("mobSkillID", z, 0), MapleDataTool.getInt("level", z, 0), true);
       }
    }

    public MCSkill getSkill(final int id) {
        return skills.get(id);
    }

    public MCSkill getGuardian(final int id) {
        return guardians.get(id);
    }

    public static class MCSkill {

        public final int cpLoss;
        public final int skillid;
        public final int level;
        public final boolean targetsAll;

        public MCSkill(int _cpLoss, int _skillid, int _level, boolean _targetsAll) {
            cpLoss = _cpLoss;
            skillid = _skillid;
            level = _level;
            targetsAll = _targetsAll;
        }

        public MobSkill getSkill() {
            return MobSkillFactory.getMobSkill(skillid, 1); //level?
        }

    }
}
