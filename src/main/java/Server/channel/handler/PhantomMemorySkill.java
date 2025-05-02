package Server.channel.handler;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Config.constants.JobConstants;
import Config.constants.skills.幻影俠盜;
import Opcode.Headler.OutHeader;
import Packet.MaplePacketCreator;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author HERTZ
 */
public class PhantomMemorySkill {

    /*
     * v263-4 幻影佩特 封印之瞳竊取操作 - hertz
     */

    public static void MemorySkillChoose(MaplePacketReader slea, MapleClient c) {
        if (c.getPlayer() == null || c.getPlayer().getMap() == null || !JobConstants.is幻影俠盜(c.getPlayer().getJob())) {
            c.sendEnableActions();
            return;
        }
        int skillId = slea.readInt(); //當前角色的封印技能
        Skill skill = SkillFactory.getSkill(skillId);
        if (skill == null) {
            c.sendEnableActions();
            return;
        }
        int teachId = slea.readInt(); //複製的技能
        Skill theskill = SkillFactory.getSkill(teachId);
        if (theskill == null && teachId != 0) {
            c.sendEnableActions();
            return;
        }
        if (c.getPlayer().getSkillLevel(skillId) > 0) {
            c.getPlayer().修改幻影裝備技能(skillId, teachId);
        }
        c.sendEnableActions();
    }

    public static void MemorySkillChange(MaplePacketReader slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null || player.getMap() == null || !JobConstants.is幻影俠盜(player.getJob())) {
            return;
        }
        int skillId = slea.readInt();
        int targetId = slea.readInt();
        boolean delete = slea.readByte() > 0;
        int skillBook = SkillFactory.getIdFromSkillId(skillId);
        Skill skill = SkillFactory.getSkill(skillId);
        int position = -1;
        if (skill == null) {
            c.announce(MaplePacketCreator.幻影複製錯誤());
            return;
        }
        if (delete) {
            SkillEntry ret = player.getSkills().get(skillId);
            if (ret == null) {
                c.announce(MaplePacketCreator.幻影複製錯誤());
                return;
            }
            player.changeSingleSkillLevel(skill, -1, (byte) -1);
            c.announce(MaplePacketCreator.幻影刪除技能(skillBook, ret.position));
        } else {
            MapleCharacter target = player.getMap().getPlayerObject(targetId);
            if (target != null) {
                int level = target.getSkillLevel(skill);
                if (level > 0) {
                    if (skillBook > 0) {
                        int skillLevel = 0;
                        switch (skillBook) {
                            case 1:
                                skillLevel = c.getPlayer().getSkillLevel(幻影俠盜.盜亦有道Ⅰ);
                                break;
                            case 2:
                                skillLevel = c.getPlayer().getSkillLevel(幻影俠盜.盜亦有道Ⅱ);
                                break;
                            case 3:
                                skillLevel = c.getPlayer().getSkillLevel(幻影俠盜.盜亦有道Ⅲ);
                                break;
                            case 4:
                                skillLevel = c.getPlayer().getSkillLevel(幻影俠盜.盜亦有道Ⅳ);
                                break;
                            case 5:
                                skillLevel = c.getPlayer().getSkillLevel(幻影俠盜.盜亦有道H);
                                break;
                        }
                        if (level > skillLevel) { //檢測複製技能等級是否大於當前封印天賦的等級
                            skillLevel = level; //如果大於就設置這個為封印天賦的等級
                        }
                        if (skillLevel > skill.getMaxLevel()) { //檢測技能的等級大於複製技能的最大等級
                            skillLevel = skill.getMaxLevel();
                        }
                        SkillEntry ret = c.getPlayer().getSkills().get(skillId);
                        int masterLevel = target.getMasterLevel(skill);
                        int[][] a = {{0, 4}, {4, 8}, {8, 11}, {11, 13}, {13, 15}};
                        if (ret != null) { //如果技能列表中有這個複製的技能
                            position = ret.position;
                            player.changeSkillRecord(skill, level, masterLevel, -1L, 0, 0, (byte) position);
                        } else {
                            for (int i = a[skillBook - 1][0]; i < a[skillBook - 1][1]; i++) {
                                if (player.getStealMemorySkill(i) == 0) {
                                    position = i;
                                    player.changeSkillRecord(skill, level, masterLevel, -1L, 0, 0, (byte) i);
                                    break;
                                }
                            }
                        }
                        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                        mplew.writeOpcode(OutHeader.LP_ChangeStealMemoryResult);
                        mplew.write(1);
                        mplew.write(0);
                        mplew.writeInt(skillBook);
                        mplew.writeInt(position - a[skillBook - 1][0]);
                        mplew.writeInt(skillId);
                        mplew.writeInt(Math.min(level, skillLevel));
                        mplew.writeInt(masterLevel);
                        c.announce(mplew.getPacket());
                    } else {
                        c.getPlayer().dropMessage(1, "複製技能出現錯誤");
                    }
                } else {
                    c.announce(MaplePacketCreator.幻影複製錯誤());
                }
            } else {
                c.announce(MaplePacketCreator.幻影複製錯誤());
            }
        }
        c.sendEnableActions();
    }

    public static void UserRequestStealSkillList(MaplePacketReader slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null || player.getMap() == null || !JobConstants.is幻影俠盜(player.getJob())) {
            return;
        }
        MapleCharacter target = player.getMap().getPlayerObject(slea.readInt());
        int n = 4;
        List<Integer> memorySkills = new LinkedList<>();
        if (target != null) {
            Map<Integer, SkillEntry> skills = target.getSkills();
            for (Entry<Integer, SkillEntry> skill : skills.entrySet()) {
                if (SkillFactory.isMemorySkill(skill.getKey()) && target.getSkillLevel(skill.getKey()) > 0) {
                    memorySkills.add(skill.getKey());
                }
            }
            c.announce(MaplePacketCreator.ResultStealSkillList(n, target, memorySkills));
        } else {
            n = 1;
        }
    }
}