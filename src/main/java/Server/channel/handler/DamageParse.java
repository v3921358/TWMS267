package Server.channel.handler;

import Client.*;
import Client.force.MapleForceAtom;
import Client.force.MapleForceFactory;
import Client.inventory.Equip;
import Client.inventory.Item;
import Client.inventory.MapleInventoryType;
import Client.inventory.MapleWeapon;
import Client.skills.ExtraSkill;
import Client.skills.Skill;
import Client.skills.SkillFactory;
import Client.skills.handler.AbstractSkillHandler;
import Client.skills.handler.SkillClassApplier;
import Client.skills.handler.SkillClassFetcher;
import Client.stat.PlayerStats;
import Client.status.MonsterStatus;
import Config.configs.ServerConfig;
import Config.constants.JobConstants;
import Config.constants.SkillConstants;
import Config.constants.skills.*;
import Config.constants.skills.冒險家_技能群組.type_劍士.初心者;
import Config.constants.skills.冒險家_技能群組.type_劍士.英雄;
import Config.constants.skills.冒險家_技能群組.type_法師.主教;
import Config.constants.skills.冒險家_技能群組.type_法師.冰雷;
import Config.constants.skills.冒險家_技能群組.type_法師.法師;
import Config.constants.skills.冒險家_技能群組.type_法師.火毒;
import Config.constants.skills.冒險家_技能群組.*;
import Config.constants.skills.皇家騎士團_技能群組.*;
import Net.auth.Auth;
import Net.server.MapleInventoryManipulator;
import Net.server.Timer;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.Element;
import Net.server.life.MapleMonster;
import Net.server.life.MapleMonsterStats;
import Net.server.life.MobSkill;
import Net.server.maps.MapleMap;
import Net.server.maps.MapleMapObject;
import Net.server.maps.MapleMapObjectType;
import Net.server.maps.MapleSummon;
import Opcode.Headler.InHeader;
import Opcode.Headler.OutHeader;
import Packet.BuffPacket;
import Packet.ForcePacket;
import Packet.MaplePacketCreator;
import Packet.SummonPacket;
import SwordieX.client.party.Party;
import SwordieX.client.party.PartyMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.HexTool;
import tools.Pair;
import tools.Randomizer;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 解析玩家所有的攻擊行為.
 *
 * @author dongjak
 */
public class DamageParse {

    private static final Logger log = LoggerFactory.getLogger("AttackParse");

