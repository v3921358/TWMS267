package Client.skills.handler.曉之陣;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.skills.handler.SkillClassFetcher;
import Config.constants.JobConstants;
import Config.constants.skills.通用V核心.曉之陣通用;
import Net.server.life.MapleMonster;
import tools.Randomizer;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.lang.reflect.Field;

import static Config.constants.skills.通用V核心.曉之陣通用.*;

public class 曉之陣 extends AbstractSkillHandler {

    public 曉之陣() {
        for (Field field : 曉之陣通用.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean containsJob(int jobWithSub) {
        return JobConstants.is曉の陣(jobWithSub);
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 集結曉之陣_上杉謙信:
            case 集結曉之陣_安倍晴明:
            case 集結曉之陣_菖蒲:
            case 集結曉之陣_武田信玄: {
                return 集結曉之陣;
            }
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 集結曉之陣) {
            final int[] array6 = new int[]{集結曉之陣_上杉謙信, 集結曉之陣_安倍晴明, 集結曉之陣_菖蒲, 集結曉之陣_武田信玄};
            final int id1 = 集結曉之陣_上杉謙信 + Randomizer.nextInt(4);
            for (int l = 集結曉之陣_上杉謙信; l <= 集結曉之陣_武田信玄; ++l) {
                chr.dispelEffect(l);
            }
            int id2;
            while ((id2 = 集結曉之陣_上杉謙信 + Randomizer.nextInt(4)) == id1) {
            }
            applier.effect = chr.getSkillEffect(id1);
            chr.getSkillEffect(id2).applyTo(chr, new Point(chr.getPosition().x + 50, chr.getPosition().y));
            return 1;
        }
        return -1;
    }

    @Override
    public int onAttack(final MapleCharacter player, final MapleMonster monster, SkillClassApplier applier) {
        AbstractSkillHandler holder = SkillClassFetcher.getHandlerByJob(player.getJobWithSub());
        if (holder == this) {
            return -1;
        }
        return holder.onAttack(player, monster, applier);
    }

    @Override
    public int onApplyMonsterEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        AbstractSkillHandler holder = SkillClassFetcher.getHandlerByJob(applyfrom.getJobWithSub());
        if (holder == this) {
            return -1;
        }
        return holder.onApplyMonsterEffect(applyfrom, applyto, applier);
    }

    @Override
    public int onApplyAttackEffect(MapleCharacter applyfrom, MapleMonster applyto, SkillClassApplier applier) {
        AbstractSkillHandler holder = SkillClassFetcher.getHandlerByJob(applyfrom.getJobWithSub());
        if (holder == this) {
            return -1;
        }
        return holder.onApplyAttackEffect(applyfrom, applyto, applier);
    }

    @Override
    public int onAfterAttack(MapleCharacter player, SkillClassApplier applier) {
        AbstractSkillHandler holder = SkillClassFetcher.getHandlerByJob(player.getJobWithSub());
        if (holder == this) {
            return -1;
        }
        return holder.onAfterAttack(player, applier);
    }
}
