package Handler;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.MapleQuestStatus;
import Client.SecondaryStat;
import Client.skills.SkillMacro;
import Config.constants.GameConstants;
import Config.constants.JobConstants;
import Config.constants.ServerConstants;
import Config.constants.SkillConstants;
import Config.constants.enums.ScriptType;
import Config.constants.skills.劍豪;
import Config.constants.skills.卡蒂娜;
import Config.constants.skills.爆拳槍神;
import Net.server.buffs.MapleStatEffect;
import Net.server.quest.MapleQuest;
import Opcode.Headler.OutHeader;
import Packet.*;
import Server.channel.handler.StatsHandling;
import Server.world.*;
import Server.world.guild.MapleGuild;
import Server.world.guild.MapleGuildResultOption;
import Server.world.messenger.MapleMessenger;
import Server.world.messenger.MapleMessengerCharacter;
import connection.OutPacket;
import connection.packet.Login;
import connection.packet.OverseasPacket;
import SwordieX.enums.GuildResponseType;
import SwordieX.overseas.extraequip.ExtraEquipResult;
import tools.*;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.*;

import static Config.constants.skills.惡魔復仇者.血之限界;
import static Packet.MaplePacketCreator.getWarpToMap;

public class warpToGameHandler {

    private static MapleCharacter chr;

    public static MapleCharacter getChr() {
        return chr;
    }

