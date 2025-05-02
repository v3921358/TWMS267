package Server.channel.handler;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.SecondaryStat;
import Client.SecondaryStatValueHolder;
import Client.skills.Skill;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Config.constants.SkillConstants;
import Config.constants.enums.UserChatMessageType;
import Config.constants.skills.爆拳槍神;
import Config.constants.skills.狂豹獵人;
import Net.server.buffs.MapleStatEffect;
import Packet.MaplePacketCreator;
import tools.data.MaplePacketReader;

import java.awt.*;



public class UserSkillUseHandler {

    public static void userSkillUseRequest(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || chr.hasBlockedInventory() || chr.getMap() == null || slea.available() < 9) {
            return;
        }
        chr.updateTick(slea.readInt());
        int skillid = slea.readInt();
        if (SkillFactory.isBlockedSkill(skillid)) {
            chr.dropMessage(5, "由於<" + SkillFactory.getSkillName(skillid) + ">技能數據異常,暫未開放使用.");
            c.sendEnableActions();
            return;
        }
        if (SkillConstants.isZeroSkill(skillid)) {
            slea.readByte(); //神之子要多1位
        }
        slea.skip(4);
        Skill skill = SkillFactory.getSkill(skillid);
        if (chr.isDebug()) {
            chr.dropMessage(1, "[Skill Use] SkillID:" + skillid + " skill is null!");
        }
        int skillLevel = slea.readInt(); /// <---------------------------
        int linkedAttackSkill = SkillConstants.getLinkedAttackSkill(skillid);
        int checkSkillLevel = chr.getTotalSkillLevel(linkedAttackSkill);
        MapleStatEffect effect = chr.inPVP() ? skill.getPVPEffect(checkSkillLevel) : skill.getEffect(checkSkillLevel);
        if (effect == null) {
            SecondaryStatValueHolder holder = chr.getBuffStatValueHolder(linkedAttackSkill);
            if (holder != null && holder.effect != null) {
                effect = holder.effect;
                skillLevel = holder.effect.getLevel();
            }
        }
        Point pos = null;
        AttackInfo ai = new AttackInfo();
        ai.skillposition = pos;
        DamageParse.calcAttackPosition(slea, chr, ai);

        pos = ai.skillposition;
        boolean passive = false;
        int unk = slea.readShort();
        int plus = slea.readShort();
        slea.skip(3);

        if (chr.isDebug()) {
            chr.dropSpouseMessage(UserChatMessageType.粉, "[Skill Use] Effect:" + SkillFactory.getSkillName(skillid) + "(" + skillid + ") Level: " + skillLevel);
            if (effect == null) {
                chr.dropMessage(1, "[Skill Use] SkillID:" + skillid + " Lv:" + skillLevel + " Effect is null!");
            }
            if (linkedAttackSkill != skillid) {
                chr.dropSpouseMessage(UserChatMessageType.粉, "[Skill Use] Linked SkillID:" + SkillFactory.getSkillName(linkedAttackSkill) + "(" + linkedAttackSkill + ") Level: " + checkSkillLevel);
            }
        }
        if (effect.getCooldown(chr) > 0 && !chr.isGm() && chr.isSkillCooling(linkedAttackSkill)) {
            chr.dropMessage(5, "還無法使用技能。");
            return;
        }
        if (skillid != 爆拳槍神.王之子 && skillid != 爆拳槍神.擺動 && skillid != 151121003) {
            c.sendEnableActions();
        }
        AbstractSkillHandler handler = effect.getSkillHandler();
        int handleRes = -1;
        if (handler != null) {
            SkillClassApplier applier = new SkillClassApplier();
            applier.effect = effect;
            applier.passive = passive;
            applier.pos = pos;
            applier.ai = ai;
            applier.unk = unk;
            applier.plus = plus;
            handleRes = handler.onSkillUse(slea, c, chr, applier);
            if (handleRes == 0) {
                return;
            } else if (handleRes == 1) {
                effect = applier.effect;
                passive = applier.passive;
                pos = applier.pos;
                ai = applier.ai;
            }
        }
        if ((slea.available() == 4 || slea.available() == 5) && skillid != 狂豹獵人.捕獲) {
            pos = slea.readPos();
        } else if (slea.available() == 12 || slea.available() == 13) {
            slea.readInt();
            pos = slea.readPosInt();
        }
        if (handleRes == -1) {
            if (skillid == 80012015) {
                SecondaryStatValueHolder holder;
                if ((holder = chr.getBuffStatValueHolder(SecondaryStat.ErdaStack)) == null || holder.value < 6) {
                    chr.dropMessage(5, "堆疊不足。");
                    return;
                }
            }
        }

        Skill linkedSkill = SkillFactory.getSkill(linkedAttackSkill);
        if (skill.isChargeSkill() || linkedSkill.isChargeSkill()) {
            chr.setKeyDownSkill_Time(0);
            if (skill.isChargeSkill()) {
                MapleStatEffect eff = chr.getSkillEffect(skillid);
                if (eff != null && eff.getCooldown() > 0) {
                    if (SkillConstants.isKeydownSkillCancelGiveCD(skillid) && !chr.isSkillCooling(skillid)) {
                        chr.registerSkillCooldown(chr.getSkillEffect(skillid), true);
                    }
                    if (!chr.isSkillCooling(skillid)) {
                        chr.send(MaplePacketCreator.skillCooltimeSet(skillid, 0));
                    }
                }
            }
            if (linkedSkill.isChargeSkill()) {
                MapleStatEffect eff = chr.getSkillEffect(linkedAttackSkill);
                if (eff != null && eff.getCooldown() > 0) {
                    if (SkillConstants.isKeydownSkillCancelGiveCD(linkedAttackSkill) && !chr.isSkillCooling(linkedAttackSkill)) {
                        chr.registerSkillCooldown(chr.getSkillEffect(linkedAttackSkill), true);
                    }
                    if (!chr.isSkillCooling(linkedAttackSkill)) {
                        chr.send(MaplePacketCreator.skillCooltimeSet(linkedAttackSkill, 0));
                    }
                }
            }
        }
        // 如果是開關技能，檢查到Buffer存在就執行取消Buffer
        if (SkillConstants.isOnOffSkill(skillid) && chr.getBuffStatValueHolder(skillid) != null) {
            chr.cancelEffect(effect, false, -1);
            return;
        }
        if (effect != null) {
            effect.applyTo(chr, pos, passive);
        }
        if (passive) {
        }
}
}