    public static AttackInfo parseAttack(InHeader header, MaplePacketReader lea, MapleCharacter chr) {
        AttackInfo ai = new AttackInfo();
        try {
            switch (header) {
                case CP_UserMeleeAttack:
                    ai.attackHeader = OutHeader.LP_UserMeleeAttack;
                    break;
                case CP_UserShootAttack:
                    ai.attackHeader = OutHeader.LP_UserShootAttack;
                    break;
                case UserNonTargetForceAtomAttack:
                case CP_UserMagicAttack:
                    ai.attackHeader = OutHeader.LP_UserMagicAttack;
                    break;
            }
            if (header == InHeader.CP_UserShootAttack) {
                ai.boxAttack = lea.readByte() != 0;
            }
            if (header == InHeader.UserNonTargetForceAtomAttack) {
                lea.readInt();
                lea.readInt();
                lea.readInt();
                lea.readInt();
                lea.readInt();
            }
            ai.fieldKey = lea.readByte(); // ex : 0x01 CHANGE MAP RANDOM 0x09
            ai.numAttackedAndDamage = lea.readByte();
            ai.mobCount = (byte) (ai.numAttackedAndDamage >>> 4 & 0xF); //攻擊怪物數
            ai.hits = (byte) (ai.numAttackedAndDamage & 0xF); //攻擊次數
            ai.skillId = lea.readInt(); //技能ID
            ai.skllv = lea.readInt();//V.162 byte=>int
            switch (header) {
                case CP_UserMagicAttack:
                case CP_UserBodyAttack:
                case CP_UserAreaDotAttack:
                case UserSpotlightAttack:
                    break;
                default:
                    ai.addAttackProc = lea.readByte();
                    break;
            }
            lea.readInt();//crc1
            lea.readInt();//crc2
            lea.readInt();//crc3
            attackBonusRecv(lea, ai);
            calcAttackPosition(lea, chr, ai);
            int skillID = ai.skillId;

            // 更新後的封包解析
            if (SkillConstants.isKeyDownSkill(skillID) || SkillConstants.isSuperNovaSkill(skillID) || skillID == 暗影神偷.暗影霧殺 || skillID == 天使破壞者.超級超新星 || skillID == 聖魂劍士.宇宙融合_爆炸 || skillID == 亞克.根源的記憶) {
                ai.keyDown = lea.readInt();
            }
            if (SkillConstants.isRushBombSkill(skillID)) {
                ai.grenadeId = lea.readInt();
            }
            if (SkillConstants.isZeroSkill(skillID)) {
                ai.zero = lea.readByte();
            }
            if (SkillConstants.isUsercloneSummonedAbleSkill(skillID)) {
                ai.bySummonedID = lea.readInt();
            }
            switch (skillID) {
                case 神射手.真必殺狙擊_1:
                case 暗夜行者.影之槍_2:
                    lea.readInt(); // Unknown
                    lea.readInt(); // Unknown
                    break;
                case 80001836:
                case 烈焰巫師.Return_元素之炎IV:
                case 烈焰巫師.Return_元素之炎III_1:
                case 烈焰巫師.Return_元素之炎II_1:
                case 烈焰巫師.Return_元素之炎I_1:
                    lea.readInt();
                    break;
            }
            ai.buckShot = lea.readByte();
            ai.someMask = lea.readByte();
            if (header == InHeader.CP_UserShootAttack) {
                int idk3 = lea.readInt();
                ai.isJablin = lea.readByte() != 0;
                if (ai.boxAttack) {
                    int boxIdk1 = lea.readInt();
                    short boxIdk2 = lea.readShort();
                    short boxIdk3 = lea.readShort();
                }
            }
            switch (header) {
                case CP_UserMeleeAttack:
                case CP_UserShootAttack:
                case CP_UserBodyAttack:
                case CP_UserAreaDotAttack:
                    short maskie = lea.readShort();
                    ai.display = maskie & 255;
                    ai.direction = maskie >>> 8 & 255;
                    break;
                default:
                    ai.display = lea.readByte();
                    ai.direction = lea.readByte();
                    break;
            }
            ai.requestTime = lea.readInt();
            ai.attackActionType = lea.readByte();
            if (SkillConstants.isEvanForceSkill(skillID)) {
                ai.idk0 = lea.readByte();
            }
            if (skillID == 精靈遊俠.落葉旋風射擊 || skillID == 80001915 || skillID == 傑諾.戰鬥轉換_分裂) {
                int idk5 = lea.readInt();
                int x = lea.readInt(); // E0 6E 1F 00
                int y = lea.readInt();
            }
            ai.attackSpeed = lea.readByte();
            ai.tick = lea.readInt();
            if (skillID == 狂豹獵人.另一個咬擊) {
                lea.readInt();
            }
            if (skillID == 80011561 || skillID == 80002463 || skillID == 80001762 || skillID == 80002212) {
                lea.readInt();
            }
            int unk = lea.readInt();// unk
            if (header == InHeader.CP_UserMeleeAttack || header == InHeader.CP_UserAreaDotAttack) {
                ai.finalAttackLastSkillID = lea.readInt();
                if (skillID > 0 && ai.finalAttackLastSkillID > 0) {
                    ai.finalAttackByte = lea.readByte();
                }
            }
            if (header == InHeader.CP_UserShootAttack) {
                int bulletSlot = lea.readInt();
                ai.cashSlot = lea.readShort();
                byte idk = lea.readByte();
                lea.skip(8);
            }
            switch(skillID){
                case 拳霸.炫風拳:
                    ai.ignorePCounter = lea.readByte() != 0;
                    break;
                case 隱月.靈魂帳幕:
                    ai.spiritCoreEnhance = lea.readInt();
                    break;
                case 英雄.劍之幻象:
                case 英雄.劍之幻象_1:
                case 英雄.劍之幻象_2:
                    if (skillID > 0 && ai.finalAttackLastSkillID == 0) {
                        lea.readByte();
                    }
                    lea.readInt(); //265更新後新增
                    break;
            }
            if (header == InHeader.UserNonTargetForceAtomAttack) {
                lea.readInt(); // hardcoded 0
            }

            // 更新後的怪物攻擊資訊解析
            for (int i = 0; i < ai.mobCount; i++) {
                AttackMobInfo mai = new AttackMobInfo();
                mai.mobId = lea.readInt();
                mai.hitAction = lea.readByte();
                mai.left = lea.readByte();
                mai.idk3 = lea.readByte();
                mai.forceActionAndLeft = lea.readByte(); // (v286 << 7) | v515 & 0x7F)
                mai.frameIdx = lea.readByte();
                mai.templateID = lea.readInt();
                mai.calcDamageStatIndexAndDoomed = lea.readByte(); // 1st bit for bDoomed, rest for calcDamageStatIndex
                mai.hitX = lea.readShort();
                mai.hitY = lea.readShort();
                mai.oldPosX = lea.readShort(); // ?
                mai.oldPosY = lea.readShort(); // ?
                if (header == InHeader.CP_UserMagicAttack) {
                    mai.hpPerc = lea.readByte();
                    if (skillID == 80001835) {
                        mai.magicInfo = lea.readByte();
                    } else {
                        mai.magicInfo = lea.readShort();
                    }
                } else {
                    mai.idk6 = lea.readShort();
                }
                lea.readInt();
                lea.readInt();// according to IDA it only int 1
                lea.readByte();
                mai.damages = new long[ai.hits];
                for (int j = 0; j < ai.hits; j++) {
                    mai.damages[j] = lea.readLong();
                }
                mai.mobUpDownYRange = lea.readInt();
                // sub_142B6F6C0 Start
                lea.readInt(); // crc
                lea.readInt();
                // sub_142B6F6C0 End
                if (skillID == 爆拳槍神.雙重壓迫 || skillID == 墨玄.墨玄_一轉_神功_昇天拳) {
                    mai.isResWarriorLiftPress = lea.readByte() != 0;
                }
                if (ai.skillId == 凱內西斯.猛烈心靈 || ai.skillId == 凱內西斯.猛烈心靈2_1 || ai.skillId == 凱內西斯.終極技_心靈射擊) {
                    lea.skip(8);
                }

                // Begin PACKETMAKER::MakeAttackInfoPacket
                mai.type = lea.readByte();
                mai.currentAnimationName = "";
                if (mai.type == 1) {
                    mai.currentAnimationName = lea.readMapleAsciiString();
                    lea.readMapleAsciiString();
                    mai.animationDeltaL = lea.readInt();
                    mai.hitPartRunTimesSize = lea.readInt();
                    if (mai.hitAction == -1) {
                        mai.hitPartRunTimes = new String[mai.hitPartRunTimesSize];
                        for (int j = 0; j < mai.hitPartRunTimesSize; j++) {
                            mai.hitPartRunTimes[j] = lea.readMapleAsciiString();
                        }
                    }
                } else if (mai.type == 2) {
                    mai.currentAnimationName = lea.readMapleAsciiString();
                    lea.readMapleAsciiString();
                    mai.animationDeltaL = lea.readInt();
                }
                lea.readByte();
                lea.readShort();
                lea.readShort();
                lea.readPos();
                lea.readShort();
                lea.readShort();

                // zero 7
                lea.readByte();
                lea.readByte();
                lea.readInt();
                lea.readByte();
                lea.readInt();
                lea.readInt();

                int count = lea.readInt();
                for (int c = 0; c < count; c++) {
                    lea.readLong();
                }
                lea.readInt();
                // End PACKETMAKER::MakeAttackInfoPacket
                ai.mobAttackInfo.add(mai);

            }


            // 更新後的技能位置資訊解析
            if (skillID == 凱撒.聲望 || skillID == 傑諾.毀滅轟炸 || SkillConstants.isScreenCenterAttackSkill(skillID)) {
                ai.ptTarget = lea.readPos();
            } else {
                if (skillID == 夜光.末日審判 || skillID == 80001837) {
                    ai.x = lea.readShort();
                    ai.y = lea.readShort();
                } else if (header == InHeader.CP_UserMagicAttack) {
                    short forcedX = lea.readShort();
                    short forcedY = lea.readShort();
                    boolean dragon = lea.readByte() != 0 && JobConstants.is龍魔導士(chr.getJob());
                    ai.forcedX = forcedX;
                    ai.forcedY = forcedY;
                    if (dragon) {
                        short rcDstRight = lea.readShort();
                        short rectRight = lea.readShort();
                        short x = lea.readShort();
                        short y = lea.readShort();
                        lea.readByte(); // always 0
                        lea.readByte(); // -1
                        lea.readByte(); // 0
                        ai.rcDstRight = rcDstRight;
                        ai.rectRight = rectRight;
                        ai.x = x;
                        ai.y = y;
                    }
                }
                if (skillID == 烈焰巫師.烈炎爆發_2) {
                    ai.option = lea.readInt();
                }
                if (skillID == 火毒.地獄爆發) {
                    byte size = lea.readByte();
                    int[] mists = new int[size];
                    for (int i = 0; i < size; i++) {
                        mists[i] = lea.readInt();
                    }
                    ai.mists = mists;
                }
                if (skillID == 火毒.致命毒霧) {
                    byte force = lea.readByte();
                    short forcedXSh = lea.readShort();
                    short forcedYSh = lea.readShort();
                    ai.force = force;
                    ai.forcedXSh = forcedXSh;
                    ai.forcedYSh = forcedYSh;
                }
                if (skillID == 火毒.藍焰斬) {
                    ai.position = lea.readPos();
                    lea.readInt(); // AtomObjectID
                    lea.readInt(); // 1
                    ai.skillposition = lea.readPosInt();
                    lea.skip(36);
                }
                if (skillID == 80001835) { // Soul Shear
                    byte sizeB = lea.readByte();
                    int[] idkArr2 = new int[sizeB];
                    short[] shortArr2 = new short[sizeB];
                    for (int i = 0; i < sizeB; i++) {
                        idkArr2[i] = lea.readInt();
                        shortArr2[i] = lea.readShort();
                    }
                    short delay = lea.readShort();
                    ai.mists = idkArr2;
                    ai.shortArr = shortArr2;
                    ai.delay = delay;
                }
                if (SkillConstants.isSuperNovaSkill(skillID)) {
                    ai.ptAttackRefPoint = lea.readPos();
                }
                if (skillID == 神之子.進階威力震擊_衝擊波) {
                    ai.idkPos = lea.readPos();
                }
                if (header == InHeader.CP_UserAreaDotAttack) {
                    ai.pos = lea.readPos();
                }
                if (SkillConstants.isAranFallingStopSkill(skillID)) {
                    ai.fh = lea.readByte();
                }
                if (header == InHeader.CP_UserShootAttack && skillID / 1000000 == 33) {
                    ai.bodyRelMove = lea.readPos();
                }

                if (skillID == 狂狼勇士.極速巔峰_目標鎖定 || skillID == 爆拳槍神.神聖連發重擊) {
                    ai.teleportPt = lea.readPos();
                }
                if (header == InHeader.CP_UserShootAttack && SkillConstants.isKeydownSkillRectMoveXY(skillID)) {
                    ai.keyDownRectMoveXY = lea.readPos();
                }
                if (skillID == 凱撒.龍烈焰 || skillID == 凱撒.惡魔之嘆 || skillID == 幻影俠盜.玫瑰四重曲) {
                    ai.Vx = lea.readShort();
                    short x, y;
                    for (int i = 0; i < ai.Vx; i++) {
                        x = lea.readShort();
                        y = lea.readShort();
                    }
                }
                if (skillID == 神之子.進階碎地猛擊) {
                    // CUser::EncodeAdvancedEarthBreak
                    // TODO
                    lea.readShort();
                    lea.readShort();
                    lea.readShort();
                }
                if (skillID == 14111006 && ai.grenadeId != 0) {
                    ai.grenadePos = lea.readPos();
                }
                if (skillID == 80001914) { // first skill is Spikes Royale, not needed?
                    ai.fh = lea.readByte();
                }
                if (header == InHeader.CP_UserShootAttack && SkillConstants.isZeroSkill(skillID) && lea.available() >= 4) {
                    ai.position = lea.readPos();
                }
                if (skillID == 主教.和平使者_1) {
                    lea.readInt();
                    lea.readInt();
                    ai.skillposition = lea.readPos();
                    lea.readByte();
                }
            }
            return ai;
        } catch (Exception e) {
            log.error("Error parseAttack, skill:" + ai.skillId, e);
            return null;
        }
    }

    private static void attackBonusRecv(MaplePacketReader lea, AttackInfo ai) {
        // sub_140A377D0 [v258.1]
        lea.readByte();//V.184 new
        ai.starSlot = lea.readShort();
        lea.readInt(); // 子彈道具ID
        lea.readByte();
        lea.readByte();
        lea.readByte();
        lea.readInt();//V.257.1 byte->int

        ai.position = lea.readPosInt(); // 角色坐標

        lea.readInt();//V.177 new
        lea.readInt();//V.179 new
        lea.readInt();//V.179 new
        int count = lea.readInt();
        for (int i = 0; i < count; i++) {
            lea.readInt();
        }
        lea.readBool(); // 通過按鍵使用
        lea.readByte();
        lea.readInt(); // v257.1
        lea.readInt(); // v257.1

        //lea.readLong(); // v257.1
        lea.readLong(); // v257.1
        lea.readLong(); // v262.4
        lea.readByte(); // v257.1
        int count2 = lea.readInt(); // v257.1
        for (int i = 0; i < count2; i++) {
            lea.readInt(); // v257.1
        }

        lea.readInt(); // v258.1
        lea.readInt(); // v258.1  -1
        lea.readInt(); // v258.1
        lea.readInt(); // v258.1
        lea.readInt(); // v258.1
        lea.readMapleAsciiString();
        lea.readInt(); // v262.4

    }

