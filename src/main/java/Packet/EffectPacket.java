/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Packet;

import Client.MapleCharacter;
import Client.skills.KSPsychicSkillEntry;
import Config.constants.skills.*;
import Config.constants.skills.冒險家_技能群組.type_劍士.劍士;
import Config.constants.skills.冒險家_技能群組.type_劍士.黑騎士;
import Config.constants.skills.冒險家_技能群組.影武者;
import Config.constants.skills.冒險家_技能群組.暗影神偷;
import Config.constants.skills.冒險家_技能群組.槍神;
import Config.constants.skills.冒險家_技能群組.箭神;
import Config.constants.skills.皇家騎士團_技能群組.暗夜行者;
import Config.constants.skills.皇家騎士團_技能群組.烈焰巫師;
import Config.constants.skills.皇家騎士團_技能群組.閃雷悍將;
import Opcode.Headler.OutHeader;
import Opcode.Opcode.EffectOpcode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * @author admin
 */
public class EffectPacket {

    private static final Logger log = LoggerFactory.getLogger(EffectPacket.class);

    public static byte[] encodeUserEffectLocal(int skillid, EffectOpcode effect, int playerLevel, int skillLevel) {
        return encodeUserEffectLocal(skillid, effect, playerLevel, skillLevel, (byte) 0x04);
    }

    public static byte[] encodeUserEffectLocal(int skillid, EffectOpcode effect, int playerLevel, int skillLevel, byte direction) {
        return encodeUserEffect(null, skillid, effect, playerLevel, skillLevel, direction);
    }

    public static byte[] onUserEffectRemote(MapleCharacter chr, int skillid, EffectOpcode effect, int playerLevel, int skillLevel) {
        return encodeUserEffect(chr, skillid, effect, playerLevel, skillLevel, (byte) 0x04);
    }

