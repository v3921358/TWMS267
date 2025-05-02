package Server.channel.handler;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.hexa.*;
import Client.skills.Skill;
import Client.skills.SkillFactory;
import Config.configs.Config;
import Config.constants.HexaConstants;
import Packet.CWvsContext;
import Packet.MaplePacketCreator;
import tools.Randomizer;
import tools.data.MaplePacketReader;

public class HexaHandler {
    public static void hexaActionHandler(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        c.announce(MaplePacketCreator.enableActions(chr, true));
        int type = slea.readInt();
        switch (type) {
            case 0: { // 技能核心開啟
                int statid = slea.readInt();
                // 取得消耗
                ErdaConsumeOption eco = HexaConstants.hexaOpenSkillCoreInfo().get(HexaConstants.getHexaSkillCoreType(statid));
                chr.reduceEdra(eco.getErda(), eco.getFragments());
                // 增加技能
                int skillLevel = 1;
                MapleHexaSkill mhs = new MapleHexaSkill(statid, skillLevel);
                HexaSkillCoreEntry hce = HexaFactory.getHexaSkills(statid);
                Skill skill = SkillFactory.getSkill(hce.getSkillId());
                if (skill != null && mhs != null) {
                    skillLevel = 1;
                    chr.changeSingleSkillLevel(skill, skillLevel, (byte) skill.getMaxLevel());
                    chr.addHexaSkill(mhs);
                }
                c.announce(CWvsContext.sendHexaSkillInfo(chr));
                c.announce(CWvsContext.sendHexaActionResult(0, 0, statid, 0));

                break;
            }
            case 1: { // 技能核心強化
                int statid = slea.readInt();
                int oldlv = slea.readInt();
                int lv = slea.readInt();
                int erda = slea.readInt();
                int fragments = slea.readInt();
                // 消耗艾爾達數量
                // TODO: 檢查機制待完成
                //ErdaConsumeOption eco = HexaConstants.hexaOpenSkillCoreInfo().get(HexaConstants.getHexaSkillCoreType(statid));
                //chr.reduceEdra(eco.getErda(), eco.getFragments());
                chr.reduceEdra(erda, fragments);
                MapleHexaSkill mhs = chr.getHexaSkills().get(statid);
                HexaSkillCoreEntry hce = HexaFactory.getHexaSkills(statid);
                Skill skill = SkillFactory.getSkill(hce.getSkillId());
                int skillLevel;
                if (skill != null && mhs != null && (skillLevel = chr.getSkillLevel(skill)) < skill.getMaxLevel() && mhs.getSkilllv() < hce.getMaxLevel()) {
                    skillLevel += 1;
                    chr.changeSingleSkillLevel(skill, skillLevel, (byte) skill.getMaxLevel());
                    mhs.setSkilllv(skillLevel);
                    chr.updateHexaSkill(mhs);
                }
                c.announce(CWvsContext.sendHexaSkillInfo(chr));// 增加技能
                c.announce(CWvsContext.sendHexaActionResult(1, 0, statid, lv));
                break;
            }
            case 2: { // 屬性核心開啟
                int statid = slea.readInt();
                int stat1 = slea.readInt();
                int stat2 = slea.readInt();
                int stat3 = slea.readInt();
                MapleHexaStat mss = new MapleHexaStat(statid, 0);
                mss.setStatPreset1(stat1, 0, stat2, 0, stat3, 0);
                mss.setStatPreset2(-1, -1, -1, -1, -1, -1);
                // 消耗艾爾達數量
                ErdaConsumeOption eco = HexaConstants.hexaOpenStatCoreInfo();
                chr.reduceEdra(eco.getErda(), eco.getFragments());

                chr.changeSingleSkillLevel(500071000, 1, 1);
                chr.addHexaStats(mss);
                c.announce(CWvsContext.sendHexaStatsInfo(chr));
                c.announce(CWvsContext.sendHexaActionResult(2, 0, statid, 0));
                break;
            }
            case 3: { // 屬性核心強化
                int statid = slea.readInt();
                MapleHexaStat mss = chr.getHexaStats().get(statid); // 取得目前核心狀態
                int nowLv = mss.getMain0Lv(); // 取得目前主能力值等級
                // 機率 官方設定全部機率加起來100%，所以一定會成功
                double probability = HexaConstants.hexaEnforcementStatProbability[nowLv];
                double now = Randomizer.nextDouble(0, 1);
                if (now < probability) {
                    mss.setStatPreset1(mss.getMain0(), mss.getMain0Lv() + 1, mss.getAddit0S1(), mss.getAddit0S1Lv(), mss.getAddit0S2(), mss.getAddit0S2Lv());
                } else if ((now < 0.5 && mss.getAddit0S1Lv() != 10) || mss.getAddit0S2Lv() == 10) { // 因為附屬能力機率是減去主能力機率後各1半，所以這邊直接0.5
                    mss.setStatPreset1(mss.getMain0(), mss.getMain0Lv(), mss.getAddit0S1(), mss.getAddit0S1Lv() + 1, mss.getAddit0S2(), mss.getAddit0S2Lv());
                } else {
                    mss.setStatPreset1(mss.getMain0(), mss.getMain0Lv(), mss.getAddit0S1(), mss.getAddit0S1Lv(), mss.getAddit0S2(), mss.getAddit0S2Lv() + 1);
                }

                // 消耗艾爾達數量
                ErdaConsumeOption eco = HexaConstants.hexaEnforcementStatInfo().get(nowLv);
                chr.reduceEdra(eco.getErda(), eco.getFragments());

                chr.updateHexaStat(mss);
                c.announce(CWvsContext.sendHexaStatsInfo(chr));
                c.announce(CWvsContext.sendHexaActionResult(3, statid, 0, 0));
                break;
            }
            case 4: { // 屬性核心儲存變更
                int statid = slea.readInt();
                MapleHexaStat mss = chr.getHexaStats().get(statid);
                mss.setStatPreset2(mss.getMain0(), mss.getMain0Lv(), mss.getAddit0S1(), mss.getAddit0S1Lv(), mss.getAddit0S2(), mss.getAddit0S2Lv());
                chr.updateHexaStat(mss);
                c.announce(CWvsContext.sendHexaStatsInfo(chr));
                break;
            }
            case 5: { // 屬性核新分頁套用
                int statid = slea.readInt();
                int preset = slea.readByte();
                MapleHexaStat mss = chr.getHexaStats().get(statid);
                mss.setPreset(preset);
                chr.updateHexaStat(mss);
                c.announce(CWvsContext.sendHexaStatsInfo(chr));
                c.announce(CWvsContext.sendHexaActionResult(5, 0, 0, 0));
                break;
            }
            case 6: { // 屬性初始化
                int statid = slea.readInt();
                MapleHexaStat mss = chr.getHexaStats().get(statid);
                mss.setStatPreset1(mss.getMain0(), 0, mss.getAddit0S1(), 0, mss.getAddit0S2(), 0);
                //消耗楓幣
                chr.updateHexaStat(mss);
                c.announce(CWvsContext.sendHexaStatsInfo(chr));
                c.announce(CWvsContext.sendHexaActionResult(6, 0, 0, 0));
                break;
            }
            case 7: { // 屬性核心能力值變更
                long meso = slea.readLong();
                int unk = slea.readInt();
                int statid = slea.readInt();
                int stat1 = slea.readInt();
                int stat2 = slea.readInt();
                int stat3 = slea.readInt();
                MapleHexaStat mss = chr.getHexaStats().get(statid);
                mss.setStatPreset1(stat1, mss.getMain0Lv(), stat2, mss.getAddit0S1Lv(), stat3, mss.getAddit0S2Lv());
                //消耗楓幣
                chr.updateHexaStat(mss);
                c.announce(CWvsContext.sendHexaStatsInfo(chr));
                c.announce(CWvsContext.sendHexaActionResult(7, 0, 0, 0));
                break;
            }
            case 8: { // 初始化功能的檢查密碼
                String pass = slea.readMapleAsciiString();
                if (Config.isDevelop() || c.CheckSecondPassword(pass)) {
                    c.announce(CWvsContext.sendHexaActionResult(8, 0, 0, 0));
                } else {
                    c.announce(CWvsContext.sendHexaActionResult(8, 1, 0, 0));
                }
                break;
            }
            default:
                System.out.println("未知的操作碼:" + type);
                c.announce(CWvsContext.sendHexaActionResult(8, 1, 0, 0));
        }
    }
}