    public static void calcAttackPosition(MaplePacketReader lea, MapleCharacter chr, AttackInfo ai) {
        // sub_140AA3FF0 [v258.1]
        lea.readInt();
        if (lea.readByte() != 0) {
            if (ai == null) {
                ai = new AttackInfo();
            }
            lea.readInt();//V.181 new
            int type;
            while ((type = lea.readInt()) > 0) {
                switch (type) {
                    case 1: {
                        if (lea.readByte() != 0) {
                            lea.readInt();
                            lea.readInt();
                            lea.readInt();
                            lea.readByte();
                            byte len = lea.readByte(); //V.177 new
                            for (byte i = 0; i < len; i++) {
                                lea.readInt(); //V.177 new
                            }
                        }
                        break;
                    }
                    case 2:// Used by Endless Argony
                        if (lea.readByte() != 0) {
                            lea.readByte();
                            lea.readByte();
                            lea.readInt();
                            lea.readInt();
                            lea.readBool();
                            lea.readInt();
                            lea.readInt();
                            lea.skip(16);//V.181 new
                        }
                        break;
                    case 3:// Used by Ark charges
                        if (lea.readByte() != 0) {
                            lea.readByte();
                            lea.readInt();
                        }
                        break;
                    case 4:
                        if (lea.readBool()) {
                            ai.rect = lea.readRect();
                            ai.skillposition = lea.readPosInt();
                            lea.readLong(); // v257.1

                            lea.readInt(); //V.184 new
                        }
                        break;
                    case 5:
                    case 6:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 22:
                    case 23:
                        lea.readByte();
                        break;
                    case 7:
                        if (lea.readByte() != 0) {
                            ai.skillposition = lea.readPosInt();
                            lea.readByte();
                            lea.readByte();
                            lea.readByte();
                            lea.readByte();
                            ai.left = lea.readByte() != 0;
                        }
                        break;
                    case 8:
                        if (lea.readByte() != 0) {
                            lea.readInt();
                            lea.readInt();
                            lea.readInt();

                            lea.readInt();
                        }
                        break;
                    case 9:
                        if (lea.readByte() != 0) {
                            lea.readInt();
                            lea.readInt();
                            lea.readInt();
                            lea.readInt();

                            lea.readInt();
                        }
                        break;
                    case 15:
                        if (lea.readBool()) {
                            int len = lea.readInt(); //V.177 new
                            for (int i = 0; i < len; i++) {
                                lea.readInt();
                                lea.readInt();
                                lea.readInt();
                                lea.readInt();
                            }
                            lea.readInt();
                            lea.readInt();
                            lea.readInt();
                            lea.readInt();
                        }
                        break;
                    case 19:
                        if (lea.readBool()) {
                            ai.unInt1 = lea.readInt();
                            lea.readInt();
                            lea.readInt();

                            lea.readInt();
                            continue;
                        }
                        break;
                    case 20:
                        if (lea.readBool()) {
                            int len = lea.readInt(); //V.177 new
                            for (int i = 0; i < len; i++) {
                                lea.readInt();
                            }
                        }
                        break;
                    case 24:
                        if (lea.readBool()) {
                            lea.readInt();
                            lea.readInt();
                            lea.readInt();
                            lea.readInt();
                            lea.readPosInt();
                            lea.readPosInt();
                        }
                        break;
                    case 25:
                        if (lea.readBool()) {
                            int size = lea.readInt();
                            for (int i = 0; i < size; i++) {
                                ai.skillSpawnInfo.add(new Pair<>(lea.readInt(), lea.readPosInt()));
                            }
                        }
                        break;
                    case 29:
                        if (lea.readBool()) {
                            lea.readInt();
                        }
                        break;
                    case 34:
                        if (lea.readBool()) {
                            int size = lea.readInt();
                            for (int i = 0; i < size; i++) {
                                lea.readInt();
                            }
                        }
                        break;
                    case 37:
                        if (lea.readBool()) {
                            lea.readInt();

                            ai.unInt1 = lea.readInt();
                            lea.readInt();
                            lea.readInt();
                            lea.readInt();
                            ai.skillposition = lea.readPosInt();
                            ai.pos = lea.readPosInt();
                            lea.readInt();
                            lea.readBool();
                            lea.readByte();
                            lea.readByte();
                        }
                        break;
                    case 39:
                        if (lea.readBool()) {
                            lea.readInt();
                            lea.readInt();
                        }
                        break;
                    case 42:
                        if (lea.readBool()) {
                            int size = lea.readInt();
                            for (int i = 0; i < size; i++) {
                                lea.readInt();
                            }
                        }
                        break;
                    case 43:
                        if (lea.readBool()) {
                            lea.readInt();
                        }
                        break;
                    case 45:
                        if (lea.readBool()) {
                            lea.readInt();
                            lea.readInt();
                            lea.readByte();
                            lea.readInt();
                            lea.readInt();
                        }
                        break;
                    case 48:
                    case 49:
                        if (lea.readBool()) {
                            int size = lea.readInt();
                            for (int i = 0; i < size; i++) {
                                lea.readLong();
                            }
                        }
                        break;
                }
            }
            int unk340 = 0;
            int unk338 = 0;
            int v8 = 0, result = 0;
            sub_1408CA760(lea, unk340 - unk338);
            if (unk338 != unk340) {
                do {
                    lea.readByte();
                    v8++;
                    result = unk340 - unk338;
                } while (v8 < result);
            }
        }
    }

    public static void sub_1408CA760(MaplePacketReader oPacket, int a2) {
        int v3 = 2 * a2 ^ (a2 >> 31);
        if (v3 >= 128) {
            do {
                oPacket.readByte(); // v3 | 0x80
                v3 >>= 7;
            }
            while (v3 >= 0x80);
        }
        oPacket.readByte(); // v3 & 0x7F
    }

    public static AttackInfo parseSummonAttack(MaplePacketReader slea, MapleCharacter chr) {
        final AttackInfo ai = new AttackInfo();
        try {
            ai.attackType = AttackInfo.AttackType.SummonedAttack;
            ai.lastAttackTickCount = slea.readInt();
            int summonSkill = slea.readInt();
            ai.skillId = slea.readInt();
            if (ai.skillId == 0) {
                ai.skillId = summonSkill;
            }
            slea.skip(1);
            slea.readInt();
            switch (ai.skillId) {
                case 伊利恩.即刻反應_破滅:
                case 伊利恩.即刻反應_破滅Ⅱ:
                    slea.readInt();
                    break;
            }
            slea.readByte();
            slea.readByte();
            slea.readInt();
            String attackTypeString = slea.readMapleAsciiString();
            ai.display = slea.readByte();
            ai.numAttackedAndDamage = slea.readByte();
            ai.mobCount = (byte) (ai.numAttackedAndDamage >>> 4 & 0xF);
            ai.hits = (byte) (ai.numAttackedAndDamage & 0xF);
            slea.readByte();
            final int n = 26 + ai.mobCount * (28 + (ai.hits << 3) + 14) + 4;
            if (ai.skillId == 機甲戰神.磁場) {
                if (slea.available() > n) {
                    slea.readInt();
                    slea.readInt();
                    slea.readInt();
                } else {
                    ai.unInt1 = 1;
                }
            }
            ai.position = slea.readPos();
            ai.skillposition = slea.readPos();
            slea.readByte();
            slea.readInt(); // -1
            switch (ai.skillId) { /* 範圍殺傷 處理 假傷*/
                case 夜使者.絕殺領域:
                case 暗夜行者.SUMMON_ATTACK_闇黑天魔:
                case 卡莉.死亡綻放:
                case 通用V核心.盜賊通用.爆破飛毒殺:
                case 槍神.海盜砲擊艇:
                case 槍神.海盜砲擊艇_1:
                case 槍神.海盜砲擊艇_2:
                    /* 僅區分代碼 */
                case 傑諾.滅世雷射光:
                    break;
                default:
                    slea.readInt();
                    break;
            }
            ai.starSlot = slea.readShort();
            if (ai.starSlot > 0) {
                slea.readInt(); // itemID
            }
            final int skillId = slea.readInt();
            if (skillId > 0 && SkillFactory.getSkill(skillId) != null) {
                ai.skillId = skillId;
            }
            if (ai.skillId == 槍神.海盜砲擊艇_2) {
                ai.skillposition = slea.readPosInt();
            }
            for (byte i = 0; i < ai.mobCount; ++i) {
                AttackMobInfo mai = new AttackMobInfo();
                mai.mobId = slea.readInt();
                mai.templateID = slea.readInt();
                mai.hitAction = slea.readByte();
                mai.left = slea.readByte();
                mai.idk3 = slea.readByte();
                mai.forceActionAndLeft = slea.readByte();
                mai.frameIdx = slea.readByte();
                mai.templateID = slea.readInt();
                mai.calcDamageStatIndexAndDoomed = slea.readByte();
                mai.hitX = slea.readShort();
                mai.hitY = slea.readShort();
                mai.oldPosX = slea.readShort();
                mai.oldPosY = slea.readShort();
                mai.idk6 = slea.readShort();
                slea.readShort();
                slea.readShort();
                slea.readInt();
                slea.readInt();
                slea.readByte();
                mai.damages = new long[ai.hits];
                for (byte j = 0; j < ai.hits; ++j) {
                    final long damage = slea.readLong();
                    if (chr.isDebug()) {
                        chr.dropMessage(6, "[Summon Attack] Mob OID: " + mai.mobId + " Idx:" + (j + 1) + " - Damage: " + damage);
                    }
                    mai.damages[j] = damage;
                }
                mai.mobUpDownYRange = slea.readInt();

                slea.readByte();
                slea.readByte();
                slea.readInt();

                slea.readInt();
                slea.readInt();
                slea.readPos();
                slea.skip(2);
                slea.skip(1);
                slea.skip(4); // templateID

                slea.readInt();

                int count = slea.readInt();
                for (int c = 0; c < count; c++) {
                    slea.readLong();
                }
                slea.read(4);
                ai.mobAttackInfo.add(mai);
            }
            return ai;
        } catch (Exception e) {
            log.error("Error parseSummonAttack, skill:" + ai.skillId, e);
            return null;
        }
    }