    public static byte[] encodeUserEffect(MapleCharacter chr, int skillid, EffectOpcode effect, int playerLevel, int skillLevel, byte direction) {
        if (EffectOpcode.UserEffect_SkillUse == effect) {
            return showBuffEffect(chr, chr != null, skillid, skillLevel, playerLevel, new Point(0, 0));
        }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (chr == null) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chr.getId());
        }
        mplew.write(1);
        if (effect == EffectOpcode.UserEffect_SkillUseBySummoned) {
            mplew.writeInt(0);
        }
        mplew.writeInt(skillid);
        switch (skillid) {
            case 80003224: // 天賦
                mplew.writeInt(246);
                mplew.writeInt(1);
                mplew.write(1);
                break;
            case 重砲指揮官.精準轟炸_2:
                mplew.write(1);
                break;
            case 天使破壞者.超級超新星:
                if (chr != null) {
                    mplew.writeInt(chr.getPosition().x);
                    mplew.writeInt(chr.getPosition().y);
                } else {
                    mplew.writeLong(0);
                }
                mplew.write(1);
                break;
            case 黑騎士.轉生:
            case 龍魔導士.龍之怒: {
                mplew.write(0);
                break;
            }
            case 影武者.隱_鎖鏈地獄: {
                mplew.write(0);
                mplew.writeInt(0);
                break;
            }
            case 狂狼勇士.挑飛_1: {
                mplew.write(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
            }
            case 狂豹獵人.捕獲: {
                mplew.writeInt(2);
                mplew.writeInt(1);
                mplew.write(0);
                break;
            }
        }
        if (skillid != 閃雷悍將.閃光 && skillid != 隱月.縮地 && skillid != 暗影神偷.黑暗瞬影) {
            switch (skillid) {
                case 暗影神偷.暗影霧殺:
                case 天使破壞者.超級超新星: {
                    if (chr != null) {
                        mplew.writeInt(chr.getPosition().x);
                        mplew.writeInt(chr.getPosition().y);
                    } else {
                        mplew.writeLong(0);
                    }
                    break;
                }
                case 爆拳槍神.彈丸填裝:
                case 爆拳槍神.旋轉加農砲:
                case 爆拳槍神.王之子_1:
                case 爆拳槍神.王之子:
                case 爆拳槍神.錘之碎擊:
                case 爆拳槍神.錘之碎擊_1:
                case 爆拳槍神.擺動:
                case 爆拳槍神.擺動_1: {
                    mplew.writeInt(0);
                    break;
                }
                case 幻影俠盜.鬼牌_2:
                case 幻影俠盜.鬼牌_3:
                case 幻影俠盜.鬼牌_4:
                case 幻影俠盜.鬼牌_5:
                case 幻影俠盜.鬼牌_6: {
                    mplew.writeInt(0);
                }
            }
        }
        if (chr == null && skillid == 狂豹獵人.獵人的呼喚) {
            mplew.write(0);
            mplew.writeShort(0);
            mplew.writeShort(0);
        }
        mplew.writeInt(0);
        if (skillid == 400041087) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (skillid == 3341503) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (skillid == 64001009) {
            mplew.write(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        mplew.writeInt(playerLevel);
        mplew.writeInt(skillLevel);
        if (direction != 3) {
            mplew.write(direction);
        }
        return mplew.getPacket();
    }

    public static byte[] showBuffEffect(MapleCharacter chr, boolean other, int skillId, int skillLevel, int n3, Point pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (other) {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chr.getId());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        }
        mplew.write(EffectOpcode.UserEffect_SkillUse.getValue());
        mplew.writeInt(skillId);
        if (chr != null) {
            mplew.write(chr.getLevel());
        } else {
            mplew.write(0);
        }
        mplew.writeInt(skillLevel);
        switch (skillId) {
            case 龍魔導士.龍之怒:
                mplew.write(0);
                break;
            case 黑騎士.轉生:
                mplew.write(0);
                break;
            case 影武者.隱_鎖鏈地獄:
                mplew.write(0);
                mplew.writeInt(0);
                break;
            case 暗影神偷.楓幣炸彈:
                break;
            case 虎影.芭蕉風_虛實_2:
                break;
            case 卡蒂娜.鏈之藝術_追擊:
            case 卡蒂娜.鏈之藝術_追擊_向上發射:
            case 卡蒂娜.鏈之藝術_追擊_向下發射:
                mplew.write(0);
                break;
            case 卡蒂娜.鏈之藝術_追擊_向前攻擊:
            case 卡蒂娜.鏈之藝術_追擊_向上攻擊:
            case 卡蒂娜.鏈之藝術_追擊_向下攻擊:
            case 卡蒂娜.鏈之藝術_追擊_1: {
                mplew.writeBool(chr != null && chr.isFacingLeft());
                mplew.writeInt(n3);
                if (pos == null) {
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                } else {
                    mplew.writeInt(pos.x);
                    mplew.writeInt(pos.y);
                }
                break;
            }
            case 機甲戰神.火箭推進器:
                break;
            case 91001020:
            case 91001017:
                break;
            case 狂豹獵人.狂獸附體:
                break;
            case 狂豹獵人.獵人的呼喚: {
                mplew.write(0);
                mplew.writeShort(0);
                mplew.writeShort(0);
                break;
            }
            case 狂豹獵人.捕獲: {
                mplew.write(0);
                break;
            }
            case 凱撒.縱向連接:
            case 天使破壞者.魔法起重機:
            case 通用V核心.連接繩索: {
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
            }
            case 暗影神偷.黑暗瞬影:
            case 閃雷悍將.閃光:
            case 夜光.星光順移:
            case 隱月.縮地:
            case 伊利恩.水晶傳送點:
            case 暗影神偷.黑影切斷_1:
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
            case 暗影神偷.暗影霧殺:
            case 天使破壞者.超級超新星:
                if (chr == null) {
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                } else {
                    mplew.writeInt(chr.getPosition().x);
                    mplew.writeInt(chr.getPosition().y);
                }
                break;
            case 烈焰巫師.火步行_JUMP_STAGE_2:
            case 烈焰巫師.火步行_JUMP:
            case 80001851:
            case 凱內西斯.心靈填充:
                break;
            case 爆拳槍神.彈丸填裝:
            case 爆拳槍神.旋轉加農砲:
            case 爆拳槍神.王之子_1:
            case 爆拳槍神.王之子:
            case 爆拳槍神.錘之碎擊:
            case 爆拳槍神.錘之碎擊_1:
            case 爆拳槍神.擺動:
            case 爆拳槍神.擺動_1:
                mplew.writeInt(0);
                break;
            case 暗夜行者.影之槍_2:
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
            case 幻影俠盜.命運鬼牌:
                mplew.writeInt(0);
                break;
            case 幻影俠盜.鬼牌_2:
            case 幻影俠盜.鬼牌_3:
            case 幻影俠盜.鬼牌_4:
            case 幻影俠盜.鬼牌_5:
            case 幻影俠盜.鬼牌_6:
                mplew.writeInt(0);
                break;
            case 通用V核心.法師通用.超載魔力:
                break;
            case 卡蒂娜.鏈之藝術_護佑_1:
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
            case 152111005:
            case 152111006:
                break;
            case 80002393:
            case 80002394:
            case 80002395:
            case 80002421:
                mplew.writeInt(0);
                break;
            case 開拓者.基本轉移:
            case 開拓者.基本轉移4轉:
                mplew.writeInt(0);
                break;
            case 精靈遊俠.元素騎士:
            case 精靈遊俠.元素騎士1:
            case 精靈遊俠.元素騎士2:
                break;
            case 虎影.魔封葫蘆符_1:
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
            case 虎影.仙技_降臨怪力亂神_1:
                break;
            case 劍士.無形的信任:
                break;
            case 凱內西斯.心靈龍捲風_1:
            case 凱內西斯.心靈龍捲風_2:
            case 凱內西斯.心靈龍捲風_3:
            case 開拓者.遺跡解放_爆破_1:
            case 皮卡啾.皮卡啾的品格_迷你啾攻擊:
                mplew.write(true);
                if (chr == null) {
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                } else {
                    mplew.writeInt((int) (chr.getPosition().getX() + (chr.isFacingLeft() ? -658 : 658)));
                    mplew.writeInt((int) (chr.getPosition().getY() - 150));
                }
                break;
            case 亞克.無限飢餓的猛獸:
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
            case 槍神.死亡板機:
            case 槍神.死亡板機_1:
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
            case 凱殷.勾爪繩索:
                mplew.writeBool(chr != null && chr.isFacingLeft());
                mplew.writeInt(n3);
                if (pos == null) {
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                } else {
                    mplew.writeInt(pos.x);
                    mplew.writeInt(pos.y);
                }
                break;
            case 凱殷.暗影步伐:
            case 凱殷.暗影步伐_1:
            case 凱殷.暗影步伐_2:
                mplew.write(true);
                if (chr == null) {
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                } else {
                    mplew.writeInt((int) (chr.getPosition().getX() + (chr.isFacingLeft() ? -658 : 658)));
                    mplew.writeInt((int) (chr.getPosition().getY() - 150));
                }
                break;
            case 80001132:
                mplew.write(0);
                break;
            default: {
                if (skillId == 80011187 || skillId == 80011188) {
                    break;
                }
                boolean result;
                if (skillId > 0) {
                    int jobBySkill = skillId / 10000;
                    if (jobBySkill - 8000 <= 1) {
                        jobBySkill = skillId / 100;
                    }
                    result = jobBySkill != 9500;
                } else {
                    result = skillId - 90000000 < 10000000;
                }
                if (result) {
                    mplew.write(n3);
                }
                break;
            }
        }
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    /**
     * 角色自己看到幸運骰子BUFF效果
     */
    public static byte[] showOwnDiceEffect(int skillid, int effectid, int effectid2, int level) {
        return showDiceEffect(-1, skillid, level, effectid, effectid2, false);
    }

    /**
     * 別人看到的幸運骰子BUFF效果
     */
    public static byte[] showDiceEffect(int chrId, int skillid, int level, int effectid, int effectid2, boolean b) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (chrId == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrId);
        }
        mplew.write(EffectOpcode.UserEffect_SkillAffected_Select.getValue());
        mplew.writeInt(effectid);
        mplew.writeInt(effectid2);
        mplew.writeInt(skillid);
        mplew.write(level);
        mplew.writeBool(b);

        return mplew.getPacket();
    }

    public static byte[] showFieldExpItemConsumed(int exp) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        mplew.write(EffectOpcode.UserEffect_FieldExpItemConsumed.getValue());
        mplew.writeInt(exp);
        return mplew.getPacket();
    }

    /*
     * 裝備道具等級提升
     */
    public static byte[] showItemLevelupEffect() {
        return showSpecialEffect(EffectOpcode.UserEffect_ItemLevelUp);
    }

    /*
     * 顯示給其他玩家看到道具等級提升效果
     */
    public static byte[] showForeignItemLevelupEffect(int chrId) {
        return showForeignEffect(chrId, EffectOpcode.UserEffect_ItemLevelUp);
    }

    /*
     * 顯示給自己看到的特殊效果
     * 0x0A = 使用護身符1次 [1E 02] [0A] [01 00 00 00 00 00]
     * 0x0D = 背後有個天使效果
     * 0x0E = 完成任務效果
     * 0x0F = 回血效果
     * 0x10 = 身上有個光點
     * 0x16 = 道具等級提升效果
     * 0x15 = 頭上有1個氈子 後面為0 = 成功 為 1 = 失敗效果
     * 0x16 = 身上有個光點效果
     * 0x18 = 消耗1個原地復活術，在當前地圖復活了。（剩餘x個） 後面接著是1個 Int
     * 0x1F = 因靈魂石的效果，在當前地圖中復活
     * 0x20 = 顯示掉血傷害多少? 0 = Miss
     * 0x22 = 顯示自己恢復Hp效果
     * 0x2F = 天使破壞者靈魂重生
     */
    public static byte[] showSpecialEffect(EffectOpcode effect) {
        return showForeignEffect(-1, effect);
    }

    public static byte[] showForeignEffect(int chrId, EffectOpcode effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (chrId == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
            mplew.writeInt(chrId);
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrId);
        }
        mplew.write(effect.getValue());

        return mplew.getPacket();
    }

    /*
     * 看到自己恢復Hp效果
     * 好像為 0x0F
     * 下面是恢復12點的例子
     * Recv SHOW_SPECIAL_EFFECT [021E] (4)
     * 1E 02 0F 0C
     * V.119.1 OK
     */
    public static byte[] showOwnHpHealed(int amount) {
        return showHpHealed(-1, amount);
    }

    /*
     * 看到其他角色恢復HP效果
     * V.119.1 OK
     */
    public static byte[] showHpHealed(int chrId, int amount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (chrId == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrId);
        }
        mplew.write(EffectOpcode.UserEffect_IncDecHPRegenEffect.getValue());
        mplew.writeInt(amount);

        return mplew.getPacket();
    }

    public static byte[] showBlessOfDarkness(int chrId, int skillId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (chrId == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrId);
        }
        mplew.write(EffectOpcode.UserEffect_SkillSpecial.getValue());
        mplew.writeInt(skillId);
        if (skillId == 箭神.魔幻箭筒) {
            mplew.write(30);
        }

        return mplew.getPacket();
    }

    /*
     * 顯示使用卡勒塔的許願珍珠的效果
     */
    public static byte[] showOwnEffectUOL(String effect, int time, int itemId) {
        return showEffectUOL(-1, effect, time, itemId);
    }

    /*
     * 顯示別人使用卡勒塔的許願珍珠的效果
     */
    public static byte[] showEffectUOL(int chrId, String effect, int time, int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (chrId == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrId);
        }
        mplew.write(EffectOpcode.UserEffect_EffectUOL.getValue());
        mplew.writeMapleAsciiString(effect);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.writeInt(time);
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    /*
     * 顯示隨機獲得道具效果
     * V.119.1 OK
     */
    public static byte[] showRewardItemAnimation(int itemId, String effect) {
        return showRewardItemAnimation(itemId, effect, -1);
    }

    /*
     * 顯示其他玩家隨機獲得道具效果
     */
    public static byte[] showRewardItemAnimation(int itemId, String effect, int chrId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (chrId == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrId);
        }
        mplew.write(EffectOpcode.UserEffect_LotteryUse.getValue()); //V.119.1 = 0x12
        mplew.writeInt(itemId);
        mplew.write(effect != null && effect.length() > 0 ? 1 : 0);
        if (effect != null && effect.length() > 0) {
            mplew.writeMapleAsciiString(effect);
        }

        return mplew.getPacket();
    }

    public static byte[] playPortalSE() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        mplew.write(EffectOpcode.UserEffect_PlayPortalSE.getValue());

        return mplew.getPacket();
    }

    /*
     * 道具製造
     * V.119.1 OK
     */
    public static byte[] ItemMaker_Success() {
        return ItemMaker_Success_3rdParty(-1);
    }

    public static byte[] ItemMaker_Success_3rdParty(int chrId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (chrId == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrId);
        }
        mplew.write(EffectOpcode.UserEffect_ItemMaker.getValue());
        mplew.writeInt(0); //成功 = 0 失敗 =1

        return mplew.getPacket();
    }

    /*
     * 顯示寵物升級效果
     * V.119.1 OK
     */
    public static byte[] showOwnPetLevelUp(byte index) {
        return showPetLevelUp(-1, index);
    }

    public static byte[] showPetLevelUp(int chrId, byte index) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (chrId == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrId);
        }
        mplew.write(EffectOpcode.UserEffect_Pet.getValue());
        mplew.write(0);
        mplew.writeInt(index);

        return mplew.getPacket();
    }

    /*
     * V.119.1 OK
     */
    public static byte[] showAvatarOriented(String data) {
        return showAvatarOriented(-1, data);
    }

    public static byte[] showAvatarOriented(int chrId, String data) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (chrId == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrId);
        }
        mplew.write(EffectOpcode.UserEffect_AvatarOriented.getValue());
        mplew.writeMapleAsciiString(data);

        return mplew.getPacket();
    }

    public static byte[] showAvatarOrientedRepeat(final boolean b, String s) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        mplew.write(EffectOpcode.UserEffect_AvatarOrientedRepeat.getValue());
        mplew.writeBool(b);
        if (b) {
            mplew.writeMapleAsciiString(s);
            mplew.writeInt(0);
            mplew.writeInt(1);
        }

        return mplew.getPacket();
    }

    /*
     * V.120.1  OK
     */
    public static byte[] playSoundWithMuteBGM(String data) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        mplew.write(EffectOpcode.UserEffect_PlaySoundWithMuteBGM.getValue()); //0x18
        mplew.writeMapleAsciiString(data);

        return mplew.getPacket();
    }

    public static byte[] showReservedEffect(final String data) {
        return showReservedEffect(false, 0, 0, data);
    }

    public static byte[] showReservedEffect(final boolean screenCoord, final int rx, final int ry, final String data) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        mplew.write(EffectOpcode.UserEffect_ReservedEffect.getValue());
        mplew.write(screenCoord);
        mplew.writeInt(rx);
        mplew.writeInt(ry);
        mplew.writeMapleAsciiString(data);

        return mplew.getPacket();
    }

    /*
     * 獲取和丟失裝備的提示 - 2
     * V.119.1 OK
     */
    public static byte[] getShowItemGain(List<Pair<Integer, Integer>> showItems) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        mplew.write(EffectOpcode.UserEffect_Quest.getValue()); //V.119.1修改以前 0x05
        mplew.write(showItems.size());
        for (Pair<Integer, Integer> items : showItems) {
            mplew.writeInt(items.left);
            mplew.writeInt(items.right);
            mplew.writeBool(false); // TMS229
        }

        return mplew.getPacket();
    }

    public static byte[] getShowItemGain(final int itemid, final short amount, final boolean b) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        mplew.write(EffectOpcode.UserEffect_Quest.getValue());
        mplew.write(1);
        mplew.writeInt(itemid);
        mplew.writeInt(amount);
        mplew.write(0);
        return mplew.getPacket();
    }

    /*
     * 顯示尖兵獲得電池
     */
    public static byte[] showOwnXenonPowerOn(String effect) {
        return showXenonPowerOn(-1, effect);
    }

    public static byte[] showXenonPowerOn(int chrId, String effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (chrId == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrId);
        }
        mplew.write(EffectOpcode.UserEffect_UpgradeTombItemUse.getValue());
        mplew.writeMapleAsciiString(effect);

        return mplew.getPacket();
    }

    public static byte[] showHakuSkillUse(int skillType, int cid, int skillLevel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (cid == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(cid);
        }
        mplew.write(EffectOpcode.UserEffect_HakuSkill.getValue());
        mplew.writeShort(0);
        mplew.writeInt(skillType);
        mplew.write(1);
        mplew.writeShort(skillLevel);

        return mplew.getPacket();
    }

    public static byte[] playerDeadConfirm(int type, boolean voice, int value, int autoReviveTime, int reviveDelay, boolean reviveEnd) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_OpenUIOnDead.getValue());
        mplew.writeInt(type);
        mplew.write(voice);
        mplew.writeInt(value);
        mplew.writeInt(0);
        mplew.write(autoReviveTime > 0);
        mplew.writeInt(autoReviveTime);
        mplew.writeInt(reviveDelay);
        mplew.write(reviveEnd);
        return mplew.getPacket();
    }

    public static byte[] ProtectBuffGain(int itemID, int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SetBuffProtector.getValue());
        mplew.writeInt(itemID);
        mplew.write(value);
        return mplew.getPacket();
    }

    public static byte[] getEffectSwitch(int cid, List<Integer> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.EFFECT_SWITCH.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(items.size());
        for (int i : items) {
            mplew.writeInt(i);
        }
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    // 心魂之手 抓取
    public static byte[] showKSPsychicGrab(int cid, int skillid, short skilllevel, List<KSPsychicSkillEntry> ksse, int n1, int n2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.GIVE_KSPSYCHIC.getValue());

        mplew.writeInt(cid);
        mplew.write(1);
        mplew.writeInt(skillid);
        mplew.writeShort(skilllevel);
        mplew.writeInt(n1); // AF 04 00 00 
        mplew.writeInt(n2);
        mplew.writeBool(true);
        for (int i = 0; i < ksse.size(); i++) {
            KSPsychicSkillEntry k = ksse.get(i);
            if (i > 0) {
                mplew.write(1);
            }
            mplew.write(1);
            mplew.writeInt(k.getOid());
            mplew.writeInt(Math.abs(k.getOid()));
            mplew.writeInt(k.getMobOid());
            if (k.getMobOid() != 0) {
                mplew.writeShort(0);
                mplew.writeInt(k.getN5());
                mplew.writeLong(150520);
                mplew.writeLong(150520);
            } else {
                mplew.writeShort(Randomizer.nextInt(19) + 1);
                mplew.writeInt(k.getN5());
                mplew.writeLong(100);
                mplew.writeLong(100);
            }
            mplew.write(1);
            mplew.writeInt(k.getN1());
            mplew.writeInt(k.getN2());
            mplew.writeInt(k.getN3());
            mplew.writeInt(k.getN4());
        }
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] showKSPsychicAttack(int cid, int skillid, short skilllevel, int n1, int n2, byte n3, int n4, int n5, int n6, int n7, int n8, int n9, int n10, int n11) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.ATTACK_KSPSYCHIC.getValue());

        mplew.writeInt(cid);
        mplew.writeInt(skillid);
        mplew.writeShort(skilllevel);
        mplew.writeInt(n1);
        mplew.writeInt(n2);
        mplew.write(n3);
        mplew.writeInt(n4);
        if (n4 != 0) {
            mplew.writeInt(n5);
            mplew.writeInt(n6);
        }
        mplew.writeInt(n7);
        mplew.writeInt(n8);
        mplew.writeInt(n9);
