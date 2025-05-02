package Client.skills.handler.其他;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.SecondaryStat;
import Client.SecondaryStatValueHolder;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.skills.handler.SkillClassFetcher;
import Client.status.MonsterStatus;
import Config.constants.skills.通用V核心;
import Net.server.MapleStatInfo;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Opcode.Headler.OutHeader;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.lang.reflect.Field;
import java.util.Map;

import static Config.constants.skills.通用V核心.*;

public class 全職通用 extends AbstractSkillHandler {

    public 全職通用() {
        for (Field field : 通用V核心.class.getDeclaredFields()) {
            try {
                skills.add(field.getInt(field.getName()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean containsJob(int jobWithSub) {
        return true;
    }

    @Override
    public int getLinkedSkillID(int skillId) {
        switch (skillId) {
            case 蜘蛛之鏡_1:
            case 蜘蛛之鏡_2:
                return 蜘蛛之鏡;
        }
        return -1;
    }

    @Override
    public int onSkillLoad(Map<SecondaryStat, Integer> statups, Map<MonsterStatus, Integer> monsterStatus, MapleStatEffect effect) {
        switch (effect.getSourceId()) {
            case 實用的時空門:
                statups.put(SecondaryStat.IndieBuffIcon, 1);
                return 1;
            case 蜘蛛之鏡:
                effect.getInfo().put(MapleStatInfo.time, 10 * 5000);
                return 1;
            case 實用的會心之眼:
                effect.setOverTime(true);
                statups.put(SecondaryStat.SharpEyes, (effect.getInfo().get(MapleStatInfo.x) << 8) + effect.getInfo().get(MapleStatInfo.y) + effect.getInfo().get(MapleStatInfo.criticaldamageMax));
                return 1;
            case 實用的神聖之火:
                effect.setOverTime(true);
                statups.put(SecondaryStat.MaxHP, effect.getInfo().get(MapleStatInfo.x));
                statups.put(SecondaryStat.MaxMP, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 實用的戰鬥命令:
                effect.setOverTime(true);
                statups.put(SecondaryStat.CombatOrders, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 實用的進階祝福:
                effect.setOverTime(true);
                statups.clear();
                statups.put(SecondaryStat.AdvancedBless, effect.getInfo().get(MapleStatInfo.x));
                statups.put(SecondaryStat.IndieMHP, effect.getInfo().get(MapleStatInfo.indieMhp));
                statups.put(SecondaryStat.IndieMMP, effect.getInfo().get(MapleStatInfo.indieMmp));
                return 1;
            case 實用的最終極速:
                effect.setOverTime(true);
                statups.put(SecondaryStat.PartyBooster, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 艾爾達斯的新星:
                monsterStatus.put(MonsterStatus.Freeze, 1);
                return 1;
            case 實用的祈禱:
                statups.put(SecondaryStat.HolySymbol, effect.getInfo().get(MapleStatInfo.x));
                return 1;

            /* [ v245 by hertz ] */
            case 冒險家通用.楓之谷世界女神的祝福:
                statups.put(SecondaryStat.IndieMMP, effect.getInfo().get(MapleStatInfo.damR)); /* 總傷害% or 傷害% */
                statups.put(SecondaryStat.BasicStatUp, effect.getInfo().get(MapleStatInfo.x)); /* 屬性增加效果 類 楓葉祝福 */
                return 1;
            case 創造的伊恩:
                effect.getInfo().put(MapleStatInfo.time, 10 * 1000);
                statups.put(SecondaryStat.BlackMageWeaponCreation, 1);
                statups.put(SecondaryStat.IndieNotDamaged, 1);
                return 1;
            case 破壞的雅達巴特:
                effect.getInfo().put(MapleStatInfo.time, 2100000000);
                statups.put(SecondaryStat.BlackMageWeaponDestruction, 1);
                statups.put(SecondaryStat.IndiePMdR, effect.getInfo().get(MapleStatInfo.x));
                return 1;
            case 破壞的雅達巴特_1:
                statups.put(SecondaryStat.BlackMageDestroy, 1);
                return 1;
            case 烈陽印記:
                effect.getInfo().put(MapleStatInfo.time, 10 * 1000);
                return 1;
        }
        return -1;
    }

    @Override
    public int onSkillUse(MaplePacketReader slea, MapleClient c, MapleCharacter chr, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 瞬移) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeOpcode(OutHeader.LP_RandomTeleportKey);
            mplew.write(Randomizer.nextInt(255));
            c.announce(mplew.getPacket());
            return 1;
        }
        return -1;
    }

    @Override
    public int onAfterRegisterEffect(MapleCharacter applyfrom, MapleCharacter applyto, SkillClassApplier applier) {
        if (applier.effect.getSourceId() == 實用的祈禱) {
            SecondaryStatValueHolder mbsvh = applyto.getBuffStatValueHolder(SecondaryStat.HolySymbol, applier.effect.getSourceId());
            if (mbsvh != null) {
                mbsvh.DropRate = applier.effect.getV();
            }
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