    public static boolean applyAttackCooldown(MapleStatEffect effect, MapleCharacter chr, int skillid, boolean isChargeSkill, boolean isBuff, boolean energy) {
        int cooldownTime = effect.getCooldown(chr);
        if (cooldownTime == 0) {
            cooldownTime = SkillFactory.getSkill(SkillConstants.getLinkedAttackSkill(skillid)).getEffect(chr.getTotalSkillLevel(skillid)).getCooldown(chr);
        }
        if (cooldownTime > 0) {
            if (chr.isSkillCooling(skillid) && !isChargeSkill && !isBuff && !SkillConstants.isNoDelaySkill(skillid)) {
                chr.dropMessage(5, "技能由於冷卻時間限制，暫時無法使用。");
                chr.getClient().sendEnableActions();
                return false;
            } else {
//                if (chr.isAdmin() || energy) {
//                    if (isBuff) {
//                        chr.dropDebugMessage(2, "[技能冷卻] 為GM消除技能冷卻時間, 原技能冷卻時間:" + cooldownTime + "秒");
//                    }
//                } else {
                chr.registerSkillCooldown(skillid, System.currentTimeMillis(), cooldownTime);
//                    chr.send(MaplePacketCreator.skillCooldown(SkillConstants.getLinkedAttackSkill(skillid), cooldownTime));
//                }
            }
        }
        return true;
    }

    /**
     * 處理攻擊怪物觸發
     */
    public static void afterAttack(MapleStatEffect attackEffect, MapleCharacter player, long totalDamage, Point pos, AttackInfo ai, boolean passive) {
        final PlayerStats stats = player.getStat();
        final PlayerSpecialStats specialStats = player.getSpecialStat();
        int job = player.getJob();
        /*HP恢復機率*/
        int hpHeal = 0, mpHeal = 0;
        for (Pair<Integer, Integer> pair : stats.hpRecover_onAttack.values()) {
            if (totalDamage > 0 && Randomizer.isSuccess(pair.right)) {
                hpHeal += pair.left * stats.getCurrentMaxHP() / 100;
            }
        }
        // 內面暴風
        if (totalDamage > 0L && player.checkInnerStormValue()) {
            player.modifyInnerStormValue(1);
        }
        if (totalDamage > 0L) {
            int add_skillId = 80002890;
            Skill skill;
            MapleStatEffect addSkillEffect;
            if ((attackEffect == null || attackEffect.getSourceId() != add_skillId) && ((skill = SkillFactory.getSkill(add_skillId)) != null && (addSkillEffect = skill.getEffect(1)) != null) && player.getBuffStatValueHolder(add_skillId) != null && !player.isSkillCooling(add_skillId)) {
                player.registerSkillCooldown(add_skillId, addSkillEffect.getCooldown(player), true);
                ExtraSkill eskill = new ExtraSkill(add_skillId, player.getPosition());
                eskill.Value = 1;
                eskill.FaceLeft = player.isFacingLeft() ? 0 : 1;
                player.send(MaplePacketCreator.RegisterExtraSkill(add_skillId, Collections.singletonList(eskill)));
            }
        }
        AbstractSkillHandler handler = attackEffect == null ? null : attackEffect.getSkillHandler();
        if (attackEffect == null) {
            handler = SkillClassFetcher.getHandlerByJob(player.getJobWithSub());
        }
        int handleRes = -1;
        if (handler != null) {
            SkillClassApplier applier = new SkillClassApplier();
            applier.effect = attackEffect;
            applier.totalDamage = totalDamage;
            applier.pos = pos;
            applier.passive = passive;
            applier.hpHeal = hpHeal;
            applier.mpHeal = mpHeal;
            applier.ai = ai;
            handleRes = handler.onAfterAttack(player, applier);
            if (handleRes == 0) {
                return;
            } else if (handleRes == 1) {
                attackEffect = applier.effect;
                totalDamage = applier.totalDamage;
                pos = applier.pos;
                passive = applier.passive;
                hpHeal = applier.hpHeal;
                mpHeal = applier.mpHeal;
                ai = applier.ai;
            }
        }
        if (hpHeal > 0 || mpHeal > 0) {
            player.addHPMP(Math.min(hpHeal, stats.getCurrentMaxHP() * stats.hpRecover_limit / 100), mpHeal, false, true);
        }
        MapleStatEffect eff;
        if (attackEffect != null && !SkillConstants.isNoApplyAttack(attackEffect.getSourceId())) {
            attackEffect.attackApplyTo(player, passive, ai.skillposition);
        }
        if (attackEffect != null && attackEffect.getSourceId() == 通用V核心.艾爾達斯的降臨) {
            player.reduceSkillCooldown(通用V核心.艾爾達斯的降臨, attackEffect.getX() * ai.mobCount * 1000);
        }

        eff = player.getEffectForBuffStat(SecondaryStat.GuidedArrow);
        List<MapleMapObject> mobs;
        if (totalDamage > 0L && eff != null && !(mobs = player.getMap().getMapObjectsInRange(player.getPosition(), 500, Collections.singletonList(MapleMapObjectType.MONSTER))).isEmpty()) {
            final MapleMap map = player.getMap();
            final int id = player.getId();
            final int key = player.getSpecialStat().getGuidedArrow().getInfo().get(0).getKey();
            final int objectID = mobs.get(Randomizer.nextInt(mobs.size())).getObjectId();
            map.broadcastMessage(player, ForcePacket.showGuidedArrow(id, key, objectID), true);
        }
    }

