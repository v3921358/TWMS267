package Packet;

import Client.*;
import Client.hexa.ErdaConsumeOption;
import Config.constants.HexaConstants;
import Net.server.life.MobSkill;
import Opcode.Headler.OutHeader;
import lombok.Getter;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.List;
import java.util.Map;

import static Opcode.Opcode.MessageOpcode.*;

public class CWvsContext extends MapleClient{

    @Getter
    private static MapleClient c;


    public static byte[] onFieldSetVariable(String key, String value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FieldValue.getValue());
        mplew.writeMapleAsciiString(key);
        mplew.writeMapleAsciiString(value);
        return mplew.getPacket();
    }


    public static byte[] giveDisease(Map<SecondaryStat, Pair<Integer, Integer>> statups, MobSkill skill, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ForcedStatSet.getValue());
        List<Pair<SecondaryStat, Pair<Integer, Integer>>> newstatups = PacketHelper.sortBuffStats(statups);
        PacketHelper.writeBuffMask(mplew, newstatups);
        for (Pair<SecondaryStat, Pair<Integer, Integer>> pair : newstatups) {
            if (pair.getLeft().canStack() || pair.getLeft().isSpecialBuff()) {
                continue;
            }
            if (SecondaryStat.isEncode4Byte(statups)) {
                if (pair.left == SecondaryStat.ReturnTeleport) {
                    mplew.writeShort(chr.getPosition().y);
                    mplew.writeShort(chr.getPosition().x);
                } else {
                    mplew.writeInt(pair.getLeft().getX() != 0 ? pair.getLeft().getX() : pair.getRight().left.intValue());
                }
            } else {
                mplew.writeShort(pair.getRight().left);
            }
            mplew.writeShort(skill.getSkillId());
            mplew.writeShort(skill.getSkillId() == 237 ? 0 : skill.getSkillLevel());
            mplew.writeInt(skill.getSkillId() == 237 ? 0 : pair.right.right);
        }
        mplew.writeShort(0);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(0);
        if (statups.containsKey(SecondaryStat.Slow)) {
            mplew.write(0);
        }
        if (statups.containsKey(SecondaryStat.BlackMageCreate)) {
            mplew.writeInt(10);
        }
        if (statups.containsKey(SecondaryStat.BlackMageDestroy)) {
            mplew.writeInt(15);
        }
        if (statups.containsKey(SecondaryStat.Stigma)) {
            mplew.writeInt(7);
        }
        mplew.writeInt(0);
        for (Pair<SecondaryStat, Pair<Integer, Integer>> pair : newstatups) {
            if (pair.left.canStack() || !pair.left.isSpecialBuff()) {
                continue;
            }
            mplew.writeInt(pair.right.left);
            mplew.writeShort(skill.getSkillId());
            mplew.writeShort(skill.getSkillId() == 237 ? 0 : skill.getSkillLevel());
            mplew.write(0);
            mplew.writeInt(0);
            mplew.writeShort(pair.right.right);
        }
        if (statups.containsKey(SecondaryStat.VampDeath)) {
            mplew.writeInt(0);
        }
        if (statups.containsKey(SecondaryStat.OutSide)) {
            mplew.writeInt(1000);
        }
        if (statups.containsKey(SecondaryStat.BossWill_Infection)) {
            mplew.writeInt(30);
        }
        mplew.writeShort(0);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.write(true);
        mplew.write(true);
        mplew.writeInt(0);
        mplew.writeZeroBytes(100);
        return mplew.getPacket();
    }
    public static byte[] sendMessage(int type, MessageOption option) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_Message.getValue());
        mplew.write(type);
        switch (type) {
            case MS_DropPickUpMessage: {
                //sub_144977CE0(v3, v2);
                mplew.writeInt(0); // delay
                mplew.write(0);
                mplew.write(option.getMode());
                switch (option.getMode() + 5) {
                    case 5: // item
                        mplew.writeInt(option.getObjectId());
                        mplew.writeInt(option.getAmount());
                        mplew.write(0);
                        break;
                    case 6: // mesos
                        mplew.write(0); // portion was lost after falling to the ground
                        mplew.writeInt(option.getLongGain());
                        mplew.writeShort(0); // smallChangeExtra
                    case 7:
                        mplew.writeInt(0);
                        mplew.writeLong(0);
                        break;
                    case 11:
                        mplew.writeInt(0);
                        break;
                    case 13:
                        mplew.writeInt(0);
                        mplew.writeShort(0);
                        break;
                }
                break;
            }
            case MS_QuestRecordMessage: {
                //sub_1449790E0(v3, v2);
                MapleQuestStatus quest = option.getQuestStatus();
                mplew.writeInt(quest.getQuest().getId());
                mplew.write(quest.getStatus());
                switch (quest.getStatus()) {
                    case 0: //新任務？
                        mplew.write(1);
                        break;
                    case 1: //更新任務
                        mplew.writeMapleAsciiString(quest.getCustomData() != null ? quest.getCustomData() : "");
                        break;
                    case 2: //完成任務
                        mplew.writeLong(PacketHelper.getTime(quest.getCompletionTime()));
                        break;
                    default:
                        System.out.println("未知LP_Message(" + type + ")值:" + quest.getStatus());
                }
                break;
            }
            case MS_CashItemExpireMessage:
                //sub_14497E550(v3, v2);
                mplew.writeInt(option.getObjectId());
                break;
            case MS_IncEXPMessage: {
                mplew.write(option.getColor());
                mplew.writeInt(option.getLongGain());
                mplew.writeInt(0);
                mplew.write(option.getOnQuest());
                mplew.writeInt(option.getDiseaseType());
                if (option.getDiseaseType() != 0) {
                    mplew.writeLong(option.getExpLost());
                }
                Map<MapleExpStat, Object> expStats = option.getExpGainData();
                long expMask = 0;
                for (MapleExpStat statupdate : expStats.keySet()) {
                    expMask |= statupdate.getFlag();
                }
                mplew.writeLong(expMask);
                if (expStats.getOrDefault(MapleExpStat.活動獎勵經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.活動獎勵經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.活動組隊經驗值, null) != null) {
                    mplew.write((byte) expStats.get(MapleExpStat.活動組隊經驗值));
                }
                int nQuestBonusRate = 0;
                if (option.getOnQuest()) {
                    mplew.write(nQuestBonusRate);
                }
                if (nQuestBonusRate > 0) {
                    mplew.write(0); // nQuestBonusRemainCount
                }
                if (expStats.getOrDefault(MapleExpStat.結婚紅利經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.結婚紅利經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.組隊額外經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.組隊額外經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.道具裝備紅利經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.道具裝備紅利經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.高級服務贈送經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.高級服務贈送經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.彩虹週獎勵經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.彩虹週獎勵經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.爆發獎勵經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.爆發獎勵經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.秘藥額外經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.秘藥額外經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.極限屬性經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.極限屬性經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.加持獎勵經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.加持獎勵經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.休息獎勵經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.休息獎勵經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.道具獎勵經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.道具獎勵經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.依道具趴增加經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.依道具趴增加經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.超值包獎勵經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.超值包獎勵經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.依道具的組隊任務趴增加經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.依道具的組隊任務趴增加經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.累積狩獵數紅利經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.累積狩獵數紅利經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.家族經驗值獎勵, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.家族經驗值獎勵));
                }
                if (expStats.getOrDefault(MapleExpStat.冷凍勇士經驗值獎勵, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.冷凍勇士經驗值獎勵));
                }
                if (expStats.getOrDefault(MapleExpStat.燃燒場地獎勵經驗, null) != null) {
                    Pair<Long, Integer> expStat = (Pair<Long, Integer>) expStats.get(MapleExpStat.燃燒場地獎勵經驗);
                    mplew.writeLong(expStat.getLeft());
                    mplew.writeInt(expStat.getRight());
                }
                if (expStats.getOrDefault(MapleExpStat.HP風險經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.HP風險經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.場地紅利經驗, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.場地紅利經驗));
                }
                if (expStats.getOrDefault(MapleExpStat.累計打獵數量獎勵經驗, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.累計打獵數量獎勵經驗));
                }
                if (expStats.getOrDefault(MapleExpStat.活動獎勵經驗值2, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.活動獎勵經驗值2));
                }
                if (expStats.getOrDefault(MapleExpStat.網咖摯友獎勵經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.網咖摯友獎勵經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.場地紅利經驗2, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.場地紅利經驗2));
                }
                if (expStats.getOrDefault(MapleExpStat.超級小豬幸運_攻擊額外經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.超級小豬幸運_攻擊額外經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.伺服器計量條活動獎勵經驗值, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.伺服器計量條活動獎勵經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.未知2, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.未知2));
                }
                if (expStats.getOrDefault(MapleExpStat.組隊經驗值增加x趴, null) != null) {
                    Pair<Long, Integer> expStat = (Pair<Long, Integer>) expStats.get(MapleExpStat.道具名經驗值);
                    mplew.writeLong(expStat.getLeft());
                }
                if (expStats.getOrDefault(MapleExpStat.蛋糕vs派餅_EXP紅利, null) != null) {
                    mplew.writeLong((long) expStats.get(MapleExpStat.蛋糕vs派餅_EXP紅利));
                }
                if (expStats.getOrDefault(MapleExpStat.道具名經驗值, null) != null) {
                    Pair<Long, Integer> expStat = (Pair<Long, Integer>) expStats.get(MapleExpStat.道具名經驗值);
                    mplew.writeLong(expStat.getLeft());
                    mplew.writeInt(expStat.getRight());
                }
                if (expStats.getOrDefault(MapleExpStat.組隊經驗值增加x趴, null) != null) {
                    Pair<Long, Integer> expStat = (Pair<Long, Integer>) expStats.get(MapleExpStat.道具名經驗值);
                    mplew.writeInt(expStat.getRight());
                }
                if (expStats.getOrDefault(MapleExpStat.寵物訓練紅利經驗值, null) != null) {
                    mplew.writeInt((int) expStats.get(MapleExpStat.寵物訓練紅利經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.組合道具獎勵經驗值, null) != null) {
                    mplew.writeInt((int) expStats.get(MapleExpStat.組合道具獎勵經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.組合道具獎勵組隊經驗值, null) != null) {
                    mplew.writeInt((int) expStats.get(MapleExpStat.組合道具獎勵組隊經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.伺服器加持經驗值, null) != null) {
                    mplew.writeInt((int) expStats.get(MapleExpStat.伺服器加持經驗值));
                }
                if (expStats.getOrDefault(MapleExpStat.累積狩獵數紅利經驗值2, null) != null) {
                    mplew.writeInt((int) expStats.get(MapleExpStat.累積狩獵數紅利經驗值2));
                }
                if (expStats.getOrDefault(MapleExpStat.艾爾達斯還原追加經驗值, null) != null) {
                    mplew.writeInt((int) expStats.get(MapleExpStat.艾爾達斯還原追加經驗值));
                }

                int 遠征隊Bonus經驗值 = 0;
                int 遠征隊關係效果Bonus經驗值 = 0;

                int expedExpMask = 0;
                if (遠征隊Bonus經驗值 > 0) {
                    expedExpMask |= 0x1;
                }
                if (遠征隊關係效果Bonus經驗值 > 0) {
                    expedExpMask |= 0x2;
                }
                mplew.writeInt(expedExpMask);

                if ((expedExpMask & 0x1) != 0) {
                    mplew.writeInt(遠征隊Bonus經驗值);
                }
                if ((expedExpMask & 0x2) != 0) {
                    mplew.writeInt(遠征隊關係效果Bonus經驗值);
                }
                boolean nexon_encry = true;
                if(nexon_encry) {
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                    mplew.writeInt(8);
                }
                break;
            }
            case MS_IncSPMessage: // OK
                //sub_1449838A0(v3, v2);
                mplew.writeShort(option.getJob());
                mplew.write(option.getAmount());
                break;
            case MS_IncPOPMessage:
                //sub_144983A00(v3, v2);
                mplew.writeInt(option.getAmount());
                break;
            case MS_IncMoneyMessage: // OK
                //sub_144983B90(v3, v2);
                mplew.writeLong(option.getLongGain());
                mplew.writeInt(option.getMode());
                if (option.getMode() == 24) {
                    mplew.writeMapleAsciiString(option.getText());
                }
                break;
            case MS_IncGPMessage:
                //sub_144984670(v3, v2);
                mplew.writeInt(option.getAmount());
                break;
            case MS_IncCommitmentMessage:
                //sub_144984800(v3, v2);
                mplew.writeInt(option.getAmount());
                mplew.writeInt(option.getAmount());
                mplew.writeInt(option.getAmount());
                break;
            case MS_GiveBuffMessage:
                mplew.writeInt(option.getObjectId());
                break;
            case MS_GeneralItemExpireMessage: {
                //sub_14497E8A0(v3, v2);
                mplew.write(option.getIntegerData().length);
                for (int itemID : option.getIntegerData()) {
                    mplew.writeInt(itemID);
                }
                break;
            }
            case MS_SystemMessage:
                mplew.writeMapleAsciiString(option.getText());
                break;
            case MS_UnkRecordMessage:
            case MS_QuestRecordExMessage: //sub_14497D520(v3, v2);
            case MS_WorldShareRecordMessage: //sub_14497D9C0(v3, v2);
                mplew.writeInt(option.getObjectId());
                mplew.writeMapleAsciiString(option.getText());
                break;
            case MS_ItemProtectExpireMessage: {
                //sub_14497EAA0(v3, v2);
                boolean type2 = false;
                mplew.writeBool(type2);
                /*if (type2) {
                    while (true) {
                        int unk = 0;
                        mplew.writeInt(0);
                         result = sub_14050B7D0(unk);
                         if (!result) {
                              break;
                         }
                    }
                }*/
                break;
            }
            case MS_ItemExpireReplaceMessage:
                int v9 = 0;
                mplew.write(v9);
                if ( v9 > 0)
                {
                    do
                    {
                        mplew.writeMapleAsciiString("");
                        --v9;
                    }
                    while ( v9 < 0 );
                }
                break;
            case MS_ItemAbilityTimeLimitedExpireMessage: {
                //sub_14497ED80(v3, v2);
                mplew.write(option.getIntegerData().length);
                for (int value : option.getIntegerData()) {
                    mplew.writeInt(value);
                }
                break;
            }
            case MS_SkillExpireMessage: {
                //sub_14497EF80(v3, v2);
                mplew.write(option.getIntegerData().length);
                for (int skillID : option.getIntegerData()) {
                    mplew.writeInt(skillID);
                }
                break;
            }
            case MS_IncNonCombatStatEXPMessage:
                //sub_144981D80(v3, v2);
                int[] data = option.getIntegerData();
                if (data == null) data = new int[0];
                long mask = 0;
                MapleTraitType[] traitTypes = MapleTraitType.values();
                for (MapleTraitType traitType : traitTypes) {
                    if (data.length >= traitType.ordinal() + 1 && data[traitType.ordinal()] > 0)
                        mask |= traitType.getStat().getValue();
                }
                mplew.writeLong(mask);
                for (int i = 0; i < traitTypes.length; i++) {
                    if (data.length >= i + 1 && data[i] > 0)
                        mplew.writeInt(data[i]);
                }
                break;
            case MS_Unk0x15:
                //sub_144982A50(v3, v2);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
            case MS_LimitNonCombatStatEXPMessage:
                //sub_144983410(v3, v2);
                mplew.writeLong(option.getMask());
                break;
            case MS_AndroidMachineHeartAlertMessage:
                break;
            case MS_IncFatigueByRestMessage:
                break;
            case MS_IncPvPPointMessage:
                //sub_144984270(v3, v2);
                mplew.writeInt(option.getAmount());
                mplew.writeInt(0);
                break;
            case MS_PvPItemUseMessage:
                mplew.writeMapleAsciiString(option.getText()); //name got Shield.
                mplew.writeMapleAsciiString(option.getText2()); //Shield applied to name.
                break;
            case MS_WeddingPortalError:
                mplew.write(0);
                break;
            case MS_PvPHardCoreExpMessage:
                //sub_1449B0620(v3, v2);
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
            case MS_NoticeAutoLineChanged:
                mplew.writeMapleAsciiString(option.getText());
            case MS_EntryRecordMessage: {
                //sub_1449AED30(v3, v2);
                int type2 = 0;
                mplew.write(type2);
                switch (type2) {
                    case 0:
                        mplew.writeShort(0);
                        mplew.writeInt(0);
                        mplew.writeInt(0);
                        break;
                    case 1:
                        mplew.writeInt(0);
                        break;
                    case 6:
                        mplew.writeInt(0);
                        mplew.writeInt(0);
                        mplew.writeInt(0);
                        break;
                    default:
                        break;
                }
                break;
            }
            case MS_NxRecordMessage:
                mplew.writeInt(option.getObjectId());
                mplew.writeMapleAsciiString(option.getText());
                break;
            case MS_Unk32:
                mplew.writeInt(0);
                break;
            case MS_IncWPMessage:
                mplew.writeInt(option.getAmount());
                break;
            case MS_MaxWPMessage:
                break;
            case MS_StylishKillMessage: { // OK
                mplew.writeBool(option.getMode() > 0 );
                if (option.getMode() > 0) {
                    mplew.writeInt(option.getLongExp());
                    mplew.writeInt(option.getObjectId());
                    mplew.writeInt(0);
                    mplew.writeInt(option.getCombo());
                } else {
                    mplew.writeLong(option.getLongExp());
                    mplew.writeInt(0);
                    mplew.writeInt(option.getCombo());
                    mplew.writeInt(option.getColor());
                }
                break;
            }
            case MS_BarrierEffectIgnoreMessage:
                //sub_144984FD0();
                break;
            case MS_ExpiredCashItemResultMessage:
                break;
            case MS_CollectionRecordMessage: // OK
                mplew.writeInt(option.getObjectId());
                mplew.writeMapleAsciiString(option.getText());
                break;
            case MS_RandomChanceMessage:
                break;
            case MS_Unk41:
                //sub_144A4B520(v3, v2);
                mplew.writeInt(option.getObjectId());
                mplew.writeMapleAsciiString(option.getText());
                break;
            case MS_AchievementInit:
                //sub_144A4B700(v3, v2);
                mplew.writeInt(0);
                break;
            case MS_AchievementMessage:
                //sub_144A131F0(v3, v2);
                // sub_1403CAF90
                // 待解
                //Sound/AchievementEff.img/GradeUp
                break;
            case MS_Unk44: {
                //sub_144A13470(v3, v2);
                int type2 = 0;
                mplew.writeInt(type2);
                for (int i = 0; i < type2; i++) {
                    mplew.writeInt(0);
                }
                break;
            }
            case MS_Unk45:
                //sub_144A13B40(v3, v2);
                // sub_1403CBAB0 待解
                break;
            case MS_Unk46:
                //sub_144A13BA0(v3, v2);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeLong(0);
                break;
            case MS_Unk48:
                mplew.writeMapleAsciiString(option.getText());
                break;
            case MS_Unk50:
                mplew.writeMapleAsciiString(option.getText());
                break;
            default:
                break;
        }
        return mplew.getPacket();
    }

    public static byte[] sendHexaEnforcementInfo() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_HEXA_ENFORCEMENTINFO.getValue());
        mplew.writeInt(4);
        mplew.writeInt(0);
        mplew.writeInt(5);
        mplew.writeInt(100);
        mplew.writeInt(1);
        mplew.writeInt(3);
        mplew.writeInt(50);
        mplew.writeInt(2);
        mplew.writeInt(4);
        mplew.writeInt(75);
        mplew.writeInt(3);
        mplew.writeInt(7);
        mplew.writeInt(125);
        mplew.writeInt(4);
        mplew.writeInt(0);
        mplew.writeInt(29);
        mplew.writeInt(1);
        mplew.writeInt(1);
        mplew.writeInt(30);
        mplew.writeInt(2);
        mplew.writeInt(1);
        mplew.writeInt(35);
        mplew.writeInt(3);
        mplew.writeInt(1);
        mplew.writeInt(40);
        mplew.writeInt(4);
        mplew.writeInt(2);
        mplew.writeInt(45);
        mplew.writeInt(5);
        mplew.writeInt(2);
        mplew.writeInt(50);
        mplew.writeInt(6);
        mplew.writeInt(2);
        mplew.writeInt(55);
        mplew.writeInt(7);
        mplew.writeInt(3);
        mplew.writeInt(60);
        mplew.writeInt(8);
        mplew.writeInt(3);
        mplew.writeInt(65);
        mplew.writeInt(9);
        mplew.writeInt(10);
        mplew.writeInt(200);
        mplew.writeInt(10);
        mplew.writeInt(3);
        mplew.writeInt(80);
        mplew.writeInt(11);
        mplew.writeInt(3);
        mplew.writeInt(90);
        mplew.writeInt(12);
        mplew.writeInt(4);
        mplew.writeInt(100);
        mplew.writeInt(13);
        mplew.writeInt(4);
        mplew.writeInt(110);
        mplew.writeInt(14);
        mplew.writeInt(4);
        mplew.writeInt(120);
        mplew.writeInt(15);
        mplew.writeInt(4);
        mplew.writeInt(130);
        mplew.writeInt(16);
        mplew.writeInt(4);
        mplew.writeInt(140);
        mplew.writeInt(17);
        mplew.writeInt(4);
        mplew.writeInt(150);
        mplew.writeInt(18);
        mplew.writeInt(5);
        mplew.writeInt(160);
        mplew.writeInt(19);
        mplew.writeInt(15);
        mplew.writeInt(350);
        mplew.writeInt(20);
        mplew.writeInt(5);
        mplew.writeInt(170);
        mplew.writeInt(21);
        mplew.writeInt(5);
        mplew.writeInt(180);
        mplew.writeInt(22);
        mplew.writeInt(5);
        mplew.writeInt(190);
        mplew.writeInt(23);
        mplew.writeInt(5);
        mplew.writeInt(200);
        mplew.writeInt(24);
        mplew.writeInt(5);
        mplew.writeInt(210);
        mplew.writeInt(25);
        mplew.writeInt(6);
        mplew.writeInt(220);
        mplew.writeInt(26);
        mplew.writeInt(6);
        mplew.writeInt(230);
        mplew.writeInt(27);
        mplew.writeInt(6);
        mplew.writeInt(240);
        mplew.writeInt(28);
        mplew.writeInt(7);
        mplew.writeInt(250);
        mplew.writeInt(29);
        mplew.writeInt(20);
        mplew.writeInt(500);
        mplew.writeInt(1);
        mplew.writeInt(29);
        mplew.writeInt(1);
        mplew.writeInt(1);
        mplew.writeInt(15);
        mplew.writeInt(2);
        mplew.writeInt(1);
        mplew.writeInt(18);
        mplew.writeInt(3);
        mplew.writeInt(1);
        mplew.writeInt(20);
        mplew.writeInt(4);
        mplew.writeInt(1);
        mplew.writeInt(23);
        mplew.writeInt(5);
        mplew.writeInt(1);
        mplew.writeInt(25);
        mplew.writeInt(6);
        mplew.writeInt(1);
        mplew.writeInt(28);
        mplew.writeInt(7);
        mplew.writeInt(2);
        mplew.writeInt(30);
        mplew.writeInt(8);
        mplew.writeInt(2);
        mplew.writeInt(33);
        mplew.writeInt(9);
        mplew.writeInt(5);
        mplew.writeInt(100);
        mplew.writeInt(10);
        mplew.writeInt(2);
        mplew.writeInt(40);
        mplew.writeInt(11);
        mplew.writeInt(2);
        mplew.writeInt(45);
        mplew.writeInt(12);
        mplew.writeInt(2);
        mplew.writeInt(50);
        mplew.writeInt(13);
        mplew.writeInt(2);
        mplew.writeInt(55);
        mplew.writeInt(14);
        mplew.writeInt(2);
        mplew.writeInt(60);
        mplew.writeInt(15);
        mplew.writeInt(2);
        mplew.writeInt(65);
        mplew.writeInt(16);
        mplew.writeInt(2);
        mplew.writeInt(70);
        mplew.writeInt(17);
        mplew.writeInt(2);
        mplew.writeInt(75);
        mplew.writeInt(18);
        mplew.writeInt(3);
        mplew.writeInt(80);
        mplew.writeInt(19);
        mplew.writeInt(8);
        mplew.writeInt(175);
        mplew.writeInt(20);
        mplew.writeInt(3);
        mplew.writeInt(85);
        mplew.writeInt(21);
        mplew.writeInt(3);
        mplew.writeInt(90);
        mplew.writeInt(22);
        mplew.writeInt(3);
        mplew.writeInt(95);
        mplew.writeInt(23);
        mplew.writeInt(3);
        mplew.writeInt(100);
        mplew.writeInt(24);
        mplew.writeInt(3);
        mplew.writeInt(105);
        mplew.writeInt(25);
        mplew.writeInt(3);
        mplew.writeInt(110);
        mplew.writeInt(26);
        mplew.writeInt(3);
        mplew.writeInt(115);
        mplew.writeInt(27);
        mplew.writeInt(3);
        mplew.writeInt(120);
        mplew.writeInt(28);
        mplew.writeInt(4);
        mplew.writeInt(125);
        mplew.writeInt(29);
        mplew.writeInt(10);
        mplew.writeInt(250);
        mplew.writeInt(2);
        mplew.writeInt(29);
        mplew.writeInt(1);
        mplew.writeInt(1);
        mplew.writeInt(23);
        mplew.writeInt(2);
        mplew.writeInt(1);
        mplew.writeInt(27);
        mplew.writeInt(3);
        mplew.writeInt(1);
        mplew.writeInt(30);
        mplew.writeInt(4);
        mplew.writeInt(2);
        mplew.writeInt(34);
        mplew.writeInt(5);
        mplew.writeInt(2);
        mplew.writeInt(38);
        mplew.writeInt(6);
        mplew.writeInt(2);
        mplew.writeInt(42);
        mplew.writeInt(7);
        mplew.writeInt(3);
        mplew.writeInt(45);
        mplew.writeInt(8);
        mplew.writeInt(3);
        mplew.writeInt(49);
        mplew.writeInt(9);
        mplew.writeInt(8);
        mplew.writeInt(150);
        mplew.writeInt(10);
        mplew.writeInt(3);
        mplew.writeInt(60);
        mplew.writeInt(11);
        mplew.writeInt(3);
        mplew.writeInt(68);
        mplew.writeInt(12);
        mplew.writeInt(3);
        mplew.writeInt(75);
        mplew.writeInt(13);
        mplew.writeInt(3);
        mplew.writeInt(83);
        mplew.writeInt(14);
        mplew.writeInt(3);
        mplew.writeInt(90);
        mplew.writeInt(15);
        mplew.writeInt(3);
        mplew.writeInt(98);
        mplew.writeInt(16);
        mplew.writeInt(3);
        mplew.writeInt(105);
        mplew.writeInt(17);
        mplew.writeInt(3);
        mplew.writeInt(113);
        mplew.writeInt(18);
        mplew.writeInt(4);
        mplew.writeInt(120);
        mplew.writeInt(19);
        mplew.writeInt(12);
        mplew.writeInt(263);
        mplew.writeInt(20);
        mplew.writeInt(4);
        mplew.writeInt(128);
        mplew.writeInt(21);
        mplew.writeInt(4);
        mplew.writeInt(135);
        mplew.writeInt(22);
        mplew.writeInt(4);
        mplew.writeInt(143);
        mplew.writeInt(23);
        mplew.writeInt(4);
        mplew.writeInt(150);
        mplew.writeInt(24);
        mplew.writeInt(4);
        mplew.writeInt(158);
        mplew.writeInt(25);
        mplew.writeInt(5);
        mplew.writeInt(165);
        mplew.writeInt(26);
        mplew.writeInt(5);
        mplew.writeInt(173);
        mplew.writeInt(27);
        mplew.writeInt(5);
        mplew.writeInt(180);
        mplew.writeInt(28);
        mplew.writeInt(6);
        mplew.writeInt(188);
        mplew.writeInt(29);
        mplew.writeInt(15);
        mplew.writeInt(375);
        mplew.writeInt(3);
        mplew.writeInt(29);
        mplew.writeInt(1);
        mplew.writeInt(2);
        mplew.writeInt(38);
        mplew.writeInt(2);
        mplew.writeInt(2);
        mplew.writeInt(44);
        mplew.writeInt(3);
        mplew.writeInt(2);
        mplew.writeInt(50);
        mplew.writeInt(4);
        mplew.writeInt(3);
        mplew.writeInt(57);
        mplew.writeInt(5);
        mplew.writeInt(3);
        mplew.writeInt(63);
        mplew.writeInt(6);
        mplew.writeInt(3);
        mplew.writeInt(69);
        mplew.writeInt(7);
        mplew.writeInt(5);
        mplew.writeInt(75);
        mplew.writeInt(8);
        mplew.writeInt(5);
        mplew.writeInt(82);
        mplew.writeInt(9);
        mplew.writeInt(14);
        mplew.writeInt(300);
        mplew.writeInt(10);
        mplew.writeInt(5);
        mplew.writeInt(110);
        mplew.writeInt(11);
        mplew.writeInt(5);
        mplew.writeInt(124);
        mplew.writeInt(12);
        mplew.writeInt(6);
        mplew.writeInt(138);
        mplew.writeInt(13);
        mplew.writeInt(6);
        mplew.writeInt(152);
        mplew.writeInt(14);
        mplew.writeInt(6);
        mplew.writeInt(165);
        mplew.writeInt(15);
        mplew.writeInt(6);
        mplew.writeInt(179);
        mplew.writeInt(16);
        mplew.writeInt(6);
        mplew.writeInt(193);
        mplew.writeInt(17);
        mplew.writeInt(6);
        mplew.writeInt(207);
        mplew.writeInt(18);
        mplew.writeInt(7);
        mplew.writeInt(220);
        mplew.writeInt(19);
        mplew.writeInt(17);
        mplew.writeInt(525);
        mplew.writeInt(20);
        mplew.writeInt(7);
        mplew.writeInt(234);
        mplew.writeInt(21);
        mplew.writeInt(7);
        mplew.writeInt(248);
        mplew.writeInt(22);
        mplew.writeInt(7);
        mplew.writeInt(262);
        mplew.writeInt(23);
        mplew.writeInt(7);
        mplew.writeInt(275);
        mplew.writeInt(24);
        mplew.writeInt(7);
        mplew.writeInt(289);
        mplew.writeInt(25);
        mplew.writeInt(9);
        mplew.writeInt(303);
        mplew.writeInt(26);
        mplew.writeInt(9);
        mplew.writeInt(317);
        mplew.writeInt(27);
        mplew.writeInt(9);
        mplew.writeInt(330);
        mplew.writeInt(28);
        mplew.writeInt(10);
        mplew.writeInt(344);
        mplew.writeInt(29);
        mplew.writeInt(20);
        mplew.writeInt(750);
        mplew.writeInt(2);
        mplew.writeInt(50000000);
        mplew.writeInt(5);
        mplew.writeInt(0);
        mplew.writeInt(10);
        mplew.writeInt(11);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(10);
        mplew.writeInt(1);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(10);
        mplew.writeInt(2);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(10);
        mplew.writeInt(3);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(20);
        mplew.writeInt(4);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(20);
        mplew.writeInt(5);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(20);
        mplew.writeInt(6);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(20);
        mplew.writeInt(7);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(30);
        mplew.writeInt(8);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(30);
        mplew.writeInt(9);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(50);
        mplew.writeInt(10);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(50);
        mplew.writeInt(10);
        mplew.writeInt(0);
        mplew.writeInt(1717986918);
        mplew.writeInt(1071015526);
        mplew.writeInt(1);
        mplew.writeInt(1717986918);
        mplew.writeInt(1071015526);
        mplew.writeInt(2);
        mplew.writeInt(1717986918);
        mplew.writeInt(1071015526);
        mplew.writeInt(3);
        mplew.writeInt(-1717986918);
        mplew.writeInt(1070176665);
        mplew.writeInt(4);
        mplew.writeInt(-1717986918);
        mplew.writeInt(1070176665);
        mplew.writeInt(5);
        mplew.writeInt(-1717986918);
        mplew.writeInt(1070176665);
        mplew.writeInt(6);
        mplew.writeInt(-1717986918);
        mplew.writeInt(1070176665);
        mplew.writeInt(7);
        mplew.writeInt(858993459);
        mplew.writeInt(1069757235);
        mplew.writeInt(8);
        mplew.writeInt(-1717986918);
        mplew.writeInt(1069128089);
        mplew.writeInt(9);
        mplew.writeInt(-1717986918);
        mplew.writeInt(1068079513);
        mplew.writeInt(21);
        mplew.writeInt(10);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(11);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(12);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(13);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(14);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(15);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(16);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(17);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(18);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(19);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(20);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(21);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(22);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(23);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(24);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(25);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(26);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(27);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(28);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(29);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(30);
        mplew.writeInt(10000000);
        mplew.writeInt(0);
        mplew.writeInt(31);
        mplew.writeInt(0);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(1);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(2);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(3);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(4);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(5);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(6);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(7);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(8);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(9);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(10);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(11);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(12);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(13);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(14);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(15);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(16);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(17);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(18);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(19);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(20);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(21);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(22);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(23);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(24);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(25);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(26);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(27);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(28);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(29);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(30);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(50000001);
        mplew.writeInt(10);
        mplew.writeInt(0);
        mplew.writeInt(200);
        mplew.writeInt(11);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(10);
        mplew.writeInt(1);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(10);
        mplew.writeInt(2);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(10);
        mplew.writeInt(3);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(20);
        mplew.writeInt(4);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(20);
        mplew.writeInt(5);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(20);
        mplew.writeInt(6);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(20);
        mplew.writeInt(7);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(30);
        mplew.writeInt(8);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(30);
        mplew.writeInt(9);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(50);
        mplew.writeInt(10);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(50);
        mplew.writeInt(10);
        mplew.writeInt(0);
        mplew.writeInt(1717986918);
        mplew.writeInt(1071015526);
        mplew.writeInt(1);
        mplew.writeInt(1717986918);
        mplew.writeInt(1071015526);
        mplew.writeInt(2);
        mplew.writeInt(1717986918);
        mplew.writeInt(1071015526);
        mplew.writeInt(3);
        mplew.writeInt(-1717986918);
        mplew.writeInt(1070176665);
        mplew.writeInt(4);
        mplew.writeInt(-1717986918);
        mplew.writeInt(1070176665);
        mplew.writeInt(5);
        mplew.writeInt(-1717986918);
        mplew.writeInt(1070176665);
        mplew.writeInt(6);
        mplew.writeInt(-1717986918);
        mplew.writeInt(1070176665);
        mplew.writeInt(7);
        mplew.writeInt(858993459);
        mplew.writeInt(1069757235);
        mplew.writeInt(8);
        mplew.writeInt(-1717986918);
        mplew.writeInt(1069128089);
        mplew.writeInt(9);
        mplew.writeInt(-1717986918);
        mplew.writeInt(1068079513);
        mplew.writeInt(21);
        mplew.writeInt(10);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(11);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(12);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(13);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(14);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(15);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(16);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(17);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(18);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(19);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(20);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(21);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(22);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(23);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(24);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(25);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(26);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(27);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(28);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(29);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(30);
        mplew.writeInt(20000000);
        mplew.writeInt(0);
        mplew.writeInt(31);
        mplew.writeInt(0);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(1);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(2);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(3);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(4);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(5);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(6);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(7);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(8);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(9);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(10);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(11);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(12);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(13);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(14);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(15);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(16);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(17);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(18);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(19);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(20);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(21);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(22);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(23);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(24);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(25);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(26);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(27);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(28);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(29);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(30);
        mplew.writeInt(100000000);
        mplew.writeInt(0);
        mplew.writeInt(2);
        mplew.writeInt(4009548);
        mplew.writeInt(4009547);
        return mplew.getPacket();
    }



    public static byte[] sendHexaStatsInfo(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_HEXA_STATS_INFO.getValue());
        PacketHelper.encodeSixStats(mplew, chr);

        return mplew.getPacket();
    }

    public static byte[] sendHexaSkillInfo(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_HEXA_SKILL_INFO.getValue());
        PacketHelper.encodeHexaSkills(mplew, chr);
        return mplew.getPacket();
    }

    public static byte[] sendHexaActionResult(int type, int value, int value2, int value3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_HEXA_ACTION_RESULT.getValue());
        mplew.writeInt(type);
        switch (type) {
            case 0: // 開啟核心
                mplew.writeInt(0);
                mplew.writeInt(40000000);
                // 40000000 雅努斯
                break;
            case 1: // 技能核心強化
                mplew.writeInt(value);
                mplew.writeInt(value2);
                mplew.writeInt(value3);
                break;
            case 2: // 開啟核心
                mplew.writeInt(value);
                mplew.writeInt(value2);
                break;
            case 4:
                mplew.writeInt(value);
                break;
            case 3: // 強化結果
                mplew.writeInt(value);
                break;
            case 5: // 切換分頁
                mplew.writeInt(value);
                break;
            case 6: // 初始化
                mplew.writeInt(value);
                break;
            case 7: // 變更核心能力值
                mplew.writeInt(value);
                break;
            case 8:
                mplew.writeInt(value);
                break;
            default:
                break;
        }


        return mplew.getPacket();
    }

    public static byte[] updateDailyGift(String key) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_Message.getValue());
        mplew.write(31);
        mplew.writeInt(15);
        mplew.writeMapleAsciiString(key);
        return mplew.getPacket();
    }
}