    public static void Start(MapleClient c) {
        //DamageSkin load
        c.getPlayer().initDamageSkinList();
        // 73+ v267
        c.write(Login.sendServerValues());
        // 74+ v267
        c.write(Login.sendServerEnvironment());
        // 66+  v267
        c.announce(c.getEncryptOpcodesData(ServerConstants.OpcodeEncryptionKey));
        // 570+ v267
        c.announce(loginExtra());
        // 367+ v267+
        c.announce(setNameTagHide());
        // 1427
        c.outPacket(OutHeader.LP_ENTER_GAME_UNK.getValue(), 0);
        // 1442
        c.outPacket(OutHeader.LP_ENTER_GAME_UNK_II.getValue(), 0);
        // start load guild 205+
        c.write(startLoadGuild(c));
        // unk 54+
        c.write(loginChecking(c));
        // unk 148+
        c.write(ChangeSkillRecordResult(c));
        // 281+  v267
        c.announce(linkSkillNotice(c));
        // 658+ v267
        c.announce(getWarpToMap(c.getPlayer(), c.getPlayer().getMap(), null, 0, true, false));
        // 232+ v267
        c.announce(MaplePacketCreator.changeHour(6, Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
        // 2653+ v267 讀取記憶鍵盤位置
        c.announce(MaplePacketCreator.getKeymap(c.getPlayer()));
        // 2655 / 657 /2656 / 2657 - pet load
        c.getPlayer().updatePetAuto();
        // 823+ v267+
        c.getPlayer().getMap().userEnterField(c.getPlayer());
        // 189+ v267+
        c.outPacket(OutHeader.LP_PartyResult.getValue(), (byte) 0x1C, (short) 0);
        // 147+ v267+
        c.outPacket(OutHeader.LP_ForcedStatReset.getValue());
        // 726+ v267+
        c.outPacket(OutHeader.LP_SetQuickMoveInfo.getValue(), (byte) 0);
        // 207+ v267+
        c.announce(MaplePacketCreator.onTownPortal(999999999, 999999999, 0, null));
        // 541+ v267+
        c.outPacket((short) 541, PacketHelper.getTime(System.currentTimeMillis()));
        // 160+ v267+
        c.outPacket(OutHeader.LP_Message.getValue(), 16907277, (byte) 0, (byte) 0, (byte) 0);
        // 594+ v267+
        c.outPacket((short) 594, 0);
        // 607+ v267+
        c.announce(infoHexa(c));
        // 632+ v267+
        c.announce(unkSkillLoad(c));
        // 203+ V267+
        c.announce(Init_Friend(c));
        // 2744+ v267
        c.announce(unk2744(c));
        // todo: 萌獸未完成
        c.write(OverseasPacket.extraEquipResult(ExtraEquipResult.initSpecialData(c.getPlayer())));

        c.getPlayer().initOnlineTime();
        c.getPlayer().giveCoolDowns(PlayerBuffStorage.getCooldownsFromStorage(c.getPlayer().getId()));
        c.getPlayer().silentGiveBuffs(PlayerBuffStorage.getBuffsFromStorage(c.getPlayer().getId()));
        c.getPlayer().initAllInfo();
        c.getPlayer().setOnline(true);

        //TODO :: 六轉技能相關
        c.announce(CWvsContext.sendHexaEnforcementInfo());

        //TODO :: 輪迴碑石解除使用鎖定
        c.write(warpToGameHandler.EquipRuneSetting());

        //TODO :: 惡魔復仇者 需要此buff打怪才不會是1
        if (JobConstants.is惡魔復仇者(c.getPlayer().getJob())) {
            c.getPlayer().getSkillEffect(血之限界).applyTo(c.getPlayer());
        }

        c.getPlayer().getScriptManager().startScript(0, "enterFieldQuest", ScriptType.Npc);

        // 墨玄 解鎖招式lock
        if (JobConstants.is墨玄(c.getPlayer().getJob())) {
            c.announce(is墨玄招式warptogame());
            c.announce(is墨玄招式lock(c, 110, 3000));
        }

        //天使破壞者 變身時間 -
        if (JobConstants.is天使破壞者(c.getPlayer().getJob())) {
            MapleQuestStatus marr1 = c.getPlayer().getQuestNAdd(MapleQuest.getInstance(29015));
            if (marr1 != null) {
                if (marr1.getStatus() == 0) {
                    marr1.setStatus((byte) 1);
                }
            }
        }

        //神之子打開武器強化欄
        if (JobConstants.is神之子(c.getPlayer().getJob())) {
            MapleQuestStatus marr = c.getPlayer().getQuestNAdd(MapleQuest.getInstance(40905));
            if ((marr != null)) {
                if (marr.getStatus() == 0) {
                    marr.setStatus((byte) 2);
                }
            }
        }
        if (JobConstants.is爆拳槍神(c.getPlayer().getJob())) {
            MapleStatEffect effect = c.getPlayer().getSkillEffect(爆拳槍神.彈丸填裝);
            if (effect != null) {
                c.getPlayer().handleAmmoClip(8);
                effect.applyTo(c.getPlayer());
            }
        }
        if (JobConstants.is卡蒂娜(c.getPlayer().getJob())) {
            MapleStatEffect effect = c.getPlayer().getSkillEffect(卡蒂娜.武器變換終章);
            if (effect != null) {
                effect.applyTo(c.getPlayer());
            }
        }
        c.getPlayer().expirationTask(false);
        /* login script */
        c.announce(GuildPacket.sendGuildResult(MapleGuildResultOption.setGuildUnk(c.getPlayer())));
        c.announce(GuildPacket.sendGuildResult(new MapleGuildResultOption(GuildResponseType.Res_SetSignInReward)));
        c.announce(GuildPacket.sendGuildResult(MapleGuildResultOption.loadGuild(c.getPlayer())));
        c.announce(GuildPacket.sendGuildResult(new MapleGuildResultOption(GuildResponseType.Res_Authkey_Update))); // TODO: 待完成 token
        if (c.getPlayer().getGuild() != null) {
            WorldGuildService.getInstance().setGuildMemberOnline(c.getPlayer().getMGC(), true, c.getChannel());
            MapleGuild gs = WorldGuildService.getInstance().getGuild(c.getPlayer().getGuildId());
            if (gs != null) {
                List<byte[]> packetList = WorldAllianceService.getInstance().getAllianceInfo(gs.getAllianceId(), true);
                if (packetList != null) {
                    for (byte[] pack : packetList) {
                        if (pack != null) {
                            c.announce(pack);
                        }
                    }
                }
            }
        } else { // 沒有公會和聯盟就設置為默認
            c.getPlayer().setGuildId(0);
            c.getPlayer().setGuildRank((byte) 5);
            c.getPlayer().setAllianceRank((byte) 5);
            c.getPlayer().saveGuildStatus();
        }
        if (c.getPlayer().getJob() == 6001 && c.getPlayer().getLevel() < 10) {
            while (c.getPlayer().getLevel() < 10) {
                c.getPlayer().gainExp(5000, true, false, true);
            }
        }
        // 狂豹獵人
        if (JobConstants.is狂豹獵人(c.getPlayer().getJob())) {
            c.announce(MaplePacketCreator.updateJaguar(c.getPlayer()));
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 1; i <= 9; i++) {
                stringBuilder.append(i).append("=1");
                if (i != 9) {
                    stringBuilder.append(";");
                }
            }
            c.announce(MaplePacketCreator.updateInfoQuest(GameConstants.美洲豹管理, stringBuilder.toString()));
        }
        // 劍豪
        if (JobConstants.is劍豪(c.getPlayer().getJob())) {
            MapleStatEffect effect = c.getPlayer().getSkillEffect(劍豪.一般姿勢效果);
            if (effect != null) {
                effect.applyTo(c.getPlayer());
            }
            c.announce(MaplePacketCreator.updateHayatoPoint(0));
        }
        // 解決進入商城卡在線時間的問題.
        c.getPlayer().fixOnlineTime();
        c.getPlayer().updateWorldShareInfo(6, "enter", DateUtil.getFormatDate(new Date(), "yyyyMM"));
        c.getPlayer().getStat().recalcLocalStats(c.getPlayer());

        // 發送道具冷卻時間
        String keyValue = c.getPlayer().getKeyValue("MapTransferItemNextTime");
        String newKeyValue = "";
        if (keyValue != null) {
            final String[] split = keyValue.split(",");
            for (String nextTime : split) {
                if (nextTime == null || !nextTime.contains("=")) {
                    continue;
                }
                final String[] split_2 = nextTime.split("=");
                if (split_2.length < 2) {
                    continue;
                }
                long nt = Long.parseLong(split_2[1]);
                if (System.currentTimeMillis() >= nt) {
                    continue;
                }
                //c.getPlayer().write(OverseasPacket.extraSystemResult(ExtraSystemResult.extraTimerSystem(Long.parseLong(split_2[0]), nt)));
                newKeyValue += nt + ",";
            }
            if (newKeyValue.isEmpty()) {
                c.getPlayer().setKeyValue("MapTransferItemNextTime", null);
            } else {
                c.getPlayer().setKeyValue("MapTransferItemNextTime", newKeyValue.substring(0, newKeyValue.length() - 1));
            }
        }
        int mvpLevel = c.getPlayer().getMvpLevel();
        if (mvpLevel > 0) {
            mvpLevel = mvpLevel < 5 ? 4 : mvpLevel;
            String gp = c.getPlayer().getWorldShareInfo(6, "gp");
            int today = Integer.parseInt(DateUtil.getCurrentDate("dd"));
            String now = DateUtil.getCurrentDate("yyyyMM")
                    + (today > 20 ? "03" : today > 10 ? "02" : "01")
                    + StringUtil.getLeftPaddedStr(String.valueOf(mvpLevel), '0', 2);
            if (!now.equals(gp)) {
                c.announce(MaplePacketCreator.mvpPacketTips());
            }
        }
        // 重製任務 - 凌晨
        if (c.getPlayer().getQuestStatus(7707) == 1) {
            MapleQuest.getInstance(7707).reset(c.getPlayer());
        }
        //開始發送玩家送到的一些未處理的消息
        MapleMessenger messenger = c.getPlayer().getMessenger();
        if (messenger != null) {
            WorldMessengerService.getInstance().silentJoinMessenger(messenger.getId(), new MapleMessengerCharacter(c.getPlayer()));
            WorldMessengerService.getInstance().updateMessenger(messenger.getId(), c.getPlayer().getName(), c.getChannel());
        }
        //檢測靈魂武器
        if (c.getPlayer().checkSoulWeapon()) {
            c.announce(BuffPacket.giveBuff(c.getPlayer(), c.getPlayer().getSkillEffect(c.getPlayer().getSoulSkillID()), Collections.singletonMap(SecondaryStat.SoulMP, c.getPlayer().getSoulSkillID())));
        }
        //顯示夜光的光暗能量點數
        if (JobConstants.is夜光(c.getPlayer().getJob())) {
            c.announce(BuffPacket.updateLuminousGauge(5000, (byte) 3));
        }
        World.clearChannelChangeDataByAccountId(c.getPlayer().getAccountID());
        //對檢測是否能進入商城的時間進行重置
        c.getPlayer().getCheatTracker().getLastlogonTime();

        // 更新禮物箱子
        c.getPlayer().updateReward();

        //c.getPlayer().updateWorldShareInfo(500606, null);
        MapleQuest.getInstance(500606).reset(c.getPlayer());
        // 加載斷線保存的BUFF
        World.TemporaryStat.LoadData(c.getPlayer());
        // 檢測極限屬性點數是否異常
        if (SkillConstants.getHyperAP(c.getPlayer()) < 0) {
            StatsHandling.ResetHyperAP(c, c.getPlayer(), true, -1, 0);
        }
    }

    public static OutPacket startLoadGuild(MapleClient c) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_GuildResult);
        outPacket.encodeInt(128);
        outPacket.encodeInt(0);
        c.write(startLoadGuildN(c));
        return outPacket;
    }

    public static OutPacket startLoadGuildN(MapleClient c) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_GuildResult);
        outPacket.encodeInt(129);
        outPacket.encodeInt(4);
        outPacket.encodeInt(100);
        outPacket.encodeInt(2000);
        outPacket.encodeInt(60);
        outPacket.encodeInt(1000);
        outPacket.encodeInt(30);
        outPacket.encodeInt(100);
        outPacket.encodeInt(15);
        outPacket.encodeInt(50);
        return outPacket;
    }


    public static OutPacket TemporaryStatSet(MapleClient c) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_TemporaryStatSet);
        outPacket.encodeArr(new byte[70]);
        outPacket.encodeByte(2);
        outPacket.encodeArr(new byte[79]);
        outPacket.encodeByte(1);
        outPacket.encodeByte(1);
        outPacket.encodeInt(0);
        outPacket.encodeByte(0);
        outPacket.encodeByte(1);
        return outPacket;
    }

    public static OutPacket loginChecking(MapleClient c) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_LOGIN_ACTION_CHECK);
        return outPacket;
    }

    public static OutPacket ChangeSkillRecordResult(MapleClient c) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_ChangeSkillRecordResult);
        outPacket.encodeByte(0);
        outPacket.encodeByte(0);
        outPacket.encodeByte(0);
        outPacket.encodeShort(7);
        outPacket.encodeInt(80003525);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeLong(150842304000000000L);
        outPacket.encodeInt(80003524);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeLong(150842304000000000L);
        outPacket.encodeInt(80003521);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeLong(150842304000000000L);
        outPacket.encodeInt(80003520);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeLong(150842304000000000L);
        outPacket.encodeInt(80003518);
        outPacket.encodeInt(3);
        outPacket.encodeInt(0);
        outPacket.encodeLong(150842304000000000L);
        outPacket.encodeInt(80003517);
        outPacket.encodeInt(3);
        outPacket.encodeInt(0);
        outPacket.encodeLong(150842304000000000L);
        outPacket.encodeInt(80003516);
        outPacket.encodeInt(3);
        outPacket.encodeInt(0);
        outPacket.encodeLong(150842304000000000L);
        outPacket.encodeByte(2);
        return outPacket;
    }


    public static byte[] is墨玄招式warptogame() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.EXTRA_SYSTEM_RESULT.getValue());
        mplew.writeInt(-1289454273);
        mplew.writeShort(36);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(-468103806);
        mplew.writeInt(0);
        mplew.write(1);
        mplew.writeInt(1830);
        mplew.write(0);
        mplew.write(220);
        return mplew.getPacket();
    }

    public static byte[] is墨玄招式lock(MapleClient c, int mode, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.EXTRA_SYSTEM_RESULT.getValue());
        mplew.writeInt(-1289454273);
        mplew.writeShort(36);
        mplew.write(0);
        mplew.writeInt(mode);
        mplew.writeInt(type);
        mplew.writeInt(1);
        mplew.write(1);
        mplew.writeInt(132902);
        mplew.write(0);
        mplew.write(32);
        return mplew.getPacket();
    }


    public static byte[] unk2744(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((short) 2744);
        mplew.writeInt(1677721600);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] Init_Friend(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FriendResult.getValue());
        mplew.writeInt(39);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] unkSkillLoad(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((short) 632);
        mplew.writeInt(-300);
        mplew.writeInt(-370);
        mplew.writeInt(300);
        mplew.writeInt(220);
        mplew.writeInt(-300);
        mplew.writeInt(-405);
        mplew.writeInt(300);
        mplew.writeInt(290);
        mplew.writeInt(17);
        mplew.writeInt(80000537);
        mplew.writeInt(80000619);
        mplew.writeInt(80000645);
        mplew.writeInt(80000678);
        mplew.writeInt(80000735);
        mplew.writeInt(80000775);
        mplew.writeInt(80000808);
        mplew.writeInt(80000842);
        mplew.writeInt(80000845);
        mplew.writeInt(80000848);
        mplew.writeInt(80000851);
        mplew.writeInt(80000854);
        mplew.writeInt(80010168);
        mplew.writeInt(80010224);
        mplew.writeInt(80010288);
        mplew.writeInt(80010317);
        mplew.writeInt(80011996);
        return mplew.getPacket();
    }

    public static byte[] infoHexa(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_HEXA_SKILL_INFO.getValue());
        mplew.writeInt(60);
        mplew.writeInt(10100);
        mplew.writeInt(2500);
        mplew.writeInt(1);
        mplew.writeInt(10200);
        mplew.writeInt(2550);
        mplew.writeInt(2);
        mplew.writeInt(10300);
        mplew.writeInt(2600);
        mplew.writeInt(3);
        mplew.writeInt(10400);
        mplew.writeInt(2650);
        mplew.writeInt(4);
        mplew.writeInt(10500);
        mplew.writeInt(2700);
        mplew.writeInt(6);
        mplew.writeInt(10600);
        mplew.writeInt(2750);
        mplew.writeInt(7);
        mplew.writeInt(10700);
        mplew.writeInt(2800);
        mplew.writeInt(8);
        mplew.writeInt(10800);
        mplew.writeInt(2850);
        mplew.writeInt(9);
        mplew.writeInt(10900);
        mplew.writeInt(2900);
        mplew.writeInt(10);
        mplew.writeInt(11000);
        mplew.writeInt(2950);
        mplew.writeInt(12);
        mplew.writeInt(11100);
        mplew.writeInt(3000);
        mplew.writeInt(13);
        mplew.writeInt(11200);
        mplew.writeInt(3050);
        mplew.writeInt(14);
        mplew.writeInt(11300);
        mplew.writeInt(3100);
        mplew.writeInt(15);
        mplew.writeInt(11400);
        mplew.writeInt(3150);
        mplew.writeInt(16);
        mplew.writeInt(11500);
        mplew.writeInt(3200);
        mplew.writeInt(18);
        mplew.writeInt(11600);
        mplew.writeInt(3250);
        mplew.writeInt(19);
        mplew.writeInt(11700);
        mplew.writeInt(3300);
        mplew.writeInt(20);
        mplew.writeInt(11800);
        mplew.writeInt(3350);
        mplew.writeInt(21);
        mplew.writeInt(11900);
        mplew.writeInt(3400);
        mplew.writeInt(22);
        mplew.writeInt(12000);
        mplew.writeInt(3450);
        mplew.writeInt(24);
        mplew.writeInt(12100);
        mplew.writeInt(3500);
        mplew.writeInt(25);
        mplew.writeInt(12200);
        mplew.writeInt(3550);
        mplew.writeInt(26);
        mplew.writeInt(12300);
        mplew.writeInt(3600);
        mplew.writeInt(27);
        mplew.writeInt(12400);
        mplew.writeInt(3700);
        mplew.writeInt(28);
        mplew.writeInt(12500);
        mplew.writeInt(3800);
        mplew.writeInt(30);
        mplew.writeInt(12600);
        mplew.writeInt(3900);
        mplew.writeInt(31);
        mplew.writeInt(12700);
        mplew.writeInt(4000);
        mplew.writeInt(32);
        mplew.writeInt(12800);
        mplew.writeInt(4500);
        mplew.writeInt(33);
        mplew.writeInt(12900);
        mplew.writeInt(5000);
        mplew.writeInt(34);
        mplew.writeInt(13000);
        mplew.writeInt(5500);
        mplew.writeInt(36);
        mplew.writeInt(13200);
        mplew.writeInt(6000);
        mplew.writeInt(37);
        mplew.writeInt(13400);
        mplew.writeInt(6500);
        mplew.writeInt(38);
        mplew.writeInt(13600);
        mplew.writeInt(7000);
        mplew.writeInt(39);
        mplew.writeInt(13800);
        mplew.writeInt(7500);
        mplew.writeInt(40);
        mplew.writeInt(14000);
        mplew.writeInt(8000);
        mplew.writeInt(42);
        mplew.writeInt(14200);
        mplew.writeInt(8500);
        mplew.writeInt(43);
        mplew.writeInt(14400);
        mplew.writeInt(9000);
        mplew.writeInt(44);
        mplew.writeInt(14600);
        mplew.writeInt(9500);
        mplew.writeInt(45);
        mplew.writeInt(14800);
        mplew.writeInt(10000);
        mplew.writeInt(46);
        mplew.writeInt(15000);
        mplew.writeInt(12000);
        mplew.writeInt(48);
        mplew.writeInt(15200);
        mplew.writeInt(14000);
        mplew.writeInt(49);
        mplew.writeInt(15400);
        mplew.writeInt(16000);
        mplew.writeInt(50);
        mplew.writeInt(15600);
        mplew.writeInt(18000);
        mplew.writeInt(51);
        mplew.writeInt(15800);
        mplew.writeInt(20000);
        mplew.writeInt(52);
        mplew.writeInt(16000);
        mplew.writeInt(22000);
        mplew.writeInt(54);
        mplew.writeInt(16200);
        mplew.writeInt(24000);
        mplew.writeInt(55);
        mplew.writeInt(16400);
        mplew.writeInt(26000);
        mplew.writeInt(56);
        mplew.writeInt(16600);
        mplew.writeInt(28000);
        mplew.writeInt(57);
        mplew.writeInt(16800);
        mplew.writeInt(30000);
        mplew.writeInt(58);
        mplew.writeInt(17000);
        mplew.writeInt(50000);
        mplew.writeInt(60);
        mplew.writeInt(17300);
        mplew.writeInt(55000);
        mplew.writeInt(61);
        mplew.writeInt(17600);
        mplew.writeInt(60000);
        mplew.writeInt(62);
        mplew.writeInt(17900);
        mplew.writeInt(65000);
        mplew.writeInt(63);
        mplew.writeInt(18200);
        mplew.writeInt(70000);
        mplew.writeInt(64);
        mplew.writeInt(18500);
        mplew.writeInt(100000);
        mplew.writeInt(66);
        mplew.writeInt(18800);
        mplew.writeInt(110000);
        mplew.writeInt(67);
        mplew.writeInt(19100);
        mplew.writeInt(120000);
        mplew.writeInt(68);
        mplew.writeInt(19400);
        mplew.writeInt(130000);
        mplew.writeInt(69);
        mplew.writeInt(19700);
        mplew.writeInt(200000);
        mplew.writeInt(70);
        mplew.writeInt(20000);
        mplew.writeInt(0);
        mplew.writeInt(72);
        return mplew.getPacket();
    }

    public static byte[] ClearKillCount(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.ENFORCE_MSG.getValue());
        mplew.writeMapleAsciiString("kill_count");
        mplew.writeMapleAsciiString("0");
        return mplew.getPacket();
    }

    public static byte[] unk_761(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((short) 761);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.writeInt(16368);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] unk_763(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((short) 763);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] unk_764(MapleClient c, byte code, int code2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((short) 764);
        mplew.write(code);
        mplew.writeInt(code2);
        return mplew.getPacket();
    }

    public static byte[] unk_765(MapleClient c, byte code) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((short) 765);
        mplew.write(code);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] unk_932(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((short) 932);
        mplew.writeInt(c.getPlayer().getId());
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] unk_781(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((short) 781);
        mplew.writeInt(c.getPlayer().getId());
        mplew.write(1);
        mplew.write(1);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] unk_600(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.NirvanaPotentialResult.getValue());
        mplew.writeInt(-442901696);
        return mplew.getPacket();
    }

    public static byte[] _nickSkillEXP(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.NickSkillExpired.getValue());
        mplew.writeInt(3);
        mplew.writeInt(1116274179);
        return mplew.getPacket();
    }

    public static byte[] updateCore(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.UPDATE_CORE_AURA.getValue());
        mplew.writeInt(491);
        mplew.writeInt(1682029);
        mplew.writeInt(1682037);
        mplew.writeInt(1682045);
        mplew.writeInt(1680110);
        mplew.writeInt(1680134);
        mplew.writeInt(1680118);
        mplew.writeInt(1680142);
        mplew.writeInt(1680166);
        mplew.writeInt(1680126);
        mplew.writeInt(1680150);
        mplew.writeInt(1680174);
        mplew.writeInt(1680198);
        mplew.writeInt(1680158);
        mplew.writeInt(1680182);
        mplew.writeInt(1680206);
        mplew.writeInt(1680230);
        mplew.writeInt(1680190);
        mplew.writeInt(1680214);
        mplew.writeInt(1680238);
        mplew.writeInt(1680262);
        mplew.writeInt(1680222);
        mplew.writeInt(1680246);
        mplew.writeInt(1680270);
        mplew.writeInt(1680294);
        mplew.writeInt(1680254);
        mplew.writeInt(1680278);
        mplew.writeInt(1680286);
        mplew.writeInt(1680358);
        mplew.writeInt(1680366);
        mplew.writeInt(1680390);
        mplew.writeInt(1680350);
        mplew.writeInt(1680374);
        mplew.writeInt(1680398);
        mplew.writeInt(1680422);
        mplew.writeInt(1680382);
        mplew.writeInt(1680406);
        mplew.writeInt(1680430);
        mplew.writeInt(1680454);
        mplew.writeInt(1680414);
        mplew.writeInt(1680438);
        mplew.writeInt(1680462);
        mplew.writeInt(1680486);
        mplew.writeInt(1680446);
        mplew.writeInt(1680470);
        mplew.writeInt(1680518);
        mplew.writeInt(1680478);
        mplew.writeInt(1680526);
        mplew.writeInt(1680550);
        mplew.writeInt(1680582);
        mplew.writeInt(1680510);
        mplew.writeInt(1680534);
        mplew.writeInt(1680558);
        mplew.writeInt(1680614);
        mplew.writeInt(1680590);
        mplew.writeInt(1680542);
        mplew.writeInt(1680566);
        mplew.writeInt(1680622);
        mplew.writeInt(1680598);
        mplew.writeInt(1680574);
        mplew.writeInt(1680630);
        mplew.writeInt(1680606);
        mplew.writeInt(1680638);
        mplew.writeInt(1682030);
        mplew.writeInt(1682038);
        mplew.writeInt(1682046);
        mplew.writeInt(1680111);
        mplew.writeInt(1680135);
        mplew.writeInt(1680119);
        mplew.writeInt(1680143);
        mplew.writeInt(1680167);
        mplew.writeInt(1680127);
        mplew.writeInt(1680151);
        mplew.writeInt(1680175);
        mplew.writeInt(1680199);
        mplew.writeInt(1680159);
        mplew.writeInt(1680183);
        mplew.writeInt(1680207);
        mplew.writeInt(1680231);
        mplew.writeInt(1680191);
        mplew.writeInt(1680215);
        mplew.writeInt(1680239);
        mplew.writeInt(1680263);
        mplew.writeInt(1680223);
        mplew.writeInt(1680247);
        mplew.writeInt(1680271);
        mplew.writeInt(1680295);
        mplew.writeInt(1680255);
        mplew.writeInt(1680279);
        mplew.writeInt(1680287);
        mplew.writeInt(1680359);
        mplew.writeInt(1680367);
        mplew.writeInt(1680391);
        mplew.writeInt(1680351);
        mplew.writeInt(1680375);
        mplew.writeInt(1680399);
        mplew.writeInt(1680423);
        mplew.writeInt(1680383);
        mplew.writeInt(1680407);
        mplew.writeInt(1680431);
        mplew.writeInt(1680455);
        mplew.writeInt(1680415);
        mplew.writeInt(1680439);
        mplew.writeInt(1680463);
        mplew.writeInt(1680487);
        mplew.writeInt(1680447);
        mplew.writeInt(1680471);
        mplew.writeInt(1680519);
        mplew.writeInt(1680479);
        mplew.writeInt(1680503);
        mplew.writeInt(1680527);
        mplew.writeInt(1680551);
        mplew.writeInt(1680583);
        mplew.writeInt(1680511);
        mplew.writeInt(1680535);
        mplew.writeInt(1680559);
        mplew.writeInt(1680615);
        mplew.writeInt(1680591);
        mplew.writeInt(1680543);
        mplew.writeInt(1680567);
        mplew.writeInt(1680623);
        mplew.writeInt(1680575);
        mplew.writeInt(1680631);
        mplew.writeInt(1680607);
        mplew.writeInt(1682031);
        mplew.writeInt(1682039);
        mplew.writeInt(1682047);
        mplew.writeInt(1680128);
        mplew.writeInt(1680112);
        mplew.writeInt(1680136);
        mplew.writeInt(1680160);
        mplew.writeInt(1680120);
        mplew.writeInt(1680144);
        mplew.writeInt(1680168);
        mplew.writeInt(1680192);
        mplew.writeInt(1680152);
        mplew.writeInt(1680176);
        mplew.writeInt(1680200);
        mplew.writeInt(1680224);
        mplew.writeInt(1680184);
        mplew.writeInt(1680208);
        mplew.writeInt(1680232);
        mplew.writeInt(1680256);
        mplew.writeInt(1680216);
        mplew.writeInt(1680240);
        mplew.writeInt(1680264);
        mplew.writeInt(1680288);
        mplew.writeInt(1680248);
        mplew.writeInt(1680272);
        mplew.writeInt(1680296);
        mplew.writeInt(1680280);
        mplew.writeInt(1680352);
        mplew.writeInt(1680360);
        mplew.writeInt(1680384);
        mplew.writeInt(1680368);
        mplew.writeInt(1680392);
        mplew.writeInt(1680416);
        mplew.writeInt(1680376);
        mplew.writeInt(1680400);
        mplew.writeInt(1680424);
        mplew.writeInt(1680448);
        mplew.writeInt(1680408);
        mplew.writeInt(1680432);
        mplew.writeInt(1680456);
        mplew.writeInt(1680480);
        mplew.writeInt(1680440);
        mplew.writeInt(1680464);
        mplew.writeInt(1680488);
        mplew.writeInt(1680512);
        mplew.writeInt(1680472);
        mplew.writeInt(1680520);
        mplew.writeInt(1680544);
        mplew.writeInt(1680576);
        mplew.writeInt(1680504);
        mplew.writeInt(1680528);
        mplew.writeInt(1680552);
        mplew.writeInt(1680608);
        mplew.writeInt(1680584);
        mplew.writeInt(1680536);
        mplew.writeInt(1680560);
        mplew.writeInt(1680616);
        mplew.writeInt(1680592);
        mplew.writeInt(1680568);
        mplew.writeInt(1680624);
        mplew.writeInt(1680632);
        mplew.writeInt(1682048);
        mplew.writeInt(1682032);
        mplew.writeInt(1682040);
        mplew.writeInt(1680129);
        mplew.writeInt(1680113);
        mplew.writeInt(1680137);
        mplew.writeInt(1680161);
        mplew.writeInt(1680121);
        mplew.writeInt(1680145);
        mplew.writeInt(1680193);
        mplew.writeInt(1680153);
        mplew.writeInt(1680177);
        mplew.writeInt(1680201);
        mplew.writeInt(1680225);
        mplew.writeInt(1680185);
        mplew.writeInt(1680209);
        mplew.writeInt(1680233);
        mplew.writeInt(1680257);
        mplew.writeInt(1680217);
        mplew.writeInt(1680241);
        mplew.writeInt(1680265);
        mplew.writeInt(1680289);
        mplew.writeInt(1680249);
        mplew.writeInt(1680273);
        mplew.writeInt(1680297);
        mplew.writeInt(1680281);
        mplew.writeInt(1680353);
        mplew.writeInt(1680361);
        mplew.writeInt(1680385);
        mplew.writeInt(1680369);
        mplew.writeInt(1680393);
        mplew.writeInt(1680417);
        mplew.writeInt(1680377);
        mplew.writeInt(1680401);
        mplew.writeInt(1680425);
        mplew.writeInt(1680449);
        mplew.writeInt(1680409);
        mplew.writeInt(1680433);
        mplew.writeInt(1680457);
        mplew.writeInt(1680481);
        mplew.writeInt(1680441);
        mplew.writeInt(1680465);
        mplew.writeInt(1680489);
        mplew.writeInt(1680513);
        mplew.writeInt(1680473);
        mplew.writeInt(1680521);
        mplew.writeInt(1680545);
        mplew.writeInt(1680577);
        mplew.writeInt(1680505);
        mplew.writeInt(1680529);
        mplew.writeInt(1680553);
        mplew.writeInt(1680609);
        mplew.writeInt(1680585);
        mplew.writeInt(1680537);
        mplew.writeInt(1680561);
        mplew.writeInt(1680617);
        mplew.writeInt(1680593);
        mplew.writeInt(1680569);
        mplew.writeInt(1680625);
        mplew.writeInt(1680633);
        mplew.writeInt(1682049);
        mplew.writeInt(1682033);
        mplew.writeInt(1682041);
        mplew.writeInt(1680130);
        mplew.writeInt(1680114);
        mplew.writeInt(1680138);
        mplew.writeInt(1680162);
        mplew.writeInt(1680122);
        mplew.writeInt(1680146);
        mplew.writeInt(1680170);
        mplew.writeInt(1680194);
        mplew.writeInt(1680154);
        mplew.writeInt(1680178);
        mplew.writeInt(1680202);
        mplew.writeInt(1680226);
        mplew.writeInt(1680186);
        mplew.writeInt(1680210);
        mplew.writeInt(1680234);
        mplew.writeInt(1680258);
        mplew.writeInt(1680218);
        mplew.writeInt(1680242);
        mplew.writeInt(1680266);
        mplew.writeInt(1680290);
        mplew.writeInt(1680250);
        mplew.writeInt(1680274);
        mplew.writeInt(1680298);
        mplew.writeInt(1680282);
        mplew.writeInt(1680354);
        mplew.writeInt(1680362);
        mplew.writeInt(1680386);
        mplew.writeInt(1680370);
        mplew.writeInt(1680394);
        mplew.writeInt(1680418);
        mplew.writeInt(1680378);
        mplew.writeInt(1680402);
        mplew.writeInt(1680426);
        mplew.writeInt(1680450);
        mplew.writeInt(1680410);
        mplew.writeInt(1680434);
        mplew.writeInt(1680458);
        mplew.writeInt(1680482);
        mplew.writeInt(1680442);
        mplew.writeInt(1680466);
        mplew.writeInt(1680490);
        mplew.writeInt(1680514);
        mplew.writeInt(1680474);
        mplew.writeInt(1680522);
        mplew.writeInt(1680546);
        mplew.writeInt(1680578);
        mplew.writeInt(1680506);
        mplew.writeInt(1680530);
        mplew.writeInt(1680554);
        mplew.writeInt(1680610);
        mplew.writeInt(1680586);
        mplew.writeInt(1680538);
        mplew.writeInt(1680562);
        mplew.writeInt(1680618);
        mplew.writeInt(1680594);
        mplew.writeInt(1680570);
        mplew.writeInt(1680626);
        mplew.writeInt(1680634);
        mplew.writeInt(1682050);
        mplew.writeInt(1682034);
        mplew.writeInt(1682042);
        mplew.writeInt(1680107);
        mplew.writeInt(1680131);
        mplew.writeInt(1680115);
        mplew.writeInt(1680139);
        mplew.writeInt(1680163);
        mplew.writeInt(1680123);
        mplew.writeInt(1680147);
        mplew.writeInt(1680171);
        mplew.writeInt(1680195);
        mplew.writeInt(1680155);
        mplew.writeInt(1680179);
        mplew.writeInt(1680203);
        mplew.writeInt(1680227);
        mplew.writeInt(1680187);
        mplew.writeInt(1680211);
        mplew.writeInt(1680235);
        mplew.writeInt(1680259);
        mplew.writeInt(1680219);
        mplew.writeInt(1680243);
        mplew.writeInt(1680267);
        mplew.writeInt(1680291);
        mplew.writeInt(1680251);
        mplew.writeInt(1680275);
        mplew.writeInt(1680283);
        mplew.writeInt(1680355);
        mplew.writeInt(1680363);
        mplew.writeInt(1680387);
        mplew.writeInt(1680347);
        mplew.writeInt(1680371);
        mplew.writeInt(1680395);
        mplew.writeInt(1680419);
        mplew.writeInt(1680379);
        mplew.writeInt(1680403);
        mplew.writeInt(1680427);
        mplew.writeInt(1680451);
        mplew.writeInt(1680411);
        mplew.writeInt(1680435);
        mplew.writeInt(1680459);
        mplew.writeInt(1680483);
        mplew.writeInt(1680443);
        mplew.writeInt(1680467);
        mplew.writeInt(1680515);
        mplew.writeInt(1680475);
        mplew.writeInt(1680523);
        mplew.writeInt(1680547);
        mplew.writeInt(1680579);
        mplew.writeInt(1680507);
        mplew.writeInt(1680531);
        mplew.writeInt(1680555);
        mplew.writeInt(1680611);
        mplew.writeInt(1680587);
        mplew.writeInt(1680539);
        mplew.writeInt(1680563);
        mplew.writeInt(1680619);
        mplew.writeInt(1680595);
        mplew.writeInt(1680571);
        mplew.writeInt(1680627);
        mplew.writeInt(1680603);
        mplew.writeInt(1680635);
        mplew.writeInt(1682027);
        mplew.writeInt(1682035);
        mplew.writeInt(1682043);
        mplew.writeInt(1680108);
        mplew.writeInt(1680132);
        mplew.writeInt(1680116);
        mplew.writeInt(1680140);
        mplew.writeInt(1680164);
        mplew.writeInt(1680124);
        mplew.writeInt(1680148);
        mplew.writeInt(1680172);
        mplew.writeInt(1680196);
        mplew.writeInt(1680156);
        mplew.writeInt(1680180);
        mplew.writeInt(1680204);
        mplew.writeInt(1680228);
        mplew.writeInt(1680188);
        mplew.writeInt(1680212);
        mplew.writeInt(1680236);
        mplew.writeInt(1680260);
        mplew.writeInt(1680220);
        mplew.writeInt(1680244);
        mplew.writeInt(1680268);
        mplew.writeInt(1680292);
        mplew.writeInt(1680252);
        mplew.writeInt(1680276);
        mplew.writeInt(1680284);
        mplew.writeInt(1680356);
        mplew.writeInt(1680364);
        mplew.writeInt(1680388);
        mplew.writeInt(1680348);
        mplew.writeInt(1680372);
        mplew.writeInt(1680396);
        mplew.writeInt(1680420);
        mplew.writeInt(1680380);
        mplew.writeInt(1680404);
        mplew.writeInt(1680428);
        mplew.writeInt(1680452);
        mplew.writeInt(1680412);
        mplew.writeInt(1680436);
        mplew.writeInt(1680460);
        mplew.writeInt(1680484);
        mplew.writeInt(1680444);
        mplew.writeInt(1680468);
        mplew.writeInt(1680516);
        mplew.writeInt(1680476);
        mplew.writeInt(1680524);
        mplew.writeInt(1680548);
        mplew.writeInt(1680580);
        mplew.writeInt(1680508);
        mplew.writeInt(1680532);
        mplew.writeInt(1680556);
        mplew.writeInt(1680612);
        mplew.writeInt(1680588);
        mplew.writeInt(1680540);
        mplew.writeInt(1680564);
        mplew.writeInt(1680620);
        mplew.writeInt(1680596);
        mplew.writeInt(1680572);
        mplew.writeInt(1680628);
        mplew.writeInt(1680604);
        mplew.writeInt(1680636);
        mplew.writeInt(1682028);
        mplew.writeInt(1682036);
        mplew.writeInt(1682044);
        mplew.writeInt(1680109);
        mplew.writeInt(1680133);
        mplew.writeInt(1680117);
        mplew.writeInt(1680141);
        mplew.writeInt(1680165);
        mplew.writeInt(1680125);
        mplew.writeInt(1680149);
        mplew.writeInt(1680173);
        mplew.writeInt(1680197);
        mplew.writeInt(1680157);
        mplew.writeInt(1680181);
        mplew.writeInt(1680205);
        mplew.writeInt(1680229);
        mplew.writeInt(1680189);
        mplew.writeInt(1680213);
        mplew.writeInt(1680237);
        mplew.writeInt(1680261);
        mplew.writeInt(1680221);
        mplew.writeInt(1680245);
        mplew.writeInt(1680269);
        mplew.writeInt(1680293);
        mplew.writeInt(1680253);
        mplew.writeInt(1680277);
        mplew.writeInt(1680285);
        mplew.writeInt(1680357);
        mplew.writeInt(1680365);
        mplew.writeInt(1680389);
        mplew.writeInt(1680349);
        mplew.writeInt(1680373);
        mplew.writeInt(1680397);
        mplew.writeInt(1680421);
        mplew.writeInt(1680381);
        mplew.writeInt(1680405);
        mplew.writeInt(1680429);
        mplew.writeInt(1680453);
        mplew.writeInt(1680413);
        mplew.writeInt(1680437);
        mplew.writeInt(1680461);
        mplew.writeInt(1680485);
        mplew.writeInt(1680445);
        mplew.writeInt(1680469);
        mplew.writeInt(1680517);
        mplew.writeInt(1680477);
        mplew.writeInt(1680525);
        mplew.writeInt(1680549);
        mplew.writeInt(1680581);
        mplew.writeInt(1680509);
        mplew.writeInt(1680533);
        mplew.writeInt(1680557);
        mplew.writeInt(1680613);
        mplew.writeInt(1680589);
        mplew.writeInt(1680541);
        mplew.writeInt(1680565);
        mplew.writeInt(1680621);
        mplew.writeInt(1680597);
        mplew.writeInt(1680573);
        mplew.writeInt(1680629);
        mplew.writeInt(1680605);
        mplew.writeInt(1680637);
        return mplew.getPacket();
    }

    public static byte[] lys(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SetTamingMobInfo.getValue());
        mplew.writeInt(c.getPlayer().getId());
        mplew.writeInt(1);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] loginExtra() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.EXTRA_SYSTEM_RESULT.getValue());
        mplew.writeInt(-1289454273);
        mplew.writeShort(2); // login type
        mplew.writeInt(-1395856379);
        mplew.writeInt(1090473174);
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        return mplew.getPacket();
    }

    public static byte[] initGuild() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_GuildResult.getValue());
        mplew.writeInt(128);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] initGuildSlotUser() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_GuildResult.getValue());
        mplew.writeInt(129);
        mplew.writeInt(4);
        mplew.writeInt(100);
        mplew.writeInt(2000);
        mplew.writeInt(60);
        mplew.writeInt(1000);
        mplew.writeInt(30);
        mplew.writeInt(100);
        mplew.writeInt(15);
        mplew.writeInt(50);
        return mplew.getPacket();
    }

    public static byte[] linkSkillNotice(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SetTeachSkillCost.getValue());
        mplew.writeInt(10);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(5000000);
        mplew.writeInt(6000000);
        mplew.writeInt(7000000);
        mplew.writeInt(8000000);
        mplew.writeInt(9000000);
        mplew.writeInt(10000000);
        return mplew.getPacket();
    }

    public static byte[] setNameTagHide() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_EventNameTagInfo.getValue());
        mplew.writeShort(0);
        mplew.write(-1);
        mplew.writeShort(0);
        mplew.write(-1);
        mplew.writeShort(0);
        mplew.write(-1);
        mplew.writeShort(0);
        mplew.write(-1);
        mplew.writeShort(0);
        mplew.write(-1);
        return mplew.getPacket();
    }

    public static byte[] chatServerPong() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CHAT_SERVER_RESULT.getValue());
        mplew.writeInt(0);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static OutPacket getShowQuestCompletion(int quest_cid) {
        OutPacket say = new OutPacket(OutHeader.LP_QuestClear.getValue());
        if (getChr() != null) {
            say.encodeInt(quest_cid);
        }
        return say;
    }

    public static OutPacket SetBackgroundEffect() {
        OutPacket outPacket = new OutPacket(OutHeader.LP_SetBackgroundEffect);
        outPacket.encodeInt(5);
        outPacket.encodeInt(1);
        outPacket.encodeInt(99);
        outPacket.encodeInt(2000);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(-1717986918);
        outPacket.encodeInt(1071225241);
        outPacket.encodeInt(100);
        outPacket.encodeInt(199);
        outPacket.encodeInt(4000);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(0);
        outPacket.encodeInt(-1717986918);
        outPacket.encodeInt(1071225241);
        outPacket.encodeInt(200);
        outPacket.encodeInt(259);
        outPacket.encodeInt(8000);
        outPacket.encodeInt(5);
        outPacket.encodeInt(500);
        outPacket.encodeInt(0);
        outPacket.encodeInt(-1717986918);
        outPacket.encodeInt(1071225241);
        outPacket.encodeInt(260);
        outPacket.encodeInt(279);
        outPacket.encodeInt(15000);
        outPacket.encodeInt(5);
        outPacket.encodeInt(1000);
        outPacket.encodeInt(0);
        outPacket.encodeInt(-1717986918);
        outPacket.encodeInt(1071225241);
        outPacket.encodeInt(280);
        outPacket.encodeInt(300);
        outPacket.encodeInt(20000);
        outPacket.encodeInt(5);
        outPacket.encodeInt(1500);
        outPacket.encodeInt(0);
        outPacket.encodeInt(-1717986918);
        outPacket.encodeInt(1071225241);
        return outPacket;
    }

    public static OutPacket EquipRuneSetting() {
        OutPacket outPacket = new OutPacket(OutHeader.LP_ReincarnationStele);
        outPacket.encodeArr("07 04 00 00 00 9B 58 F7 B7 01 07 00 00 00 00 00 00 00 00 00 08 00 00 00 00 00 00 00 00 00 09 00 00 00 00 00 0A 00 00 00 00 00 0B 00 00 00 00 00 0C 00 00 00 00 00 0D 00 00 0E 00 00 0F 00 00 00 00 00 00 00 00 00 10 00 00 11 00 01 12 00 00 13 00 01 14 00 00 00 00 00 00 00 00");
        return outPacket;
    }


    public static byte[] getMacros(SkillMacro[] macros) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SKILL_MACRO.getValue());
        int count = 0;
        for (int i = 0; i < 5; i++) {
            if (macros[i] != null) {
                count++;
            }
        }
        mplew.write(count); // number of macros
        for (int i = 0; i < 5; i++) {
            SkillMacro macro = macros[i];
            if (macro != null) {
                mplew.writeMapleAsciiString(macro.getName());
                mplew.write(macro.getShout());
                mplew.writeInt(macro.getSkill1());
                mplew.writeInt(macro.getSkill2());
                mplew.writeInt(macro.getSkill3());
            }
        }
        return mplew.getPacket();
    }
}