    public static void calcDamage(AttackInfo ai, MapleCharacter chr, int attackCount, MapleStatEffect effect) {
        final MapleMap map = chr.getMap();
        int mobCount = 1;
        if (chr.isDebug()) {
            chr.dropDebugMessage(0, "[Attack] " + ((effect != null) ? effect : "Normal"));
        }
        if (effect != null) {
            mobCount = effect.getMobCount(chr);
        }
        if (chr.getBuffedValue(SecondaryStat.BuckShot) != null) {
            attackCount = Math.min(15, attackCount * 3);
        }
        if (chr.getBuffedValue(SecondaryStat.AdrenalinBoost) != null) {
            attackCount = Math.min(15, attackCount + 2);
            mobCount = Math.min(15, mobCount + 5);
        }
        if (ai.skillId == 陰陽師.雪女招喚_1) {
            attackCount = Math.min(15, attackCount + 2);
        }
        if (ai.skillId == 閃雷悍將.消滅 && chr.getBuffedZ(SecondaryStat.IgnoreTargetDEF) != null) {
            attackCount += chr.getBuffedIntZ(SecondaryStat.IgnoreTargetDEF);
        }
        attackCount += chr.getStat().incAttackCount;
        if (JobConstants.is夜光(chr.getJob()) && ai.skillId > 0) {
            MapleStatEffect eff = chr.getEffectForBuffStat(SecondaryStat.Larkness);
            final int type = ai.skillId % 1000 / 100;
            if (eff != null) {
                switch (eff.getSourceId()) {
                    case 夜光.光蝕: {
                        if (type == 1) {
                            attackCount = Math.min(15, attackCount * 2);
                        }
                        break;
                    }
                    case 夜光.暗蝕: {
                        if (type == 2) {
                            attackCount = Math.min(15, attackCount * 2);
                        }
                        break;
                    }
                    case 夜光.平衡_光明: {
                        attackCount = Math.min(15, attackCount * 2);
                        break;
                    }
                }
            }
        }
        if (chr.getBuffedValue(SecondaryStat.Enrage) != null) {
            mobCount = 1;
        }
        if (!ServerConfig.SERVER_VERIFY_DAMAGE) {
            mobCount = 15;
        }
        if (ai.mobAttackInfo.size() > mobCount) {
            chr.dropMessage(9, "[攻擊檢測] " + ((effect != null) ? effect : "普通攻擊") + " 目標數量:" + ai.mobAttackInfo.size() + " 超過伺服器計算數量:" + mobCount);
        }
        int idx = 0;
        for (final AttackMobInfo mai : ai.mobAttackInfo) {
            final MapleMonster monster = map.getMobObject(mai.mobId);
            if (monster != null && !monster.getStats().isInvincible()) {
                long calcedMaxDamage;
                if (ServerConfig.SERVER_VERIFY_DAMAGE) {
                    calcedMaxDamage = (long) chr.getCalcDamage().calcDamage(chr, ai, ++idx, monster, monster.getStats().isBoss());
                } else {
                    calcedMaxDamage = 10000000000000L;
                }
                final MapleMonsterStats stats;
                final long fixDamage = (stats = monster.getStats()).getFixedDamage();
                final MonsterEffectHolder holder = monster.getEffectHolder(MonsterStatus.JaguarBleeding);
                if (ai.skillId == 狂豹獵人.另一個咬擊 && holder != null) {
                    attackCount = holder.value;
                }
                if (!ServerConfig.SERVER_VERIFY_DAMAGE) {
                    attackCount = 15;
                }
                if (mai.damages.length > attackCount) {
                    boolean b;
                    switch (ai.skillId) {
                        case 火毒.藍焰斬:
                        case 箭神.魔幻箭筒_2轉:
                        case 箭神.魔幻箭筒:
                        case 箭神.魔幻箭筒_4轉:
                        case 箭神.無限箭筒:
                        case 400011124:
                        case 400011125:
                        case 400011126:
                        case 400011127:
                        case 夜使者.刺客刻印_1:
                        case 夜使者.刺客刻印_飛鏢:
                        case 夜使者.刺客刻印:
                        case 夜使者.夜使者刻印:
                        case 夜使者.夜使者的標記:
                        case 烈焰巫師.Return_元素之炎I_1:
                        case 烈焰巫師.元素之炎:
                        case 烈焰巫師.元素之炎II:
                        case 烈焰巫師.元素之炎III:
                        case 烈焰巫師.Return_元素之炎III_1:
                        case 烈焰巫師.元素之炎IV:
                        case 烈焰巫師.Return_元素之炎IV:
                        case 破風使者.Switch_type_風妖精之箭I:
                        case 13101027:
                        case 破風使者.Switch_type_風妖精之箭II:
                        case 破風使者.SUMMON_攻擊型態_風妖精之箭II_攻擊:
                        case 破風使者.Switch_type_風妖精之箭Ⅲ:
                        case 破風使者.SUMMON_攻擊型態_風妖精之箭Ⅲ_攻擊:
                        case 破風使者.暴風加護:
                        case 暗夜行者.SUMMON_暗影蝙蝠_召喚獸:
                        case 暗夜行者.SUMMON_暗影蝙蝠_攻擊:
                        case 幻影俠盜.卡牌審判:
                        case 幻影俠盜.審判:
                        case 幻影俠盜.炫目卡牌:
                        case 幻影俠盜.死神卡牌:
                        case 隱月.小狐仙精通_1:
                        case 隱月.火狐精通_1:
                        case 惡魔復仇者.盾牌追擊:
                        case 惡魔復仇者.盾牌追擊_攻擊:
                        case 機甲戰神.追蹤飛彈:
                        case 機甲戰神.進階追蹤飛彈:
                        case 傑諾.追縱火箭:
                        case 傑諾.神盾系統_攻擊:
                        case 傑諾.神盾系統:
                        case 凱撒.意志之劍:
                        case 凱撒.意志之劍_變身:
                        case 凱撒.進階意志之劍:
                        case 凱撒.進階意志之劍_變身:
                        case 天使破壞者.靈魂探求者_攻擊:
                        case 天使破壞者.靈魂探求者:
                        case 天使破壞者.索魂精通:
                        case 凱內西斯.心靈傳動:
                        case 伊利恩.技藝_暗器:
                        case 伊利恩.榮耀之翼_強化暗器:
                        case 伊利恩.技藝_暗器Ⅱ:
                        case 伊利恩.技藝_暗器Ⅱ_1:
                        case 伊利恩.榮耀之翼_強化暗器_2:
                        case 凱撒.意志之劍_重磅出擊:
                        case 凱撒.意志之劍_重磅出擊_1:
                        case 火毒.持續制裁者:
                        case 箭神.殘影之矢:
                        case 破風使者.風轉奇想:
                        case 幻影俠盜.命運鬼牌:
                        case 幻影俠盜.鬼牌_1:
                        case 幻影俠盜.黑傑克:
                        case 幻影俠盜.黑傑克_1:
                        case 閃雷悍將.神雷合一:
                        case 閃雷悍將.神雷合一_1: {
                            b = true;
                            break;
                        }
                        default: {
                            b = false;
                            break;
                        }
                    }
                    if (b) {
                        attackCount = mai.damages.length;
                    } else {
                        //   chr.dropMessage(9, "[攻擊檢測] :" + ((effect != null) ? effect : "普通攻擊") + " 攻擊次數:" + mai.damages.length + " 超過伺服器計算次數:" + attackCount);
                    }
                }
                chr.getClient().outPacket(OutHeader.LP_UseAttack.getValue(), 0);

                ai.hits = (byte) attackCount;
                for (int i = 0; i < mai.damages.length; i++) {
                    long damage = mai.damages[i];
                    if (fixDamage != -1L) {
                        if (stats.getOnlyNoramlAttack()) {
                            damage = ((ai.skillId != 0) ? 0L : fixDamage);
                            calcedMaxDamage = Math.max(fixDamage, calcedMaxDamage);
                        } else {
                            damage = fixDamage;
                            calcedMaxDamage = Math.max(fixDamage, calcedMaxDamage);
                        }
                    } else if (stats.getOnlyNoramlAttack()) {
                        damage = ((ai.skillId != 0) ? 0L : Math.min(damage, 0L));
                    }
                    if (ai.skillId == 初心者.升級 && !monster.isBoss()) {
                        calcedMaxDamage = damage;
                    }
                    if (damage > calcedMaxDamage) {
                        if (chr.isDebug()) {
                            //    chr.dropMessage(9, "[攻擊檢測] :" + ((effect != null) ? effect : "普通攻擊") + " 實際傷害：" + damage + " 超過伺服器計算最大值：" + calcedMaxDamage);
                        }
//                        damage = (long)chr.getCalcDamage().calcDamage(chr, ai, ++idx, chr.getCalcDamage().getRandomDamage(chr, false), monster, monster.getStats().isBoss(), false);
//                        damage = calcedMaxDamage;
                    }
                    mai.damages[i] = damage;
//                    if (chr.isAdmin()) {
//                        long calcedMinDamage = (long) (calcedMaxDamage * chr.getStat().trueMastery / 100.0);
//                        chr.dropMessage(-1, "[解析攻擊] 實際傷害：" + damage + " 伺服器計算最大傷害：" + calcedMaxDamage + " 最小傷害：" + calcedMinDamage);
//                    }
                }
            }
        }
    }