//        mplew.writeInt(n10);
//        if (skillid == 凱內西斯.猛烈心靈2_ || skillid == 凱內西斯.猛烈心靈2_最後一擊 || skillid == 凱內西斯.終極技_心靈射擊) {
//            mplew.writeInt(n10);
//            mplew.writeInt(n11);
//        }
        mplew.writeZeroBytes(20); // 不確定結尾
        return mplew.getPacket();
    }

    public static byte[] showKSPsychicRelease(int cid, int oid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CANCEL_KSPSYCHIC.getValue());

        mplew.writeInt(cid);
        mplew.writeInt(oid);

        return mplew.getPacket();
    }

    public static byte[] showGiveKSUltimate(int chrid, int mode, int type, int oid, int skillid, short skilllevel, int n1, byte n2, short n3, short n4, short n5, int n6, int n7) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.GIVE_KSULTIMATE.getValue());

        mplew.writeInt(chrid);
        mplew.write(1);
        mplew.writeInt(mode);
        mplew.writeInt(type);
        mplew.writeInt(oid);
        mplew.writeInt(skillid);
        mplew.writeShort(skilllevel);
        mplew.writeInt(Math.abs(oid));
        mplew.writeInt(n1);
        mplew.write(n2);
        mplew.writeShort(n3);
        mplew.writeShort(n4);
        mplew.writeShort(n5);
        mplew.writeInt(n6);
        mplew.writeInt(n7);

        return mplew.getPacket();
    }

    public static byte[] showAttackKSUltimate(int oid, int attackcount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_DoActivePsychicArea.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(attackcount);
        return mplew.getPacket();
    }

    public static byte[] showCancelKSUltimate(int chrid, int oid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CANCEL_KSULTIMATE.getValue());

        mplew.writeInt(chrid);
        mplew.writeInt(oid);

        return mplew.getPacket();
    }

    public static byte[] showExpertEffect() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserRequestExJablin.getValue());

        return mplew.getPacket();
    }

    public static byte[] enforceMSG(String a, int id, int delay) {
        MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
        packet.writeShort(OutHeader.LP_WeatherEffectNotice.getValue());
        packet.writeMapleAsciiString(a);
        packet.writeInt(id);
        packet.writeInt(delay);
        packet.write(0);
        return packet.getPacket();
    }

    public static byte[] ShowTimer(MapleCharacter p, int MS) {
        MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
        packet.writeShort(OutHeader.CTX_Event_Field_Timer.getValue());
        packet.write(2);
        packet.writeInt(MS);
        p.send(packet.getPacket());
        return packet.getPacket();
    }

    public static byte[] showCombustionMessage(String text, int milliseconds, int posY) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        mplew.write(EffectOpcode.UserEffect_TextEffect.getValue());
        mplew.writeMapleAsciiString(text);
        mplew.writeInt(50);
        mplew.writeInt(milliseconds);
        mplew.writeInt(4);
        mplew.writeInt(0);
        mplew.writeInt(posY);
        mplew.writeInt(1);
        mplew.writeInt(4);
        mplew.writeInt(2);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeMapleAsciiString("");
        mplew.writeInt(0);
        mplew.write(0);

        return mplew.getPacket();
    }

    //its likely that durability items use this
    public static byte[] showHpHealed_Other(int chrId, int amount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
        mplew.writeInt(chrId);
        mplew.write(EffectOpcode.UserEffect_IncDecHPRegenEffect.getValue());
        mplew.writeInt(amount);

        return mplew.getPacket();
    }

    public static byte[] showBuffItemEffect(int chrID, int skillId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (chrID == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrID);
        }
        mplew.write(EffectOpcode.UserEffect_BuffItemEffect.getValue());
        mplew.writeInt(skillId);
        return mplew.getPacket();
    }

    public static byte[] showSkillAffected(int sourceid, short effectid, int level) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        mplew.write(effectid);
        mplew.writeInt(sourceid);
        mplew.write(level);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] showEffectById(int effectid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        mplew.write(16);
        return mplew.getPacket();
    }

    public static byte[] showMobSkillHit(int chrID, int skillID, int level) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (chrID == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrID);
        }
        mplew.write(EffectOpcode.UserEffect_MobSkillHit.getValue());
        mplew.writeInt(skillID);
        mplew.writeInt(level);
        return mplew.getPacket();
    }

    public static byte[] showRoyalGuardAttack() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
        mplew.writeShort(OutHeader.LP_RoyalGuardAttack.getValue());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] showIncDecHPRegen(int chrID, int hpDiff) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (chrID == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrID);
        }
        mplew.write(EffectOpcode.UserEffect_IncDecHPRegenEffect.getValue());
        mplew.writeInt(hpDiff);
        return mplew.getPacket();
    }

    public static byte[] showFlameWizardFlameWalk(int chrID) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FlameWizardFlameWalkEffect.getValue());
        mplew.writeInt(chrID);
        return mplew.getPacket();
    }

    public static byte[] showResetOnStateForOnOffSkill(int chrID) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (chrID == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrID);
        }
        mplew.write(EffectOpcode.UserEffect_ResetOnStateForOnOffSkill.getValue());
        return mplew.getPacket();
    }

    public static byte[] showSkillMode(int chrID, int sourceid, int mode, int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (chrID == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrID);
        }
        mplew.write(EffectOpcode.UserEffect_SkillMode.getValue());
        mplew.writeInt(sourceid);
        mplew.writeInt(mode - 1);
        mplew.writeInt(value);
        return mplew.getPacket();
    }

    public static byte[] show黑暗重生(int chrID, int sourceid, int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (chrID == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrID);
        }
        mplew.write(EffectOpcode.UserEffect_SkillPreLoopEnd.getValue());
        mplew.writeInt(sourceid);
        mplew.writeInt(value);
        return mplew.getPacket();
    }

    public static byte[] showSkillAffected(int chrID, int sourceid, int level, int direction) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (chrID == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrID);
        }
        mplew.write(EffectOpcode.UserEffect_SkillAffected.getValue());
        mplew.writeInt(sourceid);
        mplew.write(level);
        if (sourceid != 400051076) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (sourceid == 惡魔殺手.血腥烏鴉 || sourceid == 神之子.時間扭曲 || sourceid == 隱月.束縛術) {
            mplew.writeInt(direction);
        }
        return mplew.getPacket();
    }

    public static byte[] showQuestItemGain(Map<Integer, Integer> map) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        mplew.write(EffectOpcode.UserEffect_Quest.getValue());
        mplew.write(map.size());
        if (map.isEmpty()) {
            mplew.writeMapleAsciiString("");
            mplew.writeInt(0);
        } else {
            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                mplew.writeInt(entry.getKey());
                mplew.writeInt(entry.getValue());
                mplew.writeBool(false); // TMS229
            }
        }
        return mplew.getPacket();
    }

    public static byte[] DragonWreckage(int id, Point position, int n2, int addWreckages, int sourceid, int n5, int size) {
        byte[] packet;
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ADD_WRECKAGE.getValue());
        mplew.writeInt(id);
        mplew.writeInt(position.x);
        mplew.writeInt(position.y);
        mplew.writeInt(n2);
        mplew.writeInt(addWreckages);
        mplew.writeInt(sourceid);
        mplew.writeInt(n5);
        mplew.writeInt(size);
        packet = mplew.getPacket();
        return packet;
    }

    public static byte[] PapulatusFieldEffect() {
        final MaplePacketLittleEndianWriter mplew;
        (mplew = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.PapulatusFieldEffect);
        mplew.writeInt(2);
        mplew.writeInt(4);
        for (int i = 0; i < 4; ++i) {
            mplew.writeInt(i);
            mplew.writeInt(1);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static byte[] PapulatusFieldEffect(final int n, final int n2, final int n3) {
        final MaplePacketLittleEndianWriter mplew;
        (mplew = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.PapulatusFieldEffect);
        mplew.writeInt(2);
        mplew.writeInt(1);
        mplew.writeInt(n);
        mplew.writeInt((n3 <= 0) ? 6 : 5);
        mplew.writeInt((n3 <= 0) ? 210 : n2);
        mplew.writeInt(n3);
        return mplew.getPacket();
    }

    public static byte[] PapulatusFieldEffect(final int n, final int n2) {
        final MaplePacketLittleEndianWriter mplew;
        (mplew = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.PapulatusFieldEffect);
        mplew.writeInt(0);
        mplew.writeInt(2);
        for (int i = 0; i < 2; ++i) {
            mplew.writeInt(n2 + i);
            mplew.writeInt(Randomizer.nextInt(2));
            mplew.writeShort(0);
            mplew.writeShort(24640);
            mplew.writeShort(64);
            mplew.writeInt(0);
            mplew.write(0);
            mplew.writeShort(Randomizer.rand(16436, 16450));
        }
        return mplew.getPacket();
    }

    public static byte[] showIncubatorEffect(int chrID, int effectId, String info) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (chrID == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrID);
        }
        mplew.write(EffectOpcode.UserEffect_IncubatorUse.getValue());
        mplew.writeInt(effectId);
        mplew.writeMapleAsciiString(info);
        return mplew.getPacket();
    }

    public static byte[] showJobChanged(int chrID, int job) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (chrID == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrID);
        }
        mplew.write(EffectOpcode.UserEffect_JobChanged.getValue());
        mplew.writeInt(job);
        return mplew.getPacket();
    }

    public static byte[] showHoYoungHeal(int chrID, int skillId, Point position, int amount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (chrID == -1) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chrID);
        }
        mplew.write(EffectOpcode.UserEffect_HoYoungHeal.getValue());
        mplew.writeInt(skillId);
        mplew.writeInt(1);
        mplew.writePosInt(position);
        mplew.writeInt(amount);

        return mplew.getPacket();
    }

    /* 賽蓮封包 */

    public static byte[] BossFieldSkillEffect(int skillId, int skillLv, int delay, int unk1, int unk2,
                                              int screenDuration, int unk3, int attackDuration, int height, int unk4, int size, String packets) {
        final MaplePacketLittleEndianWriter mplew;
        (mplew = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.LP_FieldSkillRequest);
        mplew.writeInt(skillId);
        mplew.writeInt(skillLv);
        mplew.writeInt(delay);
        mplew.writeInt(unk1);
        mplew.writeInt(unk2);
        mplew.writeInt(screenDuration);
        mplew.writeInt(unk3);
        mplew.writeInt(attackDuration);
        mplew.writeInt(height);
        mplew.writeInt(unk4);
        mplew.writeInt(size);
        for (String s : packets.split(",")) {
            mplew.writeInt(Integer.parseInt(s));
        }
        return mplew.getPacket();
    }


    public static byte[] BossSerenNoonFieldSkill(int mapLeft, int mapRight, int raysCount) {
        final MaplePacketLittleEndianWriter mplew;
        (mplew = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.LP_FieldSkillRequest);
        mplew.writeInt(100023);
        mplew.writeInt(1);
        mplew.writeInt(0);
        mplew.writeInt(300);
        mplew.writeInt(100);
        mplew.writeInt(3000);
        mplew.writeInt(150);
        mplew.writeInt(3000);
        mplew.writeInt(300);
        mplew.writeInt(0);
        mplew.writeInt(raysCount);
        for (int i = 0; i < raysCount; i++) {
            int randX = 0;
            int randX2 = 0;
            if (i % 2 == 0) {
                randX = (int) Math.floor(Math.random() * mapRight);
                randX2 = (int) Math.floor(Math.random() * mapLeft);
            } else {
                randX = (int) Math.floor(Math.random() * mapLeft);
                randX2 = (int) Math.floor(Math.random() * mapRight);
            }
            mplew.writeInt(randX);
            mplew.writeInt(randX2);
            mplew.writeInt(i * 1000);
        }
        return mplew.getPacket();
    }

    public static byte[] encodeUserEffectByPickUpItem(MapleCharacter chr, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (chr == null) {
            mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        } else {
            mplew.writeShort(OutHeader.LP_UserEffectRemote.getValue());
            mplew.writeInt(chr.getId());
        }
        mplew.write(EffectOpcode.UserEffect_PickUpItem.getValue());
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }


}