    public static void applyAttack(final AttackInfo ai, final Skill theSkill, final MapleCharacter player, final MapleStatEffect attackEffect, boolean passive) {
        /* 判斷是否需要消耗子彈 */
        boolean noBullet = ai.starSlot == 0 || JobConstants.noBulletJob(player.getJob());
        if (!noBullet && player.getBuffedValue(SecondaryStat.SoulArrow) == null
                && player.getBuffedValue(SecondaryStat.NoBulletConsume) == null
                && player.getSkillEffect(夜使者.無形鏢) == null
                && player.getSkillEffect(槍神.無形彈藥) == null
                && player.getSkillEffect(破風使者.精靈的援助) == null
                && player.getSkillEffect(暗夜行者.無形鏢Ⅱ) == null
                && player.getSkillEffect(箭神.無形之箭_弓) == null
                && player.getSkillEffect(神射手.無形之箭_弩弓) == null
                && player.getSkillEffect(狂豹獵人.無形之箭_弩弓) == null
                && ai.skillId != 箭神.箭座_攻擊) {
            short bulletConsume = 0;
            if (attackEffect != null) {
                bulletConsume = (short) attackEffect.getBulletConsume();
            }
            if (bulletConsume == 0) {
                bulletConsume = ai.hits;
            }
            if (bulletConsume > 0) {
                if (!MapleInventoryManipulator.removeFromSlot(player.getClient(), MapleInventoryType.USE, ai.starSlot, bulletConsume, false, true)) {
                    return;
                }
            }
        }
        if (attackEffect != null && attackEffect.getSourceId() == 80011273 && Auth.checkPermission("MVPEquip_1113220")) {
            Equip eq = null;
            for (Item item : player.getInventory(MapleInventoryType.EQUIPPED).listById(1113220)) { // 幽暗戒指
                if (((Equip) item).isMvpEquip()) {
                    eq = (Equip) item;
                    break;
                }
            }
            if (eq != null) {
                int enhanceNum;
                int cooldown;
                int growSize;
                int moveCount;
                boolean forever = eq.getExpiration() < 0;
                if ((!forever && !player.isSilverMvp()) || eq.getStarForceLevel() < 15) {
                    enhanceNum = 1;
                    cooldown = 0;
                    growSize = 100;
                    moveCount = 10;
                } else if ((!forever && !player.isGoldMvp()) || eq.getStarForceLevel() < 20) {
                    enhanceNum = 15;
                    cooldown = 0;
                    growSize = 150;
                    moveCount = 15;
                } else if ((!forever && !player.isDiamondMvp()) || eq.getStarForceLevel() < 25) {
                    enhanceNum = 20;
                    cooldown = 0;
                    growSize = 300;
                    moveCount = 20;
                } else {
                    enhanceNum = 25; /* STAR */
                    cooldown = 0; /* 冷卻 */
                    growSize = 750; /* 距離 */
                    moveCount = 30; /* 怪物數量 */
                }
                //player.dropSpouseMessage(UserChatMessageType.系統, "MVP幽暗戒指" + (enhanceNum > 1 ? ("[" + enhanceNum + "★]") : "") + "效果啟動，拉起最多" + moveCount + "個怪物，冷卻時間" + cooldown + "秒，範圍增加" + growSize);
                Point p = player.getPosition().getLocation();
                boolean isFacingLeft = player.isFacingLeft();
                Rectangle box = attackEffect.calculateBoundingBox(p, isFacingLeft);
                box.grow(growSize, growSize);
                MapleMonster mob;
                for (final MapleMapObject mmo : player.getMap().getMonstersInRect(box)) {
                    mob = (MapleMonster) mmo;
                    if (mob.isBoss() || mob.isEliteMob() || mob.getStats().isIgnoreMoveImpact()) {
                        continue;
                    }
                    if (moveCount-- <= 0) {
                        break;
                    }
                    mob.move(p);
                }
            }
        }

        final MapleMap map = player.getMap();
        MapleMonster mob = null;
        MapleStatEffect effect;
        if (!ai.mobAttackInfo.isEmpty() && (effect = player.getSkillEffect(法師.實戰的知識)) != null && effect.makeChanceResult(player)) {
            for (final AttackMobInfo mai : ai.mobAttackInfo) {
                final MapleMonster monster = map.getMobObject(mai.mobId);
                if (monster != null && (mob == null || mob.getMobMaxMp() < monster.getMobMaxMp())) {
                    mob = monster;
                }
            }
            if (mob != null) {
                player.getTempValues().put("實戰的知識OID", mob.getObjectId());
                effect.unprimaryApplyTo(player, player.getPosition(), true);
            }
        }

        if (ai.skillId == 冰雷.冰雪之精神) {
            player.getTempValues().put("冰雪之精神攻擊數量", ai.mobAttackInfo.size() == 1);
        }
        passive = ai.passive || passive;
        long totalDamage = 0;
        boolean comboChecked = false;
        for (final AttackMobInfo mai : ai.mobAttackInfo) {
            final MapleMonster monster = map.getMobObject(mai.mobId);
            if (monster != null) {
                // 黑翼胸章
                MapleStatEffect effect11;
                if ((effect11 = player.getSkillEffect(80011158)) != null) {
                    List<MonsterStatus> toRemove = new LinkedList<>();
                    toRemove.add(MonsterStatus.PImmune);
                    toRemove.add(MonsterStatus.MImmune);
                    toRemove.add(MonsterStatus.PCounter);
                    toRemove.add(MonsterStatus.MCounter);
                    if (toRemove.stream().anyMatch(stat -> monster.getEffects().containsKey(stat))) {
                        monster.removeEffect(toRemove);
                        effect11.unprimaryPassiveApplyTo(player);
                    }
                }

                monster.switchController(player);
                long damage = 0L;
                byte numDamage = 0;
                for (long dmg : mai.damages) {
                    if (numDamage < ai.hits) {
                        damage += dmg;
                    }
                    ++numDamage;
                }
                if (theSkill != null && !theSkill.isIgnoreCounter() && !theSkill.isVSkill() && player.getBuffedValue(SecondaryStat.IgnoreAllCounter) == null && player.getBuffedValue(SecondaryStat.IgnoreAllImmune) == null) {
                    if (monster.getEffectHolder(MonsterStatus.PCounter) != null && player.getBuffedValue(SecondaryStat.IgnorePImmune) == null && (ai.isCloseRangeAttack || ai.isRangedAttack)) {
                        player.addHPMP(-5000, 0, false, true);
                    } else if (monster.getEffectHolder(MonsterStatus.MCounter) != null && ai.isMagicAttack) {
                        player.addHPMP(-5000, 0, false, true);
                    }
                }
                totalDamage += damage;
                final MapleForceFactory mff = MapleForceFactory.getInstance();
                final PlayerStats stats = player.getStat();
                int n4 = 0;
                int n5 = 0;
                for (Pair<Integer, Integer> pair : stats.getHPRecoverItemOption().values()) {
                    if (Randomizer.isSuccess(pair.right)) {
                        n4 += pair.left;
                    }
                }
                for (Pair<Integer, Integer> pair : stats.getMPRecoverItemOption().values()) {
                    if (Randomizer.isSuccess((pair).right)) {
                        n5 += pair.left;
                    }
                }
                AbstractSkillHandler handler = attackEffect == null ? null : attackEffect.getSkillHandler();
                if (handler == null) {
                    handler = SkillClassFetcher.getHandlerByJob(player.getJobWithSub());
                }
                int handleRes = -1;
                if (handler != null) {
                    SkillClassApplier applier = new SkillClassApplier();
                    applier.ai = ai;
                    applier.theSkill = theSkill;
                    applier.effect = attackEffect;
                    applier.passive = passive;
                    handleRes = handler.onAttack(player, monster, applier);
                    if (handleRes == 0) {
                        continue;
                    } else if (handleRes == 1) {
                        passive = applier.passive;
                    }
                }
                MapleStatEffect eff;
                if (JobConstants.is惡魔(player.getJob())) {
                    eff = player.getEffectForBuffStat(SecondaryStat.VampiricTouch);
                    final Party party = player.getParty();
                    if (eff != null) {
                        if (party != null) {
                            final Rectangle rect = attackEffect.calculateBoundingBox(player.getPosition(), player.isFacingLeft());
                            for (PartyMember member : party.getMembers()) {
                                if (member.getCharID() != player.getId()
                                        && member.getChr() != null
                                        && member.getChr().getMap() == player.getMap()
                                        && rect.contains(member.getChr().getPosition())
                                        && member.getChr().getCheatTracker().canNextVampiricTouch()) {
                                    member.getChr().addHPMP(Math.min(member.getChr().getStat().getCurrentMaxHP() * eff.getW() / 100, (int) Math.min(125000, eff.getX() * totalDamage / 100L)), 0, false, true);
                                }
                            }
                        }
                        if (player.getCheatTracker().canNextVampiricTouch()) {
                            n4 += (int) Math.min((long) stats.getCurrentMaxHP() * eff.getW() / 100, eff.getX() * totalDamage / 100L);
                        }
                    }
                    n5 = 0;
                }
                if (n4 > 0 || n5 > 0) {
                    player.addHPMP(Math.min(n4, stats.getCurrentMaxHP() * stats.hpRecover_limit / 100), Math.min(n5, stats.mpRecover_limit), false, n4 > 0);
                }
                if (JobConstants.is幻影俠盜(player.getJob())) {
                    eff = player.getSkillEffect(幻影俠盜.死神卡牌);
                    int maxJS = 40;
                    if (eff == null) {
                        eff = player.getSkillEffect(幻影俠盜.炫目卡牌);
                        maxJS = 20;
                    }
                    if (eff != null && attackEffect != null && attackEffect.getSourceId() != 幻影俠盜.玫瑰四重曲_1 && attackEffect.getSourceId() != 幻影俠盜.死神卡牌 && attackEffect.getSourceId() != 幻影俠盜.炫目卡牌) {
                        if (eff.makeChanceResult(player) && Randomizer.nextInt(100) < player.getStat().critRate) {
                            player.getMap().broadcastMessage(player, ForcePacket.forceAtomCreate(mff.getMapleForce(player, eff, 0)), true);
                            if (player.getJudgementStack() < maxJS) {
                                player.incJudgementStack();
                                player.updateJudgementStack();
                            }
                        }
                    }
                }
                // 處理惡魔獵手攻擊時吸收精氣
                if (JobConstants.is惡魔殺手(player.getJob()) && attackEffect != null) {
                    switch (attackEffect.getSourceId()) {
                        case 惡魔殺手.惡魔狂斬:
                        case 惡魔殺手.惡魔狂斬1:
                        case 惡魔殺手.惡魔狂斬2:
                        case 惡魔殺手.惡魔狂斬3:
                        case 惡魔殺手.惡魔覺醒_1:
                        case 惡魔殺手.惡魔覺醒_2:
                        case 惡魔殺手.惡魔覺醒_3:
                        case 惡魔殺手.惡魔覺醒_4: {
                            int dfheal = Randomizer.nextInt(5) + 1;
                            player.getMap().broadcastMessage(player, ForcePacket.forceAtomCreate(mff.getMapleForce(player, attackEffect, monster.getObjectId())), true);
                            if ((eff = player.getSkillEffect(惡魔殺手.強化惡魔之力)) != null && eff.makeChanceResult(player)) {
                                dfheal *= 2;
                                player.getMap().broadcastMessage(player, ForcePacket.forceAtomCreate(mff.getMapleForce(player, attackEffect, monster.getObjectId())), true);
                            }
                            player.addHPMP(0, dfheal, false);
                            player.handleForceGain(dfheal);
                            break;
                        }
                    }
                }
                if (JobConstants.is凱撒(player.getJob())) {
                    player.getSkillEffect(凱撒.龍烈焰);
                    if (attackEffect != null && (attackEffect.getSourceId() == 凱撒.龍烈焰 || attackEffect.getSourceId() == 凱撒.惡魔之嘆)) {
                        attackEffect.applyAffectedArea(player, monster.getPosition());
                    }
                }
                if (JobConstants.is凱內西斯(player.getJob())) {
                    final MapleStatEffect skillEffect6 = player.getSkillEffect(凱內西斯.心靈傳動);
                    final int n15;
                    if (totalDamage > 0L && skillEffect6 != null && skillEffect6.makeChanceResult(player) && (n15 = ((attackEffect != null) ? attackEffect.getSourceId() : 0)) != 凱內西斯.心靈傳動 && n15 != 凱內西斯.心靈推手 && n15 != 凱內西斯.心靈推手2 && n15 != 凱內西斯.心靈推手3 && n15 != 凱內西斯.永恆壞滅 && n15 != 凱內西斯.心靈領域 && n15 != 凱內西斯.終極技_心靈射擊 && n15 != 凱內西斯.終極技_梅泰利爾 && n15 != 凱內西斯.終極技_深層衝擊 && n15 != 凱內西斯.終極技_火車扔擲 && n15 != 凱內西斯.終極技_BPM) {
                        final int n16 = 0;
                        player.getMap().broadcastMessage(player, ForcePacket.forceAtomCreate(mff.getMapleForce(player, skillEffect6, n16)), true);
                    }
                }
                if (JobConstants.is伊利恩(player.getJob())) {
                    if (attackEffect != null && (attackEffect.getSourceId() == 伊利恩.即刻反應_文明爭戰 || attackEffect.getSourceId() == 伊利恩.即刻反應_文明爭戰Ⅱ)) {
                        eff = null;
                        if (player.getSkillEffect(伊利恩.完成詛咒之印) != null) {
                            eff = player.getSkillEffect(伊利恩.完成詛咒之印);
                        } else if (player.getSkillEffect(伊利恩.熟練詛咒之印) != null) {
                            eff = player.getSkillEffect(伊利恩.熟練詛咒之印);
                        } else if (player.getSkillEffect(伊利恩.詛咒之印) != null) {
                            eff = player.getSkillEffect(伊利恩.詛咒之印);
                        }
                        if (eff != null) {
                            int effLevel = eff.getLevel();
                            eff = SkillFactory.getSkill(伊利恩.詛咒之印_怪物狀態).getEffect(effLevel);
                            if (eff != null) {
                                eff.applyMonsterEffect(player, monster, eff.getMobDebuffDuration(player));
                            }
                        }
                    }
                    final MapleStatEffect skillEffect7;
                    if (attackEffect != null && attackEffect.getSourceId() == 伊利恩.技藝_暗器Ⅱ && (skillEffect7 = player.getSkillEffect(伊利恩.技藝_暗器Ⅱ_1)) != null) {
                        List<Integer> moboids = player.getMap().getMapObjectsInRange(monster.getPosition(), 500, Collections.singletonList(MapleMapObjectType.MONSTER)).stream().map(MapleMapObject::getObjectId).collect(Collectors.toList());
                        player.getMap().broadcastMessage(player, ForcePacket.forceAtomCreate(mff.getMapleForce(player, skillEffect7, 0, moboids, monster.getPosition())), true);
                    }
                }
                if (JobConstants.is虎影(player.getJob())) {
                    if (attackEffect != null) {
                        if (attackEffect.getSourceId() == 虎影.魔封葫蘆符) {
                            player.getMap().broadcastMessage(player, ForcePacket.forceAtomCreate(mff.getMapleForce(player, attackEffect, monster.getObjectId(), Collections.emptyList(), monster.getPosition())), true);
                        }
                    }
                }
                // todo 神通術
//                if (effect != null && effect.getSourceid() == 暗影神偷.妙手術 && !monster.isSteal() && effect.makeChanceResult(player)) {
//                    player.getMap().dropFromMonster(player, monster, 0, false, true);
//                    monster.setSteal(true);
//                }
                if (monster.isAlive()) {
                    if ((eff = player.getSkillEffect(80011158)) != null && (monster.isBuffed(MonsterStatus.PImmune) || monster.isBuffed(MonsterStatus.MImmune) || monster.isBuffed(MonsterStatus.PCounter) || monster.isBuffed(MonsterStatus.MCounter))) {
                        monster.removeEffect(Arrays.asList(MonsterStatus.PImmune, MonsterStatus.MImmune, MonsterStatus.PCounter, MonsterStatus.MCounter));
                        eff.unprimaryPassiveApplyTo(player);
                    }
                    if ((eff = player.getSkillEffect(80011159)) != null && (monster.isBuffed(MonsterStatus.PowerUp) || monster.isBuffed(MonsterStatus.MagicUp) || monster.isBuffed(MonsterStatus.PGuardUp) || monster.isBuffed(MonsterStatus.MGuardUp) || monster.isBuffed(MonsterStatus.HardSkin))) {
                        monster.removeEffect(Arrays.asList(MonsterStatus.PowerUp, MonsterStatus.MagicUp, MonsterStatus.PGuardUp, MonsterStatus.MGuardUp, MonsterStatus.HardSkin));
                        eff.unprimaryPassiveApplyTo(player);
                    }
                    if (damage > 0L) {
                        SecondaryStatValueHolder holder;
                        if ((holder = player.getBuffStatValueHolder(SecondaryStat.ErdaStack)) != null && holder.value < 6 && Randomizer.isSuccess(holder.effect.getX())) {
                            holder.value += 1;
                            player.send(BuffPacket.giveBuff(player, holder.effect, Collections.singletonMap(SecondaryStat.ErdaStack, holder.sourceID)));
                        }
                        applyMonsterEffect(attackEffect, player, monster, totalDamage);
                        monster.damage(player, ai.skillId, damage, false);
                    }
                }
                if (!monster.isAlive() && !comboChecked && !player.isStopComboKill()) {
                    player.dropComboKillBall(monster.getPosition());
                    comboChecked = true;
                }
                applyAttackEffect(attackEffect, player, monster, totalDamage);
                if (!player.inEvent() && !player.isOverMobLevelTip() && !monster.isBoss() && Math.abs(player.getLevel() - monster.getMobLevel()) > 20) {
                    player.setOverMobLevelTip(true);
                    player.dropSpecialTopMsg("狩獵不在等級範圍內的怪物時，經驗值與楓幣獲得量會大幅減少。", 3, 20, 20, 0);
                }
            }
        }
        if (totalDamage > 0 && ai.skillId > 0 && !SkillConstants.isPassiveAttackSkill(ai.skillId)) {
            int finalSkillId = player.getStat().getFinalAttackSkill();
            switch (finalSkillId) {
                case 拳霸.戰艦鯨魚號_1:
                    switch (SkillConstants.getLinkedAttackSkill(ai.skillId)) {
                        case 海盜.旋風斬:
                        case 拳霸.炫風拳:
                        case 拳霸.閃_連殺:
                        case 拳霸.海龍衝鋒:
                        case 拳霸.海龍正拳:
                            break;
                        default:
                            finalSkillId = 0;
                            break;
                    }
                    break;
                case 重砲指揮官.幸運木桶_1:
                    if (ai.attackType == AttackInfo.AttackType.SummonedAttack) {
                        finalSkillId = 0;
                    }
                    break;
            }
            final MapleStatEffect skillEffect = player.getSkillEffect(finalSkillId);
            if (player.isDebug()) {
                player.dropMessageIfAdmin(5, "開始處理終極攻擊, SkillID:" + ai.skillId + ",FinalSkillID:" + finalSkillId + ",Effect:" + skillEffect);
            }
            if (finalSkillId > 0 && finalSkillId != ai.skillId && skillEffect != null) {
                final boolean suc = finalSkillId == 重砲指揮官.幸運木桶_1 || skillEffect.makeChanceResult(player);
                final Item item;
                MapleWeapon wt;
                if ((item = player.getInventory(MapleInventoryType.EQUIPPED).getItem((JobConstants.is神之子(player.getJob()) && player.isBeta()) ? (short) -10 : -11)) == null) {
                    wt = MapleWeapon.沒有武器;
                } else {
                    wt = MapleWeapon.getByItemID(item.getItemId());
                }
                if (wt != MapleWeapon.沒有武器) {
                    List<Integer> oids = new LinkedList<>();
                    if (suc) {
                        for (final AttackMobInfo mai : ai.mobAttackInfo) {
                            oids.add(mai.mobId);
                        }
                    }
                    player.getClient().announce(MaplePacketCreator.FinalAttack(player, player.getCheatTracker().getFinalAttackTime(), suc, ai.skillId, finalSkillId, wt.getWeaponType(), oids));
                }
            }
        }
        //傷害解析結束
        if (totalDamage > 0 && player.getBuffStatValueHolder(80011248) != null) {
            player.addShieldHP(totalDamage > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) totalDamage);
        }
        SecondaryStatValueHolder holder = player.getBuffStatValueHolder(SecondaryStat.RunePurification);
        SecondaryStatValueHolder summonHolder;
        if (holder != null && holder.value == 1 && (summonHolder = player.getBuffStatValueHolder(SecondaryStat.IndieBuffIcon, 80002888)) != null) {
            if (holder.z >= 1000) {
                holder.value = 2;
                holder.startTime = System.currentTimeMillis();
                summonHolder.startTime = holder.startTime;
                holder.schedule.cancel(true);
                summonHolder.schedule.cancel(true);
                holder.schedule = Timer.BuffTimer.getInstance().schedule(new MapleStatEffect.CancelEffectAction(player, holder.effect, holder.startTime, new EnumMap<>(holder.effect.getStatups())), holder.localDuration);
                summonHolder.schedule = holder.schedule;

                MapleSummon summon = player.getSummonBySkillID(80002888);
                if (summon != null) {
                    summon.setCreateTime(holder.startTime);
                }
            }
            player.send(BuffPacket.giveBuff(player, null, Collections.singletonMap(SecondaryStat.RunePurification, 80002888)));
        }
        player.monsterMultiKill();
        DamageParse.afterAttack(attackEffect, player, totalDamage, ai.rangedAttackPos, ai, passive);
        player.setLastAttackSkillId(ai.skillId);
        player.getCheatTracker().checkAttack(ai.skillId, ai.lastAttackTickCount);
    }

    public static void applyMonsterEffect(MapleStatEffect effect, MapleCharacter applyfrom, MapleMonster applyto, long totalDamage) {
        final MapleForceFactory mmf = MapleForceFactory.getInstance();
        if (JobConstants.is暗夜行者(applyfrom.getJob())) {
            final MapleStatEffect effecForBuffStat6;
            final MapleStatEffect skillEffect14;
            if ((effecForBuffStat6 = applyfrom.getEffectForBuffStat(SecondaryStat.ElementDarkness)) != null && applyto.isAlive() && effect != null && effecForBuffStat6.applyMonsterEffect(applyfrom, applyto, effecForBuffStat6.getDotTime(applyfrom) * 1000) && (skillEffect14 = applyfrom.getSkillEffect(暗夜行者.吸收活力)) != null) {
                skillEffect14.unprimaryPassiveApplyTo(applyfrom);
            }
            final MapleStatEffect effecForBuffStat7 = applyfrom.getEffectForBuffStat(SecondaryStat.NightWalkerBat);
            final MapleSummon summonBySkillID;
            if (effect != null && effect.getBulletCount() > 1 && effecForBuffStat7 != null && applyto.isAlive() && applyfrom.getSummonCountBySkill(暗夜行者.SUMMON_暗影蝙蝠_召喚獸) > 0 && (effecForBuffStat7.makeChanceResult(applyfrom) || applyto.getEffectHolder(applyto.getId(), 暗夜行者.SUMMON_元素闇黑) != null && effecForBuffStat7.makeChanceResult(applyfrom)) && (summonBySkillID = applyfrom.getSummonBySkillID(暗夜行者.SUMMON_暗影蝙蝠_召喚獸)) != null) {
                final MapleStatEffect nj = summonBySkillID.getEffect();
                final int n6 = 0;
                final MapleForceAtom a = mmf.getMapleForce(applyfrom, nj, n6);
                a.setForcedTarget(summonBySkillID.getPosition());
                applyfrom.getMap().broadcastMessage(applyfrom, SummonPacket.summonSkill(applyfrom.getId(), summonBySkillID.getObjectId(), 0), true);
                applyfrom.getMap().broadcastMessage(applyfrom, ForcePacket.forceAtomCreate(a), true);
                applyfrom.removeSummon(summonBySkillID, 0);
            }
        }

        SecondaryStatValueHolder holder;
        if (effect != null && applyto.isAlive() && totalDamage > 0 && (holder = applyfrom.getBuffStatValueHolder(SecondaryStat.ErdaRevert)) != null) {
            holder.effect.applyMonsterEffect(applyfrom, applyto, effect.getMobDebuffDuration(applyfrom));
        }

        int debuffDuration;
        if (effect != null && (debuffDuration = effect.getMobDebuffDuration(applyfrom)) > 0 && debuffDuration != 2100000000 && !effect.getMonsterStatus().isEmpty() && effect.getMobCount() > 0 && effect.getAttackCount() > 0) {
            effect.applyMonsterEffect(applyfrom, applyto, debuffDuration);
        }
    }

    public static void applyAttackEffect(MapleStatEffect effect, MapleCharacter applyfrom, MapleMonster applyto, long totalDamage) {
        AbstractSkillHandler handler = effect == null ? null : effect.getSkillHandler();
        if (handler == null) {
            handler = SkillClassFetcher.getHandlerByJob(applyfrom.getJobWithSub());
        }
        int handleRes = -1;
        if (handler != null) {
            SkillClassApplier applier = new SkillClassApplier();
            applier.effect = effect;
            applier.totalDamage = totalDamage;
            handleRes = handler.onApplyAttackEffect(applyfrom, applyto, applier);
            if (handleRes == 0) {
                return;
            } else if (handleRes == 1) {
                effect = applier.effect;
                totalDamage = applier.totalDamage;
            }
        }

        if (effect != null && applyto.isAlive()) {
            final Skill skill = SkillFactory.getSkill(effect.getSourceId());
            MonsterEffectHolder meh;
            if ((meh = applyto.getEffectHolder(MonsterStatus.BahamutLightElemAddDam)) != null && skill != null && skill.getId() != meh.sourceID && skill.getElement() == Element.神聖) {
                applyto.removeEffect(Collections.singletonList(MonsterStatus.BahamutLightElemAddDam));
            }
        }

        MapleStatEffect eff;
        if (!applyfrom.isSkillCooling(盜賊.小偷的狡詐) && (eff = applyfrom.getSkillEffect(盜賊.小偷的狡詐)) != null) {
            if (applyto.getObjectId() == applyfrom.getBuffedIntZ(SecondaryStat.NoviceMagicianLink)
                    || applyto.getObjectId() == applyfrom.getBuffedIntZ(SecondaryStat.Shadower_Assassination)
                    || applyto.getAllEffects().values().stream()
                    .flatMap(Collection::stream).collect(Collectors.toCollection(LinkedList::new)).stream()
                    .anyMatch(meh -> meh != null && !(meh.effect instanceof MobSkill))) {
                eff.applyTo(applyfrom);
            }
        }

        if (JobConstants.is冒險家法師(applyfrom.getJob())) {
            int n3;
            final int n2 = applyfrom.getJob() == 212 ? (n3 = 火毒.神秘狙擊) : applyfrom.getJob() == 222 ? (n3 = 冰雷.神秘狙擊) : applyfrom.getJob() == 232 ? (n3 = 主教.神秘狙擊) : (n3 = 0);
            final int n4 = n3;
            final MapleStatEffect skillEffect2;
            if (n2 > 0 && totalDamage > 0L && (skillEffect2 = applyfrom.getSkillEffect(n4)) != null && skillEffect2.makeChanceResult(applyfrom)) {
                skillEffect2.unprimaryPassiveApplyTo(applyfrom);
            }
        }
        final MapleMonster mobObject;
        if ((applyto.getEffectHolder(MonsterStatus.SeperateSoulP) != null || applyto.getEffectHolder(MonsterStatus.SeperateSoulC) != null) && applyto.getSeperateSoulSrcOID() > 0 && (mobObject = applyfrom.getMap().getMobObject(applyto.getSeperateSoulSrcOID())) != null) {
            mobObject.damage(applyfrom, effect != null ? effect.getSourceId() : 0, totalDamage, false);
        }
        if (!applyto.isAlive()) {
            eff = applyfrom.getSkillEffect(菈菈.大自然夥伴);
            if (eff == null) {
                eff = applyfrom.getSkillEffect(菈菈.大自然夥伴_傳授);
            }
            if (eff != null && !applyfrom.isSkillCooling(eff.getSourceId())) {
                Skill skil = SkillFactory.getSkill(菈菈.大自然夥伴_計數);
                MapleStatEffect eff1;
                if (skil != null && (eff1 = skil.getEffect(eff.getLevel())) != null) {
                    SecondaryStatValueHolder mbsvh = applyfrom.getBuffStatValueHolder(SecondaryStat.AMLinkSkill);
                    if (mbsvh == null) {
                        eff1.unprimaryApplyTo(applyfrom, applyfrom.getPosition(), true);
                    } else {
                        mbsvh.value = Math.min(mbsvh.value + 1, eff.getX());
                        if (mbsvh.value >= eff.getX()) {
                            applyfrom.dispelEffect(SecondaryStat.AMLinkSkill);
                            eff.unprimaryApplyTo(applyfrom, applyfrom.getPosition(), true);
                        } else {
                            mbsvh.startTime = System.currentTimeMillis();
                            applyfrom.send(BuffPacket.giveBuff(applyfrom, mbsvh.effect, Collections.singletonMap(SecondaryStat.AMLinkSkill, mbsvh.effect.getSourceId())));
                        }
                    }
                }
            }
        }
        if (!applyto.isAlive() && applyfrom.checkSoulWeapon()) {
            applyfrom.handleSoulMP(applyto);
            applyfrom.checkSoulState(false);
        }
    }

}
