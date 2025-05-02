package Packet;

import Client.*;
import Client.inventory.*;
import Client.skills.ExtraSkill;
import Client.skills.InnerSkillEntry;
import Client.skills.SkillEntry;
import Client.skills.SkillMacro;
import Client.stat.DeadDebuff;
import Client.stat.MapleHyperStats;
import Config.configs.ServerConfig;
import Config.constants.*;
import Config.constants.enums.UserChatMessageType;
import Config.constants.skills.*;
import Config.constants.skills.冒險家_技能群組.type_劍士.聖騎士;
import Config.constants.skills.冒險家_技能群組.type_劍士.英雄;
import Config.constants.skills.冒險家_技能群組.type_劍士.黑騎士;
import Config.constants.skills.冒險家_技能群組.type_法師.主教;
import Config.constants.skills.冒險家_技能群組.type_法師.冰雷;
import Config.constants.skills.冒險家_技能群組.type_法師.火毒;
import Config.constants.skills.冒險家_技能群組.*;
import Config.constants.skills.皇家騎士團_技能群組.*;
import Database.DatabaseConnectionEx;
import Net.server.*;
import Net.server.events.MapleSnowball;
import Net.server.life.MobSkill;
import Net.server.maps.*;
import Net.server.maps.MapleNodes.MaplePlatform;
import Net.server.movement.LifeMovementFragment;
import Net.server.shops.HiredFisher;
import Net.server.shops.HiredMerchant;
import Net.server.shops.MaplePlayerShopItem;
import Opcode.Headler.OutHeader;
import Opcode.Opcode.MessageOpcode;
import Server.channel.ChannelServer;
import Server.channel.handler.AttackInfo;
import Server.channel.handler.AttackMobInfo;
import Server.channel.handler.InventoryHandler;
import Server.channel.handler.TakeDamageHandler;
import Server.world.World;
import Server.world.WorldGuildService;
import Server.world.guild.MapleGuild;
import SwordieX.client.character.ExtendSP;
import SwordieX.client.character.avatar.AvatarLook;
import SwordieX.client.character.keys.FuncKeyMap;
import connection.OutPacket;
import connection.packet.FieldPacket;
import SwordieX.field.fieldeffect.FieldEffect;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.*;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

import static Config.constants.enums.BroadcastMessageType.*;
import static Packet.PacketHelper.encodeInventory;

public class MaplePacketCreator {

    public final static Map<MapleStat, Long> EMPTY_STATUPDATE = Collections.emptyMap();
    /*
     * 更新角色內在能力技能
     * 參數 角色
     * 參數 是否升級
     */

    private static final Logger log = LoggerFactory.getLogger(MaplePacketCreator.class);

    public static byte[] getWzCheck(String WzCheckPack) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WZ_CHECK.getValue());
        mplew.write(HexTool.getByteArrayFromHexString(WzCheckPack));
        return mplew.getPacket();
    }

    /**
     * 客戶端驗證
     *
     * @param fileValue
     * @return 返回客戶端檢查結果
     */
    public static byte[] getClientAuthentication(int fileValue) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_AuthenMessage.getValue());
        mplew.writeInt(fileValue);
        return mplew.getPacket();
    }

    public static byte[] getServerIP(int port, int charId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SelectCharacterResult.getValue());
        int a1 = 0;
        mplew.write(a1);
        mplew.writeMapleAsciiString("");
        mplew.write(0);
        switch (a1) {
            case 39:
            case 55:
            case 67:
                break;
            case 83:
                int v3 = 1;
                mplew.write(v3);
                if (v3 > 0) {
                    mplew.writeInt(0); // > 0 && <= 2
                    mplew.writeMapleAsciiString("normal");
                    mplew.writeMapleAsciiString("normal");
                    mplew.writeMapleAsciiString("normal");
                }
                break;
            default:
                mplew.write(ServerConstants.getIPBytes(ServerConstants.getIPv4Address()));
                mplew.writeShort(port);
                mplew.writeInt(charId);
                mplew.writeInt(1);
                mplew.writeInt(1);
                mplew.writeInt(1);
                mplew.writeInt(414498149); // CHANGE 267
                mplew.writeShort(0);
                mplew.writeInt(0);
                mplew.write(20);
                mplew.writeInt(1000);
                mplew.writeInt(775850237); // CHANGE 267
                mplew.writeInt(4);
                mplew.writeInt(1922946074);
                break;
        }
        return mplew.getPacket();
    }

    public static byte[] DummyGamaniaNat(int port, int charId) {
        MaplePacketLittleEndianWriter DummyNat = new MaplePacketLittleEndianWriter();
        DummyNat.writeShort(OutHeader.LP_SelectCharacterResult.getValue());
        int a2 = 0;
        DummyNat.write(a2);
        DummyNat.writeMapleAsciiString("");
        DummyNat.write(0);
        switch (a2) {
            case 39:
            case 55:
            case 67:
                break;
            case 83:
                int v3 = 1;
                DummyNat.write(v3);
                if (v3 > 0) {
                    DummyNat.writeInt(0); // > 0 && <= 2
                    DummyNat.writeMapleAsciiString("normal");
                    DummyNat.writeMapleAsciiString("normal");
                    DummyNat.writeMapleAsciiString("normal");
                }
                break;
            default:
                DummyNat.write(ServerConstants.getGamaniaServerIP());
                DummyNat.writeShort(port);
                DummyNat.writeInt(charId);
                DummyNat.writeInt(1);
                DummyNat.writeInt(1);
                DummyNat.writeInt(1);
                DummyNat.writeInt(414498149); // CHANGE 267
                DummyNat.writeShort(0);
                DummyNat.writeInt(0);
                DummyNat.write(20);
                DummyNat.writeInt(1000);
                DummyNat.writeInt(775850237); // CHANGE 267
                DummyNat.writeInt(4);
                DummyNat.writeInt(1922946074);
                break;
        }
        return DummyNat.getPacket();
    }

    /* 257 */
    public static byte[] getChannelChange(MapleClient c, int port) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MigrateCommand.getValue());
        ChannelServer toch = ChannelServer.getInstance(c.getChannel()); //角色從商城出來更換的頻道信息
        mplew.write(toch.getChannel());
        mplew.write(ServerConstants.getIPBytes(ServerConstants.getIPv4Address()));
        mplew.writeShort(port);
        return mplew.getPacket();
    }

    /*
     * 隱藏頭頂稱號
     * V.112.1新增
     */
    public static byte[] cancelTitleEffect(MapleCharacter chr) {
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
        chr.send(mplew.getPacket());
        return mplew.getPacket();
    }

    public static byte[] sendFieldToPosition(MapleCharacter player, MapleMap to, Point position) {
        return getWarpToMap(player, to, position, 0, false, false);
    }

    public static byte[] getWarpToMap(MapleCharacter player, MapleMap to, int spawnPoint, boolean revive) {
        return getWarpToMap(player, to, null, spawnPoint, false, revive);
    }

    /**
     * Gets character info for a character.
     *
     * @param player   The character to get info about.
     * @param position
     * @return The character info packet. 42 8B 8E 0F
     */
    public static byte[] getWarpToMap(MapleCharacter player, MapleMap to, Point position, int spawnPoint, boolean load, boolean revive) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SET_FIELD.getValue());
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        mplew.writeInt(player.getClient().getChannel() - 1);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(load ? 1 : 2);
        mplew.writeInt(load ? 0 : to.getFieldType());
        mplew.writeInt(Math.abs(to.getBottom() - to.getTop()));
        mplew.writeInt(Math.abs(to.getRight() - to.getLeft()));
        mplew.writeBool(load);
        int nNotifierCheck = 0;
        mplew.writeShort(nNotifierCheck); // size :: v104
        if (nNotifierCheck != 0) {
            // pBlockReasonIter
            mplew.writeMapleAsciiString("");
            for (int i = 0; i < nNotifierCheck; i++) {
                // sNotifierMessage
                mplew.writeMapleAsciiString("");
            }
        }
        int mapId = to.getId();
        if (load) {
            int WarpToMapCrc = Randomizer.nextInt();
            int WarpToMapCrc2 = Randomizer.nextInt();
            int WarpToMapCrc3 = Randomizer.nextInt();
            mplew.writeInt(new newCRand32().random());
            mplew.writeInt(new newCRand32().random());
            mplew.writeInt(new newCRand32().random());
            PacketHelper.addCharacterInfo(mplew, player, -1);
            boolean bUnk = true;
            mplew.writeBool(bUnk);
            if (bUnk) {
                bUnk = false;
                mplew.writeBool(bUnk);
                if (bUnk) {
                    mplew.writeInt(0);
                }
                mplew.writeLong(PacketHelper.getTime(-2));

                // 官方
                mplew.write(0);
                mplew.writeLong(PacketHelper.getTime(-2));
                mplew.writeLong(0);
                /* IDA
                mplew.writeInt(0);
                for (int i = 0; i < 3; i++) {
                    mplew.writeInt(0);
                }
                mplew.write(0);
                */

                mplew.writeLong(0);
            }
        } else {
            mplew.writeBool(revive);
            mplew.writeInt(mapId); //地圖ID
            mplew.write(spawnPoint);
            mplew.writeInt(player.getStat().getHp()); // 角色HP
            mplew.writeInt(0);
            mplew.write(position != null);
            if (position != null) {
                mplew.writeInt(position.x);
                mplew.writeInt(position.y);
            }
        }
        boolean bUnk = false;
        mplew.write(bUnk);
        if (bUnk) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        // CWvsContext::SetWhiteFadeInOut
        mplew.write(2);
        // bChatBlockReason
        mplew.writeBool(to.getFieldType() >= 182 && to.getFieldType() <= 184);
        mplew.writeInt(100);

        /* FieldCustom */
        boolean bCFieldCustom = false; // to.isCustomMap()
        mplew.writeBool(bCFieldCustom);
        if (bCFieldCustom) {
            mplew.writeInt(0); // partyBonusExpRate
            mplew.writeMapleAsciiString("" /* to.getCustomMapBgm() */); // BGM
            mplew.writeInt(0/* to.getCustomBgMapID() */); // bgFieldID
        }
        // CWvsContext::OnInitPvPStat
        mplew.writeBool(false);
        // 萌獸
        int buffersize_familiar = 4;
        mplew.writeInt(buffersize_familiar);
        mplew.write(new byte[buffersize_familiar]);

        // bCanNotifyAnnouncedQuest
        //mplew.writeBool(false);
        // bCField::DrawStackEventGauge
        mplew.writeBool(JobConstants.isSeparatedSpJob(player.getJob()));

        //sub_145715160
        mplew.writeLong(0);
        mplew.writeInt(-1);

        // nCField::DrawStackEventGauge
        boolean v88 = false;
        mplew.writeBool(v88);
        if (v88) {
            mplew.writeInt(0);
        }
        if (mapId / 10 == 10520011 || mapId / 10 == 10520051 || mapId == 105200519) {
            String[] aS = new String[0];
            mplew.write(aS.length);
            for (String s : aS) {
                mplew.writeMapleAsciiString(s);
            }
        }
        //EncodeTextEquipInfo
        mplew.writeInt(0);
        //EncodeFreezeHotEventInfo
        mplew.write(0);
        mplew.writeInt(0);
        //EncodeEventBestFriendInfo
        mplew.writeInt(0);

        boolean setChrMenuItem = true;
        mplew.writeBool(setChrMenuItem);//V.153 new
        if (setChrMenuItem) {
            mplew.writeInt(-1); // 6
            mplew.writeInt(0); // 10000
            mplew.writeInt(0); // 100023
            mplew.writeInt(999999999); // 993034000
            mplew.writeInt(999999999); // 993034000
            mplew.writeMapleAsciiString(""); // 打招呼只能在 <萬聖節俱樂部> 裡進行。
        }
        mplew.writeInt(0);
        mplew.writeMapleAsciiString("");
        mplew.writeMapleAsciiString("");

        // 星期天楓之谷訊息
        boolean bSundayMaple = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == 1;
        mplew.writeBool(bSundayMaple);//V.153 new
        if (bSundayMaple) {
            mplew.writeMapleAsciiString("UI/UIWindowEvent.img/sundayMaple");
            mplew.writeMapleAsciiString("#sunday# #fnNanum Gothic ExtraBold##fs20##fc0xFFFFFFFF#完成怪物公園經驗值新增#fc0xFFFFD800#50%！\\r\\n" + //
                    "#sunday# #fs20##fc0xFFFFFFFF#烈焰戰狼消滅經驗值#fc0xFFFFD800#2倍！\\r\\n" + //
                    "#sunday# #fnNanum Gothic ExtraBold##fs20##fc0xFFFFFFFF#大波斯菊花瓣獲得量即可獲得量#fc0xFFFFD800#2倍！"); // #sunday# #fn???? ExtraBold##fs20##fc0xFFFFFFFF#星力強化費用#fc0xFFFFD800#7折 #fc0xFFFFFFFF#優惠！\n#sunday# #fs20##fc0xFFFFFFFF#咒文的痕跡強化成功機率#fc0xFFFFD800#提升！
            mplew.writeMapleAsciiString("#fn???? ExtraBold##fs15##fc0xFFB7EC00#2023年10月22日星期天"); // #fs15##fc0xFFB7EC00#2019年10月6日星期天
            mplew.writeInt(60); // 3C 00 00 00
            mplew.writeInt(210); // E8 00 00 00
        }
        mplew.writeInt(0);

        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(0);

        // sub_140E190B0
        int unkSize = 0;
        mplew.writeInt(unkSize);
        for (int i = 0; i < unkSize; i++) {
            mplew.writeMapleAsciiString("");
            mplew.writeInt(0);
        }
        // sub_140E190B0

        mplew.write(0);

        // sub_145BA4BB0
        int size = 0;
        mplew.writeInt(size);
        for (int i = 0; i < size; i++) {
            mplew.writeInt(0); // ?
        }
        // end sub_145BA4BB0

        return mplew.getPacket();
    }

    /**
     * Gets an empty stat update.
     *
     * @return The empy stat update packet.
     */
    public static byte[] enableActions(MapleCharacter chr, boolean useTriggerForUI) {
        return updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, useTriggerForUI, chr);
    }

    /**
     * Gets an update for specified stats.
     *
     * @param stats The stats to update.
     * @param chr
     * @return The stat update packet.
     */
    public static byte[] updatePlayerStats(Map<MapleStat, Long> stats, MapleCharacter chr) {
        return updatePlayerStats(stats, false, chr);
    }

    /**
     * Gets an update for specified stats.
     *
     * @param stats           The list of stats to update.
     * @param useTriggerForUI Result of an item reaction(?)
     * @param chr
     * @return The stat update packet.
     */
    public static byte[] updatePlayerStats(Map<MapleStat, Long> stats, boolean useTriggerForUI, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_StatChanged.getValue());
        mplew.write(useTriggerForUI ? 1 : 0);
        mplew.write(0);
        mplew.write(1); // v264 > change = 1
        long updateMask = 0;
        for (MapleStat statupdate : stats.keySet()) {
            updateMask |= statupdate.getValue();
        }
        mplew.writeInt(updateMask);
        mplew.writeInt(0); // v264 > change = 1
        for (Entry<MapleStat, Long> statupdate : stats.entrySet()) {
            switch (statupdate.getKey()) {
                case 皮膚: //0x01
                    mplew.write(statupdate.getValue().byteValue());
                    break;
                case 職業: //0x20
                    mplew.writeShort(statupdate.getValue().shortValue());
                    mplew.writeShort(chr.getSubcategory());
                    break;
                case 疲勞: //0x80000
                case 力量: //0x40
                case 敏捷: //0x80
                case 智力: //0x100
                case 幸運: //0x200
                case AVAILABLEAP: //0x4000
                    mplew.writeShort(statupdate.getValue().shortValue());
                    break;
                case AVAILABLESP: //0x8000
                    if (JobConstants.isSeparatedSpJob(chr.getJob())) {
                        OutPacket outPacket = new OutPacket();
                        new ExtendSP(chr).encode(outPacket);
                        mplew.write(outPacket.getData());
                    } else {
                        mplew.writeShort(chr.getRemainingSp());
                    }
                    break;
                case TRAIT_LIMIT: //0x8000000
                    mplew.write(0);
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                    mplew.write(0);
                    break;
                case TODAYS_TRAITS: //0x4000000
                    mplew.writeZeroBytes(21);
                    break;
                case BATTLE_EXP: //0x10000000
                    chr.getCharacterCard().connectData(mplew);
                    break;
                case 經驗: //0x10000
                case 楓幣: //0x40000
                    mplew.writeLong(statupdate.getValue());
                    break;
                case BATTLE_POINTS: //0x40000000
                    mplew.write(5);
                    mplew.write(6);
                    break;
                case BATTLE_RANK: //0x20000000
                    mplew.writeInt(chr.getStat().pvpExp);
                    mplew.write(chr.getStat().pvpRank);
                    mplew.writeInt(chr.getBattlePoints());
                    break;
                default:
                    mplew.writeInt(statupdate.getValue());
                    break;
            }
        }
        mplew.writeBool(updateMask == 0);
        if (updateMask == 0) {
            mplew.write(1);
        }
        boolean unk = false;
        mplew.writeBool(unk);
        if (unk) {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    /**
     * 更新極限屬性分頁
     *
     * @param chr
     * @param pos
     * @param action
     * @return packet
     */
    public static byte[] updateHyperPresets(MapleCharacter chr, int pos, byte action) {
        MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
        packet.writeShort(OutHeader.LP_HyperPreset.getValue());
        packet.write(pos);
        packet.write(action);
        if (action != 0) {
            for (int i = 0; i <= 2; i++) {
                packet.writeInt(chr.loadHyperStats(i).size());
                for (MapleHyperStats mhsz : chr.loadHyperStats(i)) {
                    packet.writeInt(mhsz.getPosition());
                    packet.writeInt(mhsz.getSkillid());
                    packet.writeInt(mhsz.getSkillLevel());
                }
            }
        }
        return packet.getPacket();
    }

    /*
     * 武陵道場移動
     */
    public static byte[] instantMapWarp(byte portal) {
//        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
//
//        mplew.writeShort(OutHeader.CURRENT_MAP_WARP.getValue());
//        mplew.writeShort(0);
//        mplew.writeInt(portal); // 6
//
//        return mplew.getPacket();
        return userTeleport(false, 0, portal, null);
    }

    /*
     * 火焰傳動痕跡
     */
    public static byte[] sendSkillUseResult(boolean success, int skillId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SkillUseResult.getValue());
        mplew.writeBool(success);
        mplew.writeInt(skillId);
        return mplew.getPacket();
    }

    /*
     * 角色移動到地圖的另外1個坐標地點
     */
    public static byte[] instantMapWarp(int charId, Point pos) {
//        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
//
//        mplew.writeShort(OutHeader.CURRENT_MAP_WARP.getValue());
//        mplew.write(0x00);
//        mplew.write(0x02);
//        mplew.writeInt(charId); //角色ID
//        mplew.writePos(pos); //移動到的坐標
//
//        return mplew.getPacket();
        return userTeleport(false, 2, charId, pos);
    }

    public static byte[] userTeleportOnRevive(final int charid, final Point point) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.UserTeleportOnRevive.getValue());
        mplew.writeInt(charid);
        mplew.writePosInt(point.getLocation());
        return mplew.getPacket();
    }

    public static byte[] userTeleport(final boolean b, final int n, final int charid, final Point point) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserTeleport.getValue());
        mplew.writeBool(b);
        mplew.write(n);
        mplew.writeInt(charid);
        mplew.writeShort((int) point.getX());
        mplew.writeShort((int) point.getY());
        return mplew.getPacket();
    }

    /**
     * Gets a packet to spawn a portal.
     *
     * @param townId   The ID of the town the portal goes to.
     * @param targetId The ID of the target.
     * @param skillId
     * @param pos      Where to put the portal.
     * @return The portal spawn packet.
     */
    public static byte[] onTownPortal(int townId, int targetId, int skillId, Point pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_TownPortal.getValue());
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        if (townId != 999999999 && targetId != 999999999) {
            mplew.writeInt(skillId);
            mplew.writePos(pos);
        }

        return mplew.getPacket();
    }

    public static byte[] getRandomPortalCreated(MapleRandomPortal portal) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_RandomPortalCreated.getValue());
        mplew.write(portal.getAppearType().ordinal());
        mplew.writeInt(portal.getObjectId());
        mplew.writePos(portal.getPosition());
        mplew.writeInt(portal.getMapid());
        mplew.writeInt(portal.getOwerid());

        return mplew.getPacket();
    }

    public static byte[] getRandomPortalTryEnterRequest() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_RandomPortalTryEnterRequest.getValue());
        return mplew.getPacket();
    }

    public static byte[] getRandomPortalRemoved(MapleRandomPortal portal) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_RandomPortalRemoved.getValue());
        mplew.write(portal.getAppearType().ordinal());
        mplew.writeInt(portal.getObjectId());
        mplew.writeInt(portal.getMapid());

        return mplew.getPacket();
    }

    public static byte[] getFieldVoice(String patch) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_PlaySound.getValue());
        mplew.writeMapleAsciiString(patch);

        return mplew.getPacket();
    }

    /*
     * 重置屏幕
     */
    public static byte[] resetScreen() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.RESET_SCREEN.getValue());

        return mplew.getPacket();
    }

    /**
     * 發送地圖錯誤信息到客戶端 數據包的值大概如下: 0x01: 現在關閉了縮地門 0x02: 因某種原因，不能去那裡 0x03:
     * 對不起，正在準備楓之谷ONLINE商城 - 彈出窗口 0x04: 因為有地氣阻擋，無法接近。 0x05：無法進行瞬間移動的地區。 - 彈出窗口
     * 0x06：無法進行瞬間移動的地區。 0x07: 隊員的等級差異太大，無法入場。 0x08: 只有組隊成員才能入場的地圖 0x09:
     * 只有隊長可以申請入場。 0x0A: 請在隊員全部聚齊後申請入場。 0x0B:
     * 你因不當行為，而遭遊戲管理員禁止攻擊，禁止獲取經驗值和楓幣，禁止交易，禁止丟棄道具，禁止開啟個人商店與精靈商人，禁止組隊，禁止使用拍賣系統，因此無法使用改功能。
     * 0x0C: 只有遠征隊員可以進入該地圖。 0x0D: 所有副本人數已滿。請使用其他頻道。 0x0E: 遠征隊入場時間已結束，無法進入。
     */
    public static byte[] mapBlocked(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.MAP_BLOCKED.getValue());
        mplew.write(type);

        return mplew.getPacket();
    }

    /**
     * 發送錯誤信息到客戶端 數據包的值大概如下: 0x01: 日前無法進入該頻道，請稍後在嘗試。 0x02: 現在無法進入楓之谷商城。請稍後在嘗試。
     * 0x03: 只有在PVE伺服器中可以使用。 0x04: 根據非活躍帳號保護政策，限制使用商城。必須登錄官方網站進行身份認證後，才能正常使用。
     * 0x05: 現在無法操作。請稍後再試。- 對話框提示 0x06：日前無法進入，請玩家稍後在試.(電擊象伺服器目前不開放拍賣平台)
     * 0x07：日前拍賣系統擁塞中，請稍後再試！ 0x08:
     * 你因不當行為，而遭遊戲管理員禁止攻擊，禁止獲取經驗值和楓幣，禁止交易，禁止丟棄道具，禁止開啟個人商店與精靈商人，禁止組隊，禁止使用拍賣系統，因此無法使用改功能。
     */
    public static byte[] serverBlocked(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.SERVER_BLOCKED.getValue());
        mplew.write(type);

        return mplew.getPacket();
    }

    /**
     * 發送錯誤信息到客戶端 數據包的值大概如下: 0x01: 現在無法登錄大亂鬥伺服器。請稍後重新嘗試。 0x02: 從頻道中獲取隊員信息失敗。
     * 0x03: 只有隊長可以進行。 0x04: 存在未復活的隊員。 0x05：有隊員在其他地方。 0x06：只有在大亂鬥伺服器中可以使用。 0x07:
     * 無 0x08: 不符合頻道入場條件的隊員無法移動。請重新確認。 - 對話框提示 0x09: 組隊狀態下無法入場的模式。請退出組隊後重新嘗試。 -
     * 對話框提示
     */
    public static byte[] partyBlocked(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.PARTY_BLOCKED.getValue());
        mplew.write(type);

        return mplew.getPacket();
    }

    public static byte[] serverMessage(String message) {
        return serverMessage(4, 0, message, false, null, Collections.emptyList(), 0);
    }

    public static byte[] serverNotice(int type, String message) {
        return serverMessage(type, 0, message, false, null, Collections.emptyList(), 0);
    }

    public static byte[] serverNotice(int type, int channel, String message) {
        return serverMessage(type, channel, message, false, null, Collections.emptyList(), 0);
    }

    public static byte[] serverNotice(int type, int channel, String message, boolean smegaEar) {
        return serverMessage(type, channel, message, smegaEar, null, Collections.emptyList(), 0);
    }

    private static byte[] serverMessage(int type, int channel, String message, boolean megaEar, Item item, List<String> list, int rareness) {
        return serverMessage(type, channel, message, megaEar, item, list, rareness, "", 0);
    }

    private static byte[] serverMessage(int type, int channel, String message, boolean megaEar, Item item, List<String> list, int rareness, String speakerName, int speakerId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_BroadcastMsg.getValue());
        String speekerName = "";
        int chrId = 0;
        mplew.write(type);
        boolean msg = type != ANNOUNCED_QUEST_OPEN && type != ANNOUNCED_QUEST_CLOSED && type != 27;
        if (type == SLIDE || type == 26) {
            msg = true;
            mplew.writeBool(msg);
        }
        if (type == 35) {
            msg = false;
        }
        if (msg) {
            mplew.writeMapleAsciiString(message);
        }
        switch (type) {
            case SPEAKER_WORLD:
            case SPEAKER_WORLD_GUILD_SKILL:
                PacketHelper.addChaterName(mplew, speekerName, message, chrId);
            case CAKE_TYPE:
            case PIE_TYPE:
            case HEART_TYPE:
            case BONE_TYPE:
                mplew.write(channel - 1);
                mplew.writeBool(megaEar);
                break;
            case ITEM_SPEAKER:
                PacketHelper.addChaterName(mplew, speekerName, message, chrId);
                mplew.write(channel - 1);
                mplew.writeBool(megaEar);
                mplew.writeInt(item == null ? 0 : item.getItemId());
                boolean achievement = false;
                mplew.writeInt(item != null ? 1 : achievement ? 2 : 0);
                if (item == null && achievement) {
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                    mplew.writeDouble(0);
                } else if (item != null) {
                    mplew.write(1);
                    PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
                    mplew.writeMapleAsciiString(item.getName());
                }
                break;
            case SPEAKER_BRIDGE:
                PacketHelper.addChaterName(mplew, speekerName, message, chrId);
                mplew.write(channel - 1);
                break;
            case BLOW_WEATHER:
                mplew.writeInt(1);
                break;
            case WEATHER_MSG:
                mplew.writeInt(item != null ? item.getItemId() : 0);
                mplew.writeInt(30);
                PacketHelper.addItemPosition(mplew, item, true, false);
                if (item != null) {
                    PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
                }
                break;
            case 33:
            case 34:
                mplew.writeInt(0);
                mplew.writeMapleAsciiString(list == null || list.isEmpty() ? "" : list.get(0));
                PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
                break;
            case PICKUP_ITEM_WORLD:
            case MAKING_SKILL_MEISTER_ITEM:
                PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
                break;
            case ART_SPEAKER_WORLD:
                PacketHelper.addChaterName(mplew, speekerName, list.get(1));
                mplew.write(list.size());
                if (list.size() > 1) {
                    mplew.writeMapleAsciiString(list.get(1));
                    PacketHelper.addChaterName(mplew, speekerName, list.get(1));
                }
                if (list.size() > 2) {
                    mplew.writeMapleAsciiString(list.get(2));
                    PacketHelper.addChaterName(mplew, speekerName, list.get(2));
                }
                mplew.write(channel - 1);
                mplew.writeBool(megaEar);
                break;
            case LOTTERY_ITEM_SPEAKER:
                PacketHelper.addItemPosition(mplew, item, true, false);
                if (item != null) {
                    PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
                }
                break;
            case 26:
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
            default:
                break;
        }

        switch (type) {
            case SPEAKER_CHANNEL:
                PacketHelper.addChaterName(mplew, speekerName, message, chrId);
                break;
            case GACHAPON_MEGAPHONE: {
                mplew.writeInt((item != null) ? item.getItemId() : 0);
                mplew.writeInt((channel > 0) ? (channel - 1) : -1);
                mplew.writeInt(rareness);//顏色代碼 0 為綠色 1 2 為紅色 3為黃色
                PacketHelper.addItemPosition(mplew, item, true, false);
                if (item != null) {
                    PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
                }
                break;
            }
            case GACHAPON_MSG: {
                mplew.writeInt(item == null ? 0 : item.getItemId());
                PacketHelper.addItemPosition(mplew, item, true, false);
                if (item != null) {
                    PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
                }
                break;
            }
            case NOTICE_WITHOUT_PREFIX:
            case LOTTERY_ITEM_SPEAKER_WORLD: {
                mplew.writeInt((item != null) ? item.getItemId() : 0);
                break;
            }
            case UTIL_DLG_EX: {
                mplew.writeInt(1);
                break;
            }
            case ANNOUNCED_QUEST_OPEN: {
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
            }
            case ANNOUNCED_QUEST_CLOSED: {
                mplew.writeInt(0);
                break;
            }
            case EVENT_MSG_WITH_CHANNEL: {
                mplew.writeInt(channel - 1);
                break;
            }
            case NOTICE_WINDOW: {
                mplew.writeInt(1);
                mplew.writeInt(1);
                break;
            }
            case 33:
            case 34: {
                mplew.writeMapleAsciiString("");
                mplew.writeInt(0);
                break;
            }
            case LOTTERY_ITEM_SPEAKER: {
                mplew.writeBool(item != null);
                if (item != null) {
                    PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
                }
                break;
            }
            case PICKUP_ITEM_WORLD:
            case MAKING_SKILL_MEISTER_ITEM: {
                PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
                break;
            }
        }
        mplew.writeZeroBytes(10);
        return mplew.getPacket();
    }

    /*
     * 抽獎喇叭
     */
    public static byte[] getGachaponMega(String name, String message, Item item, int rareness, int channel) {
        List<String> messages = new LinkedList<>();
        messages.add(name);
        return serverMessage(GACHAPON_MEGAPHONE, channel, message, false, item, messages, rareness);
    }

    /**
     * 繽紛喇叭
     */
    public static byte[] tripleSmega(List<String> message, boolean ear, int channel) {
        String s = message.get(0);
        return serverMessage(ART_SPEAKER_WORLD, channel, s == null ? "" : s, ear, null, message, 0);
    }

    /**
     * 情景喇叭
     */
    public static byte[] getAvatarMega(MapleCharacter chr, int channel, int itemId, List<String> message, boolean ear) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_AvatarMegaphoneUpdateMessage.getValue());
        mplew.writeInt(itemId);
        mplew.writeMapleAsciiString(chr.getMedalText() + chr.getName());
        for (int i = 0; i < 4; i++) {
            mplew.writeMapleAsciiString(message.get(i));
        }
        final StringBuilder sb = new StringBuilder();
        for (String ignored : message) {
            sb.append(sb).append("\n\r");
        }
        PacketHelper.addChaterName(mplew, chr.getName(), sb.toString());
        mplew.writeInt(channel - 1); // channel
        mplew.write(ear ? 1 : 0);
        chr.getAvatarLook().encode(mplew, true);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] gachaponMsg(String msg, Item item) {
        return serverMessage(GACHAPON_MSG, 0, msg, false, item, null, 0);
    }

    /*
     * 道具喇叭
     */
    public static byte[] itemMegaphone(String msg, boolean whisper, int channel, Item item) {
        return serverMessage(ITEM_SPEAKER, channel, msg, whisper, item, null, 0);
    }

    public static byte[] getChatItemText(int speekerId, String text, String speekerName, boolean whiteBG, int show, boolean b, int n, Item item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserChatItem.getValue());
        mplew.writeInt(speekerId);
        mplew.writeBool(false); // whiteBG - true時無法查看道具訊息
        mplew.writeMapleAsciiString(text);
        PacketHelper.addChaterName(mplew, speekerName, text, speekerId);
        mplew.write(show);
        mplew.writeBool(b);
        mplew.write(n);
        boolean achievement = false;
        mplew.writeInt(item != null ? 1 : achievement ? 2 : 0);
        if (item == null && achievement) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeDouble(0);
        } else if (item != null) {
            mplew.write(1);
            PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
            mplew.writeMapleAsciiString(item.getName());
        }
        return mplew.getPacket();
    }

    public static byte[] getChatText(int speekerId, String text, String speekerName, boolean whiteBG, int show, boolean b, int n) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserChat.getValue());
        MapleCharacter talkPlayer = MapleCharacter.getCharacterById(speekerId);
        mplew.writeInt(speekerId);
        mplew.writeBool(whiteBG);//Gm白色背景
        mplew.writeMapleAsciiString(text);
        mplew.writeMapleAsciiString(speekerName);
        mplew.writeMapleAsciiString(text);
        mplew.writeShort(-4700);
        mplew.writeInt(n);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(speekerId);
        for (int x = 0; x < 4; x++) {
            mplew.writeInt(0);
        }
        mplew.write(show);
        mplew.write(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] GameMaster_Func(int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_AdminResult.getValue());
        mplew.write(value);
        mplew.writeZeroBytes(17);
        return mplew.getPacket();
    }

    public static byte[] ShowAranCombo(int combo) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ModCombo.getValue());
        mplew.writeInt(combo);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] comboRecharge(int combo) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_IncComboByComboRecharge.getValue());
        mplew.writeInt(combo);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] rechargeCombo(int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserBuffzoneEffect.getValue());
        mplew.writeInt(value);

        return mplew.getPacket();
    }

    public static byte[] getPacketFromHexString(String hex) {
        return HexTool.getByteArrayFromHexString(hex);
    }

    public static byte[] showGainExp(long gain, boolean white, boolean bOnQuest, int diseaseType, long expLost, Map<MapleExpStat, Object> expStats) {
        MessageOption option = new MessageOption();
        option.setColor(white ? 1 : 0);
        option.setLongGain(gain);
        option.setOnQuest(bOnQuest);
        option.setDiseaseType(diseaseType);
        option.setExpLost(expLost);
        option.setExpGainData(expStats);
        return CWvsContext.sendMessage(MessageOpcode.MS_IncEXPMessage, option);
    }

    /**
     * 發送封包到客戶端 顯示角色獲得人氣的信息
     *
     * @param gain 增加或者減少多少名聲值.
     * @return v257 封包數據.
     * @implNote : 13 00 00 00 06 00 00 00 00 01 00 00 00
     */
    public static byte[] getShowFameGain(int gain) {
        MessageOption option = new MessageOption();
        option.setAmount(gain);
        return CWvsContext.sendMessage(MessageOpcode.MS_IncPOPMessage, option);
    }

    /**
     * 發送封包到客戶端 顯示角色獲得楓幣的信息
     *
     * @param gain   增加或者減少多少楓幣.
     * @param inChat 是否在聊天框中顯示
     * @return 封包數據.
     */
    public static byte[] showMesoGain(long gain, boolean inChat) {
        if (!inChat) {
            gain = Math.min(Integer.MAX_VALUE, gain);
            MessageOption option = new MessageOption();
            option.setMode(1);
            option.setLongGain(gain);
            return CWvsContext.sendMessage(MessageOpcode.MS_DropPickUpMessage, option);
        } else {
            MessageOption option = new MessageOption();
            option.setLongGain(gain);
            option.setMode(-1);
            option.setText("");
            return CWvsContext.sendMessage(MessageOpcode.MS_IncMoneyMessage, option);
        }
    }

    /**
     * 發送封包到客戶端 顯示角色獲得裝備的信息
     *
     * @param itemId   道具的ID.
     * @param quantity 增加或者減少多少.
     * @return 封包數據.
     */
    public static byte[] getShowItemGain(int itemId, int quantity) {
        return getShowItemGain(itemId, quantity, false);
    }

    public static byte[] getShowItemGain(int itemId, int quantity, boolean inChat) {
        if (inChat) {
            return EffectPacket.getShowItemGain(Collections.singletonList(new Pair<>(itemId, quantity)));
        }

        MessageOption option = new MessageOption();
        option.setMode(0);
        option.setObjectId(itemId);
        option.setAmount(quantity);
        return CWvsContext.sendMessage(MessageOpcode.MS_DropPickUpMessage, option);
    }

    /**
     * 非商城道具到期
     *
     * @param itemId
     * @return
     */
    public static byte[] showItemExpired(int itemId) {
        MessageOption option = new MessageOption();
        option.setIntegerData(new int[]{itemId});
        return CWvsContext.sendMessage(MessageOpcode.MS_GeneralItemExpireMessage, option);
    }

    /**
     * 技能到期提示
     *
     * @param update
     * @return
     */
    public static byte[] showSkillExpired(Map<Integer, SkillEntry> update) {
        MessageOption option = new MessageOption();
        option.setIntegerData(update.keySet().stream().mapToInt(Integer::intValue).toArray());
        return CWvsContext.sendMessage(MessageOpcode.MS_SkillExpireMessage, option);
    }

    /**
     * 商城道具到期
     *
     * @param itemId
     * @return
     */
    public static byte[] showCashItemExpired(int itemId) {
        MessageOption option = new MessageOption();
        option.setObjectId(itemId);
        return CWvsContext.sendMessage(MessageOpcode.MS_CashItemExpireMessage, option);
    }

    public static byte[] spawnPlayerMapobject(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserEnterField.getValue()); // 封包頭
        mplew.writeInt(chr.getAccountID()); // 帳戶ID
        mplew.writeInt(chr.getId()); // 角色ID
        mplew.writeInt(0);
        mplew.writeInt(chr.getLevel()); // 角色等級
        mplew.writeMapleAsciiString(chr.getName()); // 角色名稱
        // 添加公會相關信息
        MapleGuild gs;
        if (chr.getGuildId() > 0) {
            gs = WorldGuildService.getInstance().getGuild(chr.getGuildId());
        } else {
            gs = null;
        }
        mplew.writeShort(0); // 2
        mplew.writeInt(gs == null ? 0 : gs.getId()); // guild id - 公會ID 6
        mplew.writeMapleAsciiString(gs == null ? "" : gs.getName()); // guild name - 公會NAME 7
        mplew.writeShort(gs == null ? 0 : gs.getLogoBG()); // 公會標誌背景 9
        mplew.write(gs == null ? 0 : gs.getLogoBGColor()); // 公會標誌背景顏色 10
        mplew.writeShort(gs == null ? 0 : gs.getLogo()); // 公會標誌 12
        mplew.writeInt(gs == null || gs.getImageLogo() == null ? 0 : gs.getId()); // 公會圖像ID 16
        mplew.write(0); // 17
        mplew.write(0); // 18
        mplew.write(0); // 17
        mplew.write(0); // 18
        mplew.write(0); // 17
        mplew.write(chr.getGender()); // 性別
        mplew.writeInt(chr.getFame()); // 名聲
        mplew.writeHexString("00 00 00 00 00 00 00 00 00 00 00 00 00 8C 16 05 00 00 00 F4 00 00 00 00 00 00 00 00 00 08 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 84 06 00 00 10 00 00 00 10 00 00 10 00 00 00 00 00 00 02 00 01 00 00 00 00 00 00 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 F8 1F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 84 8E 00 00 00 00 00 00 00 00 00 00 00 00 01 84 8E 00 00 00 00 00 00 00 00 00 00 00 00 01 84 8E 00 00 00 00 00 00 00 00 00 00 01 84 8E 00 00 01 77 BB FB 0D 00 00 00 00 00 00 00 00 00 00 01 84 8E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 84 8E 00 00 00 00 00 00 00 00 00 00 01 84 8E 00 00 00 00 00 00 00 00 00 00 00 00 01 84 8E 00 00 00 00 00 00 00 00 00 00 00 00 01 84 8E 00 00 00 00 00 00 00 00 00 00 01 84 8E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
        mplew.writeShort(chr.getJob()); // jobid
        mplew.writeShort(chr.getJobWithSub()); // sub
        mplew.write(0); // 18
        mplew.writeInt(chr.getStat().getStarForce()); // star
        mplew.writeInt(chr.getStat().getArc()); // ARC
        mplew.writeInt(chr.getStat().getAut()); // AUT
////
        chr.getAvatarLook().encode(mplew, true); // 角色外觀信息
        if (JobConstants.is神之子(chr.getJob())) {
            chr.getSecondAvatarLook().encode(mplew, true); // 如果是神之子，添加第二個角色外觀信息
        }
////
        //  sub_1403E30E0 v257 {
        mplew.writeInt(0); // 1
        mplew.write(-1);
        mplew.writeInt(0); // 2
        mplew.write(-1);
        // }
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
//
        //開始加載玩家特殊效果信息 sub_144B91CE0 {
        int buffSrc = chr.getBuffSource(SecondaryStat.RideVehicle);
        if (chr.getBuffedValue(SecondaryStat.NewFlying) != null && buffSrc > 0) {
            addMountId(mplew, chr, buffSrc);
            mplew.writeInt(chr.getId());
        } else {
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        mplew.writeInt(0); //未知
        // }
//
        mplew.writeInt(Math.min(250, chr.getInventory(MapleInventoryType.CASH).countById(5110000))); //紅心巧克力 max is like 100. but w/e
        mplew.writeInt(chr.getItemEffect()); //眼睛之類的特殊效果 5010073 - 人氣美女 的特效
        mplew.writeInt(chr.getItemEffectType()); //幻影殘像之類的特殊效果
        mplew.writeInt(chr.getActiveNickItemID()); //頭頂上面的稱號 3700135
        String sUnk = null;
        mplew.writeBool(sUnk != null);
        if (sUnk != null) {
            mplew.writeMapleAsciiString(sUnk);
        }
        //mplew.writeInt(chr.getDamageSkin()); //傷害皮膚效果
        //mplew.writeInt(chr.getDamageSkin());
        mplew.writeInt(0); // TMS.230
        mplew.writeMapleAsciiString("");//V.163 new
        mplew.writeMapleAsciiString("");//V.163 new
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(1);
        PortableChair chair = chr.getChair();
        mplew.write(chair == null ? 0 : chair.getUnk());
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeShort(-1);
        mplew.writeInt(chair == null ? 0 : chair.getItemId());
//        mplew.writeInt(chair == null ? 0 : chair.getMeso());
//        mplew.writeInt(chair == null ? 0 : chair.getType());
        mplew.writeInt(0);
        mplew.writePos(chr.getPosition());
        mplew.write(chr.getStance());
        mplew.writeShort(chr.getCurrentFH());
        mplew.write(1);
        mplew.write(1);
        writeChairData(mplew, chr);
        //開始加載玩家的寵物信息
        for (int i = 0; i <= 4; i++) { // 寵物
            MaplePet pet = chr.getSpawnPet(i);
            boolean isPetSpawned = pet != null && i != 4;
            mplew.write(isPetSpawned);
            if (!isPetSpawned) {
                break;
            }
            mplew.writeInt(chr.getPetIndex(pet));
            PetPacket.addPetInfo(mplew, pet);
//
        }
        //PacketHelper.addSkillPets(mplew, chr);// todo
        mplew.writeBool(false); //暫時註釋,包數據錯誤導致第二玩家會看到第二隻小白·官方沒有只發送該包·不單獨發送召喚包 PacketHelper.addLittleWhite(mplew, chr);
        //開始玩家坐騎信息
        mplew.writeInt(chr.getMount() != null ? chr.getMount().getLevel() : 1); // 坐騎等級
        mplew.writeInt(chr.getMount() != null ? chr.getMount().getExp() : 0); // 坐騎經驗
        mplew.writeInt(chr.getMount() != null ? chr.getMount().getFatigue() : 0); // 坐騎疲勞
//
        mplew.writeBool(false); // 未知 V.144新增
//
        //開始PlayerShop和MiniGame
        PacketHelper.addAnnounceBox(mplew, chr); //沒有占1個 00
        //開始玩家小黑板信息
        mplew.writeBool(chr.getChalkboard() != null && chr.getChalkboard().length() > 0);
        if (chr.getChalkboard() != null && chr.getChalkboard().length() > 0) {
            mplew.writeMapleAsciiString(chr.getChalkboard());
        }
        //開始玩家戒指信息
        Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> rings = chr.getRings(true);
        addRingInfo(mplew, rings.getLeft());
        addRingInfo(mplew, rings.getMid());
        addMRingInfo(mplew, rings.getRight(), chr);
        boolean loadObjSwords = true;
        mplew.writeBool(loadObjSwords);
        if (loadObjSwords) {
            Map<Integer, ForceAtomObject> map = chr.getForceAtomObjects();
            mplew.writeInt(map.size());
            for (ForceAtomObject sword : map.values()) {
                AdelePacket.encodeForceAtomObject(mplew, sword);
            }
        }
        int berserk = 0;
        mplew.write(berserk);
        if ((berserk & 0x8) != 0) {
            mplew.writeInt(0);
        }
        if ((berserk & 0x10) != 0) {
            mplew.writeInt(0);
        }
        if ((berserk & 0x20) != 0) {
            mplew.writeInt(0);
        }
//
        mplew.writeInt(chr.getMount().getItemId());
        if (JobConstants.is凱撒(chr.getJob())) {
            String string2 = chr.getOneInfo(12860, "extern");
            mplew.writeInt(string2 == null ? 0 : Integer.parseInt(string2));
            string2 = chr.getOneInfo(12860, "inner");
            mplew.writeInt(string2 == null ? 0 : Integer.parseInt(string2));
            string2 = chr.getOneInfo(12860, "premium");
            mplew.write(string2 == null ? 0 : Integer.parseInt(string2));
        }
        mplew.writeInt(chr.getMeisterSkillEff());
        // 去人物稱號 X8 - X9
        mplew.writeHexString("FF FF FF FF FF");
        mplew.writeInt(0);
        mplew.write(1);
        mplew.writeInt(464907);
        mplew.write(0);
        mplew.write(1);
        mplew.writeBool(false);
        // sub_2BA92C0 TMS237
        mplew.writeInt(0);
        // sub_2BAF220 TMS237
        mplew.write(0);
        mplew.writeInt(0);
        // sub_2BAF260 TMS237
        mplew.writeInt(0);

        mplew.writeBool(JobConstants.is凱內西斯(chr.getJob()) && chr.getBuffedIntValue(SecondaryStat.KinesisPsychicEnergeShield) > 0); // 心魂本能特效
        mplew.write(0);//V.156 new
        mplew.write(0);//V.156 new
        mplew.writeInt(1051291); // 9B 0A 10 00 換裝套裝
        mplew.writeBool(false);//V.156 new
        mplew.writeInt(0);
        mplew.writeInt(0);

        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(chr.getSkillSkin().size());

        chr.send(InventoryPacket.showDamageSkin(chr.getId(), chr.getDamageSkin()));

        mplew.writeInt(0);
        mplew.writeLong(0);
        mplew.writeInt(-1);
        return mplew.getPacket();
    }

    public static void addMountId(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, int buffSrc) {
        Item c_mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -123);
        Item mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18);
        int mountId = GameConstants.getMountItem(buffSrc, chr);
        if (mountId == 0 && c_mount != null && chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -124) != null) {
            mplew.writeInt(c_mount.getItemId());
        } else if (mountId == 0 && mount != null && chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -19) != null) {
            mplew.writeInt(mount.getItemId());
        } else {
            mplew.writeInt(mountId);
        }
    }

    public static byte[] removePlayerFromMap(int chrId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserLeaveField.getValue());
        mplew.writeInt(chrId);
        return mplew.getPacket();
    }

    public static byte[] facialExpression(MapleCharacter from, int expression) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserEmotion.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(expression);
        mplew.writeInt(-1); //itemid of expression use
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] movePlayer(int chrId, List<LifeMovementFragment> moves, final int gatherDuration, final int nVal1, final Point mPos, final Point oPos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserMove.getValue());
        mplew.writeInt(chrId);
        PacketHelper.serializeMovementList(mplew, gatherDuration, nVal1, mPos, oPos, moves, null);

        return mplew.getPacket();
    }

    public static byte[] UserMeleeAttack(MapleCharacter chr, int skilllevel, int itemId, AttackInfo attackInfo, boolean hasMoonBuff) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserMeleeAttack.getValue());
        addAttackBody(mplew, chr, skilllevel, itemId, attackInfo, hasMoonBuff, false);
        mplew.writeZeroBytes(20);
        return mplew.getPacket();
    }

    public static byte[] UserBodyAttack(MapleCharacter chr, int skilllevel, int itemId, AttackInfo attackInfo, boolean hasMoonBuff) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserBodyAttack.getValue());
        addAttackBody(mplew, chr, skilllevel, itemId, attackInfo, hasMoonBuff, false);
        mplew.writeZeroBytes(20);
        return mplew.getPacket();
    }

    public static byte[] UserShootAttack(MapleCharacter chr, int skilllevel, int itemId, AttackInfo attackInfo) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserShootAttack.getValue());
        addAttackBody(mplew, chr, skilllevel, itemId, attackInfo, false, true);
        if (JobConstants.is神之子(chr.getJob()) && attackInfo.skillId >= 100000000) {
            mplew.writeInt(attackInfo.position.x);
            mplew.writeInt(attackInfo.position.y);
        } else if (attackInfo.skillposition != null) {
            if (attackInfo.skillId == 破風使者.季風) {    // 季候風為全屏技能，不需要坐標信息
                mplew.writeLong(0);
            } else {
                mplew.writePos(attackInfo.skillposition); // 有些技能要發送技能的坐標信息
            }
        }
        mplew.writeZeroBytes(20);

        return mplew.getPacket();
    }

    public static byte[] UserMagicAttack(MapleCharacter chr, int skilllevel, int itemId, AttackInfo attackInfo) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserMagicAttack.getValue());
        addAttackBody(mplew, chr, skilllevel, itemId, attackInfo, false, false);
        mplew.writeZeroBytes(18);
        return mplew.getPacket();
    }

    public static void addAttackBody(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, int skilllevel, int itemId, AttackInfo ai, boolean hasMoonBuff, boolean isShootAttack) {
        int skillId = ai.skillId;
        mplew.writeInt(chr.getId()); //角色的ID
        mplew.writeBool(isShootAttack); //V.116.1新增 未知 好像箭矢炮盤 這個地方是1
        mplew.write(ai.numAttackedAndDamage);
        mplew.writeInt(chr.getLevel()); //角色的等級 byte->int V.156
        mplew.writeInt(skilllevel > 0 && skillId > 0 ? skilllevel : 0);//V.161 byte=>int
        if (skilllevel > 0 && skillId > 0) {
            mplew.writeInt(skillId);
        }
        if (JobConstants.is神之子(chr.getJob()) && skillId >= 100000000) {
            mplew.write(0);
        }
        if (isShootAttack && (skillId == 夜使者.四飛閃 || skillId == 重砲指揮官.加農砲連擊 || eA(skillId) > 0)) {
            mplew.writeInt(0);//V.161 byte=>int
        }
        if (skillId == 80001850) {
            mplew.writeInt(0);
        } else if (SkillConstants.getLinkedAttackSkill(skillId) == 陰陽師.紫扇仰波) {
            mplew.write(0);
        }
        mplew.write(isShootAttack ? 8 : ny(skillId));
        int mask = 0;
        int l = 0, r = 0;
        if (hasMoonBuff) {
            mask = 2;
            l = 聖魂劍士.沉月;
            r = 20;
        }
        mplew.write(mask);
        mplew.writeInt(0);//nOption3
        mplew.writeInt(0);//nBySummonedID
        mplew.writeInt(0);
        mplew.write(false);
        if ((mask & 2) != 0) {
//            mplew.writeInt(0);//buckShotInfo.nSkillID
//            mplew.writeInt(0);//buckShotInfo.nSLV
            mplew.writeInt(l);
            mplew.writeInt(r);
        }
        if ((mask & 8) != 0) {
            mplew.write(0);
        }
        mplew.write(ai.display); //攻擊的動作效果 bLeft = ((unsigned int)v29 >> 15) & 1;
        mplew.write(ai.direction); //攻擊的方向 nAction = v29 & 0x7F
        mplew.write(-1); //攻擊的速度
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.write(0);//bShowFixedDamage
        mplew.write(0);
        mplew.write(ai.attackSpeed);//nActionSpeed
        mplew.write(0); // v262 +
        mplew.writeInt(itemId);//nBulletItemID
//        }
        for (AttackMobInfo oned : ai.mobAttackInfo) {
            if (oned.damages != null) {
                mplew.writeInt(oned.mobId);
                mplew.write(oned.hitAction);
                mplew.write(oned.left);
                mplew.write(oned.idk3);
                mplew.write(oned.forceActionAndLeft);
                mplew.write(oned.frameIdx);
                mplew.writeInt(0); //這個地方好像是顏色相關 但神之子切換狀態的時候 這個地方為1 打出的攻擊顯示顏色和普通的不同
                mplew.writeInt(0);//V.149 new

                if (skillId == 凱內西斯.擷取心靈 || skillId == 80011050) {
                    mplew.write(oned.damages.length);
                }
                for (long damage : oned.damages) {
                    mplew.writeLong(damage);
                }
                if (sub_870CC0(skillId)) { // 220
                    mplew.writeInt(0);
                }
                if (skillId == 爆拳槍神.雙重壓迫 || skillId == 墨玄.墨玄_一轉_神功_昇天拳) {
                    mplew.write(0);
                }
                if (skillId == 虎影.魔封葫蘆符_1) {
                    mplew.writeInt(0);
                }
            }
        }

        //魂騎士.日月斬
        if (skillId == 冰雷.雷霆萬鈞 || skillId == 聖魂劍士.宇宙融合 || skillId == 烈焰巫師.鳳凰爆裂 || skillId == 80003075) {
            mplew.writeInt(ai.charge);
        } else if (skillId == 暗影神偷.暗影霧殺 || skillId == 天使破壞者.超級超新星
                || skillId == 80001431 || skillId == 80003084 || skillId == 80011562 || skillId == 神之子.暗影之雨 || skillId == 狂狼勇士.瑪哈的領域 || skillId == 破風使者.季風 || skillId == 暗夜行者.道米尼奧 || skillId == 閃雷悍將.海神降臨
                || skillId == 神之子.進階威力震擊_衝擊波
                || skillId == 神之子.暗影降臨_劍氣
                || skillId == 80011561 || skillId == 80002463 || skillId == 80001762 || skillId == 80002212
                || skillId == 暗夜行者.影之槍_2
                || skillId == 神射手.分裂之矢_1
                || skillId == 神射手.光速神弩_1
                || skillId == 伊利恩.榮耀之翼_強化暗器 || skillId == 伊利恩.榮耀之翼_強化暗器_1 || skillId == 亞克.深淵技能
                || skillId == 凱內西斯.終極_心靈彈丸_1
                || skillId == 通用V核心.異界通用.異界的虛空 || skillId == 神之子.超越者優伊娜的心願_追加打擊
                || skillId == 80002452
                || skillId == 聖騎士.雷神戰槌 || skillId == 聖騎士.雷神戰槌_1
                || skillId == 凱內西斯.引力法則_1
                || skillId == 夜使者.飛閃起爆符_1 || skillId == 夜使者.飛閃起爆符_2
                || skillId == 亞克.無限飢餓的猛獸
                || skillId == 英雄.劍之幻象_1 || skillId == 英雄.劍之幻象_2
                || skillId == 亞克.迷惑之拘束_2
                || skillId == 凱殷.化身_1
                || skillId == 閃雷悍將.槍雷連擊_7 || skillId == 閃雷悍將.槍雷連擊_8
                || skillId == 凱殷.崩壞爆破_2 || skillId == 凱殷.具現_強化崩壞爆破_3 || skillId == 凱殷.具現_強化崩壞爆破_4
                || skillId == 菈菈.釋放_日光井_1 || skillId == 菈菈.釋放_日光井_4
                || skillId == 聖騎士.神聖烙印_1 || skillId == 重砲指揮官.迷你砲彈_1 || skillId == 重砲指揮官.迷你砲彈_2) {
            mplew.writePosInt(chr.getPosition());
        } else if (skillId == 夜光.解放寶珠_2) {
            mplew.writeInt(0);
            mplew.writeRect(new Rectangle());
        } else if (skillId == 破風使者.寒冰亂舞 || skillId == 幻獸師.旋風飛行) {
            mplew.writeShort(0);
            mplew.writeShort(0);
        } else if (skillId == 米哈逸.閃光交叉) {
            mplew.write(0);
        } else if (skillId == 幻獸師.隊伍攻擊) {
            mplew.writeInt(0);
        } else if (skillId == 陰陽師.御身消滅) {
            mplew.writeShort(0);
            mplew.write(0);
        } else if (skillId == 狂狼勇士.極速巔峰_目標鎖定
                || skillId == 爆拳槍神.神聖連發重擊
                || skillId == 暗影神偷.滅殺刃影 || skillId == 暗影神偷.滅殺刃影_1 || skillId == 暗影神偷.滅殺刃影_2 || skillId == 暗影神偷.滅殺刃影_3
                || skillId == 聖魂劍士.雙重狂斬
                || skillId == 拳霸.閃_連殺) {
            mplew.write(1);
            mplew.writePosInt(ai.skillposition == null ? ai.position : ai.skillposition);
        }
        if (skillId == 煉獄巫師.深淵閃電_1) {
            mplew.writeRect(new Rectangle());
        }
        if (sub_8748E0(skillId)) {
            mplew.writeShort(0);
            mplew.writeShort(0);
        }
        if (sub_874950(skillId)) {
            mplew.writeInt(0);
            mplew.write(0);
        }
        if (sub_874720(skillId)) {
            mplew.writeInt(0);
            mplew.write(0);
        }
        if (skillId == 亞克.無法停止的衝動 || skillId == 亞克.無法停止的本能 || skillId == 拳霸.海龍衝鋒 || skillId == 劍豪.一閃_稜) {
            mplew.write(0);
        }
        if (skillId == 陰陽師.御身消滅) {
            mplew.writeShort(0);
            mplew.write(0);
        }
        if (skillId == 開拓者.分裂魔矢) {
            mplew.writeInt(0);
            mplew.write(0);
        }
    }

    private static boolean sub_870CC0(final int a1) {
        if (a1 > 凱內西斯.擷取心靈) {
            return a1 >= 凱內西斯.擷取心靈2 && (a1 <= 凱內西斯.終極技_心靈射擊 || a1 == 凱內西斯.猛烈心靈2_最後一擊);
        } else
            return a1 == 凱內西斯.擷取心靈 || a1 == 凱內西斯.心靈領域_攻擊 || a1 == 凱內西斯.猛烈心靈 || a1 == 凱內西斯.猛烈心靈2;
    }

    private static boolean sub_874950(int a1) {
        boolean v1; // zf

        if (a1 > 開拓者.基本爆破4轉_1) {
            if (a1 == 開拓者.古代神矢_爆破_1) {
                return true;
            }
            v1 = a1 == 開拓者.究極炸裂_1;
        } else {
            if (a1 == 開拓者.基本爆破4轉_1 || a1 == 開拓者.基本爆破_1 || a1 == 開拓者.三重衝擊_1) {
                return true;
            }
            v1 = a1 == 開拓者.基本爆破強化_1;
        }
        return v1;
    }

    private static boolean sub_874720(int a1) {
        boolean v1; // zf

        if (a1 > 伊利恩.神怒寶劍_2) {
            if (a1 > 卡蒂娜.召喚_AD大砲_1) {
                if (a1 == 拳霸.海之霸主_1 || a1 == 重砲指揮官.超級巨型加農砲彈) {
                    return true;
                }
                v1 = a1 - 重砲指揮官.超級巨型加農砲彈 == 8;
            } else {
                if (a1 == 卡蒂娜.召喚_AD大砲_1) {
                    return true;
                }
                if (a1 > 夜使者.散式投擲_四飛閃) {
                    v1 = a1 == 夜使者.風魔手裏劍;
                } else {
                    if (a1 >= 夜使者.散式投擲_雙飛斬 || a1 == 陰陽師.怨靈解放陣) {
                        return true;
                    }
                    v1 = a1 == 陰陽師.怨靈解放陣_2;
                }
            }
            return v1;
        }
        if (a1 < 伊利恩.神怒寶劍_1) {
            if (a1 > 黑騎士.斷罪之槍) {
                switch (a1) {
                    case 烈焰巫師.炙熱元素火焰:
                    case 凱內西斯.心靈龍捲風_4:
                    case 凱內西斯.心靈龍捲風_5:
                    case 凱內西斯.心靈龍捲風_6:
                    case 火毒.劇毒新星:
                    case 煉獄巫師.黑魔祭壇:
                    case 凱內西斯.終極_移動物質:
                        return true;
                    default:
                        return false;
                }
            }
            if (a1 != 黑騎士.斷罪之槍) {
                if (a1 > 伊利恩.技藝_子彈Ⅱ) {
                    v1 = a1 == 伊利恩.技藝_朗基努斯;
                } else {
                    if (a1 == 伊利恩.技藝_子彈Ⅱ || a1 == 80002691) {
                        return true;
                    }
                    v1 = a1 == 伊利恩.技藝_子彈;
                }
                return v1;
            }
        }
        return true;
    }

    private static boolean sub_8748E0(int a1) {
        boolean v1; // zf

        if (a1 <= 卡蒂娜.召喚_炸彈投擲_1) {
            if (a1 == 卡蒂娜.召喚_炸彈投擲_1) {
                return true;
            }
            if (a1 > 開拓者.基本爆破強化_1) {
                if (a1 == 開拓者.基本爆破4轉_1) {
                    return true;
                }
                v1 = a1 == 開拓者.古代神矢_爆破_1;
            } else {
                if (a1 == 開拓者.基本爆破強化_1 || a1 == 開拓者.基本爆破_1) {
                    return true;
                }
                v1 = a1 == 開拓者.三重衝擊_1;
            }
            return v1;
        }
        if (a1 > 凱內西斯.終極_移動物質_1) {
            v1 = a1 == 開拓者.究極炸裂_1;
            return v1;
        }
        if (a1 != 凱內西斯.終極_移動物質_1) {
            if (a1 < 凱內西斯.心靈龍捲風_1) {
                return false;
            }
            if (a1 > 凱內西斯.心靈龍捲風_3) {
                v1 = a1 == 火毒.劇毒新星_1;
                return v1;
            }
        }
        return true;
    }

    public static int eA(final int n) {
        switch (n) {
            case 英雄.狂暴攻擊:
                return 英雄.狂暴攻擊_爆擊;
            case 英雄.狂暴攻擊_爆擊:
                return 英雄.狂暴攻擊_攻擊加成;
            case 聖騎士.鬼神之擊:
                return 聖騎士.鬼神之擊_攻擊加成;
            case 聖騎士.騎士衝擊波:
                return 聖騎士.騎士衝擊波_攻擊加成;
            case 聖騎士.神聖衝擊:
            case 聖騎士.神聖衝擊_1:
                return 聖騎士.神聖衝擊_額外攻擊;
            case 火毒.火焰之襲:
                return 火毒.火焰之襲_額外攻擊;
            case 冰雷.閃電連擊:
                return 冰雷.閃電連擊_攻擊加成;
            case 箭神.暴風神射:
                return 箭神.暴風神射_多重射擊;
            case 箭神.驟雨狂矢:
                return 箭神.驟雨狂矢_攻擊加成;
            case 神射手.光速神弩:
            case 神射手.光速神弩_1:
            case 神射手.光速神弩II:
            case 神射手.光速神弩II_1:
            case 神射手.覺醒神弩:
            case 神射手.覺醒神弩II:
            case 神射手.覺醒神弩II_1:
                return 神射手.光速神弩_攻擊加成;
            case 神射手.必殺狙擊:
                return 神射手.必殺狙擊_額外攻擊;
            case 開拓者.基本釋放:
            case 開拓者.基本釋放強化:
            case 開拓者.基本爆破:
            case 開拓者.基本爆破強化:
            case 開拓者.基本爆破4轉:
            case 開拓者.基本爆破_1:
            case 開拓者.基本爆破強化_1:
            case 開拓者.基本爆破4轉_1:
            case 開拓者.基本轉移:
            case 開拓者.基本轉移4轉:
            case 開拓者.基本轉移_1:
            case 開拓者.基本轉移4轉_1:
                return 開拓者.基本之力_額外攻擊;
            case 暗影神偷.冷血連擊:
                return 暗影神偷.冷血連擊_額外攻擊;
            case 影武者.幻影箭:
                return 影武者.幻影箭_攻擊加成;
            case 影武者.短劍升天:
                return 影武者.短劍升天_額外攻擊;
            case 拳霸.閃_連殺:
                return 拳霸.閃_連殺_攻擊加成;
            case 拳霸.勾拳爆破:
                return 拳霸.勾拳爆破_額外攻擊;
            case 槍神.爆頭射擊:
                return 槍神.爆頭射擊_攻擊加成;
            case 重砲指揮官.加農砲火箭:
                return 重砲指揮官.加農砲火箭_攻擊加成;
            case 重砲指揮官.雙胞胎猴子:
            case 重砲指揮官.雙胞胎猴子_1:
                return 重砲指揮官.雙胞胎猴子_傷害分裂;
            case 聖魂劍士.雙重狂斬:
                return 聖魂劍士.分裂與狂斬_額外目標;
            case 烈焰巫師.極致熾烈_1:
                return 烈焰巫師.滅絕炙陽_範圍增加;
            case 烈焰巫師.Return_元素之炎I_1:
            case 烈焰巫師.Return_元素之炎II_1:
            case 烈焰巫師.Return_元素之炎III_1:
            case 烈焰巫師.Return_元素之炎IV:
                return 烈焰巫師.元素火焰_速發反擊;
            case 暗夜行者.四倍緩慢:
                return 暗夜行者.五連投擲_爆擊機率;
            case 閃雷悍將.颱風:
            case 閃雷悍將.疾風:
                return 閃雷悍將.疾風_次數強化;
            case 閃雷悍將.霹靂:
                return 閃雷悍將.霹靂_次數強化;
            case 狂狼勇士.比耀德:
            case 狂狼勇士.比耀德_2擊:
            case 狂狼勇士.比耀德_3擊:
            case 狂狼勇士.比耀德_1:
            case 狂狼勇士.芬里爾墬擊:
                return 狂狼勇士.比耀德_攻擊加成;
            case 狂狼勇士.終極之矛_1:
            case 狂狼勇士.終極之矛:
                return 21120047; // 狂狼勇士.終極之矛_加碼攻擊
            case 21120006: // 狂狼勇士.極冰暴風
                return 21120049; // 狂狼勇士.極冰暴風_加碼攻擊
            case 龍魔導士.閃雷之捷_攻擊:
                return 龍魔導士.龍之捷_雷霆攻擊加成;
            case 隱月.鬼斬:
            case 隱月.真_鬼斬:
                return 隱月.鬼斬_次數強化;
            case 惡魔殺手.惡魔衝擊:
                return 惡魔殺手.惡魔衝擊_攻擊加成;
            case 機甲戰神.巨型火炮_IRON_B:
                return 機甲戰神.巨型火炮_IRON_B_攻擊加成;
            case 爆拳槍神.錘之碎擊_2:
            case 爆拳槍神.衝擊波動_1:
                return 爆拳槍神.重擊_衝擊波動攻擊加成;
            case 劍豪.神速無雙:
                return 劍豪.神速無雙_次數強化;
            case 劍豪.一閃:
                return 劍豪.一閃_次數強化;
            case 劍豪.瞬殺斬:
            case 劍豪.瞬殺斬_1:
                return 劍豪.瞬殺斬_次數強化;
            case 陰陽師.破邪連擊符:
                return 陰陽師.破邪連擊符_擴充符;
            case 51121008: // 米哈逸.聖光爆發
                return 51120048; // 米哈逸.聖光爆發_攻擊加成
            case 米哈逸.靈魂突擊:
                return 米哈逸.靈魂突擊_獎勵加成;
            case 米哈逸.閃光交叉:
            case 米哈逸.閃光交叉_安裝:
                return 米哈逸.閃光交叉_攻擊加成;
            case 凱撒.藍焰恐懼:
            case 凱撒.藍焰恐懼_變身:
                return 凱撒.藍焰恐懼_加碼攻擊;
            case 天使破壞者.三位一體:
            case 天使破壞者.三位一體_2擊:
            case 天使破壞者.三位一體_3擊:
                return 天使破壞者.三位一體_三重反擊;
            case 伊利恩.技藝_暗器:
            case 伊利恩.榮耀之翼_強化暗器:
            case 伊利恩.技藝_暗器Ⅱ:
                return 伊利恩.暗器_額外攻擊;
            case 伊利恩.技藝_朗基努斯:
                return 伊利恩.朗基努斯_額外攻擊;
            case 伊利恩.水晶技能_德烏斯:
            case 伊利恩.水晶技能_德烏斯_1:
                return 伊利恩.德烏斯_額外攻擊;
            default:
                return 0;
        }
    }

    public static int ny(int n2) {
        switch (n2) {
            case 火毒.火靈結界:
            case 惡魔殺手.變形:
            case 陰陽師.破魔陣:
            case 天使破壞者.超級超新星: {
                return 4;
            }
        }
        return 0;
    }

    /*
     * 特殊攻擊效果顯示
     */
    public static byte[] showSpecialAttack(int chrId, int tickCount, int pot_x, int pot_y, int display, int skillId, int skilllevel, boolean isLeft, int speed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ThrowGrenade.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(tickCount);
        mplew.writeInt(pot_x);
        mplew.writeInt(pot_y);
        mplew.writeInt(display);
        mplew.writeInt(skillId);
        mplew.writeInt(0);//119
        mplew.writeInt(skilllevel);
        mplew.write(isLeft ? 1 : 0);
        mplew.writeInt(speed);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    /*
     * 更新玩家外觀
     */
    public static byte[] updateCharLook(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserAvatarModified.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        chr.getAvatarLook().encode(mplew, false);
        mplew.writeInt(0);
        mplew.write(0xFF);
        mplew.writeInt(0);
        mplew.write(0xFF);
        Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
        addRingInfo(mplew, rings.getLeft());
        addRingInfo(mplew, rings.getMid());
        addMRingInfo(mplew, rings.getRight(), chr);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    /*
     * 更新神之子切換後的外觀
     */
    public static byte[] updateZeroLook(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ZeroTag.getValue());
        mplew.writeInt(chr.getId());
        chr.getAvatarLook().encode(mplew, false);
        mplew.writeHexString("00 00 00 00 FF 00 00 00 00 FF");

        return mplew.getPacket();
    }

    public static byte[] removeZeroFromMap(int chrId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ZeroLastAssistState.getValue());
        mplew.writeInt(chrId);

        return mplew.getPacket();
    }

    // spawnPlayer
    public static void addRingInfo(MaplePacketLittleEndianWriter mplew, List<MapleRing> rings) {
        mplew.writeBool(!rings.isEmpty());
        if (rings.size() > 0) {
            mplew.writeInt(rings.size());
            for (MapleRing ring : rings) {
                mplew.writeLong(ring.getRingId()); //自己的戒指ID
                mplew.writeLong(ring.getPartnerRingId()); //對方的戒指ID
                mplew.writeInt(ring.getItemId()); //戒指的道具ID
            }
        }
    }

    /*
     * 結婚戒指
     */
    public static void addMRingInfo(MaplePacketLittleEndianWriter mplew, List<MapleRing> rings, MapleCharacter chr) {
        mplew.write(rings.size() > 0);
        if (rings.size() > 0) {
            MapleRing ring = rings.get(0);
            mplew.writeInt(chr.getId());
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeInt(ring.getItemId());
        }
    }

    public static byte[] damagePlayer(int chrId, int type, int monsteridfrom, int damage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserHit.getValue());
        mplew.writeInt(chrId);
        mplew.write(type);
        mplew.writeInt(damage);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(monsteridfrom);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(damage);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] damagePlayer(TakeDamageHandler.UserHitInfo info) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserHit.getValue());
        mplew.writeInt(info.getCharacterID());
        mplew.write(info.getType());
        mplew.writeInt(info.getDamage());
        mplew.writeBool(info.isCritical());
        mplew.writeBool(info.isUnkb());
        if (!info.isUnkb()) {
            mplew.write(0);
        }
        if (info.getType() < -1) {
            if (info.getType() == -8) {
                mplew.writeInt(1);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeBool(false);//V.181 new
            }
        } else {
            mplew.writeInt(info.getTemplateID());
            mplew.write(info.getDirection());
            mplew.writeInt(info.getObjectID());
            mplew.writeInt(info.getSkillID());
            mplew.writeInt(info.getRefDamage());
            mplew.write(info.getDefType());
            if (info.getRefDamage() > 0) {
                mplew.writeBool(info.isRefPhysical());
                mplew.writeInt(info.getRefOid());
                mplew.write(info.getRefType());
                mplew.writePos(info.getPos());
            }
            mplew.write(info.getOffset());
            if ((info.getOffset() & 1) != 0) {
                mplew.writeInt(info.getOffset_d());
            }
        }
        mplew.writeInt(info.getDamage());
        if (info.getDamage() <= 0) {
            mplew.writeInt(info.getOffset_d());
        }
        return mplew.getPacket();
    }

    public static byte[] DamagePlayer2(int dam) {
        MaplePacketLittleEndianWriter pw = new MaplePacketLittleEndianWriter();
        pw.writeShort(OutHeader.DAMAGE_PLAYER2.getValue());
        pw.writeInt(dam);
        return pw.getPacket();
    }

    /**
     * 更新任務
     *
     * @param quest
     * @return
     */
    public static byte[] updateQuest(MapleQuestStatus quest) {
        MessageOption option = new MessageOption();
        option.setQuestStatus(quest);
        return CWvsContext.sendMessage(MessageOpcode.MS_QuestRecordMessage, option);
    }

    /*
     * 更新任務信息
     */
    public static byte[] updateInfoQuest(int quest, String data) {
        MessageOption option = new MessageOption();
        option.setObjectId(quest);
        option.setText(data == null ? "" : data);
        return CWvsContext.sendMessage(MessageOpcode.MS_QuestRecordExMessage, option);
    }

    /**
     * 更新任務信息
     *
     * @param quest     任務ID
     * @param npc       任務NPC
     * @param nextquest 下一項任務
     * @param updata    是否更新數據
     * @return 返回任務更新數據結果數據包
     */
    public static byte[] updateQuestInfo(int quest, int npc, int nextquest, boolean updata) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserQuestResult.getValue());
        mplew.write(12); // change v262+
        mplew.writeInt(quest);
        mplew.writeInt(npc);
        mplew.writeInt(nextquest);
        mplew.writeBool(updata);

        return mplew.getPacket();
    }

    public static byte[] startQuestTimeLimit(int n2, int n3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserQuestResult.getValue());
        mplew.write(7);
        mplew.writeShort(1);
        mplew.writeInt(n2);
        mplew.writeInt(n3);

        return mplew.getPacket();
    }

    public static byte[] stopQuestTimeLimit(int n2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserQuestResult.getValue());
        mplew.write(0x13);
        mplew.writeInt(n2);

        return mplew.getPacket();
    }

    /*
     * 更新重新獲取勳章任務信息
     */
    public static byte[] updateMedalQuestInfo(byte op, int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MedalReissueResult.getValue());
        /*
         * 0x00 = 領取成功
         * 0x03 = 已經有這個勳章
         */
        mplew.write(op);
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    public static byte[] updateMount(MapleCharacter chr, boolean levelup) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SetTamingMobInfo.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(chr.getMount().getLevel());
        mplew.writeInt(chr.getMount().getExp());
        mplew.writeInt(chr.getMount().getFatigue());
        mplew.write(levelup ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] showCharacterInfo(MaplePacketReader slea, MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_RemoteCharacterInfo.getValue());
        int unk = slea.readInt();
        String name = slea.readMapleAsciiString();
        MapleCharacter TouchP = player.getClient().getChannelServer().getPlayerStorage().getCharacterByName(name);
        mplew.writeInt(0);
        mplew.writeInt(new newCRand32().random());
        mplew.writeHexString("01 00 01 00 01 00 00 01 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 01 00 00 01 00 00 00 00 00 00 00 01 00 00 00 01 00 00 00 00 00 01 00 00 00 00 00 00 00 00 01 01 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
        mplew.writeInt(unk);
        mplew.writeInt(unk);
        mplew.writeInt(unk);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.writeInt(TouchP.getId());
        mplew.writeInt(TouchP.getId());
        mplew.writeInt(3);
        mplew.writeAsciiString(TouchP.getName(), 15); //角色ID
        mplew.write(TouchP.getGender());
        mplew.writeShort(133);
        mplew.writeInt(TouchP.getFace());
        mplew.writeInt(TouchP.getHair());
        mplew.writeInt(TouchP.getLevel());
        mplew.writeShort(TouchP.getJob());
        mplew.writeShort(TouchP.getStat().getStr()); // 初始
        mplew.writeShort(TouchP.getStat().getDex()); // 初始
        mplew.writeShort(TouchP.getStat().getInt()); // 初始
        mplew.writeShort(TouchP.getStat().getLuk()); // 初始
        mplew.writeInt(TouchP.getStat().getMaxHp(true));
        mplew.writeInt(TouchP.getStat().getMaxHp());
        mplew.writeInt(TouchP.getStat().getMaxMp(true));
        mplew.writeInt(TouchP.getStat().getMaxMp());
        mplew.writeHexString("37 00 01 01 21 00 00 00 B1 0A 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 10 6D E4 95 1A DF DA 01 10 27 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 00 00 00 00 00 00 00 00 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 06 00 00 00 00 00 00 00 00 00 00 00 06 00 00 00 00 00 00 00 00 00 40 E0 FD 3B 37 4F 01 56 D9 34 01 00 00 00 00 0A 00 00 00 00 06 07 00 00 00 00 00 00 00 00 1A DF DA 01 20 94 E4 95 00 80 05 BB 46 E6 17 02 00 40 E0 FD 3B 37 4F 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 32 00 00 00 00 B8 3E 78 47 41 D9 01 00 40 E0 FD 3B 37 4F 01 00 00 00 00 FF 00 00 00 00 FF");
        mplew.writeInt(TouchP.getInventory(MapleInventoryType.EQUIP).getSlotLimit()); // equip slots
        mplew.writeInt(TouchP.getInventory(MapleInventoryType.USE).getSlotLimit()); // use slots
        mplew.writeInt(TouchP.getInventory(MapleInventoryType.SETUP).getSlotLimit()); // set-up slots
        mplew.writeInt(TouchP.getInventory(MapleInventoryType.ETC).getSlotLimit()); // etc slots
        mplew.writeInt(TouchP.getInventory(MapleInventoryType.CASH).getSlotLimit()); // cash slots
        mplew.writeInt(TouchP.getInventory(MapleInventoryType.DECORATION).getSlotLimit()); // decoration slots
        MapleInventory iv = TouchP.getInventory(MapleInventoryType.EQUIPPED);
        List<Item> equippedList = iv.newList(); //獲取裝備中的道具列表
        Collections.sort(equippedList); //對道具進行排序
        List<Item> equipped = new ArrayList<>(); // 普通裝備
        List<Item> equippedCash = new ArrayList<>(); // 現金裝備
        List<Item> equippedDragon = new ArrayList<>(); // 龍裝備

        List<Item> equippedMechanic = new ArrayList<>(); // 機甲裝備
        List<Item> equippedAndroid = new ArrayList<>(); // 機器人的裝備
        List<Item> equippedLolitaCash = new ArrayList<>(); // 天使破壞者裝備
        List<Item> equippedBit = new ArrayList<>(); // 拼圖
        List<Item> equippedZeroBetaCash = new ArrayList<>(); // 神之子培塔時裝
        List<Item> equippedArcane = new ArrayList<>(); // 秘法符文
        List<Item> equippedAuthenticSymbol = new ArrayList<>(); // 真實符文
        List<Item> equippedTotem = new ArrayList<>(); // 圖騰
        List<Item> equippedMonsterEqp = new ArrayList<>(); // 獸魔裝備
        List<Item> equippedHakuFan = new ArrayList<>(); // 花狐裝備
        List<Item> equippedUnknown = new ArrayList<>(); // 未知
        List<Item> equippedCashPreset = new ArrayList<>(); // 現金裝備
        for (Item item : equippedList) {
            if (item.getPosition() < 0 && item.getPosition() > -100) { // 普通裝備
                equipped.add(item);
            } else if (item.getPosition() <= -100 && item.getPosition() > -1000) { //現金裝備
                equippedCash.add(item);
            } else if (item.getPosition() <= -1000 && item.getPosition() > -1100) { // 龍裝備 龍面具(1000), 龍墜飾(1001), 龍之翼(1002), 龍尾巴(1003)
                equippedDragon.add(item);
            } else if (item.getPosition() <= -1100 && item.getPosition() > -1200) { // 機甲裝備 戰神引擎(1100), 戰神手臂(1101), 戰神腿部(1102), 戰神身軀(1103), 戰神電晶體(1104)
                equippedMechanic.add(item);
            } else if (item.getPosition() <= -1200 && item.getPosition() > -1300) { // 機器人的裝備 帽子(1200), 披風(1201), 臉飾(1202), 上衣(1203), 褲裙(1204), 鞋子(1205), 手套(1206)
                equippedAndroid.add(item);
            } else if (item.getPosition() <= -1300 && item.getPosition() > -1310) { // 天使破壞者裝備 帽子(1300), 披風(1301), 臉飾(1302), 上衣(1303), 手套(1304)
                equippedLolitaCash.add(item);
            } else if (item.getPosition() <= -1400 && item.getPosition() > -1500) { // 拼圖(1400)~(1425)
                equippedBit.add(item);
            } else if (item.getPosition() <= -1500 && item.getPosition() > -1600) { // 神之子培塔時裝 眼飾(1500), 帽子(1501), 臉飾(1502), 耳環(1503), 披風(1504), 上衣(1505), 手套(1506), 武器(1507), 褲裙(1508), 鞋子(1509), 戒指1(1510), 戒指2(1511)
                equippedZeroBetaCash.add(item);
            } else if (item.getPosition() <= -1600 && item.getPosition() > -1606) { // 秘法符文 (1600)~(1605)
                equippedArcane.add(item);
            } else if (item.getPosition() <= -1700 && item.getPosition() > -1706) { // 真實符文 (1700)~(1705)
                equippedAuthenticSymbol.add(item);
            } else if (item.getPosition() <= -5100 && item.getPosition() > -5107) { // 獸魔裝備 帽子(5101), 披風(5102), 上衣(5103), 手套(5104), 鞋子(5105), 武器(5106)
                equippedMonsterEqp.add(item);
            } else if (item.getPosition() == -5200) { // 花狐裝備 扇子(5200)
                equippedHakuFan.add(item);
            } else if (item.getPosition() <= -1800 && item.getPosition() > -1830) { // 現金裝備分頁(1801)~(1830)
                equippedCashPreset.add(item);
            } else if (item.getPosition() <= -5000 && item.getPosition() > -5002) { // 圖騰(5000)~(5002)
                equippedTotem.add(item);
            } else if (item.getPosition() <= -6000 && item.getPosition() > -6200) { // 未知
                TouchP.getSkillSkin().put(MapleItemInformationProvider.getInstance().getSkillSkinFormSkillId(item.getItemId()), item.getItemId());
                equippedUnknown.add(item);
            }
            mplew.write(0);
            mplew.write(0);
            mplew.write(1);
            encodeInventory(mplew, equippedList, TouchP);
            mplew.writeHexString("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 05 00 E9 03 00 00 01 00 00 00 00 80 05 BB 46 E6 17 02 2D DC C4 04 01 00 00 00 00 7A 59 72 DC E2 DA 01 49 00 00 00 00 00 00 00 00 80 05 BB 46 E6 17 02 0C 00 00 00 00 00 00 00 00 80 05 BB 46 E6 17 02 13 BD C4 04 01 00 00 00 00 80 05 BB 46 E6 17 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 FF FF FF FF 02 00 00 00 00 00 00 00 00 FF FF FF FF 03 00 00 00 00 00 00 00 00 FF FF FF FF 04 00 00 00 00 00 00 00 00 FF FF FF FF 05 00 00 00 00 00 00 00 00 FF FF FF FF 06 00 00 00 00 00 00 00 00 FF FF FF FF 07 00 00 00 00 00 00 00 00 FF FF FF FF 08 00 00 00 00 00 00 00 00 FF FF FF FF 09 00 00 00 00 00 00 00 00 FF FF FF FF 0A 00 00 00 00 00 00 00 00 FF FF FF FF 0B 00 00 00 00 00 00 00 00 FF FF FF FF 0C 00 00 00 00 00 00 00 00 FF FF FF FF 0D 00 00 00 00 00 00 00 00 FF FF FF FF 0E 00 00 00 00 00 00 00 00 FF FF FF FF 0F 00 00 00 00 00 00 00 00 FF FF FF FF 10 00 00 00 00 00 00 00 00 FF FF FF FF 11 00 00 00 00 00 00 00 00 FF FF FF FF 12 00 00 00 00 00 00 00 00 FF FF FF FF 13 00 00 00 00 00 00 00 00 FF FF FF FF 14 00 00 00 00 00 00 00 00 FF FF FF FF 15 00 00 00 00 00 00 00 00 FF FF FF FF 16 00 00 00 00 00 00 00 00 FF FF FF FF 17 00 00 00 00 00 00 00 00 FF FF FF FF 18 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 FF FF FF FF 02 00 00 00 00 00 00 00 00 FF FF FF FF 03 00 00 00 00 00 00 00 00 FF FF FF FF 04 00 00 00 00 00 00 00 00 FF FF FF FF 05 00 00 00 00 00 00 00 00 FF FF FF FF 06 00 00 00 00 00 00 00 00 FF FF FF FF 07 00 00 00 00 00 00 00 00 FF FF FF FF 08 00 00 00 00 00 00 00 00 FF FF FF FF 09 00 00 00 00 00 00 00 00 FF FF FF FF 0A 00 00 00 00 00 00 00 00 FF FF FF FF 0B 00 00 00 00 00 00 00 00 FF FF FF FF 0C 00 00 00 00 00 00 00 00 FF FF FF FF 0D 00 00 00 00 00 00 00 00 FF FF FF FF 0E 00 00 00 00 00 00 00 00 FF FF FF FF 0F 00 00 00 00 00 00 00 00 FF FF FF FF 10 00 00 00 00 00 00 00 00 FF FF FF FF 11 00 00 00 00 00 00 00 00 FF FF FF FF 12 00 00 00 00 00 00 00 00 FF FF FF FF 13 00 00 00 00 00 00 00 00 FF FF FF FF 14 00 00 00 00 00 00 00 00 FF FF FF FF 15 00 00 00 00 00 00 00 00 FF FF FF FF 16 00 00 00 00 00 00 00 00 FF FF FF FF 17 00 00 00 00 00 00 00 00 FF FF FF FF 18 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 12 00 00 00 6E 1B 00 00 00 00 68 1E 00 00 00 00 6A 1E 00 00 00 00 D9 3F 00 00 00 00 1E 40 00 00 00 00 7B 41 00 00 00 00 5E 49 00 00 03 00 54 3D 31 4B 4A 00 00 1F 00 65 78 70 69 72 65 64 3D 31 3B 64 61 74 65 3D 30 3B 69 64 3D 30 3B 73 6C 6F 74 70 6F 73 3D 30 63 67 00 00 00 00 9F 69 00 00 00 00 A2 69 00 00 00 00 A3 69 00 00 00 00 6A AD 07 00 00 00 70 74 2F 00 00 00 72 74 2F 00 00 00 74 74 2F 00 00 00 75 74 2F 00 00 00 76 74 2F 00 00 00 01 15 00 00 00 00 00 66 00 00 00 3C 00 00 00 36 00 00 00 36 00 00 00 00 00 00 00 66 03 00 00 E9 02 00 00 66 03 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 F0 3F 00 00 00 00 00 00 F0 3F 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF 03 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 06 00 00 00 01 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00 03 00 00 00 00 00 00 00 04 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 06 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 83 FE FF FF F5 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 2C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 00 00 00 FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00");
        }
        return mplew.getPacket();
    }

    //    public static byte[] mountInfo(MapleCharacter chr) {
//        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
//
//        mplew.writeShort(OutHeader.UPDATE_MOUNT.getValue());
//        mplew.writeInt(chr.getId());
//        mplew.write(1);
//        mplew.writeInt(chr.getMount().getLevel());
//        mplew.writeInt(chr.getMount().getExp());
//        mplew.writeInt(chr.getMount().getFatigue());
//
//        return mplew.getPacket();
//    }
    public static byte[] updateSkill(int skillid, int level, int masterlevel, long expiration) {
        boolean isProfession = skillid == 92000000 || skillid == 92010000 || skillid == 92020000 || skillid == 92030000 || skillid == 92040000;
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ChangeSkillRecordResult.getValue());
        mplew.writeBool(!isProfession);
        mplew.writeBool(isProfession);
        mplew.write(0x00); //未知 V.114 新增
        mplew.writeShort(1); //有多少個技能
        mplew.writeInt(skillid);
        mplew.writeInt(level);
        mplew.writeInt(masterlevel);
        PacketHelper.addExpirationTime(mplew, expiration);
        mplew.write(8);

        return mplew.getPacket();
    }

    public static byte[] updateSkills(Map<Integer, SkillEntry> update) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ChangeSkillRecordResult.getValue());
        mplew.write(0x01); //刪除技能為 0x00 獲得技能為 0x01
        mplew.write(0x00);
        mplew.write(0x00); //未知 V.114 新增
        mplew.writeShort(update.size());
        for (Entry<Integer, SkillEntry> skills : update.entrySet()) {
            mplew.writeInt(skills.getKey());
            mplew.writeInt(skills.getValue().skillevel);
            mplew.writeInt(skills.getValue().masterlevel);
            PacketHelper.addExpirationTime(mplew, skills.getValue().expiration);
        }
        mplew.write((byte) Randomizer.rand(1, 255));

        return mplew.getPacket();
    }

    public static byte[] updatePetSkill(int skillid, int level, int masterlevel, long expiration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ChangeSkillRecordResult.getValue());
        mplew.write(0x00);
        mplew.write(0x01); //寵物是0x01
        mplew.write(0x00); //未知 V.114 新增
        mplew.writeShort(0x01); //技能的數量
        mplew.writeInt(skillid);
        mplew.writeInt(level == 0 ? -1 : level);
        mplew.writeInt(masterlevel);
        PacketHelper.addExpirationTime(mplew, expiration);
        mplew.write(8);

        return mplew.getPacket();
    }

    public static byte[] updateQuestMobKills(MapleQuestStatus status) {
        MessageOption option = new MessageOption();
        MapleQuestStatus quest = new MapleQuestStatus(status.getQuest(), 1);
        StringBuilder sb = new StringBuilder();
        for (int kills : status.getMobKills().values()) {
            sb.append(StringUtil.getLeftPaddedStr(String.valueOf(kills % 1000), '0', 3));
        }
        quest.setCustomData(sb.toString());
        option.setQuestStatus(quest);
        return CWvsContext.sendMessage(MessageOpcode.MS_QuestRecordMessage, option);
    }

    /*
     * 發送角色的鍵盤設置
     */
    public static byte[] getKeymap(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FuncKeyMappedInit.getValue());
        for (int l = 0; l < FuncKeyMap.MAX_LAYOUT; l++) {
            chr.getFuncKeyMaps().get(l).setMode(1);
            chr.getFuncKeyMaps().get(l).encode(mplew);
        }
        mplew.write(0);
        chr.getQuickSlot().writeData(mplew);
        // v264+
        mplew.write(0);
        mplew.writeInt(82);
        mplew.writeInt(71);
        mplew.writeInt(73);
        mplew.writeInt(29);
        mplew.writeInt(83);
        mplew.writeInt(79);
        mplew.writeInt(81);
        mplew.writeInt(2);
        mplew.writeInt(3);
        mplew.writeInt(4);
        mplew.writeInt(5);
        mplew.writeInt(16);
        mplew.writeInt(17);
        mplew.writeInt(18);
        mplew.writeInt(19);
        mplew.writeInt(6);
        mplew.writeInt(7);
        mplew.writeInt(8);
        mplew.writeInt(9);
        mplew.writeInt(20);
        mplew.writeInt(30);
        mplew.writeInt(31);
        mplew.writeInt(32);
        mplew.writeInt(10);
        mplew.writeInt(11);
        mplew.writeInt(33);
        mplew.writeInt(34);
        mplew.writeInt(37);
        mplew.writeInt(38);
        mplew.writeInt(49);
        mplew.writeInt(50);
        return mplew.getPacket();
    }

    /*
     * 寵物自動加血
     */
    public static byte[] petAutoHP(int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_PetConsumeItemInit.getValue());
        mplew.writeInt(itemId);
        return mplew.getPacket();
    }

    /*
     * 寵物自動加藍
     */
    public static byte[] petAutoMP(int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_PetConsumeMPItemInit.getValue());
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    /*
     * 寵物自動加BUFF狀態
     */
    public static byte[] petAutoBuff(int skillId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_PET_AUTO_BUFF.getValue());
        mplew.writeInt(skillId);

        return mplew.getPacket();
    }

    /*
     * 打開釣魚記錄NPC
     */
    public static byte[] openFishingStorage(int type, HiredFisher hf, MerchItemPackage pack, int playrId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        /*
         * AF 00
         * 21
         * FF FF FF FF FF FF FF FF
         * 00
         * 00 00 00 00 00 00 00 00
         * 9E 4E 08 00
         */
        mplew.writeShort(OutHeader.FISHING_STORE.getValue());
        mplew.write(type);
        switch (type) {
            case 33: {
                mplew.writeInt(-1);
                break;
            }
            case 35: {
                mplew.writeInt(pack != null ? (int) pack.getMesos() : 0);
                mplew.writeLong(pack != null ? (long) ((int) pack.getExp()) : 0);
                writeHiredFisher(mplew, hf, pack, playrId);
                break;
            }
            case 28:
            case 30: {
                mplew.writeInt(hf.getObjectId());
                writeHiredFisher(mplew, hf, pack, playrId);
                break;
            }
            case 15: {
                mplew.writeInt(0);
                mplew.write(0);
                break;
            }
            case 22: {
                mplew.writeInt(hf.getOwnerId());
                mplew.write(1);
                break;
            }
            case 23: {
                mplew.writeInt(hf.getOwnerId());
                break;
            }
            case 43:
            case 45: {
                mplew.writeLong(DateUtil.getKoreanTimestamp(hf.getStartTime()));
                mplew.writeLong(DateUtil.getKoreanTimestamp(hf.getStopTime()));
            }
        }

        return mplew.getPacket();
    }

    public static void writeHiredFisher(MaplePacketLittleEndianWriter mplew, HiredFisher hf, MerchItemPackage itemPackage, int playrId) {
        long l2 = -1;
        mplew.writeLong(l2);
        mplew.writeInt(0);
        EnumMap<MapleInventoryType, ArrayList<Item>> items = new EnumMap<>(MapleInventoryType.class);
        items.put(MapleInventoryType.EQUIP, new ArrayList<>());
        items.put(MapleInventoryType.USE, new ArrayList<>());
        items.put(MapleInventoryType.SETUP, new ArrayList<>());
        items.put(MapleInventoryType.ETC, new ArrayList<>());
        items.put(MapleInventoryType.CASH, new ArrayList<>());
        items.put(MapleInventoryType.DECORATION, new ArrayList<>());
        if (hf != null) {
            hf.getItems().forEach(item -> items.get(ItemConstants.getInventoryType(item.getItem().getItemId())).add(item.getItem()));
        } else if (itemPackage != null) {
            itemPackage.getItems().forEach(item -> items.get(ItemConstants.getInventoryType(item.getItemId())).add(item));
        }
        items.forEach((key, value) -> {
            mplew.write(value.size());
            value.forEach(item -> PacketHelper.GW_ItemSlotBase_Encode(mplew, item));
        });
        items.clear();
        mplew.writeInt(hf != null ? hf.getOwnerId() : playrId);
    }

    public static byte[] fairyPendantMessage(int position, int stage, int percent, long startTime, long time, boolean inChat) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_BonusExpRateChanged.getValue());
        mplew.writeInt(Math.abs(position)); // 道具的位置
        mplew.writeInt(stage); // 累計階段
        mplew.writeInt(percent); // 百分比經驗提示
        mplew.writeLong(PacketHelper.getTime(startTime)); // 開始時間
        mplew.writeLong(time); // 今天套用時間(分鐘)
        mplew.write(inChat ? 1 : 0);
        return mplew.getPacket();
    }

    public static byte[] giveFameResponse(int mode, String charname, int newfame) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_GivePopularityResult.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(charname);
        mplew.write(mode);
        mplew.writeInt(newfame);

        return mplew.getPacket();
    }

    public static byte[] giveFameErrorResponse(int status) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        /*
         * * 0: ok, use giveFameResponse<br> 1: the username is incorrectly
         * entered<br> 2: users under level 15 are unable to toggle with
         * fame.<br> 3: can't raise or drop fame anymore today.<getFitOptionList> 4: can't
         * raise or drop fame for this character for this month anymore.<br> 5:
         * received fame, use receiveFame()<br> 6: level of fame neither has
         * been raised nor dropped due to an unexpected error
         */
        mplew.writeShort(OutHeader.LP_GivePopularityResult.getValue());
        mplew.write(status);

        return mplew.getPacket();
    }

    public static byte[] receiveFame(int mode, String charnameFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_GivePopularityResult.getValue());
        mplew.write(5);
        mplew.writeMapleAsciiString(charnameFrom);
        mplew.write(mode);

        return mplew.getPacket();
    }

    /*
     * 終於試出來了 STOP_CLOCK = CLOCK + 6
     */
    public static byte[] stopClock() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_DestroyClock.getValue());
        return mplew.getPacket();
    }

    public static byte[] practiceMode(boolean b) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.PRACTICE_MODE.getValue());
        mplew.writeBool(b);
        return mplew.getPacket();
    }

    /**
     * 召喚煙霧效果
     */
    public static byte[] spawnMist(MapleAffectedArea mist) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_AffectedAreaCreated.getValue());
        mplew.writeInt(mist.getObjectId());
        mplew.write(mist.getAreaType()); //2 = invincible, so put 1 for recovery aura
        mplew.writeInt(mist.getOwnerId());
        mplew.writeInt(mist.getSkillID());
        mplew.writeShort(mist.getSkillLevel());//V.160 byte=>short
        mplew.writeShort(mist.getSkillDelay()); //延遲
        mplew.writeRect(mist.getArea());
        if (mist.getSkillID() == 菈菈.發現_風之鞦韆) {
            mplew.writeRect(mist.getArea());
        }
        mplew.writeInt(mist.getSubtype());
        mplew.writePos(mist.getPosition());
        mplew.writeInt((mist.getSkillID() == 227) ? mist.getPosition().x : 0);
        mplew.writeInt(mist.getForce());
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        switch (mist.getSkillID()) {
            case 夜使者.絕對領域:
            case 機甲戰神.扭曲領域:
            case 狂豹獵人.鑽孔集裝箱:
            case 狂豹獵人.連弩陷阱:
            case 皮卡啾.帕拉美:
            case 皮卡啾.博拉多利:
            case 米哈逸.閃光交叉_安裝:
            case 伊利恩.朗基努斯領域:
            case 海盜.海盜旗幟:
            case 龍魔導士.歐尼斯之氣息:
            case 龍魔導士.粉碎_回歸:
            case 卡蒂娜.鏈之藝術_漩渦:
            case 墨玄.神功_破空拳神力的氣息:
            case 雪吉拉.我製造的_雪人:
            case 暗影神偷.煙幕彈:
                mplew.writeBool(mist.isFacingLeft());
                break;
        }
        mplew.writeInt(mist.getLeftTime());
        mplew.writeInt(0); // TMS 220
        mplew.writeInt(0); // TMS 226
        mplew.write(mist.BUnk1);//V.160 new
        mplew.write(mist.BUnk2); // TMS 244
        mplew.write(false); // TMS 226

        if (mist.getSkillID() == 主教.神聖之水) {
            mplew.writeInt(0);
        }
        mplew.writeInt(0); // TMS 263.1 +
        //
        mplew.writeInt(0); // TMS 265.3 +
        mplew.writeInt(0); // TMS 265.3 +
        return mplew.getPacket();
    }

    /**
     * 移除煙霧效果
     */
    public static byte[] removeMist(int oid, boolean eruption) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_AffectedAreaRemoved.getValue());
        mplew.writeInt(oid);
        mplew.write(eruption);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] spawnLove(int oid, int itemid, String name, String msg, Point pos, int ft) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MessageBoxEnterField.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(itemid);
        mplew.writeMapleAsciiString(msg);
        mplew.writeMapleAsciiString(name);
        mplew.writeShort(pos.x);
        mplew.writeShort(pos.y + ft);

        return mplew.getPacket();
    }

    public static byte[] removeLove(int oid, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MessageBoxLeaveField.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    public static byte[] itemEffect(int chrId, int itemid, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserSetActiveEffectItem.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(itemid);
        mplew.writeInt(type);

        return mplew.getPacket();
    }

    public static byte[] showTitleEffect(int chrId, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserSetActiveNickItem.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(itemid);
        mplew.writeBool(false);

        return mplew.getPacket();
    }

    /**
     * 顯示角色椅子
     */
    public static byte[] UserSetActivePortableChair(MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserSetActivePortableChair.getValue());
        mplew.writeInt(player.getId());
        PortableChair chair = player.getChair();
        mplew.writeInt(chair == null ? 0 : chair.getItemId());
//        mplew.writeInt(chair == null ? 0 : chair.getMeso());
//        mplew.writeInt(chair == null ? 0 : chair.getType());
//        mplew.write(chair == null ? 0 : chair.getUnk());
        writeChairData(mplew, player);
        mplew.write(0);
        return mplew.getPacket();
    }

    private static void writeChairData(MaplePacketLittleEndianWriter mplew, MapleCharacter player) {
        PortableChair chair = player.getChair();
        boolean hasChair = chair != null && !ServerConfig.BLOCK_CHAIRS_SET.contains(chair.getItemId());
        mplew.writeBool(hasChair);
        if (hasChair) {
            switch (ItemConstants.getChairType(chair.getItemId())) {
                case TOWER:
                    encodeTowerChairInfo(mplew, player);
                    break;
                case MESO:
                    mplew.writeLong(0L);
                    break;
                case TEXT:
                    mplew.writeMapleAsciiString(chair.getMsg());
                    PacketHelper.addChaterName(mplew, player.getName(), chair.getMsg());
                    break;
                case LV:
                    boolean hasArr = chair.getArr() != null;
                    mplew.writeBool(hasArr);
                    if (hasArr) {
                        mplew.writeInt(chair.getUn2());
                        mplew.writeInt(chair.getArr().length);
                        for (Triple triple : chair.getArr()) {
                            mplew.writeInt((Integer) triple.getLeft());
                            Pair right = (Pair) triple.getRight();
                            AvatarLook left = (AvatarLook) right.getLeft();
                            mplew.writeInt(left != null ? left.getJob() : 0);
                            String mid = (String) triple.getMid();
                            mplew.writeMapleAsciiString(mid);
                            mplew.writeBool(left != null);
                            if (left != null) {
                                left.encode(mplew, false);
                            }

                            mplew.writeBool(right.getRight() != null);
                            if (right.getRight() != null) {
                                ((AvatarLook) right.getRight()).encode(mplew, false);
                            }
                        }
                        mplew.writeInt(0);
                    }
                    break;
                case POP:
                    mplew.writeInt(1);
                    for (int i = 0; i < 1; i++) {
                        mplew.writeMapleAsciiString(player.getName());
                        mplew.writeInt(player.getFame());
                    }
                    break;
                case TIME:
                    mplew.writeInt(0);
                    break;
                case STARFORCE:
                case RANDOM:
                case MIRROR:
                case ANDROID:
                case ROTATED_SLEEPING_BAG_CHAIR:
                case EVENT_POINT:
                case EVENT_POINT_GENDERLY:
                case EVENT_POINT_CLONE:
                case YETI://??
                case MAPLE_GLOBE://??
                    break;
                case TRICK_OR_TREAT:
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                    break;
                case CELEBRATE:
                    mplew.writeInt(chair.getItemId());
                    break;
                case IDENTITY:
                    mplew.writeInt(player.getAccountID());
                    mplew.write(0);
                    mplew.writeInt(0);
                    break;
                case POP_BUTTON:
                    mplew.writeInt(0);
                    break;
                case ROLLING_HOUSE:
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                    mplew.write(0);
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                    break;
                case MANNEQUIN:
                    mplew.writeInt(0);
                    break;
                case PET:
                    for (int i = 0; i < 3; i++) {
                        mplew.writeInt(0);
                        mplew.writeInt(0);
                        mplew.writeInt(0);
                    }
                    break;
                case SCORE:
                    mplew.writeInt(0);
                    break;
                case SCALE_AVATAR:
                    mplew.writeBool(false);
                    break;
                case WASTE:
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                    break;
                case ROLLING_HOUSE_2019:
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                    mplew.write(0);
                    mplew.writeInt(0);
                    mplew.writeInt(0);

                    mplew.writeInt(0);
                    break;
                case CHAR_LV:
                    mplew.writeInt(player.getLevel());
                    break;
                case HASH_TAG:
                case UN22:
                case TRAITS:
                default:
                    mplew.writeInt(chair.getUn3());
                    mplew.writeInt(chair.getUn4());
                    mplew.write(chair.getUn5());
                    break;
            }
        }
    }

    public static void encodeTowerChairInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter player) {
        String string;
        ArrayList<Integer> arrayList = new ArrayList<>();
        player.getInfoQuest(7266);
        for (int i2 = 0; i2 < 6 && (string = player.getOneInfo(7266, String.valueOf(i2))) != null && Integer.valueOf(string) > 0; ++i2) {
            arrayList.add(Integer.valueOf(string));
        }
        mplew.writeInt(arrayList.size());
        arrayList.forEach(mplew::writeInt);
    }

    public static byte[] showSitOnTimeCapsule() {// 3010587
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserSitOnTimeCapsule.getValue());
        return mplew.getPacket();
    }

    public static byte[] addChairMeso(int cid, int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserSetActivePortableChair.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(value);
        mplew.writeInt(1);

        return mplew.getPacket();
    }

    public static byte[] useTowerChairSetting() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.USE_TOWERCHAIR_SETTING_RESULT.getValue());
        return mplew.getPacket();
    }

    /*
     * 取消椅子
     */
    public static byte[] UserSitResult(int playerId, int chairId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CANCEL_CHAIR_TRIGGER_UI.getValue());
        mplew.writeInt(playerId);
        mplew.writeInt(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] spawnReactor(MapleReactor reactor) {
        /*
         * Recv REACTOR_SPAWN [01B1] (24)
         * B1 01
         * FB 2A 00 00
         * 68 77 89 00
         * 00
         * 60 13 1D 00
         * 00
         * 06 00 44 47 54 65 73 74
         * ??..hw?.`......DGTest
         */
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ReactorEnterField.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.writeInt(reactor.getReactorId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getPosition());
        mplew.write(reactor.getFacingDirection()); // stance
        mplew.writeMapleAsciiString(reactor.getName());

        return mplew.getPacket();
    }

    public static byte[] triggerReactor(MapleReactor reactor, int stance) {
        return triggerReactor(reactor, stance, 0, 0, 0);
    }

    public static byte[] triggerReactor(MapleReactor reactor, int stance, int n2, int cid, int n4) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ReactorChangeState.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getPosition());
        mplew.writeShort(stance);
        mplew.write(n4);
        mplew.writeInt(n2);
        mplew.writeInt(cid);

        return mplew.getPacket();
    }

    public static byte[] triggerReactor(MapleReactor reactor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ReactorChangeState.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getPosition());
        mplew.writeShort(reactor.getHitStart());//hitstart
        mplew.write(reactor.getProperEventIdx());
        mplew.writeInt(reactor.getStateEnd());
        mplew.writeInt(reactor.getOwnerID());

        return mplew.getPacket();
    }

    public static byte[] destroyReactor(MapleReactor reactor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ReactorRemove.getValue());
        mplew.writeInt(reactor.getObjectId());

        return mplew.getPacket();
    }

    public static byte[] reactorLeaveField(MapleReactor reactor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ReactorLeaveField.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.write(0);
        mplew.writePos(reactor.getPosition());
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] musicChange(String song) {
        return FieldPacket.fieldEffect(FieldEffect.changeBGM(song, 0, 0, 0));
    }

    public static byte[] showEffect(String effect) {
        return FieldPacket.fieldEffect(FieldEffect.getFieldBackgroundEffectFromWz(effect, 0));
    }

    public static byte[] playSound(String sound) {
        return FieldPacket.fieldEffect(FieldEffect.playSound(sound, 100, 0, 0));
    }

    public static byte[] startMapEffect(String msg, int itemid, boolean active) {
        return startMapEffect(msg, itemid, -1, active);
    }

    public static byte[] startMapEffect(String msg, int itemid, int effectType, boolean active) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_BlowWeather.getValue());
//        mplew.write(active ? 0 : 1);
//        mplew.writeInt(itemid);
//        if (effectType > 0) {
//            mplew.writeInt(effectType);
//        }
//        if (active) {
//            mplew.writeMapleAsciiString(msg);
//        }
//        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(itemid);
        if (itemid == 116) {
            mplew.writeInt(effectType);
        }
        if (itemid > 0) {
            mplew.writeMapleAsciiString(msg);
            mplew.writeInt(15);
            mplew.write(0);
        }

        return mplew.getPacket();
    }

    public static byte[] removeMapEffect() {
        return startMapEffect(null, 0, -1, false);
    }

    /*
     * 顯示占卜結果
     */
    public static byte[] showPredictCard(String name, String otherName, int love, int cardId, int commentId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.SHOW_PREDICT_CARD.getValue());
        mplew.writeMapleAsciiString(name);
        mplew.writeMapleAsciiString(otherName);
        mplew.writeInt(love);
        mplew.writeInt(cardId);
        mplew.writeInt(commentId);

        return mplew.getPacket();
    }

    public static byte[] UserSkillPrepare(int fromId, int skillId, byte level, byte display, byte direction, byte speed, Point position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserSkillPrepare.getValue());
        mplew.writeInt(fromId); //角色ID
        mplew.writeInt(skillId); //技能ID
        mplew.write(level); //技能等級
        mplew.write(display); //技能效果
        mplew.write(direction); //攻擊方向
        mplew.write(speed); //速度
        if (position != null) {
            mplew.writePos(position); //有些技能這個地方要寫個坐標信息
        }

        return mplew.getPacket();
    }

    public static byte[] skillCancel(MapleCharacter from, int skillId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserSkillCancel.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);
        mplew.writeInt(skillId);

        return mplew.getPacket();
    }

    public static byte[] sendHint(String hint, int width, int time, Point pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (width < 1) {
            width = hint.length() * 10;
            if (width < 40) {
                width = 40;
            }
        }
        if (time < 5) {
            time = 5;
        }
        mplew.writeShort(OutHeader.LP_UserBalloonMsg.getValue());
        mplew.writeMapleAsciiString(hint);
        mplew.writeShort(width);
        mplew.writeShort(time);
        mplew.writeBool(pos == null);
        if (pos != null) {
            mplew.writePosInt(pos);
        }

        return mplew.getPacket();
    }

    public static byte[] showEquipEffect() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.SHOW_EQUIP_EFFECT.getValue());

        return mplew.getPacket();
    }

    public static byte[] showEquipEffect(int team) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.SHOW_EQUIP_EFFECT.getValue());
        mplew.writeShort(team);

        return mplew.getPacket();
    }

    public static byte[] useSkillBook(MapleCharacter chr, int skillid, int maxlevel, boolean canuse, boolean success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SkillLearnItemResult.getValue());
        mplew.write(1);
        mplew.writeInt(chr.getId());
        mplew.write(1);
        mplew.writeInt(skillid);
        mplew.writeInt(maxlevel);
        mplew.writeBool(canuse);
        mplew.writeBool(success);

        return mplew.getPacket();
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

    public static byte[] boatPacket(int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        // 1034: balrog boat comes, 1548: boat comes, 3: boat leaves
        mplew.writeShort(OutHeader.BOAT_EFFECT.getValue());
        mplew.writeShort(effect); // 0A 04 balrog
        //this packet had 3: boat leaves

        return mplew.getPacket();
    }

    public static byte[] boatEffect(int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        // 1034: balrog boat comes, 1548: boat comes, 3: boat leaves
        mplew.writeShort(OutHeader.BOAT_EFF.getValue());
        mplew.writeShort(effect); // 0A 04 balrog
        //this packet had the other ones o.o

        return mplew.getPacket();
    }

    public static byte[] removeItemFromDuey(boolean remove, int Package) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_Parcel.getValue());
        mplew.write(0x18);
        mplew.writeInt(Package);
        mplew.write(remove ? 3 : 4);

        return mplew.getPacket();
    }

    public static byte[] sendDuey(byte operation, List<MapleDueyActions> packages) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_Parcel.getValue());
        mplew.write(operation);
        switch (operation) {
            case 0x09: { // Request 13 Digit AS
                mplew.write(1);
                // 0xFF = error
                break;
            }
            case 0x0A: { // 打開送貨員
                mplew.write(0);
                mplew.write(packages.size());
                for (MapleDueyActions dp : packages) {
                    mplew.writeInt(dp.getPackageId());
                    mplew.writeAsciiString(dp.getSender(), 15);
                    mplew.writeInt(dp.getMesos());
                    mplew.writeLong(PacketHelper.getTime(dp.getSentTime()));
                    mplew.writeZeroBytes(202);
                    if (dp.getItem() != null) {
                        mplew.write(1);
                        PacketHelper.GW_ItemSlotBase_Encode(mplew, dp.getItem());
                    } else {
                        mplew.write(0);
                    }
                }
                mplew.write(0);
                break;
            }
        }
        return mplew.getPacket();
    }

    public static byte[] enableTV() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.ENABLE_TV.getValue());
        mplew.writeInt(0);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] removeTV() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.REMOVE_TV.getValue());

        return mplew.getPacket();
    }

    public static byte[] sendTV(MapleCharacter chr, List<String> messages, int type, MapleCharacter partner, int delay) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.START_TV.getValue());
        mplew.write(partner != null ? 2 : 1);
        mplew.write(type); // type   Heart = 2  Star = 1  Normal = 0
        chr.getAvatarLook().encode(mplew, false);
        mplew.writeMapleAsciiString(chr.getName());

        if (partner != null) {
            mplew.writeMapleAsciiString(partner.getName());
        } else {
            mplew.writeShort(0);
        }
        for (int i = 0; i < messages.size(); i++) {
            if (i == 4 && messages.get(4).length() > 15) {
                mplew.writeMapleAsciiString(messages.get(4).substring(0, 15)); // hmm ?
            } else {
                mplew.writeMapleAsciiString(messages.get(i));
            }
        }
        mplew.writeInt(delay); // time limit shit lol 'Your thing still start in blah blah seconds'
        if (partner != null) {
            partner.getAvatarLook().encode(mplew, false);
        }

        return mplew.getPacket();
    }

    public static byte[] showQuestMsg(final String msg) {
        return serverNotice(5, msg);
    }

    public static byte[] Mulung_Pts(int recv, int total) {
        return showQuestMsg("獲得了 " + recv + " 點修煉點數。總修煉點數為 " + total + " 點。");
    }

    public static byte[] showOXQuiz(int questionSet, int questionId, boolean askQuestion) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_Quiz.getValue());
        mplew.write(askQuestion ? 1 : 0);
        mplew.write(questionSet);
        mplew.writeShort(questionId);

        return mplew.getPacket();
    }

    public static byte[] leftKnockBack() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SnowBallTouch.getValue());

        return mplew.getPacket();
    }

    public static byte[] rollSnowball(int type, MapleSnowball.MapleSnowballs ball1, MapleSnowball.MapleSnowballs ball2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SnowBallState.getValue());
        mplew.write(type); // 0 = normal, 1 = rolls from start to end, 2 = down disappear, 3 = up disappear, 4 = move
        mplew.writeInt(ball1 == null ? 0 : (ball1.getSnowmanHP() / 75));
        mplew.writeInt(ball2 == null ? 0 : (ball2.getSnowmanHP() / 75));
        mplew.writeShort(ball1 == null ? 0 : ball1.getPosition());
        mplew.write(0);
        mplew.writeShort(ball2 == null ? 0 : ball2.getPosition());
        mplew.writeZeroBytes(11);

        return mplew.getPacket();
    }

    public static byte[] enterSnowBall() {
        return rollSnowball(0, null, null);
    }

    public static byte[] hitSnowBall(int team, int damage, int distance, int delay) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SnowBallHit.getValue());
        mplew.write(team);// 0 is down, 1 is up
        mplew.writeShort(damage);
        mplew.write(distance);
        mplew.write(delay);

        return mplew.getPacket();
    }

    public static byte[] snowballMessage(int team, int message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SnowBallMsg.getValue());
        mplew.write(team);// 0 is down, 1 is up
        mplew.writeInt(message);

        return mplew.getPacket();
    }

    public static byte[] finishedSort(int type) {
        /*
         * [41 00] [01] [01]
         */
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SortItemResult.getValue());
        mplew.write(1);
        mplew.write(type);

        return mplew.getPacket();
    }

    // 00 01 00 00 00 00
    public static byte[] coconutScore(int[] coconutscore) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CoconutScore.getValue());
        mplew.writeShort(coconutscore[0]);
        mplew.writeShort(coconutscore[1]);

        return mplew.getPacket();
    }

    public static byte[] hitCoconut(boolean spawn, int id, int type) {
        // FF 00 00 00 00 00 00
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CoconutHit.getValue());
        if (spawn) {
            mplew.write(0);
            mplew.writeInt(0x80);
        } else {
            mplew.writeInt(id);
            mplew.write(type); // What action to do for the coconut.
        }

        return mplew.getPacket();
    }

    public static byte[] finishedGather(int type) {
        /*
         * [40 00] [01] [01]
         */
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_GatherItemResult.getValue());
        mplew.write(1);
        mplew.write(type);
        return mplew.getPacket();
    }

    public static byte[] yellowChat(String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserChatMsg.getValue()); //沒有找到封包使用就用這個
        mplew.writeShort(0x07);
        mplew.writeMapleAsciiString(msg);
        return mplew.getPacket();
    }

    public static byte[] getPeanutResult(int ourItem) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_IncubatorResult.getValue());
        mplew.writeBool(false);
        mplew.writeInt(ourItem);
        return mplew.getPacket();
    }

    public static byte[] getPeanutResult(int itemId, short quantity, int ourItem, int ourSlot) {
        return getPeanutResult(itemId, quantity, ourItem, ourSlot, null);
    }

    public static byte[] getPeanutResult(int itemId, short quantity, int ourItem, int ourSlot, Item item) {
        return getPeanutResult(itemId, quantity, ourItem, ourSlot, 0, (short) 0, (byte) 0, item);
    }

    public static byte[] getPeanutResult(int itemId, short quantity, int ourItem, int ourSlot, byte fever) {
        return getPeanutResult(itemId, quantity, ourItem, ourSlot, fever, null);
    }

    public static byte[] getPeanutResult(int itemId, short quantity, int ourItem, int ourSlot, byte fever, Item item) {
        return getPeanutResult(itemId, quantity, ourItem, ourSlot, 0, (short) 0, fever, item);
    }

    public static byte[] getPeanutResult(int itemId, short quantity, int ourItem, int ourSlot, int itemId2, short quantity2) {
        return getPeanutResult(itemId, quantity, ourItem, ourSlot, itemId2, quantity2, (byte) 0, null);
    }

    public static byte[] getPeanutResult(int itemId, short quantity, int ourItem, int ourSlot, int itemId2, short quantity2, byte fever, Item item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_IncubatorResult.getValue());
        boolean success = true;
        mplew.writeBool(success);
        if (success) {
            mplew.writeInt(itemId);
            mplew.writeShort(quantity);
            mplew.writeInt(1); // todo:  use 1 v267
            mplew.writeInt(ourItem);
            mplew.writeInt(ourSlot);
            mplew.writeInt(itemId2);
            mplew.writeInt(quantity2);
            mplew.write(fever);
            mplew.write(item != null);
            if (item != null) {
                PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
            }
        } else {
            mplew.writeInt(ourItem);
        }
        return mplew.getPacket();
    }

    /*
     * 發送玩家升級信息和 家族 公會 相關
     */
    public static byte[] sendLevelup(boolean family, int level, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //[80 00] [01] [15 00 00 00] [09 00 53 48 5A 42 47 BF CE B7 A8]
        mplew.writeShort(OutHeader.LP_NotifyLevelUp.getValue());
        mplew.write(family ? 1 : 2);
        mplew.writeInt(level);
        mplew.writeMapleAsciiString(name);

        return mplew.getPacket();
    }

    public static byte[] sendMarriage(boolean family, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_NotifyWedding.getValue());
        mplew.write(family ? 1 : 0);
        mplew.writeMapleAsciiString(name);

        return mplew.getPacket();
    }

    public static byte[] sendJobup(boolean family, int jobid, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_NotifyJobChange.getValue());
        mplew.write(family ? 1 : 0);
        mplew.writeInt(jobid); //or is this a short
        mplew.writeMapleAsciiString(name);

        return mplew.getPacket();
    }

    /*
     * 顯示龍飛行效果
     */
    public static byte[] showDragonFly(int chrId, int type, int mountId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_DragonGlide.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(type);
        if (type == 0) {
            mplew.writeInt(mountId);
        }

        return mplew.getPacket();
    }

    public static byte[] temporaryStats_Aran() {
        Map<MapleStat.Temp, Integer> stats = new EnumMap<>(MapleStat.Temp.class);
        stats.put(MapleStat.Temp.力量, 999);
        stats.put(MapleStat.Temp.敏捷, 999);
        stats.put(MapleStat.Temp.智力, 999);
        stats.put(MapleStat.Temp.幸運, 999);
        stats.put(MapleStat.Temp.物攻, 255);
        stats.put(MapleStat.Temp.命中, 999);
        stats.put(MapleStat.Temp.迴避, 999);
        stats.put(MapleStat.Temp.速度, 140);
        stats.put(MapleStat.Temp.跳躍, 120);
        return temporaryStats(stats);
    }

    public static byte[] temporaryStats_Balrog(MapleCharacter chr) {
        Map<MapleStat.Temp, Integer> stats = new EnumMap<>(MapleStat.Temp.class);
        int offset = 1 + (chr.getLevel() - 90) / 20;
        //every 20 levels above 90, +1

        stats.put(MapleStat.Temp.力量, chr.getStat().getTotalStr() / offset);
        stats.put(MapleStat.Temp.敏捷, chr.getStat().getTotalDex() / offset);
        stats.put(MapleStat.Temp.智力, chr.getStat().getTotalInt() / offset);
        stats.put(MapleStat.Temp.幸運, chr.getStat().getTotalLuk() / offset);
        stats.put(MapleStat.Temp.物攻, chr.getStat().getTotalWatk() / offset);
        stats.put(MapleStat.Temp.物防, chr.getStat().getTotalMagic() / offset);
        return temporaryStats(stats);
    }

    public static byte[] temporaryStats(Map<MapleStat.Temp, Integer> mystats) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ForcedStatSet.getValue());
        //str 0x1, dex 0x2, int 0x4, luk 0x8
        //level 0x10 = 255
        //0x100 = 999
        //0x200 = 999
        //0x400 = 120
        //0x800 = 140
        int updateMask = 0;
        for (MapleStat.Temp statupdate : mystats.keySet()) {
            updateMask |= statupdate.getValue();
        }
        mplew.writeInt(updateMask);
        Integer value;
        for (final Entry<MapleStat.Temp, Integer> statupdate : mystats.entrySet()) {
            value = statupdate.getKey().getValue();
            if (value >= 1) {
                if (value <= 0x200) { //level 0x10 - is this really short or some other? (FF 00)
                    mplew.writeShort(statupdate.getValue().shortValue());
                } else {
                    mplew.write(statupdate.getValue().byteValue());
                }
            }
        }

        return mplew.getPacket();
    }

    public static byte[] temporaryStats_Reset() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ForcedStatReset.getValue());

        return mplew.getPacket();
    }

    /*
     * 傳授技能後顯示的窗口
     */
    public static byte[] sendLinkSkillWindow(int skillId) {
        return UIPacket.sendUIWindow(0x03, skillId);
    }

    /*
     * 組隊搜索窗口
     */
    public static byte[] sendPartyWindow(int npc) {
        return UIPacket.sendUIWindow(0x15, npc);
    }

    /*
     * 道具修理窗口
     */
    public static byte[] sendRepairWindow(int npc) {
        return UIPacket.sendUIWindow(0x21, npc);
    }

    /*
     * 專業技術窗口
     */
    public static byte[] sendProfessionWindow(int npc) {
        return UIPacket.sendUIWindow(0x2A, npc);
    }

    public static byte[] sendRedLeaf(int points, boolean viewonly) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);
        mplew.writeShort(OutHeader.LP_UserOpenUIWithOption.getValue());
        mplew.writeInt(0x73);
        mplew.writeInt(points);
        mplew.writeInt(viewonly ? 1 : 0); //只是查看，完成按鈕被禁用
        return mplew.getPacket();
    }

    public static byte[] sendPVPMaps() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_PvPStatusResult.getValue());
        mplew.write(1); //max amount of players
        mplew.writeInt(0);
        for (int i = 0; i < 3; i++) {
            mplew.writeInt(1); //how many peoples in each map
        }
        mplew.writeLong(0);
        for (int i = 0; i < 3; i++) {
            mplew.writeInt(1);
        }
        mplew.writeLong(0);
        for (int i = 0; i < 4; i++) {
            mplew.writeInt(1);
        }
        for (int i = 0; i < 10; i++) {
            mplew.writeInt(1);
        }
        mplew.writeInt(0x0E);
        mplew.writeShort(0x64); ////PVP 1.5 EVENT!
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] sendPyramidUpdate(int amount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.PYRAMID_UPDATE.getValue());
        mplew.writeInt(amount); //1-132 ?

        return mplew.getPacket();
    }

    public static byte[] sendPyramidResult(byte rank, int amount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.PYRAMID_RESULT.getValue());
        mplew.write(rank);
        mplew.writeInt(amount); //1-132 ?

        return mplew.getPacket();
    }

    //show_status_info - 01 53 1E 01
    //10/08/14/19/11
    //update_quest_info - 08 53 1E 00 00 00 00 00 00 00 00
    //show_status_info - 01 51 1E 01 01 00 30
    //update_quest_info - 08 51 1E 00 00 00 00 00 00 00 00
    public static byte[] sendPyramidEnergy(String type, String amount) {
        return sendString(1, type, amount);
    }

    public static byte[] sendString(int type, String object, String amount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        switch (type) {
            case 1:
                mplew.writeShort(OutHeader.ENERGY.getValue()); //武林道場會出現
                break;
            case 2:
                mplew.writeShort(OutHeader.GHOST_POINT.getValue()); //金字塔會出現
                break;
            case 3:
                mplew.writeShort(OutHeader.GHOST_STATUS.getValue()); //金字塔會出現
                break;
        }
        mplew.writeMapleAsciiString(object); //massacre_hit, massacre_cool, massacre_miss, massacre_party, massacre_laststage, massacre_skill
        mplew.writeMapleAsciiString(amount);

        return mplew.getPacket();
    }

    public static byte[] sendGhostPoint(String type, String amount) {
        return sendString(2, type, amount); //PRaid_Point (0-1500???)
    }

    public static byte[] sendGhostStatus(String type, String amount) {
        return sendString(3, type, amount); //Red_Stage(1-5), Blue_Stage, blueTeamDamage, redTeamDamage
    }

    public static byte[] MulungEnergy(int energy) {
        return sendPyramidEnergy("energy", String.valueOf(energy));
    }

    //    public static byte[] getPollQuestion() {
//        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
//
//        mplew.writeShort(OutHeader.GAME_POLL_QUESTION.getValue());
//        mplew.writeInt(1);
//        mplew.writeInt(14);
//        mplew.writeMapleAsciiString(ServerConstants.Poll_Question);
//        mplew.writeInt(ServerConstants.Poll_Answers.length); // pollcount
//        for (byte i = 0; i < ServerConstants.Poll_Answers.length; i++) {
//            mplew.writeMapleAsciiString(ServerConstants.Poll_Answers[i]);
//        }
//
//        return mplew.getPacket();
//    }
//
//    public static byte[] getPollReply(String message) {
//        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
//
//        mplew.writeShort(OutHeader.GAME_POLL_REPLY.getValue());
//        mplew.writeMapleAsciiString(message);
//
//        return mplew.getPacket();
//    }
    public static byte[] showEventInstructions() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_Desc.getValue());
        mplew.write(0);

        return mplew.getPacket();
    }

    /*
     * 打開商店搜索器 -- OK
     */
    public static byte[] getOwlOpen() { //best items! hardcoded
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ShopScannerResult.getValue());
        mplew.write(0x0A); //V.112修改 以前是0x09
        List<Integer> owlItems = RankingWorker.getItemSearch();
        mplew.write(owlItems.size());
        for (int i : owlItems) {
            mplew.writeInt(i);
        }

        return mplew.getPacket();
    }

    /*
     * 搜索的結果 - OK
     */
    public static byte[] getOwlSearched(int itemSearch, List<HiredMerchant> hms) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ShopScannerResult.getValue());
        mplew.write(0x09); //V.112修改 以前是0x08
        mplew.writeInt(0);
        mplew.writeShort(0); //V.112新增 未知
        mplew.writeInt(itemSearch); //要搜索的道具ID
        int size = 0;
        for (HiredMerchant hm : hms) {
            size += hm.searchItem(itemSearch).size();
        }
        mplew.writeInt(size);
        for (HiredMerchant hm : hms) {
            List<MaplePlayerShopItem> items = hm.searchItem(itemSearch);
            for (MaplePlayerShopItem item : items) {
                mplew.writeMapleAsciiString(hm.getOwnerName());
                mplew.writeInt(hm.getMap().getId());
                mplew.writeMapleAsciiString(hm.getDescription());
                mplew.writeInt(item.item.getQuantity()); //道具數量
                mplew.writeInt(item.bundles); //道具份數
                mplew.writeLong(item.price); //道具價格
                switch (InventoryHandler.OWL_ID) {
                    case 0:
                        mplew.writeInt(hm.getOwnerId()); //擁有者ID
                        break;
                    case 1:
                        mplew.writeInt(hm.getStoreId()); //保管的ID?
                        break;
                    default:
                        mplew.writeInt(hm.getObjectId()); //僱傭商人工具ID？
                        break;
                }
                mplew.write(hm.getChannel() - 1); //僱傭商店在幾頻道
                mplew.write(ItemConstants.getInventoryType(itemSearch, false).getType());
                if (ItemConstants.getInventoryType(itemSearch, false) == MapleInventoryType.EQUIP) {
                    PacketHelper.GW_ItemSlotBase_Encode(mplew, item.item);
                }
            }
        }
        return mplew.getPacket();
    }

    public static byte[] getRPSMode(byte mode, int mesos, int selection, int answer) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_RPSGame.getValue());
        mplew.write(mode);
        switch (mode) {
            case 6: { //not enough mesos
                if (mesos != -1) {
                    mplew.writeInt(mesos);
                }
                break;
            }
            case 8: { //open (npc)
                mplew.writeInt(9000019);
                break;
            }
            case 11: { //selection vs answer
                mplew.write(selection);
                mplew.write(answer); // FF = lose, or if selection = answer then lose ???
                break;
            }
        }
        return mplew.getPacket();
    }

    /*
     * 玩家請求跟隨
     */
    public static byte[] followRequest(int chrid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.FOLLOW_REQUEST.getValue());
        mplew.writeInt(chrid);

        return mplew.getPacket();
    }

    /*
     * 跟隨狀態
     */
    public static byte[] followEffect(int initiator, int replier, Point toMap) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserFollowCharacter.getValue());
        mplew.writeInt(initiator);
        mplew.writeInt(replier);
        if (replier == 0) { //cancel
            mplew.write(toMap == null ? 0 : 1); //1 -> x (int) y (int) to change map
            if (toMap != null) {
                mplew.writeInt(toMap.x);
                mplew.writeInt(toMap.y);
            }
        }
        return mplew.getPacket();
    }

    /*
     * 返回跟隨的信息
     */
    public static byte[] getFollowMsg(int opcode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserFollowCharacterFailed.getValue());
        /*
         * 0x01 = 當前位置無法接受跟隨請求
         * 0x05 = 拒絕跟隨請求
         */
        mplew.writeLong(opcode);

        return mplew.getPacket();
    }

    /*
     * 跟隨移動
     */
    public static byte[] moveFollow(int gatherDuration, int nVal1, Point otherStart, Point myStart, Point otherEnd, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserPassiveMove.getValue());
        PacketHelper.serializeMovementList(mplew, gatherDuration, nVal1, otherStart, myStart, moves, new int[]{
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                (byte) ((int) otherEnd.getX() & 0xFF), (byte) (((int) otherEnd.getX() >>> 8) & 0xFF), (byte) ((int) otherEnd.getY() & 0xFF), (byte) (((int) otherEnd.getY() >>> 8) & 0xFF),
                (byte) ((int) otherStart.getX() & 0xFF), (byte) (((int) otherStart.getX() >>> 8) & 0xFF), (byte) ((int) otherStart.getY() & 0xFF), (byte) (((int) otherStart.getY() >>> 8) & 0xFF)
        });

        return mplew.getPacket();
    }

    /*
     * 跟隨斷開的信息
     */
    public static byte[] getFollowMessage(String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserChatMsg.getValue());
        mplew.writeShort(0x0B); //?
        mplew.writeMapleAsciiString(msg); //white in gms, but msea just makes it pink.. waste
        return mplew.getPacket();
    }

    public static byte[] getMovingPlatforms(MapleMap map) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_MobOrderFromSvr.getValue());
        mplew.writeInt(map.getPlatforms().size());
        for (MaplePlatform mp : map.getPlatforms()) {
            mplew.writeMapleAsciiString(mp.name);
            mplew.writeInt(mp.start);
            mplew.writeInt(mp.SN.size());
            for (int x = 0; x < mp.SN.size(); x++) {
                mplew.writeInt(mp.SN.get(x));
            }
            mplew.writeInt(mp.speed);
            mplew.writeInt(mp.x1);
            mplew.writeInt(mp.x2);
            mplew.writeInt(mp.y1);
            mplew.writeInt(mp.y2);
            mplew.writeInt(mp.x1);//?
            mplew.writeInt(mp.y1);
            mplew.writeShort(mp.r);
        }
        return mplew.getPacket();
    }

    public static byte[] sendEngagementRequest(String name, int chrId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.ENGAGE_REQUEST.getValue());
        mplew.write(0); //mode, 0 = engage, 1 = cancel, 2 = answer.. etc
        mplew.writeMapleAsciiString(name); // name
        mplew.writeInt(chrId); // playerid

        return mplew.getPacket();
    }

    /*
     * 0x0D = 恭喜你訂婚成功.
     * 0x0E = 結婚成功.
     * 0x0F = 訂婚失敗.
     * 0x10 = 離婚成功.
     * 0x12 = 結婚典禮預約已經成功接受.
     * 0x15 = 該道具不能用於神之子  新增
     * 0x16 = 當前頻道、地圖找不到該角色或角色名錯誤.   以前0x15
     * 0x17 = 對方不在同一地圖. 以前0x16
     * 0x18 = 道具欄已滿.請整理其他窗口.    以前0x17
     * 0x19 = 對方的道具欄已滿. 以前0x18
     * 0x1A = 同性不能結婚. 以前0x19
     * 0x1B = 您已經是訂婚的狀態.   以前0x1A
     * 0x1C = 對方已經是訂婚的狀態. 以前0x1B
     * 0x1D = 您已經是結婚的狀態.   以前0x1C
     * 0x1E = 對方已經是結婚的狀態. 以前0x1D
     * 0x1F = 您處於不能求婚的狀態. 以前0x1E
     * 0x20 = 對方處於無法接受求婚的狀態.   以前0x1F
     * 0x21 = 很遺憾對方取消了您的求婚請求. 以前0x20
     * 0x22 = 對方鄭重地拒絕了您的求婚. 以前0x21
     * 0x23 = 已成功取消預約.請以後再試.    以前0x22
     * 0x24 = 預約後無法取消結婚典禮.   以前0x23
     * 0x24 = 無
     * 0x26 = 此請帖無效.   以前0x25
     */
    public static byte[] sendEngagement(byte msg, int item, MapleCharacter male, MapleCharacter female) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.ENGAGE_RESULT.getValue());
        mplew.write(msg); // 結婚任務 1103
        switch (msg) {
            case 0x0D:
            case 0x0E:
            case 0x14: {
                mplew.writeInt(0); // ringid or uniqueid
                mplew.writeInt(male.getId());
                mplew.writeInt(female.getId());
                mplew.writeShort(msg == 0x0E ? 0x03 : 0x01);
                mplew.writeInt(item);
                mplew.writeInt(item);
                mplew.writeAsciiString(male.getName(), 15);
                mplew.writeAsciiString(female.getName(), 15);
                break;
            }
            case 0x11: {
                mplew.writeMapleAsciiString(male.getName());
                mplew.writeMapleAsciiString(female.getName());
                mplew.writeShort(0);
                break;
            }
        }

        return mplew.getPacket();
    }

    /*
     * 美洲豹更新
     */
    public static byte[] updateJaguar(MapleCharacter from) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_WildHunterInfo.getValue());
        PacketHelper.addJaguarInfo(mplew, from);
        return mplew.getPacket();
    }

    public static byte[] teslaTriangle(int chrId, int sum1, int sum2, int sum3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserTeslaTriangle.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(sum1);
        mplew.writeInt(sum2);
        mplew.writeInt(sum3);

        return mplew.getPacket();
    }

    public static byte[] spawnMechDoor(MechDoor md, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_OpenGateCreated.getValue());
        mplew.write(animated ? 0 : 1);
        mplew.writeInt(md.getOwnerId());
        mplew.writePos(md.getPosition());
        mplew.write(md.getId());
        mplew.writeInt(md.getPartyId());

        return mplew.getPacket();
    }

    public static byte[] removeMechDoor(MechDoor md, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_OpenGateRemoved.getValue());
        mplew.write(animated ? 0 : 1);
        mplew.writeInt(md.getOwnerId());
        mplew.write(md.getId());

        return mplew.getPacket();
    }

    public static byte[] useSPReset(int chrId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SkillResetItemResult.getValue());
        mplew.write(1);
        mplew.writeInt(chrId);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] useAPReset(int chrId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_AbilityResetItemResult.getValue());
        mplew.write(1);
        mplew.writeInt(chrId);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] report(int err) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.REPORT_RESULT.getValue());
        mplew.write(err); //0 = success
        if (err == 2) {
            mplew.write(0);
            mplew.writeInt(1);
        }
        return mplew.getPacket();
    }

    public static byte[] getClock(int timesend) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CTX_Event_Field_Timer.getValue());
        mplew.write(2); // clock type. if you send 3 here you have to send another byte (which does not matter at all) before the timestamp
        mplew.writeInt(timesend);
        return mplew.getPacket();
    }

    public static byte[] getClockTime(int hour, int min, int sec) { // Current Time
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        /*
         * Recv CLOCK [00BE] (6)
         * [BE 00] [01] [0F] [0B] [0F]
         * ?....
         */
        mplew.writeShort(OutHeader.CTX_Event_Field_Timer.getValue());
        mplew.write(1); //Clock-Type
        mplew.write(hour);
        mplew.write(min);
        mplew.write(sec);

        return mplew.getPacket();
    }

    public static byte[] getClock3(final int n, final int n2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.CTX_Event_Field_Timer.getValue());
        mplew.write(3);
        mplew.write(1);
        mplew.writeInt(n2);
        return mplew.getPacket();
    }

    public static byte[] getClock40(final int n, final int n2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.CTX_Event_Field_Timer.getValue());
        mplew.write(40);
        mplew.writeInt(n2);
        mplew.writeInt(n);
        return mplew.getPacket();
    }

    public static byte[] setClockPause(final boolean pause, final int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.CTX_Event_Field_Timer.getValue());
        mplew.write(7);
        mplew.write(0);
        mplew.writeInt(duration);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] getClockMillis(final int millis) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.CTX_Event_Field_Timer.getValue());
        mplew.write(6);
        mplew.writeInt(millis);
        return mplew.getPacket();
    }

    public static byte[] StartClockEvent(final int passedSec, final int durationSec) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CTX_Event_Field_Timer.getValue());
        mplew.write(7);
        mplew.write(0);
        mplew.writeInt(durationSec - passedSec);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] getClockGiantBoss(final int duration, final int leftTime) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.CTX_Event_Field_Timer.getValue());
        mplew.write(103);
        mplew.writeInt(duration);
        mplew.writeInt(leftTime);

        return mplew.getPacket();
    }

    /*
     * 開啟舉報系統
     */
    public static byte[] enableReport() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort(OutHeader.LP_ClaimSvrStatusChanged.getValue());
        mplew.write(1);
        return mplew.getPacket();
    }

    /*
     * 舉報系統消息
     */
    public static byte[] reportResponse(byte mode, int remainingReports) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SetClaimSvrAvailableTime.getValue());
        mplew.writeShort(mode);
        if (mode == 2) {
            mplew.write(1);
            mplew.writeInt(remainingReports);
        }

        return mplew.getPacket();
    }

    public static byte[] pamSongUI() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.PAM_SONG.getValue());
        //mplew.writeInt(0); //no clue
        return mplew.getPacket();
    }

    public static byte[] showTraitGain(MapleTraitType trait, int amount) {
        MessageOption option = new MessageOption();
        MapleTraitType[] traitTypes = MapleTraitType.values();
        int[] data = new int[traitTypes.length];
        for (MapleTraitType traitType : traitTypes) {
            data[traitType.ordinal()] = traitType == trait ? amount : 0;
        }
        option.setIntegerData(data);
        return CWvsContext.sendMessage(MessageOpcode.MS_IncNonCombatStatEXPMessage, option);
    }

    public static byte[] showTraitMaxed(MapleTraitType trait) {
        MessageOption option = new MessageOption();
        option.setMask(trait.getStat().getValue());
        return CWvsContext.sendMessage(MessageOpcode.MS_LimitNonCombatStatEXPMessage, option);
    }

    /*
     * 採集的信息
     * 0x09 還無法採集。
     * 0x0B 開始採集
     */
    public static byte[] harvestMessage(int oid, MapleEnumClass.HarvestMsg msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_GatherRequestResult.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(msg.getCode());

        return mplew.getPacket();
    }

    public static byte[] showHarvesting(int chrId, int tool) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_GatherActionSet.getValue());
        mplew.writeInt(chrId);
        mplew.write(tool > 0 ? 1 : 0);
        if (tool > 0) {
            mplew.writeInt(tool);
            mplew.writeInt(0);
        }

        return mplew.getPacket();
    }

    public static byte[] harvestResult(int chrId, boolean success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserGatherResult.getValue());
        mplew.writeInt(chrId);
        mplew.write(success ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] makeExtractor(int chrId, String cname, Point pos, int timeLeft, int itemId, int fee) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_DecomposerEnterField.getValue());
        mplew.writeInt(chrId);
        mplew.writeMapleAsciiString(cname);
        mplew.writeInt(pos.x);
        mplew.writeInt(pos.y);
        mplew.writeShort(timeLeft); //fh or time left, dunno
        mplew.writeInt(itemId); //3049000, 3049001...
        mplew.writeInt(fee);

        return mplew.getPacket();
    }

    public static byte[] removeExtractor(int chrId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_DecomposerLeaveField.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(1); //probably 1 = animation, 2 = make something?

        return mplew.getPacket();
    }

    public static byte[] spouseMessage(UserChatMessageType getType, String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserChatMsg.getValue());
        mplew.writeShort(getType.getType());
        mplew.writeMapleAsciiString(msg);
        return mplew.getPacket();
    }

    public static byte[] multiLineMessage(UserChatMessageType type, String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserSetUtilDlg.getValue());
        mplew.writeShort(type.getType());
        mplew.writeMapleAsciiString(type.getMsg(msg));

        return mplew.getPacket();
    }

    /*
     * 打開礦物背包
     */
    public static byte[] openBag(int index, int itemId, boolean firstTime) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //[53 01] [00 00 00 00] [19 12 42 00] 00 00
        mplew.writeShort(OutHeader.LP_UserBagItemUseResult.getValue());
        mplew.writeInt(index);
        mplew.writeInt(itemId);
        mplew.writeShort(firstTime ? 1 : 0); //this might actually be 2 bytes

        return mplew.getPacket();
    }

    /*
     * 道具製造開始
     */
    public static byte[] craftMake(int chrId, int something, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        //[EC 00] [9E 4E 08 00] [7C 01 00 00] [A0 0F 00 00]
        mplew.writeShort(OutHeader.LP_UserSetOneTimeAction.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(something);
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    /*
     * 道具製作成功
     */
    public static byte[] craftFinished(int chrId, int craftID, int craftType, int ranking, int itemId, int quantity, int exp) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserMakingSkillResult.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(craftType);
        if (craftType == 1) {
            mplew.writeInt(ranking);
            mplew.writeBool(true);
            int n = 1;
            mplew.writeInt(n);
            while (n > 0) {
                mplew.writeInt(craftID);
                mplew.writeInt(itemId);
                mplew.writeInt(quantity);
                mplew.writeInt(0);
                mplew.writeInt(0);
                n--;
            }
            mplew.writeInt(exp);
        } else if (craftType == 2) {
            mplew.writeInt(craftID);
            mplew.writeInt(ranking);
            /*
             * 0x18	SOSO
             * 0x19	GOOD
             * 0x1A	COOL
             * 0x1B	FAIL	由於未知原因 製作道具失敗
             * 0x1C	FAIL	物品製作失敗.
             * 0x1D	FAIL	分解機已撤除，分解取消。
             * 0x1E	FAIL	分解機的主任無法繼續獲得手續費。
             */
            boolean success = ranking == 25 || ranking == 26 || ranking == 27;
            mplew.writeBool(success);//V.149 new
            if (success) { //只有製作成功才發送 製作出來的道具和數量
                mplew.writeInt(itemId);
                mplew.writeInt(quantity);
            }
            mplew.writeInt(exp);
        }
        return mplew.getPacket();
    }

    /*
     * 道具製作熟練度已滿的提示
     */
    public static byte[] craftMessage(String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserNoticeMsg.getValue());
        mplew.writeShort(18);
        mplew.writeAsciiString(msg);
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] showItemSkillSocketUpgradeEffect(int cid, boolean result) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserItemSkillSocketUpgradeEffect.getValue());
        mplew.writeInt(cid);
        mplew.writeBool(result);

        return mplew.getPacket();
    }

    public static byte[] showItemSkillOptionUpgradeEffect(int cid, boolean result, boolean destroyed, int itemID, short option) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserItemSkillOptionUpgradeEffect.getValue());
        mplew.writeInt(cid);
        mplew.writeBool(result);
        mplew.writeBool(destroyed);
        mplew.writeInt(itemID);
        mplew.writeInt(option);

        return mplew.getPacket();
    }

    public static byte[] shopDiscount(int percent) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SetPotionDiscountRate.getValue());
        mplew.write(percent);

        return mplew.getPacket();
    }

    public static byte[] pendantSlot(boolean p) { //slot -59
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SetBuyEquipExt.getValue());
        mplew.write(p ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] updatePendantSlot(boolean add, int days) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_PendantSlotIncResult.getValue());
        mplew.writeInt(add ? 1 : 0);
        mplew.writeInt(days);

        return mplew.getPacket();
    }

    public static byte[] getBuffBar(long millis) { //You can use the buff again _ seconds later. + bar above head
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_NotifyHPDecByField.getValue());
        mplew.writeLong(millis);

        return mplew.getPacket();
    }

    public static byte[] updateGender(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.UPDATE_GENDER.getValue());
        mplew.write(chr.getGender());

        return mplew.getPacket();
    }

    /*
     * 顯示副本進度
     */
    public static byte[] achievementRatio(int amount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SetAchieveRate.getValue()); //not sure
        mplew.writeInt(amount);

        return mplew.getPacket();
    }

    public static byte[] updateSpecialStat(String stat, int array, int mode, boolean unk, int chance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ResultInstanceTable.getValue());
        mplew.writeMapleAsciiString(stat);
        mplew.writeInt(array);
        mplew.writeInt(mode);
        mplew.write(unk ? 1 : 0);
        mplew.writeInt(chance);

        return mplew.getPacket();
    }

    public static byte[] getQuickSlot(MapleQuickSlot quickslot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_QuickslotMappedInit.getValue());
        quickslot.writeData(mplew);

        return mplew.getPacket();
    }

    public static byte[] updateImp(MapleImp imp, int mask, int index, boolean login) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ItemPotChange.getValue());
        mplew.write(login ? 0 : 1); //0 = unchanged, 1 = changed
        mplew.writeInt(index + 1);
        mplew.writeInt(mask);
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0) {
            Pair<Integer, Integer> i = MapleItemInformationProvider.getInstance().getPot(imp.getItemId());
            if (i == null) {
                return new byte[0];
            }
            mplew.writeInt(i.left);
            mplew.write(imp.getLevel()); //probably type
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.STATE.getValue()) != 0) {
            mplew.write(imp.getState());
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.FULLNESS.getValue()) != 0) {
            mplew.writeInt(imp.getFullness());
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.CLOSENESS.getValue()) != 0) {
            mplew.writeInt(imp.getCloseness());
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.CLOSENESS_LEFT.getValue()) != 0) {
            mplew.writeInt(1); //how much closeness is available to get right now
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.MINUTES_LEFT.getValue()) != 0) {
            mplew.writeInt(0); //how much mins till next closeness
            mplew.writeLong(0);
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.LEVEL.getValue()) != 0) {
            mplew.write(1); //k idk
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.FULLNESS_2.getValue()) != 0) {
            mplew.writeInt(imp.getFullness()); //idk
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.UPDATE_TIME.getValue()) != 0) {
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.CREATE_TIME.getValue()) != 0) {
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.AWAKE_TIME.getValue()) != 0) {
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.SLEEP_TIME.getValue()) != 0) {
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.MAX_CLOSENESS.getValue()) != 0) {
            mplew.writeInt(100); //max closeness available to be gotten
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.MAX_DELAY.getValue()) != 0) {
            mplew.writeInt(1000); //idk, 1260?
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.MAX_FULLNESS.getValue()) != 0) {
            mplew.writeInt(1000);
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.MAX_ALIVE.getValue()) != 0) {
            mplew.writeInt(1); //k ive no idea
        }
        if ((mask & ImpFlag.SUMMONED.getValue()) != 0 || (mask & ImpFlag.MAX_MINUTES.getValue()) != 0) {
            mplew.writeInt(10); //max minutes?
        }
        mplew.write(0); //or 1 then lifeID of affected pot, OR IS THIS 0x80000?

        return mplew.getPacket();
    }

    public static byte[] showStatusMessage(String info, String data) {
        MessageOption option = new MessageOption();
        option.setText(info);
        option.setText2(data);
        return CWvsContext.sendMessage(MessageOpcode.MS_PvPItemUseMessage, option);
    }

    public static byte[] changeTeam(int cid, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserPvPTeamChanged.getValue());
        mplew.writeInt(cid);
        mplew.write(type); //2?

        return mplew.getPacket();
    }

    /*
     * 顯示快速移動
     */
    public static byte[] setQuickMoveInfo(List<MapleQuickMove> quickMoves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SetQuickMoveInfo.getValue());
        if (quickMoves.size() <= 0) {
            mplew.write(0);
        } else {
            mplew.write(quickMoves.size());
            int i = 0;
            for (MapleQuickMove mqm : quickMoves) {
                mplew.writeInt(i++);
                mplew.writeInt(mqm.CLOSE_AFTER_CLICK ? 0 : mqm.NPC); //NPCid
                mplew.writeInt(mqm.VALUE); //NPC編號
                mplew.writeInt(mqm.MIN_LEVEL); //傳送需要的等級
                mplew.writeMapleAsciiString(mqm.DESC); //NPC功能介紹
                mplew.writeInt(0);
                mplew.writeMapleAsciiString(""); //NPC功能介紹
                mplew.writeLong(PacketHelper.getTime(-2)); //00 40 E0 FD 3B 37 4F 01
                mplew.writeLong(PacketHelper.getTime(-1)); //00 80 05 BB 46 E6 17 02
            }
        }
        return mplew.getPacket();
    }

    public static byte[] updateCardStack(int total) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_IncJudgementStack.getValue());
        mplew.write(1);// TMS 220
        mplew.write(total);

        return mplew.getPacket();
    }

    public static byte[] 美洲豹攻擊效果(int skillid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_JaguarSkill.getValue());
        mplew.writeInt(skillid);

        return mplew.getPacket();
    }

    public static byte[] openPantherAttack(boolean on) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_JaguarActive.getValue());
        mplew.writeBool(on);

        return mplew.getPacket();

    }

    public static byte[] showRedNotice(String msg) {
        MessageOption option = new MessageOption();
        option.setText(msg);
        return CWvsContext.sendMessage(MessageOpcode.MS_SystemMessage, option);
    }

    /* 怪物收藏訊息 */
    public static byte[] monsterBookMessage(String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ScriptProgressMessage.getValue());
        mplew.writeMapleAsciiString(msg);
        return mplew.getPacket();
    }

    public static byte[] sendloginSuccess() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LOGIN_SUCC.getValue());
        return mplew.getPacket();
    }

    public static byte[] showCharCash(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SetMaplePoint.getValue());
        mplew.writeInt(chr.getCSPoints(2));

        return mplew.getPacket();
    }

    public static byte[] showMiracleTime() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SetMiracleTime.getValue());
        long time = System.currentTimeMillis();
        mplew.writeLong(PacketHelper.getTime(time));
        mplew.writeLong(PacketHelper.getTime(time + 70000));
        mplew.writeInt(200);
        mplew.writeInt(0);
        mplew.writeInt(0); // 2735606022
        mplew.writeMapleAsciiString("");
        mplew.writeMapleAsciiString("夢幻方塊時間到了！！ 從下午4點到6點期間，只要使用商城方塊類商品的話，即會提升道具潛在能力值等級的機率唷！詳細內容請觀看官網公告。");

        return mplew.getPacket();
    }

    public static byte[] showPlayerCash(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SHOW_PLAYER_CASH.getValue());
        mplew.writeInt(chr.getCSPoints(1));
        mplew.writeInt(chr.getCSPoints(2));

        return mplew.getPacket();
    }

    public static byte[] playerCashUpdate(int mode, int toCharge, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.PLAYER_CASH_UPDATE.getValue());
        mplew.writeInt(mode);
        mplew.writeInt(toCharge == 1 ? chr.getCSPoints(1) : 0);
        mplew.writeInt(chr.getCSPoints(2));
        mplew.write(toCharge);
        mplew.write(0); //未知
        mplew.write(0); //未知
        return mplew.getPacket();
    }

    public static byte[] playerSoltUpdate(int itemid, int acash, int mpoints) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.SHOW_CHARSOLE.getValue());

        mplew.writeInt(itemid);
        mplew.writeInt(acash);
        mplew.writeInt(mpoints);
        mplew.write(1);
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static byte[] sendTestPacket(String test) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(HexTool.getByteArrayFromHexString(test));
        return mplew.getPacket();
    }

    /*
     * 傳授技能的提示
     */
    public static byte[] UpdateLinkSkillResult(int skillId, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.UPDATE_LINKSKILL_RESULT.getValue());
        mplew.writeInt(skillId); // [ skill id X4] [ mode X4] 00 00 00 00
        mplew.writeInt(mode);
        return mplew.getPacket();
    }

    public static final byte[] DeleteLinkSkillResult(Map<Integer, Integer> map) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.DELETE_LINKSKILL_RESULT.getValue());
        mplew.writeInt(map.size());
        for (Entry<Integer, Integer> entry : map.entrySet()) {
            mplew.writeInt(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        return mplew.getPacket();
    }

    public static final byte[] SetLinkSkillResult(int skillId, Pair<Integer, SkillEntry> skillinfo, int linkSkillId, int linkSkillLevel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SET_LINKSKILL_RESULT.getValue());
        PacketHelper.writeSonOfLinkedSkill(mplew, skillId, skillinfo);
        mplew.writeInt(linkSkillId);
        if (linkSkillId > 0) {
            mplew.writeInt(linkSkillLevel);
        }
        return mplew.getPacket();
    }

    public static byte[] getDojangRanking() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_DojangRanking.getValue());
        mplew.write(0);
        mplew.writeInt(239);
        List<Integer> list = Arrays.asList(0, 1, 2, 8);
        mplew.writeInt(list.size());
        for (int i : list) {
            mplew.write(i);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(i == 1 ? -1 : 0);
            mplew.writeInt(i == 1 ? -1 : 0);
            mplew.writeInt(i == 1 ? -1 : 0);
            mplew.writeInt(i == 1 ? -1 : 101);
            mplew.writeInt(i == 1 ? -1 : 0);
            mplew.writeInt(i == 1 ? -1 : 101);
        }
        mplew.writeInt(list.size());
        for (int i : list) {
            encodeDojangRanking(mplew, i, Collections.emptyList());
        }
        return mplew.getPacket();
    }

    private static void encodeDojangRanking(MaplePacketLittleEndianWriter p, int i, List<AvatarLook> looks) {
        p.write(i);
        p.writeInt(looks.size());
        for (int n = 0; n < looks.size(); n++) {
            p.writeInt(looks.get(n).getJob());
            p.writeInt(1); // 等級
            p.writeInt(1400); // 30321
            p.writeInt(n + 1);
            p.writeMapleAsciiString(""); // 名稱
            p.write(1);
            p.write(looks.get(n).getPackedCharacterLook());
        }
    }

    /*
     * 顯示武林道場消息
     */
    public static byte[] getMulungMessage(boolean dc, String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.MULUNG_MESSAGE.getValue());
        mplew.write(dc ? 1 : 0);
        mplew.writeMapleAsciiString(msg);

        return mplew.getPacket();
    }

    //    public static byte[] showSilentCrusadeMsg(byte type, short chapter) {
//        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
//
//        mplew.writeShort(OutHeader.SILENT_CRUSADE_MSG.getValue());
//        mplew.write(type);
//        mplew.writeShort(chapter - 1);
//
//        return mplew.getPacket();
//    }
    /*
     * 確認十字商店交易
     */
    public static byte[] confirmCrossHunter(byte code) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_CrossHunterCompleteResult.getValue());
        /*
         * 0x00 物品購買完成。
         * 0x01 道具不夠.
         * 0x02 背包空間不足。
         * 0x03 無法擁有更多物品。
         * 0x04 現在無法購買物品。
         */
        mplew.write(code);

        return mplew.getPacket();
    }

    /*
     * 打開1個網頁地址
     */
    public static byte[] openWeb(byte nValue1, byte nValue2, String web) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserOpenURL.getValue());
        mplew.write(nValue1);
        mplew.write(nValue2);
        mplew.writeMapleAsciiString(web);

        return mplew.getPacket();
    }

    public static byte[] openWebUI(int n, String sUOL, String sURL) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserOpenWebUI.getValue());
        mplew.writeInt(n);
        mplew.writeMapleAsciiString(sUOL);
        mplew.writeMapleAsciiString(sURL);

        return mplew.getPacket();
    }

    public static byte[] updateInnerSkill(MapleCharacter player, InnerSkillEntry ise1, InnerSkillEntry ise2, InnerSkillEntry ise3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        // 封包頭與固定欄位 (依照原有封包格式)
        mplew.writeShort(OutHeader.LP_CharacterPotentialSet.getValue());
        mplew.write(1);
        mplew.write(0);
        mplew.write(3);
        mplew.write(0);

        InnerSkillEntry[] skills = { ise1, ise2, ise3 };
        for (InnerSkillEntry ise : skills) {
            if (ise != null) {
                mplew.write(ise.getPosition());
                mplew.writeInt(ise.getSkillId());
                mplew.write(ise.getSkillLevel());
                mplew.write(ise.getRank());
            }
        }
        try (Connection con = DatabaseConnectionEx.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "REPLACE INTO innerskills (skillid, characterid, skilllevel, position, rank) VALUES (?, ?, ?, ?, ?)")) {
                for (InnerSkillEntry ise : skills) {
                    if (ise != null) {
                        ps.setInt(1, ise.getSkillId());
                        ps.setInt(2, player.getId());
                        ps.setInt(3, ise.getSkillLevel());
                        ps.setInt(4, ise.getPosition());
                        ps.setInt(5, ise.getRank());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mplew.getPacket();
    }




    private static int getRandomSkillIdByRank(int rank) {
        switch (rank) {
            case 0:
                return Randomizer.rand(70000000, 70000013);
            case 1:
                return Randomizer.rand(70000014, 70000026);
            case 2:
                return Randomizer.rand(70000027, 70000039);
            case 3:
                return Randomizer.rand(70000041, 70000062);
            default:
                throw new IllegalArgumentException("Unknown rank: " + rank);
        }
    }

    private static int writeSkillEntry(MaplePacketLittleEndianWriter mplew, InnerSkillEntry entry, int skillId, int index) {
        int skillLevel;
        switch (entry.getRank()) {
            case 0:
                skillLevel = Randomizer.rand(1, 20);
                break;
            case 1:
                skillLevel = Randomizer.rand(10, 20);
                break;
            case 2:
                skillLevel = Randomizer.rand(20, 30);
                break;
            case 3:
                skillLevel = Randomizer.rand(30, 40);
                break;
            default:
                throw new IllegalArgumentException("Unknown rank: " + entry.getRank());
        }

        mplew.write(index);
        mplew.writeInt(skillId);
        mplew.write(skillLevel);
        mplew.write(entry.getRank());
        return skillLevel;
    }

    /*
     * 更新角色內在能力
     * 參數 角色
     * 參數 是否升級
     */
    public static byte[] updateInnerStats(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CTX_SHOW_CHARACTER_HONOR_POINT.getValue());
        mplew.writeInt(chr.getHonor()); //聲望點數
        return mplew.getPacket();
    }

    /*
     * 系統警告
     * 楓之谷運營員NPC自定義對話
     */
    public static byte[] sendPolice(String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.MAPLE_ADMIN.getValue());
        mplew.writeMapleAsciiString(text);

        return mplew.getPacket();
    }

    public static byte[] testPacket(String testmsg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(HexTool.getByteArrayFromHexString(testmsg));

        return mplew.getPacket();
    }

    public static byte[] testPacket(byte[] testmsg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(testmsg);

        return mplew.getPacket();
    }

    public static byte[] testPacket(String op, String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(HexTool.getByteArrayFromHexString(op));
        mplew.writeMapleAsciiString(text);

        return mplew.getPacket();
    }

    /*
     * 幻影 封印之瞳 - v261-4
     */
    public static byte[] ResultStealSkillList(int n, MapleCharacter chr, List<Integer> memorySkills) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ResultStealSkillList.getValue());
        mplew.write(1);
        mplew.writeInt(chr.getId());
        mplew.writeInt(n);
        mplew.writeInt(chr.getJob());
        mplew.writeInt(memorySkills.size());
        for (int skill : memorySkills) {
            mplew.writeInt(skill);
        }
        return mplew.getPacket();
    }

    /*
     * Recv SKILL_MEMORY [002E] (12)
     * 2E 00
     * 01
     * 03
     * 01 00 00 00 - 技能在第個欄
     * 01 00 00 00 - 技能在當前欄的位置
     * ............
     */
    public static byte[] 幻影刪除技能(int skillBook, int position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ChangeStealMemoryResult.getValue());
        mplew.write(1);
        mplew.write(3);
        mplew.writeInt(skillBook);
        mplew.writeInt(position);

        return mplew.getPacket();
    }

    public static byte[] 修改幻影裝備技能(int skillId, int teachId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ResultSetStealSkill.getValue());
        mplew.write(1);
        mplew.write(1);
        mplew.writeInt(skillId);
        mplew.writeInt(teachId);

        return mplew.getPacket();
    }

    public static byte[] 幻影複製錯誤() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ChangeStealMemoryResult.getValue());
        mplew.write(1);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] 幻影複製技能(int position, int skillId, int level) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ChangeStealMemoryResult.getValue());
        mplew.write(1);
        mplew.write(0);
        if (position < 4) {
            mplew.writeInt(1);
            mplew.writeInt(position);
        } else if (position < 8) {
            mplew.writeInt(2);
            mplew.writeInt(position - 4);
        } else if (position < 11) {
            mplew.writeInt(3);
            mplew.writeInt(position - 8);
        } else if (position < 13) {
            mplew.writeInt(4);
            mplew.writeInt(position - 11);
        } else if (position < 15) {
            mplew.writeInt(5);
            mplew.writeInt(position - 13);
        }
        mplew.writeInt(skillId);
        mplew.writeInt(level);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    /*
     * 未知封包 右鍵點擊玩家出現的返回封包
     * 好像不發送申請交易的一方就無法交易中放道具
     */
    public static byte[] CheckTrickOrTreatRequest() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserTrickOrTreatResult.getValue());
        mplew.writeInt(7);
        return mplew.getPacket();
    }

    public static byte[] SystemProcess() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_CheckProcess.getValue());

        mplew.writeHexString("00 00 00 00 01");
        // 總標題長度
        mplew.writeShort(10);
        // 總標題（不顯示）
        mplew.writeHexString("B7 AC A4 A7 A8 A6 AC A1 B0 CA 00");
        mplew.writeHexString("00 00 00 00");
        boolean trigger = false;
        try {
            File file = new File("config/EventList.json");
            if (file.exists()) {
                ObjectMapper objectMapper = new ObjectMapper();
                Map jsonMap = objectMapper.readValue(file, Map.class);
                assert jsonMap.get("eventList") instanceof List;
                List<Map<String, Object>> eventList = (List<Map<String, Object>>) jsonMap.get("eventList");
                int count = 0;
                if (!eventList.isEmpty()) {
                    // 活動數量
                    mplew.writeInt(eventList.size());
                    for(Map<String, Object> eventMap : eventList) {
                        String title = eventMap.get("name").toString();
                        Charset big5Charset = Charset.forName("Big5");
                        int len = title.getBytes(big5Charset).length;
                        // 活動id
                        mplew.write(count);
                        mplew.writeHexString("01 00 00");
                        // title內容長度
                        mplew.writeShort(len);
                        // title內容
                        mplew.write(title.getBytes(big5Charset));
                        mplew.writeZeroBytes(2);
                        // 開始時間
                        mplew.writeInt(eventMap.get("timeStart") instanceof Number?((Number) eventMap.get("timeStart")).intValue():0);
                        // 結束時間
                        mplew.writeInt(eventMap.get("timeEnd") instanceof Number?((Number) eventMap.get("timeEnd")).intValue():0);
                        // 開始日期
                        mplew.writeInt(eventMap.get("dateStart") instanceof Number?((Number) eventMap.get("dateStart")).intValue():0);
                        // 結束日期
                        mplew.writeInt(eventMap.get("dateEnd") instanceof Number?((Number) eventMap.get("dateEnd")).intValue():0);
                        mplew.writeHexString("00 00 00 00 00 00 00 00 00 00 00 00");
                        mplew.writeHexString("01");

                        List<Object> rewards = (List<Object>) eventMap.get("rewards");
                        if (rewards != null && !rewards.isEmpty()) {
                            // 道具數量
                            mplew.writeInt(rewards.size());
                            for(Object reward : rewards){
                                // 道具id
                                mplew.writeInt(reward instanceof Number?((Number) reward).intValue():2000005);
                            }
                        }else{
                            // 道具數量
                            mplew.writeInt(0);
                        }
                        mplew.writeHexString("00 00 00 00 00 00");
                        trigger = true;
                        count++;
                    }
                }
            }else{
                System.out.println("未讀取EventList.json配置, 活動列表將使用預設值");
            }
        } catch (Exception e) {
            System.out.println("讀取活動列表配置(EventList.json)錯誤，活動列表將使用預設值。錯誤訊息：" + e);
        }

        if (!trigger) {
            mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(OutHeader.LP_CheckProcess.getValue());

            mplew.writeHexString("00 00 00 00 01");
            // 總標題長度
            mplew.writeShort(10);
            // 總標題（不顯示）
            mplew.writeHexString("B7 AC A4 A7 A8 A6 AC A1 B0 CA 00");
            mplew.writeHexString("00 00 00 00");
            mplew.writeInt(1);
            String title = "[TEST] 測試";
            Charset big5Charset = Charset.forName("Big5");
            int len = title.getBytes(big5Charset).length;

            // 活動id
            mplew.write(1);
            mplew.writeHexString("01 00 00");
            // title內容長度
            mplew.writeShort(len);
            // title內容
            mplew.write(title.getBytes(big5Charset));
            mplew.writeZeroBytes(2);
            // 開始時間
            mplew.writeInt(0);
            // 結束時間
            mplew.writeInt(235900);
            // 開始日期
            mplew.writeInt(20240911);
            // 結束日期
            mplew.writeInt(20240912);
            mplew.writeHexString("00 00 00 00 00 00 00 00 00 00 00 00");
            mplew.writeHexString("01");
            // 道具數量
            mplew.writeInt(1);
            // 道具id
            mplew.writeInt(2000005);
            mplew.writeHexString("00 00 00 00 00 00");
        }

        // 特征碼：00 00 00 00 00 00 1A 00 00 00 7E
        // 從1A 00 00 00 7E開始往後面貼 都是週日的活動
        mplew.writeHexString("1A 00 00 00 7E 01 00 00 15 00 BD FC B8 67 C5 E7 AD C8 A5 5B AB F9 AE C4 AA 47 2B 31 30 30 25 00 00 00 00 11 00 00 00 C8 00 00 00 E8 17 34 01 E8 17 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 17 00 BD FC B8 67 C5 E7 AD C8 A5 5B AB F9 AE C4 AA 47 20 2B 31 30 30 25 21 00 00 00 00 26 00 B3 73 C4 F2 C0 BB B1 FE 43 6F 6D 62 6F 6B 69 6C 6C AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 17 00 00 00 02 00 00 00 E8 17 34 01 E8 17 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 04 00 00 00 05 00 00 00 00 00 00 00 EF 17 34 01 EF 17 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 10 00 00 00 E8 03 00 00 0B 00 00 00 E8 03 00 00 06 00 00 00 E8 03 00 00 00 00 00 00 00 00 89 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F 35 AC 50 A1 41 31 30 AC 50 A1 41 31 35 AC 50 23 6B B6 A5 AC 71 A4 57 B9 C1 B8 D5 B1 6A A4 C6 AE C9 A6 A8 A5 5C BE F7 B2 76 31 30 30 25 A1 49 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 AB 44 AC A1 B0 CA B9 EF B6 48 29 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 05 00 00 00 04 00 00 00 32 00 00 00 EF 17 34 01 EF 17 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 35 30 25 00 00 00 00 10 00 00 00 68 01 00 00 F6 17 34 01 F6 17 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 BD FC B8 67 C5 E7 AD C8 A5 5B AB F9 AB F9 C4 F2 AE C9 B6 A1 BC 57 A5 5B 32 AD BF A1 49 00 00 00 00 19 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 13 00 00 00 0B 00 00 00 00 00 00 00 F6 17 34 01 F6 17 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 04 00 00 00 04 00 00 00 00 00 00 00 FD 17 34 01 FD 17 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 AC 50 A4 4F B1 6A A4 C6 B6 4F A5 CE 20 33 30 25 20 A7 E9 A6 A9 96 00 23 65 AC 50 B4 C1 A4 D1 AD 6E B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E 20 AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 23 6B 20 B6 4F A5 CE 20 33 30 25 20 A7 E9 A6 A9 23 6E 0D 0A 28 A8 BE C3 7A B6 4F A5 CE A9 4D B4 4C B6 51 B8 CB B3 C6 B7 7C B1 71 AC A1 B0 CA B9 EF B6 48 B0 A3 A5 7E A1 41 4D 56 50 AC 4F A6 62 A4 77 A7 E9 A6 A9 20 33 30 25 A4 57 B0 6C A5 5B AE 4D A5 CE 29 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1D 00 00 00 02 00 00 00 FD 17 34 01 FD 17 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 C2 49 BC C6 31 A4 D1 C0 F2 B1 6F 32 AD BF 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 A9 3A 34 01 A9 3A 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 C2 49 BC C6 31 A4 D1 C0 F2 B1 6F 32 AD BF 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 17 00 00 00 02 00 00 00 A9 3A 34 01 A9 3A 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 C2 49 BC C6 31 A4 D1 C0 F2 B1 6F 32 AD BF 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 C8 00 00 00 B0 3A 34 01 B0 3A 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 C2 49 BC C6 31 A4 D1 C0 F2 B1 6F 32 AD BF 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 B7 3A 34 01 B7 3A 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 C2 49 BC C6 31 A4 D1 C0 F2 B1 6F 32 AD BF 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 C8 00 00 00 B7 3A 34 01 B7 3A 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 C2 49 BC C6 31 A4 D1 C0 F2 B1 6F 32 AD BF 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 06 00 00 00 04 00 00 00 32 00 00 00 BE 3A 34 01 BE 3A 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 C2 49 BC C6 31 A4 D1 C0 F2 B1 6F 32 AD BF 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 04 00 00 00 28 00 00 00 01 00 00 00 0A 3B 34 01 0A 3B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 00 00 0E 00 C2 49 BC C6 31 A4 D1 C0 F2 B1 6F 32 AD BF 7F 00 23 65 AC 50 B4 C1 A4 D1 B7 ED B5 4D B4 4E AC 4F B7 AC A4 A7 A8 A6 A1 49 20 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA 21 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 31 30 AC 50 A5 48 A4 55 AC 50 A4 4F B1 6A A4 C6 A6 A8 A5 5C AE C9 31 2B 31 B1 6A A4 C6 A1 49 23 6B 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 B1 C6 B0 A3 A6 62 AC A1 B0 CA B9 EF B6 48 A4 A7 A5 7E 29 00 00 13 00 AC 50 A4 4F 31 30 AC 50 A5 48 A4 55 B1 6A A4 C6 31 2B 31 13 00 00 00 0B 00 00 00 00 00 00 00 0A 3B 34 01 0A 3B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1C 00 A9 C7 AA AB A6 AC C2 C3 B5 6E BF FD B7 73 A9 C7 AA AB BE F7 B2 76 20 32 AD BF A1 49 00 00 00 00 15 00 BD FC B8 67 C5 E7 AD C8 A5 5B AB F9 AE C4 AA 47 2B 31 30 30 25 14 00 00 00 1D 00 00 00 02 00 00 00 11 3B 34 01 11 3B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 B6 F8 B3 4E A4 A7 AA 65 A8 43 A4 E9 BC FA C0 79 32 AD BF A1 49 00 00 00 00 26 00 B3 73 C4 F2 C0 BB B1 FE 43 6F 6D 62 6F 6B 69 6C 6C AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 07 00 00 00 29 00 00 00 81 A3 07 00 18 3B 34 01 18 3B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 27 00 3F 3F 3F 20 3F 3F 3F 20 3F 3F 2C 20 3F 3F 3F 20 3F 3F 3F 20 3F 3F 20 35 3F 20 3F 3F 20 3F 3F 3F 20 31 3F 20 3F 3F 21 00 00 00 00 19 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 1B 00 00 00 63 00 00 00 18 3B 34 01 18 3B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 04 00 00 00 04 00 00 00 00 00 00 00 1F 3B 34 01 1F 3B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 C2 49 BC C6 31 A4 D1 C0 F2 B1 6F 32 AD BF 96 00 23 65 AC 50 B4 C1 A4 D1 AD 6E B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E 20 AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 23 6B 20 B6 4F A5 CE 20 33 30 25 20 A7 E9 A6 A9 23 6E 0D 0A 28 A8 BE C3 7A B6 4F A5 CE A9 4D B4 4C B6 51 B8 CB B3 C6 B7 7C B1 71 AC A1 B0 CA B9 EF B6 48 B0 A3 A5 7E A1 41 4D 56 50 AC 4F A6 62 A4 77 A7 E9 A6 A9 20 33 30 25 A4 57 B0 6C A5 5B AE 4D A5 CE 29 00 00 1B 00 AC 50 A4 4F 35 A1 42 31 30 A1 42 31 35 AC 50 31 30 30 25 B1 6A A4 C6 A6 A8 A5 5C 03 00 00 00 04 00 00 00 32 00 00 00 74 3B 34 01 75 3B 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 8F 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F 35 AC 50 A1 42 31 30 AC 50 A1 42 31 35 AC 50 A1 41 A6 62 23 6B B9 C1 B8 D5 B1 6A A4 C6 AE C9 A1 41 A6 A8 A5 5C BE F7 B2 76 31 30 30 25 A1 49 23 6E 0D 0A A1 5D B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 A1 5E 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 04 00 00 00 05 00 00 00 00 00 00 00 7B 3B 34 01 7B 3B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00");

        mplew.writeHexString("FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 10 00 00 00 E8 03 00 00 0B 00 00 00 E8 03 00 00 06 00 00 00 E8 03 00 00 00 00 00 00 00 00 89 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F 35 AC 50 A1 41 31 30 AC 50 A1 41 31 35 AC 50 23 6B B6 A5 AC 71 A4 57 B9 C1 B8 D5 B1 6A A4 C6 AE C9 A6 A8 A5 5C BE F7 B2 76 31 30 30 25 A1 49 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 AB 44 AC A1 B0 CA B9 EF B6 48 29 00 00 19 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 35 30 25 05 00 00 00 04 00 00 00 32 00 00 00 7B 3B 34 01 7B 3B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 B0 B6 A4 6A AA BA C6 46 BB EE C0 F2 B1 6F BE F7 B2 76 35 AD BF 13 00 00 00 0B 00 00 00 00 00 00 00 82 3B 34 01 82 3B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 B0 B6 A4 6A C6 46 BB EE C0 F2 B1 6F B2 76 35 AD BF A1 49 00 00 00 00 1A 00 AF 50 BF 56 BE D4 AF 54 B0 68 B3 F5 BC FA C0 79 B8 67 C5 E7 AD C8 A8 E2 AD BF 14 00 00 00 22 00 00 00 C8 00 00 00 89 3B 34 01 89 3B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 D5 3B 34 01 D5 3B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 17 00 BD FC B8 67 C5 E7 AD C8 A5 5B AB F9 AE C4 AA 47 20 2B 31 30 30 25 21 00 00 00 00 15 00 BD FC B8 67 C5 E7 AD C8 A5 5B AB F9 AE C4 AA 47 2B 31 30 30 25 13 00 00 00 0B 00 00 00 00 00 00 00 D5 3B 34 01 D5 3B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1C 00 A9 C7 AA AB A6 AC C2 C3 B5 6E BF FD B7 73 A9 C7 AA AB BE F7 B2 76 20 32 AD BF A1 49 00 00 00 00 2A 00 B3 73 C4 F2 C0 BB B1 FE 43 6F 6D 62 6F 6B 69 6C 6C AF 5D A4 6C AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 04 00 00 00 04 00 00 00 00 00 00 00 DC 3B 34 01 DC 3B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 17 00 AC 50 A4 4F B1 6A A4 C6 B6 4F A5 CE 20 33 30 25 20 A7 E9 A6 A9 A1 49 96 00 23 65 AC 50 B4 C1 A4 D1 AD 6E B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E 20 AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 23 6B 20 B6 4F A5 CE 20 33 30 25 20 A7 E9 A6 A9 23 6E 0D 0A 28 A8 BE C3 7A B6 4F A5 CE A9 4D B4 4C B6 51 B8 CB B3 C6 B7 7C B1 71 AC A1 B0 CA B9 EF B6 48 B0 A3 A5 7E A1 41 4D 56 50 AC 4F A6 62 A4 77 A7 E9 A6 A9 20 33 30 25 A4 57 B0 6C A5 5B AE 4D A5 CE 29 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 05 00 00 00 04 00 00 00 32 00 00 00 DC 3B 34 01 DC 3B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1D 00 00 00 02 00 00 00 E3 3B 34 01 E3 3B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 B6 F8 B3 4E A4 A7 AA 65 A8 43 A4 E9 BC FA C0 79 32 AD BF A1 49 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 03 00 00 00 04 00 00 00 32 00 00 00 EA 3B 34 01 EA 3B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 59 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 35 A7 E9 A1 49 23 6B 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 06 00 00 00 04 00 00 00 32 00 00 00 37 3C 34 01 37 3C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1F 00 A8 A4 A6 E2 A4 BA BC E7 AD AB B7 73 B3 5D A9 77 B6 4F A5 CE 20 35 30 25 20 A7 E9 A6 A9 A1 49 00 00 00 00 13 00 AC 50 A4 4F 31 30 AC 50 A5 48 A4 55 B1 6A A4 C6 31 2B 31 14 00 00 00 1E 00 00 00 C8 00 00 00 3E 3C 34 01 3E 3C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 48 00 43 6F 6D 62 6F 20 6B 69 6C 6C 20 B2 79 B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 20 2B 33 30 30 25 A1 49 5C 72 5C 6E 20 AA 69 C3 B9 BB 50 B4 B6 A8 BD A6 AB AA BA BD E0 AA F7 C2 79 A4 48 B8 67 C5 E7 AD C8 20 32 AD BF A1 49 00 00 00 00 19 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 35 30 25 00 00 00 00 19 00 00 00 0A 00 00 00 41 3C 34 01 4E 3C 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 12 00 00 00 C0 27 09 00 41 3C 34 01 4E 3C 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 13 00 00 00 C0 27 09 00 41 3C 34 01 4E 3C 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 BD FC B8 67 C5 E7 AD C8 A5 5B AB F9 AE C4 AA 47 2B 31 30 30 25 00 00 00 00 12 00 5B 3F 3F 3F 3F 5D 20 3F 20 3F 3F 3F 20 3F 3F 20 3F 3F 00 00 00 00 11 00 00 00 C8 00 00 00 41 3C 34 01 4E 3C 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 26 00 B3 73 C4 F2 C0 BB B1 FE 43 6F 6D 62 6F 6B 69 6C 6C AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 13 00 AC 50 A4 4F B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 33 30 25 00 00 00 00 0D 00 00 00 06 00 00 00 41 3C 34 01 4E 3C 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 B0 B6 A4 6A AA BA C6 46 BB EE C0 F2 B1 6F BE F7 B2 76 35 AD BF 00 00 00 00 23 00 00 00 E8 03 00 00 41 3C 34 01 4E 3C 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 17 00 B0 B6 A4 6A AA BA C6 46 BB EE C0 F2 B1 6F BE F7 B2 76 35 AD BF A1 49 00 00 00 00 12 00 5B 3F 3F 3F 3F 5D 20 3F 3F 3F 3F 20 3F 3F 3F 20 3F 3F 14 00 00 00 22 00 00 00 96 00 00 00 41 3C 34 01 4E 3C 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 17 00 5B 3F 3F 3F 3F 5D 20 3F 3F 3F 3F 20 3F 3F 20 3F 3F 20 3F 3F 20 3F 3F 08 00 00 00 21 00 00 00 1E 00 00 00 41 3C 34 01 4E 3C 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 17 00 00 00 02 00 00 00 45 3C 34 01 45 3C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 BD FC B8 67 C5 E7 AD C8 A5 5B AB F9 AE C4 AA 47 2B 31 30 30 25 03 00 00 00 04 00 00 00 32 00 00 00 9F 3C 34 01 9F 3C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 59 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 35 A7 E9 A1 49 23 6B 00 00 26 00 B3 73 C4 F2 C0 BB B1 FE 43 6F 6D 62 6F 6B 69 6C 6C AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 13 00 00 00 0B 00 00 00 00 00 00 00 A6 3C 34 01 A6 3C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1C 00 A9 C7 AA AB A6 AC C2 C3 B5 6E BF FD B7 73 A9 C7 AA AB BE F7 B2 76 20 32 AD BF A1 49 00 00 00 00 19 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 A6 3C 34 01 A6 3C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 05 00 00 00 04 00 00 00 32 00 00 00 AD 3C 34 01 AD 3C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 35 30 25 03 00 00 00 04 00 00 00 32 00 00 00 B4 3C 34 01 B4 3C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 5D 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 C0 75 B4 66 35 30 25 A1 49 23 6B 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 13 00 00 00 0B 00 00 00 00 00 00 00 01 3D 34 01 01 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 B0 B6 A4 6A AA BA C6 46 BB EE C0 F2 B1 6F BE F7 B2 76 35 AD BF 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 C8 00 00 00 01 3D 34 01 01 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 C2 49 BC C6 31 A4 D1 C0 F2 B1 6F 32 AD BF 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 06 00 00 00 04 00 00 00 32 00 00 00 08 3D 34 01 08 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1F 00 A8 A4 A6 E2 A4 BA BC E7 AD AB B7 73 B3 5D A9 77 B6 4F A5 CE 20 35 30 25 20 A7 E9 A6 A9 A1 49 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 0E 00 00 00 2C 01 00 00 0F 3D 34 01 0F 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 AC F0 B5 6F A5 F4 B0 C8 A5 69 A7 B9 A6 A8 A6 B8 BC C6 A1 41 B8 67 C5 E7 AD C8 20 33 AD BF A1 49 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 0D 00 00 00 09 00 00 00 0F 3D 34 01 0F 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 C2 49 BC C6 31 A4 D1 C0 F2 B1 6F 32 AD BF 00 00 00 00 1B 00 AC 50 A4 4F 35 A1 42 31 30 A1 42 31 35 AC 50 31 30 30 25 B1 6A A4 C6 A6 A8 A5 5C 00 00 00 00 1B 00 00 00 63 00 00 00 0F 3D 34 01 0F 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 24 00 A8 43 20 39 39 20 43 6F 6D 62 6F B7 7C B0 6C A5 5B B2 A3 A5 58 20 43 6F 6D 62 6F 20 6B 69 6C 6C B2 79 A1 49 00 00 00 00 19 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 35 30 25 04 00 00 00 28 00 00 00 01 00 00 00 16 3D 34 01 16 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 00 00 23 00 A6 62 31 30 AC 50 A5 48 A4 55 A6 A8 A5 5C AC 50 A4 4F B1 6A A4 C6 AE C9 20 31 2B 31 20 B1 6A A4 C6 A1 49 5D 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 C0 75 B4 66 35 30 25 A1 49 23 6B 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 17 00 00 00 02 00 00 00 62 3D 34 01 62 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 C2 49 BC C6 31 A4 D1 C0 F2 B1 6F 32 AD BF 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1D 00 00 00 02 00 00 00 62 3D 34 01 62 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 C2 49 BC C6 31 A4 D1 C0 F2 B1 6F 32 AD BF 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 04 00 00 00 09 00 00 00 00 00 00 00 AF 3C 34 01 65 3D 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 0C 00 00 00 E8 03 00 00 0D 00 00 00 E8 03 00 00 0E 00 00 00 E8 03 00 00 0F 00 00 00 E8 03 00 00 00 00 00 00 0E 00 C2 49 BC C6 31 A4 D1 C0 F2 B1 6F 32 AD BF 00 00 00 00 19 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 00 00 00 00 27 00 00 00 C8 00 00 00 B4 3C 34 01 B4 3C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 27 00 00 00 C8 00 00 00 01 3D 34 01 01 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 27 00 00 00 C8 00 00 00 08 3D 34 01 08 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 00 00 00 00 27 00 00 00 C8 00 00 00 0F 3D 34 01 0F 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 00 00 00 00 27 00 00 00 C8 00 00 00 16 3D 34 01 16 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 00 00 00 00 27 00 00 00 C8 00 00 00 62 3D 34 01 62 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 03 00 00 00 04 00 00 00 32 00 00 00 69 3D 34 01 69 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 59 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 35 A7 E9 A1 49 23 6B 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 77 3D 34 01 77 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 ");

        mplew.writeHexString("FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 21 00 43 6F 6D 62 6F 20 6B 69 6C 6C 20 B2 79 B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 20 2B 33 30 30 25 A1 49 00 00 00 00 1B 00 AC 50 A4 4F 35 A1 42 31 30 A1 42 31 35 AC 50 31 30 30 25 B1 6A A4 C6 A6 A8 A5 5C 04 00 00 00 05 00 00 00 00 00 00 00 70 3D 34 01 70 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 10 00 00 00 E8 03 00 00 0B 00 00 00 E8 03 00 00 06 00 00 00 E8 03 00 00 00 00 00 00 00 00 89 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F 35 AC 50 A1 41 31 30 AC 50 A1 41 31 35 AC 50 23 6B B6 A5 AC 71 A4 57 B9 C1 B8 D5 B1 6A A4 C6 AE C9 A6 A8 A5 5C BE F7 B2 76 31 30 30 25 A1 49 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 AB 44 AC A1 B0 CA B9 EF B6 48 29 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 C8 00 00 00 70 3D 34 01 70 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 26 00 B3 73 C4 F2 C0 BB B1 FE 43 6F 6D 62 6F 6B 69 6C 6C AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 15 00 AC 50 A4 4F 31 30 AC 50 A5 48 A4 55 A1 41 31 2B 31 B1 6A A4 C6 00 00 00 00 11 00 00 00 C8 00 00 00 77 3D 34 01 77 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 17 00 BD FC B8 67 C5 E7 AD C8 A5 5B AB F9 AE C4 AA 47 20 2B 31 30 30 25 21 00 00 00 00 19 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 35 30 25 00 00 00 00 0E 00 00 00 2C 01 00 00 CA 3D 34 01 CA 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 AC F0 B5 6F A5 F4 B0 C8 A5 69 A7 B9 A6 A8 A6 B8 BC C6 A1 41 B8 67 C5 E7 AD C8 20 33 AD BF A1 49 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 0D 00 00 00 09 00 00 00 CA 3D 34 01 CA 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 AC F0 B5 6F A5 F4 B0 C8 A5 69 A7 B9 A6 A8 A6 B8 BC C6 A1 41 B8 67 C5 E7 AD C8 20 33 AD BF A1 49 00 00 00 00 1C 00 AC 50 A4 4F 20 35 A1 42 31 30 A1 42 31 35 AC 50 31 30 30 25 B1 6A A4 C6 A6 A8 A5 5C 00 00 00 00 1B 00 00 00 63 00 00 00 D1 3D 34 01 D1 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 24 00 A8 43 20 39 39 20 43 6F 6D 62 6F B7 7C B0 6C A5 5B B2 A3 A5 58 20 43 6F 6D 62 6F 20 6B 69 6C 6C B2 79 A1 49 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 17 00 00 00 02 00 00 00 D1 3D 34 01 D1 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 16 00 BD FC B8 67 C5 E7 AD C8 A5 5B AB F9 AE C4 AA 47 20 2B 31 30 30 25 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 05 00 00 00 04 00 00 00 32 00 00 00 D8 3D 34 01 D8 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1F 00 5B A5 5B B3 74 AC A1 B0 CA 5D 20 BA EB AD 5E A9 C7 AA AB B5 6E B3 F5 BE F7 B2 76 BC 57 A5 5B 04 00 00 00 04 00 00 00 00 00 00 00 DF 3D 34 01 DF 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 AC 50 A4 4F B1 6A A4 C6 B6 4F A5 CE 20 33 30 25 20 A7 E9 A6 A9 96 00 23 65 AC 50 B4 C1 A4 D1 AD 6E B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E 20 AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 23 6B 20 B6 4F A5 CE 20 33 30 25 20 A7 E9 A6 A9 23 6E 0D 0A 28 A8 BE C3 7A B6 4F A5 CE A9 4D B4 4C B6 51 B8 CB B3 C6 B7 7C B1 71 AC A1 B0 CA B9 EF B6 48 B0 A3 A5 7E A1 41 4D 56 50 AC 4F A6 62 A4 77 A7 E9 A6 A9 20 33 30 25 A4 57 B0 6C A5 5B AE 4D A5 CE 29 00 00 1D 00 5B A5 5B B3 74 AC A1 B0 CA 5D 20 BD FC A6 41 A6 B8 B5 6E B3 F5 AE C9 B6 A1 C1 59 B5 75 14 00 00 00 1D 00 00 00 02 00 00 00 DF 3D 34 01 DF 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 B6 F8 B3 4E A4 A7 AA 65 A8 43 A4 E9 BC FA C0 79 32 AD BF A1 49 00 00 00 00 19 00 5B A5 5B B3 74 AC A1 B0 CA 5D 20 BD FC A7 4E AB 6F AE C9 B6 A1 B4 EE A4 D6 03 00 00 00 04 00 00 00 32 00 00 00 2C 3E 34 01 2C 3E 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 59 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 35 A7 E9 A1 49 23 6B 00 00 1B 00 5B A5 5B B3 74 AC A1 B0 CA 5D 20 BD FC B8 67 C5 E7 AD C8 AE C4 AA 47 BC 57 A5 5B 08 00 00 00 21 00 00 00 1E 00 00 00 33 3E 34 01 33 3E 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1F 00 5B A5 5B B3 74 AC A1 B0 CA 5D 20 AC F0 B5 6F A5 F4 B0 C8 A8 43 A4 E9 A6 B8 BC C6 BC 57 A5 5B 04 00 00 00 28 00 00 00 01 00 00 00 33 3E 34 01 33 3E 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 00 00 00 00 7F 00 23 65 AC 50 B4 C1 A4 D1 B7 ED B5 4D B4 4E AC 4F B7 AC A4 A7 A8 A6 A1 49 20 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA 21 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 31 30 AC 50 A5 48 A4 55 AC 50 A4 4F B1 6A A4 C6 A6 A8 A5 5C AE C9 31 2B 31 B1 6A A4 C6 A1 49 23 6B 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 B1 C6 B0 A3 A6 62 AC A1 B0 CA B9 EF B6 48 A4 A7 A5 7E 29 00 00 23 00 5B A5 5B B3 74 AC A1 B0 CA 5D 20 AA 69 C3 B9 BB 50 B4 B6 A8 BD A6 AB BE F7 B2 76 B6 7D A9 6C B4 EE A4 D6 06 00 00 00 04 00 00 00 32 00 00 00 3A 3E 34 01 3A 3E 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1F 00 A8 A4 A6 E2 A4 BA BC E7 AD AB B7 73 B3 5D A9 77 B6 4F A5 CE 20 35 30 25 20 A7 E9 A6 A9 A1 49 00 00 00 00 1D 00 5B A5 5B B3 74 AC A1 B0 CA 5D 20 AF 50 B5 4B BE D4 AF 54 B8 67 C5 E7 AD C8 BC 57 A5 5B 00 00 00 00 10 00 00 00 68 01 00 00 8D 3E 34 01 8E 3E 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 09 00 64 65 73 63 20 74 65 73 74 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 8D 3E 34 01 8E 3E 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 09 00 64 65 73 63 20 74 65 73 74 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 0D 00 00 00 09 00 00 00 94 3E 34 01 95 3E 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 16 00 BD FC B8 67 C5 E7 AD C8 A5 5B AB F9 AE C4 AA 47 20 2B 31 30 30 25 0C 00 6D 65 73 73 61 67 65 20 74 65 73 74 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 0E 00 00 00 2C 01 00 00 94 3E 34 01 95 3E 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 09 00 64 65 73 63 20 74 65 73 74 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 04 00 00 00 28 00 00 00 01 00 00 00 9B 3E 34 01 9C 3E 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 00 00 09 00 64 65 73 63 20 74 65 73 74 48 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 5C 72 5C 6E 31 30 AC 50 A5 48 A4 55 AC 50 A4 4F 31 2B 31 B1 6A A4 C6 A1 49 5C 6E B0 74 B5 6F AF A6 AA 6B B2 C5 A4 E5 33 30 AD D3 A1 49 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 04 00 00 00 04 00 00 00 00 00 00 00 A2 3E 34 01 A3 3E 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 09 00 64 65 73 63 20 74 65 73 74 5C 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 5C 72 5C 6E AC 50 A4 4F A7 E9 A6 A9 33 30 25 A1 49 5C 6E BF 55 BF 4E A6 61 B9 CF B3 CC A6 68 31 35 B6 A5 AC 71 2F AB EC B4 5F B3 74 AB D7 B4 A3 A4 C9 2F AB F9 C4 F2 AE C9 B6 A1 BC 57 A5 5B A1 49 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 16 00 00 00 0F 00 00 00 A2 3E 34 01 A3 3E 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 09 00 64 65 73 63 20 74 65 73 74 00 00 00 00 15 00 B0 B6 A4 6A AA BA C6 46 BB EE C0 F2 B1 6F BE F7 B2 76 35 AD BF 00 00 00 00 15 00 00 00 80 4F 12 00 A2 3E 34 01 A3 3E 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 09 00 64 65 73 63 20 74 65 73 74 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 14 00 00 00 40 77 1B 00 A2 3E 34 01 A3 3E 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 09 00 64 65 73 63 20 74 65 73 74 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 03 00 00 00 04 00 00 00 32 00 00 00 A9 3E 34 01 AA 3E 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1E 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 20 2B 33 30 30 25 52 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 5C 72 5C 6E A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE A7 E9 A6 A9 35 30 25 A1 49 5C 6E A9 47 A4 E5 AA BA B2 AA B8 F1 46 45 56 45 52 20 54 49 4D 45 A1 49 00 00 13 00 AC 50 A4 4F 31 30 AC 50 A5 48 A4 55 B1 6A A4 C6 31 2B 31 04 00 00 00 09 00 00 00 00 00 00 00 97 3E 34 01 F1 3E 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 0C 00 00 00 E8 03 00 00 0D 00 00 00 E8 03 00 00 0E 00 00 00 E8 03 00 00 0F 00 00 00 E8 03 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 F6 3E 34 01 F6 3E 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 C8 00 00 00 F6 3E 34 01 F6 3E 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 04 00 00 00 05 00 00 00 00 00 00 00 FD 3E 34 01 FD 3E 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 10 00 00 00 E8 03 00 00 0B 00 00 00 E8 03 00 00 06 00 00 00 E8 03 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 05 00 00 00 04 00 00 00 32 00 00 00 FD 3E 34 01 FD 3E 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 35 30 25 14 00 00 00 1D 00 00 00 02 00 00 00 0B 3F 34 01 0B 3F 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 03 00 00 00 04 00 00 00 32 00 00 00 B7 61 34 01 B7 61 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1C 00 AC 50 A4 4F 20 35 A1 42 31 30 A1 42 31 35 AC 50 B1 6A A4 C6 A6 A8 A5 5C 31 30 30 25 00 00 00 00 10 00 00 00 68 01 00 00 BE 61 34 01 BE 61 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 BE 61 34 01 BE 61 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 14 00 A5 AC B5 DC BA B8 A4 70 A9 6A AA BA B9 DA A4 DB A6 76 B0 74 00 00 00 00 19 00 00 00 0A 00 00 00 50 63 34 01 5D 63 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 32 00 AD 59 A6 B3 B4 5B AD 6E B2 BE B0 CA AA BA B2 7B AA F7 B9 44 A8 E3 A1 41 BD D0 A7 51 A5 CE B5 DC BA B8 A4 70 A9 6A AA BA B9 DA A4 A7 A7 D6 B1 B6 A1 49 00 00 00 00 1C 00 5B AB E6 B3 74 AC A1 B0 CA 5D BD FC A6 41 A6 B8 B5 6E B3 F5 AE C9 B6 A1 C1 59 B5 75 00 00 00 00 12 00 00 00 C0 27 09 00 50 63 34 01 5D 63 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1E 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 20 2B 33 30 30 25 00 00 00 00 18 00 5B AB E6 B3 74 AC A1 B0 CA 5D BD FC A7 4E AB 6F AE C9 B6 A1 B4 EE A4 D6 00 00 00 00 13 00 00 00 C0 27 09 00 50 63 34 01 5D 63 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 16 00 BD FC B8 67 C5 E7 AD C8 A5 5B AB F9 AE C4 AA 47 20 2B 31 30 30 25 00 00 00 00 1A 00 5B AB E6 B3 74 AC A1 B0 CA 5D BD FC B8 67 C5 E7 AD C8 AE C4 AA 47 BC 57 A5 5B 00 00 00 00 11 00 00 00 C8 00 00 00 50 63 34 01 5D 63 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1E 00 5B AB E6 B3 74 AC A1 B0 CA 5D AC F0 B5 6F A5 F4 B0 C8 A8 43 A4 E9 A6 B8 BC C6 BC 57 A5 5B 00 00 00 00 0D 00 00 00 06 00 00 00 50 63 34 01 5D 63 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 22 00 5B AB E6 B3 74 AC A1 B0 CA 5D AA 69 C3 B9 BB 50 B4 B6 A8 BD A6 AB BE F7 B2 76 B6 7D A9 6C B4 EE A4 D6 00 00 00 00 23 00 00 00 F4 01 00 00 50 63 34 01 5D 63 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1C 00 5B AB E6 B3 74 AC A1 B0 CA 5D AF 50 B5 4B BE D4 AF 54 B8 67 C5 E7 AD C8 BC 57 A5 5B 14 00 00 00 22 00 00 00 96 00 00 00 50 63 34 01 5D 63 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 22 00 5B AB E6 B3 74 AC A1 B0 CA 5D A2 D0 A2 DD A2 E1 A2 E1 B2 D5 B6 A4 A5 5B AB F9 AE C4 AA 47 BC 57 A5 5B 08 00 00 00 21 00 00 00 1E 00 00 00 50 63 34 01 5D 63 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1E 00 AC 50 A4 4F 20 35 A1 42 31 30 A1 42 31 35 AC 50 20 31 30 30 25 20 B1 6A A4 C6 A6 A8 A5 5C 00 00 00 00 11 00 00 00 C8 00 00 00 B4 3C 34 01 B4 3C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 14 00 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE A7 E9 A6 A9 20 35 30 25 00 00 00 00 25 00 00 00 2C 01 00 00 B4 3C 34 01 B4 3C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE A7 E9 A6 A9 20 35 30 25 14 00 00 00 1E 00 00 00 C8 00 00 00 01 3D 34 01 01 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 12 00 B5 DC BA B8 A4 70 A9 6A AA BA B9 DA A4 A7 A7 D6 B1 B6 14 00 00 00 22 00 00 00 C8 00 00 00 01 3D 34 01 01 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 34 00 AD 59 A6 B3 B7 51 AD 6E B2 BE B0 CA AA BA B2 7B AA F7 B9 44 A8 E3 A1 41 BD D0 A7 51 A5 CE A5 AC B5 DC BA B8 A4 70 A9 6A AA BA B9 DA A4 DB A6 76 B0 74 A1 49 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 03 00 00 00 04 00 00 00 32 00 00 00 08 3D 34 01 08 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 9E 00 23 65 AC 50 B4 C1 A4 E9 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C B6 67 A4 E9 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 B6 4F A5 CE 23 6B A7 E9 A6 A9 33 30 25 23 6E 0D 0A 28 A8 BE C3 7A B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 A4 A3 A5 5D A7 74 A6 62 AC A1 B0 CA B9 EF B6 48 A4 BA A1 41 4D 56 50 2F BA F4 A9 40 C0 75 B4 66 B1 4E B0 6C A5 5B AE 4D A5 CE 33 30 25 AA BA A7 E9 A6 A9 29 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 06 00 00 00 04 00 00 00 32 00 00 00 08 3D 34 01 08 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 18 00 5B B6 67 A4 E9 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 E9 C0 75 B4 66 A1 49 04 00 00 00 04 00 00 00 00 00 00 00 0F 3D 34 01 0F 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 AA 00 23 65 AC 50 B4 C1 A4 E9 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C B6 67 A4 E9 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 23 6B B6 4F A5 CE C0 75 B4 66 33 30 25 23 6E 0D 0A 28 A8 BE A4 EE AF 7D C3 61 B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 A1 43 B0 A3 AD EC A6 B3 33 30 25 C0 75 B4 66 BB F9 AE E6 A5 7E A1 41 4D 56 50 2F BA F4 A9 40 C0 75 B4 66 A5 69 A6 41 C3 42 A5 7E BE 41 A5 CE 29 00 00 18 00 5B B6 67 A4 E9 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 E9 C0 75 B4 66 A1 49 00 00 00 00 27 00 00 00 C8 00 00 00 B4 3C 34 01 B4 3C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 18 00 5B B6 67 A4 E9 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 E9 C0 75 B4 66 A1 49 00 00 00 00 27 00 00 00 C8 00 00 00 01 3D 34 01 01 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 14 00 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE A7 E9 A6 A9 20 35 30 25 00 00 00 00 27 00 00 00 C8 00 00 00 08 3D 34 01 08 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 16 00 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 20 35 30 25 A1 49 00 00 00 00 15 00 AC 50 A4 4F 20 31 30 AC 50 A5 48 A4 55 B1 6A A4 C6 20 31 2B 31 00 00 00 00 27 00 00 00 C8 00 00 00 0F 3D 34 01 0F 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1A 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE A7 E9 A6 A9 20 35 30 25 00 00 00 00 27 00 00 00 C8 00 00 00 16 3D 34 01 16 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 15 00 B0 B6 A4 6A AA BA C6 46 BB EE C0 F2 B1 6F BE F7 B2 76 35 AD BF 00 00 00 00 27 00 00 00 C8 00 00 00 62 3D 34 01 62 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 17 00 B0 B6 A4 6A AA BA C6 46 BB EE C0 F2 B1 6F BE F7 B2 76 35 AD BF A1 49 00 00 00 00 18 00 5B B6 67 A4 E9 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 E9 C0 75 B4 66 A1 49 00 00 00 00 27 00 00 00 C8 00 00 00 69 3D 34 01 69 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 12 00 B5 DC BA B8 A4 70 A9 6A AA BA B9 DA A4 A7 A7 D6 B1 B6 00 00 00 00 27 00 00 00 C8 00 00 00 70 3D 34 01 70 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 32 00 AD 59 A6 B3 B7 51 AD 6E B2 BE B0 CA AA BA B2 7B AA F7 B9 44 A8 E3 A1 41 BD D0 A7 51 A5 CE B5 DC BA B8 A4 70 A9 6A AA BA B9 DA A4 A7 A7 D6 B1 B6 A1 49 00 00 00 00 20 00 AC 50 A4 4F 20 35 AC 50 A1 42 31 30 AC 50 A1 42 31 35 AC 50 B1 6A A4 C6 31 30 30 25 A6 A8 A5 5C 04 00 00 00 28 00 00 00 01 00 00 00 62 3D 34 01 62 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 00 00 00 00 81 00 23 65 AC 50 B4 C1 A4 E9 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C B6 67 A4 E9 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F 20 35 AC 50 A1 42 31 30 AC 50 A1 42 31 35 AC 50 23 6B B1 6A A4 C6 AE C9 A6 A8 A5 5C B2 76 20 31 30 30 25 A1 49 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 29 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 69 3D 34 01 69 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 69 3D 34 01 69 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 06 00 00 00 04 00 00 00 32 00 00 00 70 3D 34 01 70 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 BC E7 AF E0 AD AB B3 5D B6 4F A5 CE 35 30 25 A7 E9 A6 A9 A1 49 00 00 00 00 18 00 5B B6 67 A4 E9 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 E9 C0 75 B4 66 A1 49 00 00 00 00 27 00 00 00 C8 00 00 00 77 3D 34 01 77 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 04 00 00 00 05 00 00 00 00 00 00 00 77 3D 34 01 77 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 10 00 00 00 E8 03 00 00 0B 00 00 00 E8 03 00 00 06 00 00 00 E8 03 00 00 00 00 00 00 00 00 9C 00 23 65 AC 50 B4 C1 A4 E9 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C B6 67 A4 E9 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 B6 4F A5 CE 23 6B A7 E9 A6 A9 33 30 25 23 6E 0D 0A 28 A8 BE C3 7A B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 A4 A3 A5 5D A7 74 A6 62 AC A1 B0 CA B9 EF B6 48 A4 BA A1 41 4D 56 50 2F BA F4 A9 40 C0 75 B4 66 B1 4E B0 6C A5 5B AE 4D A5 CE 33 30 25 AA BA A7 E9 A6 A9 29 00 00 1A 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE A7 E9 A6 A9 20 35 30 25 00 00 00 00 27 00 00 00 C8 00 00 00 7E 3D 34 01 7E 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 14 00 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE A7 E9 A6 A9 20 35 30 25 03 00 00 00 04 00 00 00 32 00 00 00 7E 3D 34 01 7E 3D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 5B 00 23 65 AC 50 B4 C1 A4 E9 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C B6 67 A4 E9 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 BB 79 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 C0 75 B4 66 35 30 25 A1 49 23 6B 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 25 00 00 00 2C 01 00 00 D4 88 34 01 D4 88 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 14 00 3F 3F 3F 20 3F 3F 20 3F 3F 3F 20 3F 3F 3F 20 2B 33 30 30 25 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 14 00 00 00 22 00 00 00 C8 00 00 00 D4 88 34 01 D4 88 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 3F 3F 3F 3F 20 3F 3F 20 3F 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 04 00 00 00 28 00 00 00 01 00 00 00 DB 88 34 01 DB 88 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 00 00 12 00 3F 3F 3F 3F 20 31 30 3F 20 3F 3F 20 31 2B 31 20 3F 3F 82 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 20 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 31 30 AC 50 A5 48 A4 55 AC 50 A4 4F B1 6A A4 C6 A6 A8 A5 5C AE C9 A1 41 31 2B 31 B1 6A A4 C6 A1 49 23 6B 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 B1 C6 B0 A3 A6 62 AC A1 B0 CA B9 EF B6 48 A4 A7 A5 7E 29 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 10 00 00 00 68 01 00 00 DB 88 34 01 DB 88 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 09 00 3F 20 3F 3F 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 03 00 00 00 04 00 00 00 32 00 00 00 E2 88 34 01 E2 88 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 3F 3F 3F 20 3F 3F 20 3F 3F 20 3F 3F 20 35 30 25 20 3F 3F 5E 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 20 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 BB 79 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 C0 75 B4 66 35 30 25 A1 49 23 6B 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 13 00 00 00 0B 00 00 00 64 00 00 00 2E 89 34 01 2E 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 3F 3F 3F 3F 3F 3F 20 3F 3F 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 17 00 00 00 02 00 00 00 2E 89 34 01 2E 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 3F 3F 3F 20 3F 3F 3F 20 32 3F 3F 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 16 00 00 00 0F 00 00 00 35 89 34 01 35 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 3F 3F 3F 3F 20 3F 3F 20 31 35 3F 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 14 00 00 00 40 77 1B 00 35 89 34 01 35 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 3F 3F 3F 3F 20 3F 3F 3F 3F 20 3F 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 15 00 00 00 80 4F 12 00 35 89 34 01 35 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 3F 3F 3F 3F 20 3F 3F 3F 3F 20 3F 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 14 00 00 00 1D 00 00 00 02 00 00 00 3C 89 34 01 3C 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 11 00 3F 3F 3F 3F 3F 20 3F 3F 3F 3F 3F 20 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 11 00 00 00 C8 00 00 00 43 89 34 01 43 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 11 00 3F 20 3F 3F 3F 20 3F 3F 20 3F 3F 20 2B 31 30 30 25 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 1B 00 00 00 63 00 00 00 43 89 34 01 43 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 10 00 39 39 3F 3F 3F 3F 20 3F 3F 3F 3F 20 3F 3F 3F 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 04 00 00 00 05 00 00 00 00 00 00 00 92 89 34 01 92 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 10 00 00 00 E8 03 00 00 0B 00 00 00 E8 03 00 00 06 00 00 00 E8 03 00 00 00 00 00 00 1A 00 3F 3F 3F 3F 20 35 2C 20 31 30 2C 20 31 35 3F 20 31 30 30 25 20 3F 3F 20 3F 3F 8F 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F 35 AC 50 A1 42 31 30 AC 50 A1 42 31 35 AC 50 A1 41 A6 62 23 6B B9 C1 B8 D5 B1 6A A4 C6 AE C9 A1 41 A6 A8 A5 5C BE F7 B2 76 31 30 30 25 A1 49 23 6E 0D 0A A1 5D B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 A1 5E 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 04 00 00 00 04 00 00 00 00 00 00 00 92 89 34 01 92 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0B 00 3F 3F 3F 3F 20 33 30 25 20 3F 3F AE 00 23 65 AC 50 B4 C1 A4 E9 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 23 6B B6 4F A5 CE C0 75 B4 66 33 30 25 23 6E 0D 0A A1 5D A8 BE A4 EE AF 7D C3 61 B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 A1 43 B0 A3 AD EC A6 B3 33 30 25 C0 75 B4 66 BB F9 AE E6 A5 7E A1 41 4D 56 50 2F BA F4 A9 40 C0 75 B4 66 A5 69 A6 41 C3 42 A5 7E BE 41 A5 CE A1 5E 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 0D 00 00 00 09 00 00 00 99 89 34 01 99 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0D 00 3F 3F 3F 3F 20 3F 3F 20 3F 3F 20 3F 3F 5A 00 23 65 AC 50 B4 C1 A4 E9 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 20 3C B6 67 A4 E9 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC F0 B5 6F A5 F4 B0 C8 A8 43 A4 E9 A5 69 A7 B9 A6 A8 A6 B8 BC C6 33 AD BF 23 6B 23 6E 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 0E 00 00 00 2C 01 00 00 99 89 34 01 99 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0F 00 3F 3F 3F 3F 20 3F 3F 3F 20 3F 3F 3F 20 33 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 14 00 00 00 22 00 00 00 C8 00 00 00 99 89 34 01 99 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 3F 3F 3F 3F 20 3F 3F 20 3F 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 03 00 00 00 04 00 00 00 32 00 00 00 A0 89 34 01 A0 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 3F 3F 3F 20 3F 3F 20 3F 3F 20 3F 3F 20 35 30 25 20 3F 3F 5E 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 20 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 BB 79 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 C0 75 B4 66 35 30 25 A1 49 23 6B 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 06 00 00 00 04 00 00 00 32 00 00 00 A7 89 34 01 A7 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 3F 3F 3F 3F 20 3F 3F 3F 20 3F 3F 20 35 30 25 20 3F 3F 21 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 16 00 00 00 0F 00 00 00 F3 89 34 01 F3 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 3F 3F 3F 3F 20 3F 3F 20 31 35 3F 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 14 00 00 00 40 77 1B 00 F3 89 34 01 F3 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 3F 3F 3F 3F 20 3F 3F 3F 3F 20 3F 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 15 00 00 00 80 4F 12 00 F3 89 34 01 F3 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 3F 3F 3F 3F 20 3F 3F 3F 3F 20 3F 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 13 00 00 00 0B 00 00 00 64 00 00 00 F3 89 34 01 F3 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 3F 3F 3F 3F 3F 3F 20 3F 3F 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 05 00 00 00 04 00 00 00 32 00 00 00 FA 89 34 01 FA 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 10 00 3F 3F 3F 3F 20 3F 3F 3F 3F 20 35 30 25 20 3F 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 17 00 00 00 02 00 00 00 01 8A 34 01 01 8A 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 3F 3F 3F 20 3F 3F 3F 20 32 3F 3F 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 04 00 00 00 28 00 00 00 01 00 00 00 08 8A 34 01 08 8A 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 00 00 12 00 3F 3F 3F 3F 20 31 30 3F 20 3F 3F 20 31 2B 31 20 3F 3F 82 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 20 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 31 30 AC 50 A5 48 A4 55 AC 50 A4 4F B1 6A A4 C6 A6 A8 A5 5C AE C9 A1 41 31 2B 31 B1 6A A4 C6 A1 49 23 6B 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 B1 C6 B0 A3 A6 62 AC A1 B0 CA B9 EF B6 48 A4 A7 A5 7E 29 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 11 00 00 00 C8 00 00 00 08 8A 34 01 08 8A 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 11 00 3F 20 3F 3F 3F 20 3F 3F 20 3F 3F 20 2B 31 30 30 25 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 03 00 00 00 04 00 00 00 32 00 00 00 55 8A 34 01 55 8A 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 3F 3F 3F 20 3F 3F 20 3F 3F 20 3F 3F 20 35 30 25 20 3F 3F 5E 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 20 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 BB 79 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 C0 75 B4 66 35 30 25 A1 49 23 6B 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 25 00 00 00 2C 01 00 00 5C 8A 34 01 5C 8A 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 14 00 3F 3F 3F 20 3F 3F 20 3F 3F 3F 20 3F 3F 3F 20 2B 33 30 30 25 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 14 00 00 00 22 00 00 00 C8 00 00 00 5C 8A 34 01 5C 8A 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 3F 3F 3F 3F 20 3F 3F 20 3F 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 27 00 00 00 C8 00 00 00 FA 89 34 01 FA 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 3F 3F 3F 20 31 3F 20 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 27 00 00 00 C8 00 00 00 01 8A 34 01 01 8A 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 3F 3F 3F 20 31 3F 20 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 27 00 00 00 C8 00 00 00 08 8A 34 01 08 8A 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 3F 3F 3F 20 31 3F 20 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 27 00 00 00 C8 00 00 00 55 8A 34 01 55 8A 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 3F 3F 3F 20 31 3F 20 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 27 00 00 00 C8 00 00 00 5C 8A 34 01 5C 8A 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 3F 3F 3F 20 31 3F 20 3F 3F 20 32 3F 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 04 00 00 00 04 00 00 00 00 00 00 00 C6 88 34 01 C6 88 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 AC 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 23 6B B6 4F A5 CE C0 75 B4 66 33 30 25 23 6E 0D 0A 28 A8 BE A4 EE AF 7D C3 61 B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 A1 43 B0 A3 AD EC A6 B3 33 30 25 C0 75 B4 66 BB F9 AE E6 A5 7E A1 41 4D 56 50 2F BA F4 A9 40 C0 75 B4 66 A5 69 A6 41 C3 42 A5 7E BE 41 A5 CE 29 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 C8 00 00 00 C6 88 34 01 C6 88 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 C8 00 00 00 C6 88 34 01 C6 88 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 35 30 25 03 00 00 00 04 00 00 00 32 00 00 00 D4 88 34 01 D4 88 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 5D 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 C0 75 B4 66 35 30 25 A1 49 23 6B 00 00 17 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 06 00 00 00 04 00 00 00 32 00 00 00 D4 88 34 01 D4 88 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 14 00 A5 AC B5 DC BA B8 A4 70 A9 6A AA BA B9 DA A4 DB A7 D6 BB BC 18 00 00 00 2C 00 00 00 01 00 00 00 41 89 34 01 8D 89 34 01 A0 86 01 00 A0 86 01 00 00 00 00 00 FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 34 00 A6 70 AA 47 A6 B3 B7 51 B2 BE B0 CA AA BA B2 7B AA F7 B9 44 A8 E3 A1 41 BD D0 A7 51 A5 CE A5 AC B5 DC BA B8 A4 70 A9 6A AA BA B9 DA A4 DB A7 D6 BB BC A1 49 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 E2 88 34 01 E2 88 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 E2 88 34 01 E2 88 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 BD FC B8 67 C5 E7 AD C8 42 75 66 66 AE C4 AA 47 2B 31 30 30 25 00 00 00 00 1B 00 AC 50 A4 4F 35 A1 42 31 30 A1 42 31 35 AC 50 31 30 30 25 B1 6A A4 C6 A6 A8 A5 5C 04 00 00 00 05 00 00 00 00 00 00 00 2E 89 34 01 2E 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 10 00 00 00 E8 03 00 00 0B 00 00 00 E8 03 00 00 06 00 00 00 E8 03 00 00 00 00 00 00 00 00 89 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F 35 AC 50 A1 42 31 30 AC 50 A1 42 31 35 AC 50 23 6B A6 62 B9 C1 B8 D5 B1 6A A4 C6 AE C9 A6 A8 A5 5C BE F7 B2 76 31 30 30 25 A1 49 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 29 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 04 00 00 00 04 00 00 00 00 00 00 00 2E 89 34 01 2E 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 AC 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 23 6B B6 4F A5 CE C0 75 B4 66 33 30 25 23 6E 0D 0A 28 A8 BE A4 EE AF 7D C3 61 B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 A1 43 B0 A3 AD EC A6 B3 33 30 25 C0 75 B4 66 BB F9 AE E6 A5 7E A1 41 4D 56 50 2F BA F4 A9 40 C0 75 B4 66 A5 69 A6 41 C3 42 A5 7E BE 41 A5 CE 29 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 C8 00 00 00 35 89 34 01 35 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 C8 00 00 00 35 89 34 01 35 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 AC 50 A4 4F 35 A1 42 31 30 A1 42 31 35 AC 50 31 30 30 25 B1 6A A4 C6 A6 A8 A5 5C 04 00 00 00 05 00 00 00 00 00 00 00 43 89 34 01 43 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 10 00 00 00 E8 03 00 00 0B 00 00 00 E8 03 00 00 06 00 00 00 E8 03 00 00 00 00 00 00 00 00 89 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F 35 AC 50 A1 42 31 30 AC 50 A1 42 31 35 AC 50 23 6B A6 62 B9 C1 B8 D5 B1 6A A4 C6 AE C9 A6 A8 A5 5C BE F7 B2 76 31 30 30 25 A1 49 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 29 00 00 11 00 B0 B6 A4 6A C6 46 BB EE C0 F2 B1 6F B2 76 35 AD BF 14 00 00 00 20 00 00 00 05 00 00 00 43 89 34 01 43 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 B0 B6 A4 6A C6 46 BB EE C0 F2 B1 6F B2 76 35 AD BF A1 49 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 C8 00 00 00 99 89 34 01 99 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 C8 00 00 00 99 89 34 01 99 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 A0 89 34 01 A0 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 A0 89 34 01 A0 89 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 BD FC B8 67 C5 E7 AD C8 42 75 66 66 AE C4 AA 47 2B 31 30 30 25 00 00 00 00 1F 00 5B B3 74 AB D7 BF 45 B5 6F 5D 20 B5 D7 AD 5E A9 C7 AA AB A5 58 B2 7B BE F7 B2 76 BC 57 A5 5B 00 00 00 00 19 00 00 00 0A 00 00 00 CA 8C 34 01 1D 8D 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 5B B3 74 AB D7 BF 45 B5 6F 5D 20 BD FC A6 41 A6 B8 A5 58 B2 7B AE C9 B6 A1 B4 EE A4 D6 00 00 00 00 12 00 00 00 C0 27 09 00 CA 8C 34 01 1D 8D 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 ");

        mplew.writeHexString("FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 5B B3 74 AB D7 BF 45 B5 6F 5D 20 BD FC A7 4E AB 6F AE C9 B6 A1 B4 EE A4 D6 00 00 00 00 13 00 00 00 C0 27 09 00 CA 8C 34 01 1D 8D 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B B3 74 AB D7 BF 45 B5 6F 5D 20 BD FC B8 67 C5 E7 AD C8 AE C4 AA 47 BC 57 A5 5B 00 00 00 00 11 00 00 00 C8 00 00 00 CA 8C 34 01 1D 8D 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1F 00 5B B3 74 AB D7 BF 45 B5 6F 5D 20 AC F0 B5 6F A5 F4 B0 C8 A8 43 A4 E9 A6 B8 BC C6 BC 57 A5 5B 00 00 00 00 0D 00 00 00 06 00 00 00 CA 8C 34 01 1D 8D 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 23 00 5B B3 74 AB D7 BF 45 B5 6F 5D 20 AA 69 C3 B9 A9 4D B4 B6 A8 BD A6 AB BE F7 B2 76 B6 7D A9 6C B4 EE A4 D6 00 00 00 00 23 00 00 00 F4 01 00 00 CA 8C 34 01 1D 8D 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 5B B3 74 AB D7 BF 45 B5 6F 5D 20 AF 50 BF 56 BE D4 AF 54 B8 67 C5 E7 AD C8 BC 57 A5 5B 14 00 00 00 22 00 00 00 96 00 00 00 CA 8C 34 01 1D 8D 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 35 30 25 06 00 00 00 04 00 00 00 32 00 00 00 D2 8A 34 01 D2 8A 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 3F 3F 3F 3F 20 3F 3F 3F 20 3F 3F 20 35 30 25 20 3F 3F 21 00 00 00 00 1B 00 AC 50 A4 4F 35 A1 42 31 30 A1 42 31 35 AC 50 31 30 30 25 B1 6A A4 C6 A6 A8 A5 5C 00 00 00 00 11 00 00 00 C8 00 00 00 D2 8A 34 01 D2 8A 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 11 00 3F 20 3F 3F 3F 20 3F 3F 20 3F 3F 20 2B 31 30 30 25 00 00 00 00 11 00 B0 B6 A4 6A C6 46 BB EE C0 F2 B1 6F B2 76 35 AD BF 00 00 00 00 1B 00 00 00 63 00 00 00 1F 8B 34 01 1F 8B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 B0 B6 A4 6A C6 46 BB EE C0 F2 B1 6F B2 76 35 AD BF A1 49 00 00 00 00 17 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 00 00 00 00 16 00 00 00 0F 00 00 00 2D 8B 34 01 2D 8B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 27 00 5B B5 4C A7 51 BC ED AF 53 B4 66 A1 49 AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 E9 C0 75 B4 66 A1 49 00 00 00 00 14 00 00 00 40 77 1B 00 2D 8B 34 01 2D 8B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 BD FC B8 67 C5 E7 AD C8 BC 57 AF 71 AE C4 AA 47 2B 31 30 30 25 00 00 00 00 27 00 5B B5 4C A7 51 BC ED AF 53 B4 66 A1 49 AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 E9 C0 75 B4 66 A1 49 00 00 00 00 15 00 00 00 80 4F 12 00 2D 8B 34 01 2D 8B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 27 00 5B B5 4C A7 51 BC ED AF 53 B4 66 A1 49 AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 E9 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 C8 00 00 00 2D 8B 34 01 2D 8B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 03 00 00 00 04 00 00 00 32 00 00 00 34 8B 34 01 34 8B 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 3F 3F 3F 20 3F 3F 20 3F 3F 20 3F 3F 20 35 30 25 20 3F 3F 5E 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 20 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 BB 79 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 C0 75 B4 66 35 30 25 A1 49 23 6B 00 00 1E 00 5B AB E6 B3 74 AC A1 B0 CA 5D B5 D7 AD 5E A9 C7 AA AB B5 6E B3 F5 BE F7 B2 76 BC 57 A5 5B 00 00 00 00 19 00 00 00 0A 00 00 00 6E B1 34 01 7B B1 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 5B A5 5B B3 74 5D 20 BD FC A6 41 A6 B8 B5 6E B3 F5 AE C9 B6 A1 B4 EE A4 D6 00 00 00 00 12 00 00 00 C0 27 09 00 6E B1 34 01 7B B1 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 5B A5 5B B3 74 AC A1 B0 CA 5D 20 BD FC A7 4E AB 6F AE C9 B6 A1 B4 EE A4 D6 00 00 00 00 13 00 00 00 C0 27 09 00 6E B1 34 01 7B B1 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B A5 5B B3 74 AC A1 B0 CA 5D 20 BD FC B8 67 C5 E7 AD C8 AE C4 AA 47 BC 57 A5 5B 00 00 00 00 11 00 00 00 C8 00 00 00 6E B1 34 01 7B B1 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1F 00 5B A5 5B B3 74 5D 20 AA 69 C3 B9 BB 50 B4 B6 A8 BD A6 AB BE F7 B2 76 B0 5F A9 6C B4 EE A4 D6 00 00 00 00 23 00 00 00 F4 01 00 00 6E B1 34 01 7B B1 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 5B A5 5B B3 74 AC A1 B0 CA 5D 20 AF 50 B5 4B BE D4 AF 54 B8 67 C5 E7 AD C8 BC 57 A5 5B 14 00 00 00 22 00 00 00 96 00 00 00 6E B1 34 01 7B B1 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 C8 00 00 00 4A 8C 34 01 4A 8C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 C8 00 00 00 4A 8C 34 01 4A 8C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 51 8C 34 01 51 8C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 16 00 BD FC B8 67 C5 E7 AD C8 A5 5B AB F9 AE C4 AA 47 20 2B 31 30 30 25 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 51 8C 34 01 51 8C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 04 00 00 00 04 00 00 00 00 00 00 00 58 8C 34 01 58 8C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 A7 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 23 6B 20 B6 4F A5 CE A7 E9 A6 A9 33 30 25 23 6E 0D 0A 28 A8 BE A4 EE B7 B4 B7 6C B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 AB 44 AC A1 B0 CA B9 EF B6 48 A1 41 4D 56 50 A7 E9 A6 A9 B7 7C A6 62 AD EC A5 BB AA BA 20 33 30 25 A7 E9 A6 A9 BB F9 AE E6 A4 57 C3 42 A5 7E AE 4D A5 CE BA D6 A7 51 29 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 13 00 00 00 0B 00 00 00 64 00 00 00 5F 8C 34 01 5F 8C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0A 00 5B AC 50 A4 4F A7 E9 A6 A9 5D 04 00 00 00 04 00 00 00 00 00 00 00 4D 8C 34 01 50 8C 34 01 00 00 00 00 60 5B 03 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 8C 00 5B AC 50 A4 4F B1 6A A4 C6 B6 4F A5 CE A7 E9 A6 A9 5D 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 23 6B B6 4F A5 CE A7 E9 A6 A9 33 30 25 23 6E 0D 0A 28 A8 BE A4 EE B7 B4 B7 6C B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 AB 44 AC A1 B0 CA B9 EF B6 48 A1 41 4D 56 50 A7 E9 A6 A9 B7 7C A6 62 AD EC A5 BB AA BA 20 33 30 25 A7 E9 A6 A9 BB F9 AE E6 A4 57 C3 42 A5 7E AE 4D A5 CE BA D6 A7 51 29 00 00 17 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 06 00 00 00 04 00 00 00 32 00 00 00 66 8C 34 01 66 8C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 15 00 AC 50 A4 4F 31 30 AC 50 A5 48 A4 55 A1 41 31 2B 31 B1 6A A4 C6 04 00 00 00 28 00 00 00 01 00 00 00 B9 8C 34 01 B9 8C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 00 00 00 00 7B 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 31 30 AC 50 A5 48 A4 55 AC 50 A4 4F B1 6A A4 C6 A6 A8 A5 5C AE C9 31 2B 31 B1 6A A4 C6 A1 49 23 6B 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 29 00 00 19 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 35 30 25 03 00 00 00 04 00 00 00 32 00 00 00 B9 8C 34 01 B9 8C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 5D 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 A7 E9 A6 A9 35 30 25 A1 49 23 6B 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 C8 00 00 00 B9 8C 34 01 B9 8C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 C8 00 00 00 B9 8C 34 01 B9 8C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 11 00 B0 B6 A4 6A C6 46 BB EE C0 F2 B1 6F B2 76 35 AD BF 14 00 00 00 20 00 00 00 05 00 00 00 C0 8C 34 01 C0 8C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 B0 B6 A4 6A C6 46 BB EE C0 F2 B1 6F B2 76 35 AD BF A1 49 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 C0 8C 34 01 C0 8C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 BD FC B8 67 C5 E7 AD C8 A5 5B AB F9 AE C4 AA 47 2B 31 30 30 25 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 C0 8C 34 01 C0 8C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 13 00 00 00 0B 00 00 00 64 00 00 00 C7 8C 34 01 C7 8C 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 C8 00 00 00 14 8D 34 01 14 8D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 C8 00 00 00 14 8D 34 01 14 8D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 1B 8D 34 01 1B 8D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 16 00 BD FC B8 67 C5 E7 AD C8 A5 5B AB F9 AE C4 AA 47 20 2B 31 30 30 25 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 1B 8D 34 01 1B 8D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 1B 00 AC 50 A4 4F 35 A1 42 31 30 A1 42 31 35 AC 50 31 30 30 25 B1 6A A4 C6 A6 A8 A5 5C 04 00 00 00 05 00 00 00 00 00 00 00 22 8D 34 01 22 8D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 10 00 00 00 E8 03 00 00 0B 00 00 00 E8 03 00 00 06 00 00 00 E8 03 00 00 00 00 00 00 00 00 89 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F 35 AC 50 A1 42 31 30 AC 50 A1 42 31 35 AC 50 23 6B A6 62 B9 C1 B8 D5 B1 6A A4 C6 AE C9 A6 A8 A5 5C BE F7 B2 76 31 30 30 25 A1 49 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 29 00 00 17 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 06 00 00 00 04 00 00 00 32 00 00 00 22 8D 34 01 22 8D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 19 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 35 30 25 03 00 00 00 04 00 00 00 32 00 00 00 22 8D 34 01 22 8D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 5D 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 A7 E9 A6 A9 35 30 25 A1 49 23 6B 00 00 31 00 5B B3 C1 A7 4A A4 4F B6 71 B4 A3 A4 C9 B6 67 5D AC 50 B4 C1 A5 7C C0 75 B4 66 A1 49 AC 50 A4 4F 31 30 AC 50 A5 48 A4 55 A1 41 31 2B 31 B1 6A A4 C6 04 00 00 00 28 00 00 00 01 00 00 00 E0 AF 34 01 E0 AF 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 00 00 00 00 69 00 23 65 3C B3 C1 A7 4A A4 4F B6 71 B4 A3 A4 C9 B6 67 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 31 30 AC 50 A5 48 A4 55 AC 50 A4 4F B1 6A A4 C6 A6 A8 A5 5C AE C9 A1 41 31 2B 31 B1 6A A4 C6 A1 49 23 6B 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 29 00 00 2D 00 5B B3 C1 A7 4A A4 4F B6 71 B4 A3 A4 C9 B6 67 5D AC 50 B4 C1 A4 AD C0 75 B4 66 A1 49 B0 B6 A4 6A C6 46 BB EE C0 F2 B1 6F B2 76 35 AD BF 14 00 00 00 20 00 00 00 05 00 00 00 E1 AF 34 01 E1 AF 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 B0 B6 A4 6A C6 46 BB EE C0 F2 B1 6F B2 76 35 AD BF A1 49 00 00 00 00 37 00 5B B3 C1 A7 4A A4 4F B6 71 B4 A3 A4 C9 B6 67 5D AC 50 B4 C1 A4 BB C0 75 B4 66 A1 49 AC 50 A4 4F 35 A1 42 31 30 A1 42 31 35 AC 50 31 30 30 25 B1 6A A4 C6 A6 A8 A5 5C 04 00 00 00 05 00 00 00 00 00 00 00 E2 AF 34 01 E2 AF 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 10 00 00 00 E8 03 00 00 0B 00 00 00 E8 03 00 00 06 00 00 00 E8 03 00 00 00 00 00 00 00 00 C7 00 23 65 3C B3 C1 A7 4A A4 4F B6 71 B4 A3 A4 C9 B6 67 3E AC A1 B0 CA A1 49 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 AA BA 23 6B B6 4F A5 CE C0 75 B4 66 33 30 25 A1 41 B1 71 35 AC 50 A1 42 31 30 AC 50 A1 42 31 35 AC 50 23 6B B8 D5 B9 CF B1 6A A4 C6 AE C9 A1 41 A6 A8 A5 5C BE F7 B2 76 31 30 30 25 A1 49 23 6E 0D 0A 28 A8 BE A4 EE AF 7D C3 61 B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 A1 43 B0 A3 AD EC A6 B3 33 30 25 C0 75 B4 66 BB F9 AE E6 A5 7E A1 41 4D 56 50 2F BA F4 A9 40 C0 75 B4 66 A5 69 A6 41 C3 42 A5 7E BE 41 A5 CE 29 00 00 2F 00 5B B3 C1 A7 4A A4 4F B6 71 B4 A3 A4 C9 B6 67 5D AC 50 B4 C1 A4 BB C0 75 B4 66 A1 49 AC 50 A4 4F B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 33 30 25 04 00 00 00 04 00 00 00 00 00 00 00 E2 AF 34 01 E2 AF 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 AC 50 A4 4F B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 33 30 25 A1 49 00 00 00 00 35 00 5B B3 C1 A7 4A A4 4F B6 71 B4 A3 A4 C9 B6 67 5D AC 50 B4 C1 A4 40 C0 75 B4 66 A1 49 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 35 30 25 03 00 00 00 04 00 00 00 32 00 00 00 E4 AF 34 01 E4 AF 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 49 00 23 65 3C B3 C1 A7 4A A4 4F B6 71 B4 A3 A4 C9 B6 67 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 C0 75 B4 66 35 30 25 A1 49 23 6B 00 00 27 00 5B B3 C1 A7 4A A4 4F B6 71 B4 A3 A4 C9 B6 67 5D BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 06 00 00 00 04 00 00 00 32 00 00 00 E5 AF 34 01 E5 AF 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 2F 00 5B B3 C1 A7 4A A4 4F B6 71 B4 A3 A4 C9 B6 67 5D AC 50 B4 C1 A4 54 C0 75 B4 66 A1 49 AC 50 A4 4F B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 33 30 25 04 00 00 00 04 00 00 00 00 00 00 00 E6 AF 34 01 E6 AF 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 98 00 23 65 3C B3 C1 A7 4A A4 4F B6 71 B4 A3 A4 C9 B6 67 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 23 6B B6 4F A5 CE C0 75 B4 66 33 30 25 23 6E 0D 0A 28 A8 BE A4 EE AF 7D C3 61 B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 A1 43 B0 A3 AD EC A6 B3 33 30 25 C0 75 B4 66 BB F9 AE E6 A5 7E A1 41 4D 56 50 2F BA F4 A9 40 C0 75 B4 66 A5 69 A6 41 C3 42 A5 7E BE 41 A5 CE 29 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 29 8D 34 01 29 8D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 BD FC B8 67 C5 E7 AD C8 42 75 66 66 AE C4 AA 47 2B 31 30 30 25 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 29 8D 34 01 29 8D 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2B 00 00 00 C8 00 00 00 29 8D 34 01 2A 8D 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2A 00 00 00 C8 00 00 00 29 8D 34 01 2A 8D 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2B 00 00 00 C8 00 00 00 D5 AF 34 01 D6 AF 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2A 00 00 00 C8 00 00 00 D5 AF 34 01 D6 AF 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 C8 00 00 00 DC AF 34 01 DC AF 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 C8 00 00 00 DC AF 34 01 DC AF 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2B 00 00 00 C8 00 00 00 DC AF 34 01 DD AF 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2A 00 00 00 C8 00 00 00 DC AF 34 01 DD AF 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2B 00 00 00 C8 00 00 00 E3 AF 34 01 E4 AF 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2A 00 00 00 C8 00 00 00 E3 AF 34 01 E4 AF 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1E 00 5B AF 53 AE ED A5 7E B0 65 5D B5 D7 AD 5E A9 C7 AA AB A5 58 B2 7B BE F7 B2 76 BC 57 A5 5B 00 00 00 00 19 00 00 00 0A 00 00 00 92 B2 34 01 98 B2 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1C 00 5B AF 53 AE ED A5 7E B0 65 5D BD FC A6 41 A6 B8 A5 58 B2 7B AE C9 B6 A1 B4 EE A4 D6 00 00 00 00 12 00 00 00 C0 27 09 00 92 B2 34 01 98 B2 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 18 00 5B AF 53 AE ED A5 7E B0 65 5D BD FC A7 4E AB 6F AE C9 B6 A1 B4 EE A4 D6 00 00 00 00 13 00 00 00 C0 27 09 00 92 B2 34 01 98 B2 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B AF 53 AE ED A5 7E B0 65 5D BD FC B8 67 C5 E7 AD C8 AE C4 AA 47 BC 57 A5 5B 00 00 00 00 11 00 00 00 C8 00 00 00 92 B2 34 01 98 B2 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 22 00 5B AF 53 AE ED A5 7E B0 65 5D AA 69 C3 B9 A9 4D B4 B6 A8 BD A6 AB BE F7 B2 76 B6 7D A9 6C B4 EE A4 D6 00 00 00 00 23 00 00 00 F4 01 00 00 92 B2 34 01 98 B2 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 5B AF 53 AE ED A5 7E B0 65 5D AF 50 BF 56 BE D4 AF 54 B8 67 C5 E7 AD C8 AE C4 AA 47 BC 57 A5 5B 14 00 00 00 22 00 00 00 96 00 00 00 92 B2 34 01 98 B2 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 17 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 06 00 00 00 04 00 00 00 32 00 00 00 EA AF 34 01 EA AF 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 19 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 35 30 25 03 00 00 00 04 00 00 00 32 00 00 00 EA AF 34 01 EA AF 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 5D 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 C0 75 B4 66 35 30 25 A1 49 23 6B 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2B 00 00 00 C8 00 00 00 EA AF 34 01 EB AF 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2A 00 00 00 C8 00 00 00 EA AF 34 01 EB AF 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 F1 AF 34 01 F1 AF 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 F1 AF 34 01 F1 AF 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 BD FC B8 67 C5 E7 AD C8 42 75 66 66 AE C4 AA 47 2B 31 30 30 25 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2B 00 00 00 C8 00 00 00 F1 AF 34 01 F2 AF 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2A 00 00 00 C8 00 00 00 F1 AF 34 01 F2 AF 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1B 00 AC 50 A4 4F 35 A1 42 31 30 A1 42 31 35 AC 50 31 30 30 25 B1 6A A4 C6 A6 A8 A5 5C 04 00 00 00 05 00 00 00 00 00 00 00 3D B0 34 01 3D B0 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 10 00 00 00 E8 03 00 00 0B 00 00 00 E8 03 00 00 06 00 00 00 E8 03 00 00 00 00 00 00 00 00 89 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F 35 AC 50 A1 42 31 30 AC 50 A1 42 31 35 AC 50 23 6B A6 62 B9 C1 B8 D5 B1 6A A4 C6 AE C9 A6 A8 A5 5C BE F7 B2 76 31 30 30 25 A1 49 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 29 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2B 00 00 00 C8 00 00 00 3D B0 34 01 3E B0 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2A 00 00 00 C8 00 00 00 3D B0 34 01 3E B0 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2B 00 00 00 C8 00 00 00 44 B0 34 01 45 B0 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2A 00 00 00 C8 00 00 00 44 B0 34 01 45 B0 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 C8 00 00 00 4B B0 34 01 4B B0 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 C8 00 00 00 4B B0 34 01 4B B0 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2B 00 00 00 C8 00 00 00 4B B0 34 01 4C B0 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2A 00 00 00 C8 00 00 00 4B B0 34 01 4C B0 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2B 00 00 00 C8 00 00 00 52 B0 34 01 53 B0 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 17 00 00 00 2A 00 00 00 C8 00 00 00 52 B0 34 01 53 B0 34 01 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 31 A4 D1 C0 F2 B1 6F 32 AD BF C2 49 BC C6 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 52 B0 34 01 52 B0 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 BD FC B8 67 C5 E7 AD C8 42 75 66 66 AE C4 AA 47 2B 31 30 30 25 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 52 B0 34 01 52 B0 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 15 00 AC 50 A4 4F 31 30 AC 50 A5 48 A4 55 A1 41 31 2B 31 B1 6A A4 C6 04 00 00 00 28 00 00 00 01 00 00 00 A1 B0 34 01 A1 B0 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 00 00 00 00 7D 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 31 30 AC 50 A5 48 A4 55 AC 50 A4 4F B1 6A A4 C6 A6 A8 A5 5C AE C9 A1 41 31 2B 31 B1 6A A4 C6 A1 49 23 6B 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 29 00 00 19 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 35 30 25 03 00 00 00 04 00 00 00 32 00 00 00 A1 B0 34 01 A1 B0 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 5D 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 C0 75 B4 66 35 30 25 A1 49 23 6B 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 13 00 00 00 0B 00 00 00 64 00 00 00 A8 B0 34 01 A8 B0 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 11 00 B0 B6 A4 6A C6 46 BB EE C0 F2 B1 6F B2 76 35 AD BF 14 00 00 00 20 00 00 00 05 00 00 00 AF B0 34 01 AF B0 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 B0 B6 A4 6A C6 46 BB EE C0 F2 B1 6F B2 76 35 AD BF A1 49 00 00 00 00 1A 00 5B B7 73 AC F6 A4 B8 5D BD FC A6 41 A6 B8 A5 58 B2 7B AE C9 B6 A1 B4 EE A4 D6 00 00 00 00 12 00 00 00 40 0D 03 00 EE D6 34 01 11 D8 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B B7 73 AC F6 A4 B8 5D BD FC A6 41 A6 B8 A8 CF A5 CE AE C9 B6 A1 B4 EE A4 D6 00 00 00 00 13 00 00 00 C0 27 09 00 EE D6 34 01 11 D8 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 18 00 5B B7 73 AC F6 A4 B8 5D BD FC B8 67 C5 E7 AD C8 AE C4 AA 47 BC 57 A5 5B 00 00 00 00 11 00 00 00 C8 00 00 00 EE D6 34 01 11 D8 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 5B B7 73 AC F6 A4 B8 5D AA 69 C3 B9 A9 4D B4 B6 A8 BD A6 AB BE F7 B2 76 B6 7D A9 6C B4 EE A4 D6 00 00 00 00 23 00 00 00 F4 01 00 00 EE D6 34 01 11 D8 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B B7 73 AC F6 A4 B8 5D AF 50 BF 56 BE D4 AF 54 B8 67 C5 E7 AD C8 BC 57 A5 5B 14 00 00 00 22 00 00 00 96 00 00 00 EE D6 34 01 59 D7 34 01 90 5F 01 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 12 00 5B 3F 3F 3F 3F 5D 20 3F 3F 3F 3F 20 3F 3F 3F 20 3F 3F 14 00 00 00 22 00 00 00 96 00 00 00 5B D7 34 01 11 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 D6 B3 34 01 D6 B3 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 BD FC B8 67 C5 E7 AD C8 42 75 66 66 AE C4 AA 47 2B 31 30 30 25 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 D6 B3 34 01 D6 B3 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 C8 00 00 00 D6 B3 34 01 D6 B3 34 01 00 00 00 00 7C 99 03 00 00 00 00 00");

        mplew.writeHexString("FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 C8 00 00 00 D6 B3 34 01 D6 B3 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 04 00 00 00 04 00 00 00 00 00 00 00 23 B4 34 01 23 B4 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 AC 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 23 6B B6 4F A5 CE C0 75 B4 66 33 30 25 23 6E 0D 0A 28 A8 BE A4 EE AF 7D C3 61 B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 A1 43 B0 A3 AD EC A6 B3 33 30 25 C0 75 B4 66 BB F9 AE E6 A5 7E A1 41 4D 56 50 2F BA F4 A9 40 C0 75 B4 66 A5 69 A6 41 C3 42 A5 7E BE 41 A5 CE 29 00 00 17 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 06 00 00 00 04 00 00 00 32 00 00 00 2A B4 34 01 2A B4 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 13 00 00 00 0B 00 00 00 64 00 00 00 2A B4 34 01 2A B4 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 AC 50 A4 4F 35 A1 42 31 30 A1 42 31 35 AC 50 31 30 30 25 B1 6A A4 C6 A6 A8 A5 5C 04 00 00 00 05 00 00 00 00 00 00 00 31 B4 34 01 31 B4 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 10 00 00 00 E8 03 00 00 0B 00 00 00 E8 03 00 00 06 00 00 00 E8 03 00 00 00 00 00 00 00 00 C4 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F 35 AC 50 A1 42 31 30 AC 50 A1 42 31 35 AC 50 23 6B A6 62 B9 C1 B8 D5 B1 6A A4 C6 AE C9 A6 A8 A5 5C BE F7 B2 76 31 30 30 25 A1 49 23 6E 0D 0A 28 A8 BE A4 EE AF 7D C3 61 B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 A1 43 B0 A3 AD EC A6 B3 33 30 25 C0 75 B4 66 BB F9 AE E6 A5 7E A1 41 4D 56 50 2F BA F4 A9 40 C0 75 B4 66 A5 69 A6 41 C3 42 A5 7E BE 41 A5 CE 29 00 00 13 00 B0 B6 A4 6A C6 46 BB EE C0 F2 B1 6F B2 76 35 AD BF A1 49 14 00 00 00 20 00 00 00 05 00 00 00 31 B4 34 01 31 B4 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 B0 B6 A4 6A C6 46 BB EE C0 F2 B1 6F B2 76 35 AD BF A1 49 00 00 00 00 19 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 35 30 25 03 00 00 00 04 00 00 00 32 00 00 00 31 B4 34 01 31 B4 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 5D 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 C0 75 B4 66 35 30 25 A1 49 23 6B 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 38 B4 34 01 39 B4 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 BD FC B8 67 C5 E7 AD C8 A5 5B AB F9 AE C4 AA 47 2B 31 30 30 25 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 38 B4 34 01 39 B4 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 2C 01 00 00 38 B4 34 01 39 B4 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 2C 01 00 00 38 B4 34 01 39 B4 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 AC 50 A4 4F 35 A1 42 31 30 A1 42 31 35 AC 50 31 30 30 25 B1 6A A4 C6 A6 A8 A5 5C 04 00 00 00 05 00 00 00 00 00 00 00 EB D6 34 01 EB D6 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 10 00 00 00 E8 03 00 00 0B 00 00 00 E8 03 00 00 06 00 00 00 E8 03 00 00 00 00 00 00 00 00 DB 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 AA BA 23 6B B6 4F A5 CE C0 75 B4 66 33 30 25 A1 41 B1 71 35 AC 50 A1 42 31 30 AC 50 A1 42 31 35 AC 50 23 6B B8 D5 B9 CF B1 6A A4 C6 AE C9 A1 41 A6 A8 A5 5C BE F7 B2 76 31 30 30 25 A1 49 23 6E 0D 0A 28 A8 BE A4 EE AF 7D C3 61 B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 A1 43 B0 A3 AD EC A6 B3 33 30 25 C0 75 B4 66 BB F9 AE E6 A5 7E A1 41 4D 56 50 2F BA F4 A9 40 C0 75 B4 66 A5 69 A6 41 C3 42 A5 7E BE 41 A5 CE 29 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 04 00 00 00 04 00 00 00 00 00 00 00 EB D6 34 01 EB D6 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 AC 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 23 6B B6 4F A5 CE C0 75 B4 66 33 30 25 23 6E 0D 0A 28 A8 BE A4 EE AF 7D C3 61 B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 A1 43 B0 A3 AD EC A6 B3 33 30 25 C0 75 B4 66 BB F9 AE E6 A5 7E A1 41 4D 56 50 2F BA F4 A9 40 C0 75 B4 66 A5 69 A6 41 C3 42 A5 7E BE 41 A5 CE 29 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 F2 D6 34 01 F2 D6 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 BD FC B8 67 C5 E7 AD C8 42 75 66 66 AE C4 AA 47 2B 31 30 30 25 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 F2 D6 34 01 F2 D6 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 2C 01 00 00 F2 D6 34 01 F2 D6 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 2C 01 00 00 F2 D6 34 01 F2 D6 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 AC 50 A4 4F 31 30 AC 50 A5 48 A4 55 A1 41 31 2B 31 B1 6A A4 C6 04 00 00 00 28 00 00 00 01 00 00 00 F9 D6 34 01 F9 D6 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 00 00 00 00 AC 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 31 30 AC 50 A5 48 A4 55 AC 50 A4 4F B1 6A A4 C6 A6 A8 A5 5C AE C9 A1 41 31 2B 31 B1 6A A4 C6 A1 49 23 6B 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 29 0D 0A 23 65 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 23 66 63 30 78 46 46 46 46 43 43 30 30 23 C0 75 B4 66 35 30 25 A1 49 23 6B 00 00 19 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 35 30 25 03 00 00 00 04 00 00 00 32 00 00 00 F9 D6 34 01 F9 D6 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 5D 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 C0 75 B4 66 35 30 25 A1 49 23 6B 00 00 17 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 06 00 00 00 04 00 00 00 32 00 00 00 F9 D6 34 01 F9 D6 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 19 00 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 04 00 00 00 04 00 00 00 00 00 00 00 00 D7 34 01 00 D7 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 AC 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 23 6B B6 4F A5 CE C0 75 B4 66 33 30 25 23 6E 0D 0A 28 A8 BE A4 EE AF 7D C3 61 B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 A1 43 B0 A3 AD EC A6 B3 33 30 25 C0 75 B4 66 BB F9 AE E6 A5 7E A1 41 4D 56 50 2F BA F4 A9 40 C0 75 B4 66 A5 69 A6 41 C3 42 A5 7E BE 41 A5 CE 29 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 1B 00 00 00 63 00 00 00 17 D8 34 01 17 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 06 00 00 00 04 00 00 00 32 00 00 00 17 D8 34 01 17 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 3F 3F 3F 3F 20 3F 3F 3F 20 3F 3F 20 35 30 25 20 3F 3F 21 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 03 00 00 00 04 00 00 00 32 00 00 00 1E D8 34 01 1E D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 3F 3F 3F 20 3F 3F 20 3F 3F 20 3F 3F 20 35 30 25 20 3F 3F 5E 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 20 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 BB 79 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 C0 75 B4 66 35 30 25 A1 49 23 6B 00 00 1B 00 AC 50 A4 4F 35 A1 42 31 30 A1 42 31 35 AC 50 31 30 30 25 B1 6A A4 C6 A6 A8 A5 5C 05 00 00 00 04 00 00 00 32 00 00 00 25 D8 34 01 25 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 10 00 3F 3F 3F 3F 20 3F 3F 3F 3F 20 35 30 25 20 3F 3F 00 00 00 00 13 00 B0 B6 A4 6A C6 46 BB EE C0 F2 B1 6F B2 76 35 AD BF A1 49 14 00 00 00 1E 00 00 00 C8 00 00 00 2C D8 34 01 2C D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 B0 B6 A4 6A C6 46 BB EE C0 F2 B1 6F B2 76 35 AD BF A1 49 00 00 00 00 19 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 35 30 25 04 00 00 00 04 00 00 00 00 00 00 00 79 D8 34 01 79 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0B 00 3F 3F 3F 3F 20 33 30 25 20 3F 3F 5D 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 C0 75 B4 66 35 30 25 A1 49 23 6B 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 16 00 00 00 0F 00 00 00 79 D8 34 01 79 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 BD FC B8 67 C5 E7 AD C8 A5 5B AB F9 AE C4 AA 47 2B 31 30 30 25 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 14 00 00 00 40 77 1B 00 79 D8 34 01 79 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 15 00 00 00 80 4F 12 00 79 D8 34 01 79 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 3F 3F 3F 3F 20 3F 3F 3F 3F 20 3F 3F 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 13 00 00 00 0B 00 00 00 64 00 00 00 80 D8 34 01 80 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0E 00 3F 3F 3F 3F 3F 3F 20 3F 3F 3F 3F 20 32 3F 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 17 00 00 00 02 00 00 00 80 D8 34 01 80 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 AC 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 23 6B B6 4F A5 CE C0 75 B4 66 33 30 25 23 6E 0D 0A 28 A8 BE A4 EE AF 7D C3 61 B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 A1 43 B0 A3 AD EC A6 B3 33 30 25 C0 75 B4 66 BB F9 AE E6 A5 7E A1 41 4D 56 50 2F BA F4 A9 40 C0 75 B4 66 A5 69 A6 41 C3 42 A5 7E BE 41 A5 CE 29 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 13 00 00 00 0B 00 00 00 64 00 00 00 B6 D7 34 01 B6 D7 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 BD D7 34 01 BD D7 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 BD FC B8 67 C5 E7 AD C8 42 75 66 66 AE C4 AA 47 2B 31 30 30 25 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 BD D7 34 01 BD D7 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 2C 01 00 00 BD D7 34 01 BD D7 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1A 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 2C 01 00 00 BD D7 34 01 BD D7 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 26 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 06 00 00 00 04 00 00 00 32 00 00 00 BD D7 34 01 BD D7 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3D 00 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 28 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE C0 75 B4 66 35 30 25 03 00 00 00 04 00 00 00 32 00 00 00 BD D7 34 01 BD D7 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 57 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E 0D 0A 0D 0A A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 C0 75 B4 66 35 30 25 A1 49 23 6B 00 00 20 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 B0 B6 A4 6A C6 46 BB EE C0 F2 B1 6F B2 76 35 AD BF 14 00 00 00 20 00 00 00 05 00 00 00 BD D7 34 01 BD D7 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 B0 B6 A4 6A C6 46 BB EE C0 F2 B1 6F B2 76 35 AD BF A1 49 00 00 00 00 15 00 AC 50 A4 4F 31 30 AC 50 A5 48 A4 55 A1 41 31 2B 31 B1 6A A4 C6 04 00 00 00 28 00 00 00 01 00 00 00 C4 D7 34 01 C4 D7 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 00 00 00 00 AD 00 20 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 31 30 AC 50 A5 48 A4 55 AC 50 A4 A7 B1 6A A4 C6 A6 A8 A5 5C AE C9 31 2B 31 B1 6A A4 C6 A1 49 23 6B 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 A9 F3 AC A1 B0 CA B9 EF B6 48 A4 A4 B0 A3 A5 7E 29 0D 0A 23 65 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 23 66 63 30 78 46 46 46 46 43 43 30 30 23 35 30 25 C0 75 B4 66 A1 49 23 6B 00 00 1A 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 20 35 30 25 C0 75 B4 66 03 00 00 00 04 00 00 00 32 00 00 00 C4 D7 34 01 C4 D7 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 5D 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 35 30 25 C0 75 B4 66 A1 49 23 6B 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 C8 00 00 00 CB D7 34 01 CB D7 34 01 00 00 00 00 80 A9 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 C8 00 00 00 CB D7 34 01 CB D7 34 01 00 00 00 00 80 A9 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 C8 00 00 00 11 D8 34 01 11 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 C8 00 00 00 11 D8 34 01 11 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 23 00 AC 50 A4 4F B1 6A A4 C6 35 AC 50 A1 42 31 30 AC 50 A1 42 31 35 AC 50 31 30 30 25 B1 6A A4 C6 A6 A8 A5 5C 04 00 00 00 05 00 00 00 00 00 00 00 17 D8 34 01 17 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 10 00 00 00 E8 03 00 00 0B 00 00 00 E8 03 00 00 06 00 00 00 E8 03 00 00 00 00 00 00 00 00 83 00 23 65 AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E 20 AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F 35 AC 50 A1 42 31 30 AC 50 A1 42 31 35 AC 50 23 A6 62 B9 C1 B8 D5 B1 6A A4 C6 AE C9 A6 A8 A5 5C BE F7 B2 76 31 30 30 25 A1 49 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 A4 A3 C4 DD A9 F3 AC A1 B0 CA B9 EF B6 48 A1 43 29 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 1E D8 34 01 1E D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 16 00 BD FC B8 67 C5 E7 AD C8 42 55 46 46 AE C4 AA 47 20 2B 31 30 30 25 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 1E D8 34 01 1E D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 26 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 06 00 00 00 04 00 00 00 32 00 00 00 1E D8 34 01 1E D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3D 00 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 04 00 00 00 04 00 00 00 00 00 00 00 25 D8 34 01 25 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 AC 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 23 6B B6 4F A5 CE C0 75 B4 66 33 30 25 23 6E 0D 0A 28 A8 BE A4 EE AF 7D C3 61 B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 A1 43 B0 A3 AD EC A6 B3 33 30 25 C0 75 B4 66 BB F9 AE E6 A5 7E A1 41 4D 56 50 2F BA F4 A9 40 C0 75 B4 66 A5 69 A6 41 C3 42 A5 7E BE 41 A5 CE 29 00 00 00 00 1A 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 20 35 30 25 C0 75 B4 66 03 00 00 00 04 00 00 00 32 00 00 00 25 D8 34 01 25 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 DB 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 23 6B B6 4F A5 CE C0 75 B4 66 33 30 25 A1 49 23 6E 0D 0A 28 A8 BE A4 EE AF 7D C3 61 B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 A4 A3 A6 62 AC A1 B0 CA BD 64 B3 F2 A4 BA A1 41 B0 A3 AD EC A6 B3 33 30 25 C0 75 B4 66 BB F9 AE E6 A5 7E A1 41 4D 56 50 2F BA F4 A9 40 C0 75 B4 66 A5 69 A6 41 C3 42 A5 7E BE 41 A5 CE 29 5C 6E 23 65 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 23 66 63 30 78 46 46 46 46 43 43 30 30 23 35 30 25 C0 75 B4 66 A1 49 23 6B 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 C8 00 00 00 2C D8 34 01 2C D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 C8 00 00 00 2C D8 34 01 2C D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 B0 B6 A4 6A C6 46 BB EE C0 F2 B1 6F B2 76 35 AD BF 14 00 00 00 20 00 00 00 05 00 00 00 2C D8 34 01 2C D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 B0 B6 A4 6A C6 46 BB EE C0 F2 B1 6F B2 76 35 AD BF A1 49 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 80 D8 34 01 80 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 16 00 BD FC B8 67 C5 E7 AD C8 42 55 46 46 AE C4 AA 47 20 2B 31 30 30 25 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 80 D8 34 01 80 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 26 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 06 00 00 00 04 00 00 00 32 00 00 00 80 D8 34 01 80 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3D 00 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 23 00 AC 50 A4 4F B1 6A A4 C6 35 AC 50 A1 42 31 30 AC 50 A1 42 31 35 AC 50 31 30 30 25 B1 6A A4 C6 A6 A8 A5 5C 04 00 00 00 05 00 00 00 00 00 00 00 87 D8 34 01 87 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 10 00 00 00 E8 03 00 00 0B 00 00 00 E8 03 00 00 06 00 00 00 E8 03 00 00 00 00 00 00 00 00 81 00 23 65 AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E 20 AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F 35 AC 50 A1 42 31 30 AC 50 A1 42 31 35 AC 50 23 B9 C1 B8 D5 B1 6A A4 C6 AE C9 A6 A8 A5 5C BE F7 B2 76 31 30 30 25 A1 49 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 A4 A3 C4 DD A9 F3 AC A1 B0 CA B9 EF B6 48 A1 43 29 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 C8 00 00 00 87 D8 34 01 87 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 C8 00 00 00 87 D8 34 01 87 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 00 AC 50 A4 4F 31 30 AC 50 A5 48 A4 55 A1 41 31 2B 31 B1 6A A4 C6 04 00 00 00 28 00 00 00 01 00 00 00 8E D8 34 01 8E D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 00 00 00 00 7D 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 31 30 AC 50 A5 48 A4 55 AC 50 A4 A7 B1 6A A4 C6 A6 A8 A5 5C AE C9 31 2B 31 B1 6A A4 C6 A1 49 23 6B 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 A9 F3 AC A1 B0 CA B9 EF B6 48 A4 A4 B0 A3 A5 7E 29 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 8E D8 34 01 8E D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 16 00 BD FC B8 67 C5 E7 AD C8 42 55 46 46 AE C4 AA 47 20 2B 31 30 30 25 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 8E D8 34 01 8E D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 1E 00 00 00 C8 00 00 00 DA D8 34 01 DA D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 14 00 00 00 22 00 00 00 C8 00 00 00 DA D8 34 01 DA D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 13 00 00 00 0B 00 00 00 64 00 00 00 DA D8 34 01 DA D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 04 00 00 00 04 00 00 00 00 00 00 00 E1 D8 34 01 E1 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ED 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 23 65 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 23 66 63 30 78 46 46 46 46 43 43 30 30 23 35 30 25 C0 75 B4 66 A1 49 23 6B 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 23 6B B6 4F A5 CE 20 23 66 63 30 78 46 46 46 46 43 43 30 30 23 33 30 25 C0 75 B4 66 A1 49 23 6B 23 6E 0D 0A 28 A8 BE A4 EE AF 7D C3 61 B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 A4 A3 A6 62 AC A1 B0 CA BD 64 B3 F2 A4 BA A1 41 B0 A3 AD EC A6 B3 33 30 25 C0 75 B4 66 BB F9 AE E6 A5 7E A1 41 4D 56 50 2F BA F4 A9 40 C0 75 B4 66 A5 69 A6 41 C3 42 A5 7E BE 41 A5 CE 29 29 00 00 00 00 1A 00 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 20 35 30 25 C0 75 B4 66 03 00 00 00 04 00 00 00 32 00 00 00 E1 D8 34 01 E1 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ED 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 23 65 A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 23 66 63 30 78 46 46 46 46 43 43 30 30 23 35 30 25 C0 75 B4 66 A1 49 23 6B 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F B1 6A A4 C6 23 6B B6 4F A5 CE 20 23 66 63 30 78 46 46 46 46 43 43 30 30 23 33 30 25 C0 75 B4 66 A1 49 23 6B 23 6E 0D 0A 28 A8 BE A4 EE AF 7D C3 61 B6 4F A5 CE BB 50 B4 4C B6 51 B8 CB B3 C6 A4 A3 A6 62 AC A1 B0 CA BD 64 B3 F2 A4 BA A1 41 B0 A3 AD EC A6 B3 33 30 25 C0 75 B4 66 BB F9 AE E6 A5 7E A1 41 4D 56 50 2F BA F4 A9 40 C0 75 B4 66 A5 69 A6 41 C3 42 A5 7E BE 41 A5 CE 29 29 00 00 26 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 06 00 00 00 04 00 00 00 32 00 00 00 E1 D8 34 01 E1 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3D 00 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E BC E7 A6 62 AF E0 A4 4F AD AB B3 5D B6 4F A5 CE C0 75 B4 66 35 30 25 A1 49 00 00 00 00 13 00 AC 50 A4 4F 31 30 AC 50 A5 48 A4 55 31 2B 31 B1 6A A4 C6 04 00 00 00 28 00 00 00 01 00 00 00 E8 D8 34 01 E8 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 00 00 00 00 7D 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 31 30 AC 50 A5 48 A4 55 AC 50 A4 A7 B1 6A A4 C6 A6 A8 A5 5C AE C9 31 2B 31 B1 6A A4 C6 A1 49 23 6B 23 6E 0D 0A 28 B4 4C B6 51 B8 CB B3 C6 A9 F3 AC A1 B0 CA B9 EF B6 48 A4 A4 B0 A3 A5 7E 29 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 11 00 00 00 C8 00 00 00 E8 D8 34 01 E8 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 16 00 BD FC B8 67 C5 E7 AD C8 42 55 46 46 AE C4 AA 47 20 2B 31 30 30 25 00 00 00 00 1B 00 5B AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 5D 20 AC 50 B4 C1 A4 D1 C0 75 B4 66 A1 49 00 00 00 00 25 00 00 00 2C 01 00 00 E8 D8 34 01 E8 D8 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 B3 73 C4 F2 C0 BB B1 FE AF 5D A4 6C B8 67 C5 E7 AD C8 C0 F2 B1 6F B6 71 2B 33 30 30 25 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 17 00 00 00 2B 00 00 00 C8 00 00 00 13 DA 34 01 13 DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0F 00 3F 3F 3F 20 31 3F 20 3F 3F 20 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 17 00 00 00 2A 00 00 00 C8 00 00 00 13 DA 34 01 13 DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0A 00 3F 3F 3F 20 3F 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 05 00 00 00 04 00 00 00 32 00 00 00 7C DA 34 01 7C DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 10 00 3F 3F 3F 3F 20 3F 3F 3F 3F 20 35 30 25 20 3F 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 17 00 00 00 2B 00 00 00 C8 00 00 00 1A DA 34 01 1A DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0F 00 3F 3F 3F 20 31 3F 20 3F 3F 20 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 17 00 00 00 2A 00 00 00 C8 00 00 00 1A DA 34 01 1A DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0A 00 3F 3F 3F 20 3F 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 14 00 00 00 1E 00 00 00 C8 00 00 00 CF DA 34 01 CF DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1D 00 3F 3F 3F 20 3F 3F 3F 2C 20 3F 3F 3F 3F 3F 3F 20 3F 3F 3F 20 3F 3F 20 3F 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 1B 00 00 00 63 00 00 00 E4 DA 34 01 E4 DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 10 00 39 39 3F 3F 3F 3F 20 3F 3F 3F 3F 20 3F 3F 3F 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 17 00 00 00 2B 00 00 00 C8 00 00 00 21 DA 34 01 21 DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0F 00 3F 3F 3F 20 31 3F 20 3F 3F 20 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 17 00 00 00 2A 00 00 00 C8 00 00 00 21 DA 34 01 21 DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0A 00 3F 3F 3F 20 3F 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 04 00 00 00 04 00 00 00 00 00 00 00 21 DA 34 01 21 DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 1E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0B 00 3F 3F 3F 3F 20 33 30 25 20 3F 3F 5D 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 A4 E5 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 C0 75 B4 66 35 30 25 A1 49 23 6B 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 25 00 00 00 2C 01 00 00 DD DA 34 01 DD DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 14 00 3F 3F 3F 20 3F 3F 20 3F 3F 3F 20 3F 3F 3F 20 2B 33 30 30 25 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 17 00 00 00 2B 00 00 00 C8 00 00 00 6E DA 34 01 6E DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0F 00 3F 3F 3F 20 31 3F 20 3F 3F 20 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 17 00 00 00 2A 00 00 00 C8 00 00 00 6E DA 34 01 6E DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0A 00 3F 3F 3F 20 3F 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 03 00 00 00 04 00 00 00 32 00 00 00 D6 DA 34 01 D6 DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 13 00 3F 3F 3F 20 3F 3F 20 3F 3F 20 3F 3F 20 35 30 25 20 3F 3F 5E 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 20 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A A9 47 BB 79 AA BA B2 AA B8 F1 B1 6A A4 C6 B6 4F A5 CE 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 C0 75 B4 66 35 30 25 A1 49 23 6B 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 17 00 00 00 2B 00 00 00 C8 00 00 00 75 DA 34 01 75 DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0F 00 3F 3F 3F 20 31 3F 20 3F 3F 20 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 17 00 00 00 2A 00 00 00 C8 00 00 00 75 DA 34 01 75 DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0A 00 3F 3F 3F 20 3F 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 07 00 00 00 29 00 00 00 81 A3 07 00 75 DA 34 01 75 DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 27 00 3F 3F 3F 20 3F 3F 3F 20 3F 3F 2C 20 3F 3F 3F 20 3F 3F 3F 20 3F 3F 20 35 3F 20 3F 3F 20 3F 3F 3F 20 31 3F 20 3F 3F 21 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 13 00 00 00 0B 00 00 00 64 00 00 00 DD DA 34 01 DD DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0F 00 3F 3F 3F 20 3F 3F 3F 20 3F 3F 3F 3F 20 32 3F 00 00 00 00 15 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 20 3F 3F 3F 20 3F 3F 20 3F 3F 00 00 00 00 11 00 00 00 C8 00 00 00 83 DA 34 01 83 DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 08 00 3F 20 3F 3F 3F 20 32 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 16 00 00 00 0F 00 00 00 7C DA 34 01 7C DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 3F 3F 3F 3F 20 3F 3F 20 31 35 3F 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 15 00 00 00 80 4F 12 00 7C DA 34 01 7C DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 3F 3F 3F 3F 20 3F 3F 3F 3F 20 3F 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 00 00 00 00 14 00 00 00 40 77 1B 00 7C DA 34 01 7C DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0C 00 3F 3F 3F 3F 20 3F 3F 3F 3F 20 3F 3F 00 00 00 00 11 00 5B 3F 3F 3F 20 3F 3F 3F 5D 20 3F 3F 3F 20 3F 3F 21 04 00 00 00 05 00 00 00 00 00 00 00 83 DA 34 01 83 DA 34 01 00 00 00 00 7C 99 03 00 00 00 00 00 FF FF FF FF 01 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 10 00 00 00 E8 03 00 00 0B 00 00 00 E8 03 00 00 06 00 00 00 E8 03 00 00 00 00 00 00 1A 00 3F 3F 3F 3F 20 35 2C 20 31 30 2C 20 31 35 3F 20 31 30 30 25 20 3F 3F 20 3F 3F 8F 00 23 65 AC 50 B4 C1 A4 D1 B4 4E AC 4F AD 6E AA B1 B7 AC A4 A7 A8 A6 A1 49 3C AC 50 B4 C1 A4 D1 B7 AC A4 A7 A8 A6 3E AC A1 B0 CA A1 49 0D 0A 0D 0A 23 66 63 30 78 46 46 46 46 43 43 30 30 23 AC 50 A4 4F 35 AC 50 A1 42 31 30 AC 50 A1 42 31 35 AC 50 A1 41 A6 62 23 6B B9 C1 B8 D5 B1 6A A4 C6 AE C9 A1 41 A6 A8 A5 5C BE F7 B2 76 31 30 30 25 A1 49 23 6E 0D 0A A1 5D B4 4C B6 51 B8 CB B3 C6 A4 A3 BE 41 A5 CE A9 F3 AC A1 B0 CA A4 A4 A1 5E 00 00");
        return mplew.getPacket();
    }

    public static byte[] showGainWeaponPoint(int gainwp) {
        MessageOption option = new MessageOption();
        option.setAmount(gainwp);
        return CWvsContext.sendMessage(MessageOpcode.MS_IncWPMessage, option);
    }

    public static byte[] updateWeaponPoint(int wp) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ZeroWP.getValue());
        mplew.writeInt(wp);

        return mplew.getPacket();
    }

    public static byte[] FinalAttack(MapleCharacter character, int tick, boolean isSuccess, int skillId, int finalSkillId, int weaponType, List<Integer> monsterIds) {
        MaplePacketLittleEndianWriter packetWriter = new MaplePacketLittleEndianWriter();
        packetWriter.writeShort(OutHeader.LP_UserFinalAttackRequest.getValue());
        packetWriter.writeInt(tick);
        packetWriter.writeInt(isSuccess ? 1 : 0);
        packetWriter.writeInt(skillId);
        packetWriter.writeInt(isSuccess ? finalSkillId : 0);
        packetWriter.writeInt(isSuccess ? weaponType / 10 : 0);
        if (isSuccess) {
            packetWriter.writeInt(monsterIds.size());
            for (int monsterId : monsterIds) {
                packetWriter.writeInt(monsterId);
            }
        }
        packetWriter.writeInt(0); // add+ v267
        return packetWriter.getPacket();
    }


    public static byte[] FinalAttackOnp() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UseAttack.getValue());
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        return mplew.getPacket();
    }



    public static byte[] openWorldMap() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.VIEW_WORLDMAP.getValue());
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /*
     * 技能重生
     *
     */
    public static byte[] skillActive() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ResetOnStateForOnOffSkill.getValue());

        return mplew.getPacket();
    }

    public static byte[] skillNotActive(int skillId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SetOffStateForOnOffSkill.getValue());
        mplew.writeInt(skillId);

        return mplew.getPacket();
    }

    public static byte[] poolMakerInfo(boolean result, int count, int cooltime) { // 400051074
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.POOLMAKER_INFO.getValue());
        mplew.write(result);
        if (result) {
            mplew.writeInt(count); // 20 -> 19 -> 18 .... -> 5 ?
            mplew.writeInt(cooltime); // 60000
        }
        return mplew.getPacket();
    }

    /**
     * Created by HERTZ on 2023/12/25
     *
     * @notice 多重傷害技能
     * @Ver v257
     */
    public static byte[] multiSkillInfo(int skillId, int count, int maxCount, int timeout) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.MULTI_SKILL_INFO.getValue());
        mplew.writeInt(skillId);
        mplew.writeInt(count);
        mplew.writeInt(maxCount);
        mplew.writeInt(timeout);
        return mplew.getPacket();
    }

    public static byte[] updateHayatoPoint(int point) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SwordManPoint.getValue());
        mplew.writeShort(point);

        return mplew.getPacket();
    }

    public static byte[] sendCritAttack() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(32767);
        return mplew.getPacket();
    }

    /**
     * Created by HERTZ on 2023/12/23
     *
     * @notice 靈魂武器
     */
    public static byte[] updateSoulEffect(int chrid, boolean a) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserSoulEffect.getValue());
        mplew.writeInt(chrid);
        mplew.write(true);
        return mplew.getPacket();
    }

    /* 5 寶物之輪 */
    public static byte[] RuneStoneClearAndAllRegister(List<MapleRuneStone> runes) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_RuneStoneClearAndAllRegister.getValue());
        mplew.writeInt(runes.size());
        int runeType = 0;
        switch (World.getHoliday()) {
            case ChineseNewYear -> runeType = 6;
            case Halloween -> runeType = 1;
        }
        mplew.writeInt(0); // 0 - (基本) 1 - 萬聖節帽子(萬聖節) 2 - 5000氣球(5000天) 已刪圖 3 - 福袋(2017春節) 已刪圖 4 - 蜂蜜(2017蜜) 已刪圖 5 - 噴水(2017NOVA) 已刪圖 6 - 春節
        mplew.writeInt(2);
        for (MapleRuneStone rune : runes) {
            RuneStoneInfo(mplew, rune);
        }

        return mplew.getPacket();
    }

    public static byte[] spawnRuneStone(MapleRuneStone rune) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_RuneStoneAppear.getValue());
        int runeType = 0;
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(rune.getRuneType()); // 0 - (基本) 1 - 萬聖節帽子(萬聖節) 2 - 5000氣球(5000天) 已刪圖 3 - 福袋(2017春節) 已刪圖 4 - 蜂蜜(2017蜜) 已刪圖 5 - 噴水(2017NOVA) 已刪圖 6 - 春節
        RuneStoneInfo(mplew, rune);
        return mplew.getPacket();
    }

    public static void RuneStoneInfo(final MaplePacketLittleEndianWriter mplew, final MapleRuneStone rune) {
        mplew.writeInt(rune == null ? 0 : rune.getRuneType()); // ERuneStoneType
        mplew.writeInt(rune == null ? 0 : (int) rune.getMap().getMonsters().getFirst().getPosition().getX());
        mplew.writeInt(rune == null ? 0 : (int) rune.getMap().getMonsters().getFirst().getPosition().getY());
        mplew.write(rune != null && rune.isFacingLeft());
    }

    public static byte[] removeRuneStone(int charId, int percent, boolean lowerLv, boolean noText) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_RuneStoneDisappear.getValue());
        mplew.writeInt(0);
        mplew.writeInt(charId);
        mplew.writeInt(percent);
        mplew.write(lowerLv);
        mplew.write(noText);
        return mplew.getPacket();
    }

    public static byte[] RuneAction(MapleRuneStone.RuneStoneAction action) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_RuneStoneUseAck.getValue());
        mplew.writeInt(9);
        mplew.write(0);
        mplew.writeShort(1);
        mplew.write(action.getPacket());

        return mplew.getPacket();
    }

    /* 符文操作 */
    public static byte[] RuneAction(int type, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_RuneStoneUseAck.getValue());
        mplew.writeInt(type);
        if (time > 0) {
            mplew.writeInt(time);
        } else {
            for (int i = 0; i < 4; i++) {
                mplew.writeInt(Randomizer.nextInt(4));
            }
        }
        return mplew.getPacket();
    }

    public static byte[] showRuneEffect(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_RuneStoneSkillAck.getValue());
        mplew.writeInt(type);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] sendRuneCurseMsg(String msg) {
        return sendRuneCurseMsg(msg, false);
    }

    public static byte[] sendRuneCurseMsg(String msg, boolean isRelieve) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.RUNE_CURSE_MSG.getValue());
        mplew.writeMapleAsciiString(msg);
        mplew.writeInt(231);
        mplew.writeShort(0);
        mplew.writeInt(100);
        mplew.writeInt(100);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] RemoveRuneCurseMsg(String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.RUNE_CURSE_MSG.getValue());
        mplew.writeMapleAsciiString(msg);
        mplew.writeInt(231);
        mplew.write(1);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /* 戰鬥分析使用開關 */
    public static byte[] startBattleStatistics() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_StartDamageRecord.getValue());
        mplew.write(1);
        return mplew.getPacket();
    }

    public static byte[] CashCheck() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_PingCheckResult_ClientToGame.getValue());
        mplew.writeZeroBytes(46);
        return mplew.getPacket();
    }

    public static byte[] changeHour(int n1, int n2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_HOUR_CHANGED.getValue());
        mplew.writeShort(n1);
        mplew.writeShort(n2);

        return mplew.getPacket();
    }

    public static byte[] createObtacleAtom(int count, int type1, int type2, MapleMap map) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ObtacleAtomCreate.getValue());
        mplew.writeInt(0);
        mplew.writeInt(count);
        mplew.write(0);
        int n5 = Randomizer.nextInt(200000);
        for (int i2 = 0; i2 < count; ++i2) {
            MapleFoothold foothold = map.getFootholds().getAllRelevants().get(Randomizer.nextInt(map.getFootholds().getAllRelevants().size()));
            int n6 = foothold.getY2();
            int n7 = Randomizer.rand(map.getLeft(), map.getRight());
            Point point = map.calcPointBelow(new Point(n7, n6));
            if (point == null) {
                point = new Point(n7, n6);
            }
            mplew.write(1);
            mplew.writeInt(Randomizer.rand(type1, type2));
            mplew.writeInt(n5 + i2);
            mplew.writeInt((int) point.getX());
            if (map.getId() == 220080300) {
                mplew.writeInt(-310);
            } else {
                mplew.writeInt(map.getTop());
            }
            mplew.writeInt((int) point.getX());
            mplew.writeInt(Math.abs(map.getTop() - (int) point.getY()) + 100);
            mplew.writeInt(Randomizer.rand(5, 5));
            mplew.write(0);
            mplew.writeInt(Randomizer.rand(100, 100));
            mplew.writeInt(0);
            mplew.writeInt(Randomizer.rand(500, 1300));
            mplew.writeInt(0);
            mplew.writeInt(25);//百分比傷害 超過100的話會是補血
            mplew.writeInt(Randomizer.rand(1, 4));
            mplew.writeInt(Math.abs(map.getTop() - (int) point.getY()));
            mplew.writeInt(0);//
            mplew.writeInt(0);
            int dropspeed = Randomizer.rand(7, 10);
            if (map.getId() == 450009400) {
                mplew.writeInt(15);
            } else {
                mplew.writeInt(dropspeed);
            }
            if (map.getId() == 220080300) {
                mplew.writeInt(-310 + (310 * 2) + map.getBottom());
            } else {
                mplew.writeInt(Math.abs(map.getTop() - (int) point.getY()));
            }
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    /* 2 / 12 新增另外的掉落傷害 */
    public static byte[] DropAttack(int count, int type1, int type2, MapleMap map) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ObtacleAtomCreate.getValue());
        int SpeedRang = Randomizer.rand(4, 13);
        int DamageRang = Randomizer.rand(10, 35);
        mplew.writeInt(0);
        mplew.writeInt(count);
        mplew.write(0);
        int n5 = Randomizer.nextInt(200000);
        for (int i2 = 0; i2 < count; ++i2) {
            MapleFoothold foothold = map.getFootholds().getAllRelevants().get(Randomizer.nextInt(map.getFootholds().getAllRelevants().size()));
            int n6 = foothold.getY2();
            int n7 = Randomizer.rand(map.getLeft(), map.getRight());
            Point point = map.calcPointBelow(new Point(n7, n6));
            if (point == null) {
                point = new Point(n7, n6);
            }
            mplew.write(1);
            mplew.writeInt(Randomizer.rand(type1, type2));
            mplew.writeInt(n5 + i2);
            mplew.writeInt((int) point.getX());
            mplew.writeInt(map.getTop());
            mplew.writeInt((int) point.getX());
            mplew.writeInt(Math.abs(map.getTop() - (int) point.getY()) + 100);
            mplew.writeInt(Randomizer.rand(5, 5));
            mplew.write(0);
            mplew.writeInt(Randomizer.rand(100, 100));
            mplew.writeInt(0);
            mplew.writeInt(Randomizer.rand(500, 1300));
            mplew.writeInt(0);
            mplew.writeInt(DamageRang);//百分比傷害 超過100的話會是補血
            mplew.writeInt(Randomizer.rand(1, 4));
            mplew.writeInt(Math.abs(map.getTop() - (int) point.getY()));
            mplew.writeInt(0);//
            mplew.writeInt(0);
            mplew.writeInt(SpeedRang);/*沒這個就不會掉->速度 / 猜測負值 是 地板的上升掉落物攻擊 */
            mplew.writeInt(Math.abs(map.getTop() - (int) point.getY()));
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static byte[] UpDropAttack(int count, int type1, int type2, MapleMap map) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ObtacleAtomCreate.getValue());
        int SpeedRang = Randomizer.rand(4, 13);
        int DamageRang = Randomizer.rand(10, 35);
        mplew.writeInt(0);
        mplew.writeInt(count);
        mplew.write(0);
        int n5 = Randomizer.nextInt(200000);
        for (int i2 = 0; i2 < count; ++i2) {
            MapleFoothold foothold = map.getFootholds().getAllRelevants().get(Randomizer.nextInt(map.getFootholds().getAllRelevants().size()));
            int n6 = foothold.getY2();
            int n7 = Randomizer.rand(map.getLeft(), map.getRight());
            Point point = map.calcPointBelow(new Point(n7, n6));
            if (point == null) {
                point = new Point(n7, n6);
            }
            mplew.write(1);
            mplew.writeInt(Randomizer.rand(type1, type2));
            mplew.writeInt(n5 + i2);
            mplew.writeInt((int) point.getX());
            mplew.writeInt(map.getTop());
            mplew.writeInt((int) point.getX());
            mplew.writeInt(Math.abs(map.getTop() - (int) point.getY()) + 100);
            mplew.writeInt(Randomizer.rand(5, 5));
            mplew.write(0);
            mplew.writeInt(Randomizer.rand(100, 100));
            mplew.writeInt(0);
            mplew.writeInt(Randomizer.rand(500, 1300));
            mplew.writeInt(0);
            mplew.writeInt(DamageRang);//百分比傷害 超過100的話會是補血
            mplew.writeInt(Randomizer.rand(1, 4));
            mplew.writeInt(Math.abs(map.getTop() - (int) point.getY()));
            mplew.writeInt(0);//
            mplew.writeInt(0);
            mplew.writeInt(10);/*沒這個就不會掉->速度 / 猜測負值 是 地板的上升掉落物攻擊 */
            mplew.writeInt(Math.abs(map.getTop() - (int) point.getY()));
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static byte[] sendMarriedBefore(int n2, int n3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_WeddingProgress.getValue());
        mplew.writeInt(n2);
        mplew.writeInt(n3);

        return mplew.getPacket();
    }

    public static byte[] sendMarriedDone() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_WeddingCremonyEnd.getValue());

        return mplew.getPacket();
    }

    public static byte[] showVisitorResult(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.SHOW_VISITOR_RESULT.getValue());
        mplew.writeShort(type);
        mplew.writeShort(0);

        return mplew.getPacket();
    }

    public static byte[] updateVisitorKills(int n2, int n3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.UPDATE_VISITOR_KILL.getValue());
        mplew.writeShort(n2);
        mplew.writeShort(n3);

        return mplew.getPacket();
    }

    public static byte[] showFieldValue(String str, String act) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_FieldValue.getValue());
        mplew.writeMapleAsciiString(str);
        mplew.writeMapleAsciiString(act);

        return mplew.getPacket();
    }

    public static byte[] DressUpInfoModified(MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_DressUpInfoModified.getValue());
        PacketHelper.writeDressUpInfo(mplew, player);

        return mplew.getPacket();

    }

    public static byte[] UserRequestChangeMobZoneState(String data, int b1, List<Point> list) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.CHANGE_MOBZONESTATE_REQUEST.getValue());
        mplew.writeMapleAsciiString(data == null ? "" : data);
        mplew.writeInt(b1);
        mplew.writeInt(list.size());
        list.stream().filter(Objects::nonNull).forEach(mplew::writePosInt);

        return mplew.getPacket();
    }

    public static final byte[] LobbyTimeAction(final int n, final int n2, final int n3, int n4, int n5) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_UserTimerInfo.getValue());
        mplew.writeInt(n);
        mplew.writeInt(n2);
        mplew.writeInt(n3);
        mplew.writeInt(0);
        mplew.writeInt(n4);
        mplew.writeInt(n5);

        return mplew.getPacket();
    }

    public static byte[] SendGiantBossMap(Map<String, String> map) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.GIANT_BOSS_MAP.getValue());
        mplew.writeInt(map.size());
        for (Entry<String, String> entry : map.entrySet()) {
            mplew.writeMapleAsciiString(entry.getKey());
            mplew.writeMapleAsciiString(entry.getValue());
        }

        return mplew.getPacket();
    }

    public static byte[] ShowPortal(String string, int n2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.SHOW_PORTAL.getValue());
        mplew.writeMapleAsciiString(string);
        mplew.writeInt(n2);

        return mplew.getPacket();
    }

    public static byte[] IndividualDeathCountInfo(int cid, int EventDeadCount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_INDIVIDUAL_DEAD_COUNT_INFO.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(EventDeadCount);
        return mplew.getPacket();
    }

    public static byte[] userBonusAttackRequest(int skillid, int value, List<Integer> list) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserBonusAttackRequest.getValue());
        mplew.writeInt(skillid);
        mplew.writeInt(0);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.writeInt(0);
        switch (skillid) {
            case 400041030:
                mplew.writeInt(0);
                break;
            case 400011074:
            case 400011075:
            case 400011076:
                mplew.writeInt(1120017);
                break;
        }
        return mplew.getPacket();
    }

    public static byte[] SkillFeed() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.SKILL_FEED.getValue());
        mplew.writeInt(1);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] SetForceAtomTarget(int skillid, int unk, int size, int objid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SET_FORCE_ATOM_TARGET.getValue());
        mplew.writeInt(skillid);
        mplew.writeInt(unk);
        mplew.writeInt(size);
        mplew.writeInt(objid);
        return mplew.getPacket();
    }

    public static byte[] RegisterExtraSkill(int sourceId, List<ExtraSkill> skills) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_RegisterExtraSkill.getValue());
        mplew.writeInt(sourceId);
        mplew.writeShort(skills.size());
        for (ExtraSkill skill : skills) {
            mplew.writeInt(skill.TriggerSkillID);
            mplew.writeInt(skill.SkillID);
            mplew.writePosInt(skill.Position);
            mplew.writeShort(skill.FaceLeft);
            mplew.writeInt(skill.Delay);
            mplew.writeInt(skill.Value);

            mplew.writeInt(skill.MobOIDs.size());
            for (int oid : skill.MobOIDs) {
                mplew.writeInt(oid);
            }
            mplew.writeInt(skill.UnkList.size());
            for (int un : skill.UnkList) {
                mplew.writeInt(un);
            }

            mplew.writeInt(skill.TargetOID);

            // v260.4 add+
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.write(0);
        }

        return mplew.getPacket();
    }

    public static byte[] objSkillEffect(int objId, int skillId, int cid, Point pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.OBJ_SKILL_EFFECT.getValue());
        mplew.writeInt(objId);
        mplew.writeInt(skillId);
        mplew.writeInt(cid);
        mplew.writePosInt(pos);

        return mplew.getPacket();
    }

    public static byte[] GameExit() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.GAME_EXIT.getValue());

        return mplew.getPacket();
    }

    public static final byte[] openMapleUnion(final int n, final MapleUnion ah) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.ShowMapleUnion.getValue());
        mplew.writeInt(n); //COIN
        mplew.writeInt(0);
        mplew.write(0);
        addMapleUnionInfo(mplew, ah);
        return mplew.getPacket();
    }

    public static final byte[] updateMapleUnion(final MapleUnion mapleUnion) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.UpdateUnioMaplen.getValue());
        mplew.writeInt(101); // dummy v263填充單位
        addMapleUnionInfo(mplew, mapleUnion);
        return mplew.getPacket();
    }

    public static final void addMapleUnionInfo(final MaplePacketLittleEndianWriter mplew, final MapleUnion union) {
        mplew.writeInt(union.getAllUnions().size());
        union.getAllUnions().values().forEach(it -> writeMapleUnionData(mplew, it));
        mplew.writeInt(union.getFightingUnions().size());
        union.getFightingUnions().values().forEach(it -> writeMapleUnionData(mplew, it));
        boolean unkPuzzle = false;
        mplew.write(unkPuzzle);
        if (unkPuzzle) {
            writeMapleUnionData(mplew, null);
        }
        boolean labSS = false;
        mplew.write(labSS);
        if (labSS) {
            writeMapleUnionData(mplew, null);
        }
        boolean labSSS = false;
        mplew.write(labSSS);
        if (labSSS) {
            writeMapleUnionData(mplew, null);
        }
        boolean unkPuzzle2 = false;
        mplew.write(unkPuzzle2);
        if (unkPuzzle2) {
            writeMapleUnionData(mplew, null);
        }
    }

    public static final void writeMapleUnionData(final MaplePacketLittleEndianWriter mplew, final MapleUnionEntry union) {
        mplew.writeInt(1);
        mplew.writeInt(union.getCharacterId());
        mplew.writeInt(union.getLevel());
        mplew.writeInt(union.getJob() == 900 ? 0 : union.getJob());
        mplew.writeInt(0);
        mplew.writeInt(union.getRotate());// -1
        mplew.writeInt(union.getBoardIndex()); // 125
        mplew.writeInt(union.getLocal()); // 492
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeMapleAsciiString(union.getName());
    }

    public static byte[] MapleUnionPresetResult(int idx, MapleUnion union) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.MapleUnionPresetResult.getValue());
        mplew.writeInt(1); //0
        mplew.writeInt(256); //4
        mplew.writeInt(512); //8
        mplew.writeInt(768); //12
        mplew.writeInt(1024); //16
        mplew.writeInt(1280); //20
        mplew.writeInt(1536); //24
        mplew.writeInt(1792); //28
        mplew.write(0); //32
        mplew.writeInt(union.getFightingUnions().size());
        union.getFightingUnions().values().forEach(it -> writeMapleUnionData(mplew, it));
        mplew.write(1);
        mplew.writeInt(0); //0
        mplew.writeInt(1); //4
        mplew.writeInt(2); //8
        mplew.writeInt(3); //12
        mplew.writeInt(4); //16
        mplew.writeInt(5); //20
        mplew.writeInt(6); //24
        mplew.writeInt(7); //28
        for (int x = 0; x < 3; x++) {
            mplew.writeInt(0); //0
            mplew.writeInt(1); //4
            mplew.writeInt(256); //8
            mplew.writeInt(512); //12
            mplew.writeInt(768); //16
            mplew.writeInt(1024); //20
            mplew.writeInt(1280); //24
            mplew.writeInt(1536); //28
            mplew.writeInt(1792); //32
            mplew.write(0); //36
        }
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static final byte[] getMapleUnionCoinInfo(final int n, final int count) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.MapleUnionCoinInfo.getValue());
        mplew.writeInt(n);
        mplew.writeInt(count);
        return mplew.getPacket();
    }

    public static byte[] ArcaneRiverQuickPath() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.ARCANERIVER_QUICKPATH.getValue());
        mplew.writeInt(1);
        return mplew.getPacket();
    }

    /**
     * 聯機的多向技能
     */
    public static byte[] MultiSkillResult(int cid, int skillid, int display, int direction, int stance, int Type, int itemid, List<MapleMulitInfo> info) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.ShowMultiSkillAttack.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(skillid);
        //動作
        mplew.write(display); // = lea.readByte();
        mplew.write(direction);
        mplew.writeShort(stance);
        mplew.write(0);
        mplew.writeInt(Type);
        mplew.writeInt(itemid);
        mplew.writeInt(info.size());
        for (MapleMulitInfo mapleMulitInfo : info) {
            mapleMulitInfo.serialize(mplew);
        }

        return mplew.getPacket();
    }

    public static byte[] VSkillObjectAction(int skillid, int display, List<Integer> info) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.VSkillObjectAction.getValue());
        mplew.write(1);
        mplew.writeInt(skillid);
        mplew.writeInt(display);//V.162 byte=>int
        mplew.writeInt(info.size());
        info.forEach(mplew::writeInt);
//        if (skillid == 神炮王.宇宙無敵火炮彈) {
//            for (MapleMulitInfo mapleMulitInfo : info) {
//                mplew.writeInt(mapleMulitInfo.ObjectId);
//            }
//        } else {
//            for (MapleMulitInfo mapleMulitInfo : info) {
//                mapleMulitInfo.serialize(mplew);
//            }
//        }

        return mplew.getPacket();
    }

    public static byte[] userTossedBySkill(int id, int oid, MobSkill skill) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.UserTossedBySkill.getValue());
        mplew.writeInt(id);
        mplew.writeInt(oid);
        mplew.writeInt(skill.getSourceId());
        mplew.writeInt(skill.getLevel());
        mplew.writeInt(skill.getX());
        mplew.writeInt(0); // 226+
        return mplew.getPacket();
    }

    public static byte[] summonedBeholderRevengeAttack(int playerID, int summonOid, List<Integer> oids) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SummonedBeholderRevengeAttack.getValue());
        mplew.writeInt(playerID);
        mplew.writeInt(summonOid);
        mplew.writeInt(oids == null ? 0 : oids.size());
        if (oids != null) {
            for (int oid : oids) {
                mplew.writeInt(oid);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] summonedBeholderRevengeInfluence(int id, int objectId, int skillID, int b) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SummonedBeholderRevengeInfluence.getValue());
        mplew.writeInt(id);
        mplew.writeInt(objectId);
        mplew.writeInt(skillID);
        mplew.write(b);
        return mplew.getPacket();
    }

    public static byte[] skillCooltimeSet(int skillID, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_SkillCooltimeSetM.getValue());
        mplew.writeInt(1);
        mplew.writeInt(skillID);
        mplew.writeInt(duration);
        return mplew.getPacket();
    }

    public static byte[] RegisterElementalFocus(Map<Integer, Integer> map) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.RegisterElementalFocus.getValue());
        mplew.writeInt(map.size());
        for (Entry<Integer, Integer> o : map.entrySet()) {
            mplew.writeInt(o.getKey());
            mplew.writeInt(o.getValue());
        }
        return mplew.getPacket();
    }

    public static byte[] UserElementalFocusResult(int playerId, int sourceId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.UserElementalFocusResult.getValue());
        mplew.writeInt(playerId);
        mplew.writeInt(sourceId);
        mplew.writeInt(2);
        return mplew.getPacket();
    }

    public static byte[] showHoyoungHide(int playerId, boolean status) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.HoyoungHide.getValue());
        mplew.writeInt(playerId);
        mplew.writeBool(status);
        return mplew.getPacket();
    }


    public static byte[] sendUnkPacket688() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.UNK_733.getValue());
        mplew.writeZeroBytes(30);
        mplew.writeShort(16368);
        return mplew.getPacket();
    }

    public static byte[] LiftSkillAction(int skillid, int skilllevel, int i2, int x, int y) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LiftSkillAction.getValue());
        mplew.writeInt(skillid);
        mplew.writeInt(skilllevel);
        mplew.writeInt(i2);
        mplew.writeInt(x);
        mplew.writeInt(y);
        if (skillid == 幻影俠盜.黑傑克_3) {
            mplew.writeInt(-1L/*chr.getSkillCustomValue0(400041080)*/);
        }

        return mplew.getPacket();
    }

    public static byte[] zeroInfo(MapleCharacter chr, int mask, boolean beta) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ZeroInfo.getValue());
        chr.getStat().zeroData(mplew, chr, mask, beta);
        return mplew.getPacket();
    }

    /* MVP可領取提醒 */
    public static byte[] mvpPacketTips() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_MVP_REWARD_NOTICE.getValue());
        return mplew.getPacket();
    }

    public static byte[] showMobCollectionComplete(final int n, final List<Pair<Integer, Integer>> list, final int n2, final int n3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.MobCollectionCompleteRewardResult.getValue());
        mplew.writeInt(n);
        if (list == null || list.isEmpty()) {
            mplew.writeInt(0);
            mplew.writeInt(n2);
            mplew.writeInt(n3);
        } else {
            mplew.writeInt(list.size());
            for (final Pair<Integer, Integer> pair : list) {
                mplew.writeInt(pair.left);
                mplew.writeInt(pair.right);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] SetMapTaggedObjectSmoothVisible(ArrayList<TaggedObjRegenInfo> list) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SetMapTaggedObjectSmoothVisible.getValue());
        mplew.writeInt(list.size());
        for (final TaggedObjRegenInfo a1298 : list) {
            mplew.writeMapleAsciiString(a1298.getTag());
            mplew.writeBool(a1298.isVisible());
            mplew.writeInt(a1298.akb);
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static byte[] DynamicObjUrusSync(List<Pair<String, Point>> syncFH) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_DynamicObjUrusSync.getValue());
        mplew.writeInt(syncFH.size());
        for (final Pair<String, Point> pair : syncFH) {
            mplew.writeMapleAsciiString(pair.left);
            mplew.write(0);
            mplew.writeInt(1); // or 0 隱藏
            mplew.writePosInt(pair.right);
        }
        return mplew.getPacket();
    }

    public static byte[] UserCreateAreaDotInfo(int n, int skillId, Rectangle rect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeOpcode(OutHeader.LP_UserCreateAreaDotInfo);
        mplew.writeInt(n);
        mplew.writeInt(skillId);
        mplew.writeInt(0);
        mplew.writeRect(rect);
        return mplew.getPacket();
    }

    public static byte[] UserAreaInfosPrepare(int skillId, int n, Rectangle[] rectangles) {
        MaplePacketLittleEndianWriter p = new MaplePacketLittleEndianWriter(OutHeader.LP_UserAreaInfosPrepare);
        p.writeInt(skillId);
        p.writeInt(n);
        p.writeInt(rectangles.length);
        for (int i = 0; i < rectangles.length; i++) {
            p.writeInt(i + 1);
            p.writeRect(rectangles[i]);
        }

        return p.getPacket();
    }

    public static byte[] Unknown_42D() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeOpcode(OutHeader.LP_SpecialChairTWSitResult);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(1);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] CharacterModified(MapleCharacter chr, long l) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CharacterModified.getValue());
        mplew.writeInt(27590330); //0
        mplew.writeInt(-574354943); //4
        mplew.writeInt(92536832); //8
        mplew.writeInt(-33554432); //12
        mplew.writeInt(16777477); //16
        mplew.writeInt(1006449); //20
        mplew.writeInt(259086594); //24
        mplew.writeInt(-1738538240); //28
        mplew.writeInt(520355855); //32
        mplew.writeInt(83890112); //36
        mplew.writeInt(1054131); //40
        mplew.writeInt(274909703); //44
        mplew.writeInt(-2052061184); //48
        mplew.writeInt(1225326608); //52
        mplew.writeInt(167776471); //56
        mplew.writeInt(1352976); //60
        mplew.writeInt(376902411); //64
        mplew.writeInt(-60027904); //68
        mplew.writeInt(-1139998704); //72
        mplew.writeInt(251662596); //76
        mplew.writeInt(1115375); //80
        mplew.writeInt(284947216); //84
        mplew.writeInt(538841344); //88
        mplew.writeInt(1611989009); //92
        mplew.writeInt(369103217); //96
        mplew.writeInt(1132246); //100
        mplew.writeInt(461564698); //104
        mplew.writeInt(-1800266752); //108
        mplew.writeInt(689897489); //112
        mplew.writeInt(536875451); //116
        mplew.writeInt(1662207); //120
        mplew.writeInt(428049697); //124
        mplew.writeInt(159851008); //128
        mplew.writeInt(-1641873390); //132
        mplew.writeInt(603984426); //136
        mplew.writeInt(1122267); //140
        mplew.writeInt(1426784767); //144
        mplew.writeInt(-33423345); //148
        mplew.writeInt(50335602); //152
        mplew.writeInt(1022231); //156
        mplew.writeInt(269497093); //160
        mplew.writeInt(1602881280); //164
        mplew.writeInt(252182544); //168
        mplew.writeInt(150999173); //172
        mplew.writeInt(1102796); //176
        mplew.writeInt(285264909); //180
        mplew.writeInt(13897472); //184
        mplew.writeInt(16711697); //188
        mplew.writeInt(1202199); //192
        mplew.writeInt(307761409); //196
        mplew.writeInt(-30212352); //200
        mplew.writeInt(1997078553); //204
        mplew.writeInt(-1525678058); //208
        mplew.writeInt(20); //212
        mplew.writeInt(0); //216
        mplew.writeInt(0); //220
        mplew.writeInt(-1375731712); //224
        mplew.writeInt(956320845); //228
        mplew.writeInt(1476414542); //232
        mplew.writeInt(19541); //236
        mplew.writeInt(0); //240
        mplew.writeInt(0); //244
        mplew.writeInt(0); //248
        mplew.writeInt(0); //252
        mplew.writeInt(0); //256
        mplew.writeInt(0); //260
        mplew.writeInt(0); //264
        mplew.writeInt(0); //268
        mplew.writeInt(0); //272
        mplew.writeInt(0); //276
        mplew.writeInt(0); //280
        mplew.writeInt(0); //284
        mplew.writeInt(0); //288
        mplew.writeInt(0); //292
        mplew.writeInt(0); //296
        mplew.writeInt(0); //300
        mplew.writeInt(0); //304
        mplew.writeInt(0); //308
        mplew.writeInt(0); //312
        mplew.writeInt(0); //316
        mplew.writeInt(0); //320
        mplew.writeInt(0); //324
        mplew.writeInt(0); //328
        mplew.writeInt(0); //332
        mplew.writeInt(0); //336
        mplew.writeInt(0); //340
        mplew.writeInt(0); //344
        mplew.writeInt(0); //348
        mplew.writeInt(0); //352
        mplew.writeInt(0); //356
        mplew.writeInt(0); //360
        mplew.writeInt(0); //364
        mplew.writeInt(0); //368
        mplew.writeInt(0); //372
        mplew.writeInt(0); //376
        mplew.writeInt(0); //380
        mplew.writeInt(419430400); //384
        mplew.writeInt(604); //388
        mplew.writeInt(4096); //392
        chr.getClient().announce(mplew.getPacket());
        return mplew.getPacket();
    }

    public static byte[] ExclRequest() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeOpcode(OutHeader.LP_ExclRequest);
        return mplew.getPacket();
    }

    public static byte[] bossMessage(int mode, int mapid, int mobId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_EliteMobWorldMapNotice.getValue());
        mode = mobId <= 0 ? 1 : mode;
        mplew.write(mode);
        mplew.write(0); // ?????????
        mplew.writeInt(mapid);
        if (mode != 1) {
            mplew.writeInt(mobId);
            mplew.writeInt((1 << 16) + 1); // ?
        }
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] receiveReward(int id, byte mode, long quantity) {
        return updateReward(id, mode, null, quantity);
    }

    public static byte[] updateReward(int id, byte mode, List<MapleReward> rewards, long value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.REWARD.getValue());
        mplew.write(mode);
        switch (mode) {
            case 0x09:
                mplew.writeInt(rewards.size());
                if (!rewards.isEmpty()) {
                    for (MapleReward reward : rewards) {
                        boolean empty = reward.getId() < 1;
                        mplew.writeInt(0);
                        mplew.writeInt(empty ? 0 : reward.getId()); // 0 = blank 1+ = gift
                        if (!empty) {
                            if ((value & 1) != 0) {
                                mplew.writeLong(PacketHelper.getTime(reward.getReceiveDate() > 0 ? reward.getReceiveDate() : -2));
                                mplew.writeLong(PacketHelper.getTime(reward.getExpireDate() > 0 ? reward.getExpireDate() : -1));
                            }
                            if ((value & 2) != 0) { //nexon do here a3 & 2 when a3 is 9
                                mplew.writeInt(0);
                                mplew.writeInt(0);
                                mplew.writeInt(0);
                                mplew.writeInt(0);
                                mplew.writeInt(0);
                                mplew.writeInt(0);
                                mplew.writeMapleAsciiString("");
                                mplew.writeMapleAsciiString("");
                                mplew.writeMapleAsciiString("");
                            }
                            mplew.writeInt(reward.getType());
                            mplew.writeInt(reward.getType() == MapleReward.道具 || reward.getType() == MapleReward.現金道具 ? reward.getItemId() : 0);
                            mplew.writeInt(reward.getType() == MapleReward.道具 || reward.getType() == MapleReward.現金道具 ? reward.getAmount() : 0);
                            mplew.writeInt(0);
                            mplew.writeLong(PacketHelper.getTime(-1));
                            mplew.writeInt(0); // sn
                            mplew.writeInt(reward.getType() == MapleReward.楓點 ? reward.getAmount() : 0);
                            mplew.writeLong(reward.getType() == MapleReward.楓幣 ? reward.getAmount() : 0);
                            mplew.writeLong(reward.getType() == MapleReward.經驗 ? reward.getAmount() : 0);
                            mplew.writeInt(-99);
                            mplew.writeInt(-99);
                            mplew.writeMapleAsciiString("");
                            mplew.writeMapleAsciiString("");
                            mplew.writeMapleAsciiString("");
                            if ((value & 4) != 0) {
                                mplew.writeMapleAsciiString("");
                            }
                            if ((value & 8) != 0) {
                                mplew.writeMapleAsciiString(reward.getDesc());
                            }
                        }
                    }
                }
                break;
            case 0x0A: // 拒收 -> 刪除獎勵
                break;
            case 0x0B: // 獲得楓點。\r\n(%d 楓點)
                mplew.writeInt(id);
                mplew.writeInt(value); // 楓葉點數數量
                mplew.writeInt(0);
                break;
            case 0x0C: // 獲得此道具。
                mplew.writeInt(id);
                mplew.writeInt(0);
                break;
            case 0x0D:
                mplew.writeInt(id);
                mplew.writeInt(0);
                break;
            case 0x0E: // 獲得楓幣。\r\n(%d 楓幣)
                mplew.writeInt(id);
                mplew.writeLong(value); // 楓幣數量
                mplew.writeInt(0);
                break;
            case 0x0F: // 獲得經驗值。\r\n(%d 經驗值)
                mplew.writeInt(id);
                mplew.writeLong(value); // 經驗值數量
                mplew.writeInt(0);
                break;
            case 0x14: // 楓點領取失敗。
                break;
            case 0x15: // 道具領取失敗。
                mplew.write((byte) value);
                break;
            case 0x16: // 現金道具領取失敗。
                mplew.write((byte) value);
                break;
            case 0x17: // 楓幣領取失敗。
                break;
            case 0x18: // 經驗值領取失敗。
                break;
            case 0x1B:
                break;
            case 0x1C: // 刷新獎勵按鈕CD (秒)
                mplew.writeDouble(value);
                break;
            case 0x21: // 獎勵領取失敗。請再試一次。
                mplew.write((byte) value);
                break;
        }

        return mplew.getPacket();
    }

    public static byte[] getBossPartyCheckDone(int result, int unk_1, int unk_2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_BossPartyCheckDone.getValue());
        /*
         0 - 超過入場次數或是沒有組隊而無法申請Boss入場。
         1 - OK
         2 - 確認隊員的等級或是任務.
         3 - 確認是否有隊員登出.
         4 - 申請人員中有無法移動的玩家.
         5 - 無法使用待機列的地方。
         6 - 因未知理由而失敗。
         */
        mplew.writeInt(result);
        mplew.writeInt(unk_1);
        mplew.writeInt(unk_2);

        return mplew.getPacket();
    }

    public static byte[] getShowBossListWait(MapleCharacter chr, int usType, int[] Value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserWaitQueueReponse.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(usType);
        switch (usType) {
            case 11: {
                mplew.write(Value[0]); // nResultCode
                mplew.writeInt(Value[1]); // 40905 ??
                mplew.writeInt(Value[2]); // waitingQueueID
                mplew.writeInt(0); // nHideQuest
                mplew.writeInt(0);
                mplew.writeInt(0); // dwReason
                mplew.writeInt(Value[3]); // dwEnterField
                break;
            }
            case 13:
            case 14: {
                mplew.write(Value[0]);
                mplew.writeInt(Value[1]);
                mplew.writeInt(Value[2]);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
            }
            case 18:
                mplew.write(Value[0]);
                mplew.writeInt(Value[1]);
                mplew.writeInt(Value[2]);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
            case 20:
                mplew.write(Value[0]);
                break;
            case 21:
                int v3 = 0;
                mplew.write(v3);
                for (int v34 = 0; v34 < v3; v34++) {
                    mplew.write(0);
                }
                break;
            case 22:
                break;
            case 23:
                break;
            case 24:
                break;
            default:
                mplew.write(Value[0]);
                mplew.writeInt(Value[1]);
                mplew.writeInt(Value[2]);
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
        }

        return mplew.getPacket();
    }

    public static byte[] SpecialChairSitResult(int var0, boolean var1, boolean var2, SpecialChair var3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeOpcode(OutHeader.LP_SpecialChairSitResult);
        mplew.writeInt(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static void SpecialChairTWData(MaplePacketLittleEndianWriter mplew, boolean var1, SpecialChair var2) {
        mplew.writeInt(var2.vq());
        mplew.writeBool(var1);
        if (var1) {
            mplew.writeInt(var2.getItemId());
            mplew.writeInt(var2.vt().length);
            mplew.writeRect(var2.vs());
            mplew.writeInt(var2.getPosition().x);
            mplew.writeInt(var2.getPosition().y);
            mplew.writeInt(var2.vt().length);
            int var5 = 0;

            for (int var10000 = var5; var10000 < var2.vt().length; var10000 = var5) {
                int var3 = var2.vt()[var5];
                int var4 = var2.vr()[var5];
                mplew.writeInt(var3);
                mplew.writeBool(var3 == var2.V());
                mplew.writeInt(var3 <= 0 ? -1 : var4);
                ++var5;
            }
        }
    }

    public static byte[] SpecialChairTWRemove(int var0, int var1, int var2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(OutHeader.LP_SpecialChairTWRemove);

        mplew.writeInt(var0);
        mplew.writeInt(var1);
        mplew.writeInt(var2);
        mplew.writeInt(-1);
        mplew.writeBool(false);
        return mplew.getPacket();
    }

    public static byte[] SpecialChairTWSitResult(int var0, Map<Integer, Map<Integer, SpecialChairTW>> var1, List<Integer> var2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(OutHeader.LP_SpecialChairTWSitResult);
        return mplew.getPacket();
    }

    private static void SpecialChairTWData(MaplePacketLittleEndianWriter mplew, SpecialChairTW scTW) {
        mplew.writeInt(scTW.getItemId());
        mplew.writeInt(scTW.getPosition().x);
        mplew.writeInt(scTW.getPosition().y);
        mplew.writeRect(scTW.vs());
        SpecialChairTWData(mplew, scTW.vu(), scTW.vr());
        SpecialChairTWData(mplew, scTW.vv(), scTW.vr());
        mplew.writeBool(true);
    }

    private static void SpecialChairTWData(MaplePacketLittleEndianWriter mplew, Map<Integer, Integer> var1, int[] var2) {
        TreeMap<Integer, Integer> var3 = new TreeMap<>();
        var1.forEach((var12, var21) -> var3.put(var21, var12));
        mplew.writeInt(var3.size());
        Iterator<Entry<Integer, Integer>> var5 = var3.entrySet().iterator();

        for (Iterator<Entry<Integer, Integer>> var10000 = var5; var10000.hasNext(); var10000 = var5) {
            Entry<Integer, Integer> var6;
            Entry<Integer, Integer> var10001 = var6 = var5.next();
            mplew.write(1);
            mplew.writeInt(var10001.getValue());
            mplew.writeInt(var6.getKey());
            mplew.writeInt(var2[var6.getKey()]);
        }
    }

    public static byte[] SpecialChairTWInviteResult(int var0, int var1, int var2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(OutHeader.LP_SpecialChairTWInviteResult);
        mplew.writeInt(var0);
        mplew.writeInt(var1);
        mplew.writeInt(var2);
        return mplew.getPacket();
    }

    /**
     * 發送封包到客戶端 坐下請求
     *
     * @param
     * @return v257 封包數據.
     * @implNote : 00 00 00 00 00
     */

    public static byte[] PortableChairUseResult(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserSitResult.getValue());
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] SpecialChairTWInvite(int var0) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(OutHeader.LP_SpecialChairTWInvite);
        mplew.writeInt(var0);
        return mplew.getPacket();
    }

    public static byte[] SpecialChairTWAction(int var0) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(OutHeader.LP_SpecialChairTWSitResult);
        mplew.writeInt(var0);
        return mplew.getPacket();
    }

    /* 改變膚色 / 外貌 */
    public static byte[] getChangeBeautyResult(int cardItemID, int showLookFlag, long androidSN, List<Pair<Integer, Integer>> beautys) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(showLookFlag == 100 ? OutHeader.CHANGE_ANDROID_BEAUTY_RESULT : OutHeader.CHANGE_BEAUTY_RESULT);
        mplew.writeInt(cardItemID);
        mplew.write(beautys != null && !beautys.isEmpty());
        if (beautys == null || beautys.isEmpty()) {
            mplew.writeInt(0);
        } else {
            mplew.writeInt(0);
            mplew.writeMapleAsciiString("");
            if (showLookFlag == 101 && beautys.size() < 2) showLookFlag = 0;
            mplew.write(showLookFlag);
            mplew.write(0);
            writeBeautyResult(mplew, showLookFlag == 101 ? 0 : showLookFlag, androidSN, Collections.singletonList(beautys.getFirst()));
            mplew.write(0);
            if (showLookFlag == 101) {
                writeBeautyResult(mplew, 2, -1, Collections.singletonList(beautys.get(1)));
            } else {
                writeBeautyResult(mplew, -1, -1, Collections.emptyList());
            }
            mplew.writeInt(0);
            mplew.write(0);
        }

        // F5 //高級膚色券
        // A0 4E 00 01 == 10016 皮膚CODE
        // 00 00 00 00 01 00 00 00 00 01 00 00 00 00 01 00 00 00 03 00 00 00 00 00 00 00 FF 00 00 00 00 00 00 00 00 00 packet
        return mplew.getPacket();
    }

    public static void writeBeautyResult(MaplePacketLittleEndianWriter mplew, int showLookFlag, long androidSN, List<Pair<Integer, Integer>> beautys) {
        mplew.write(showLookFlag);
        if (showLookFlag == 100)
            mplew.writeLong(androidSN);
        mplew.writeInt(beautys.size());
        for (Pair<Integer, Integer> pair : beautys) {
            mplew.writeInt(ItemConstants.類型.膚色(pair.getRight()) ? 1 : ItemConstants.類型.臉型(pair.getRight()) ? 11 : ItemConstants.類型.髮型(pair.getRight()) ? 21 : 0);
            mplew.writeInt(pair.getRight());
            mplew.writeInt(pair.getLeft());
        }
    }

    public static byte[] getBeautyList(int showLookFlag, int cardItemID, int slot, long androidSN, List<Integer> beautyList, List<Integer> beautyList2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(OutHeader.BEAUTY_LIST);

        mplew.write(showLookFlag);
        mplew.writeInt(cardItemID);
        mplew.writeInt(beautyList != null ? 1 : 0);

        if (beautyList != null) {
            mplew.writeInt(slot);
            if (showLookFlag == 100) {
                mplew.writeLong(androidSN);
                mplew.write(beautyList.size());
                for (int beautyID : beautyList) {
                    mplew.writeInt(beautyID);
                }
            } else if (showLookFlag == 101) {
                mplew.write(beautyList.size());
                for (int beautyID : beautyList) {
                    mplew.writeInt(beautyID);
                }
                mplew.write(beautyList2.size());
                for (int beautyID : beautyList2) {
                    mplew.writeInt(beautyID);
                }
            } else {
                mplew.write(beautyList.size());
                for (int beautyID : beautyList) {
                    mplew.writeInt(beautyID);
                }
            }
        }

        return mplew.getPacket();
    }

    public static byte[] encodeCombingRoomActionRes(int a1, int a2, int a3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CombingRoomActionRes.getValue());
        mplew.write(a1);
        mplew.write(a2);
        mplew.writeInt(a3);

        return mplew.getPacket();
    }

    public static void encodeCombingRoomChangedHeard(MaplePacketLittleEndianWriter mplew, int styleType, int action, int res) {
        mplew.write(styleType);
        mplew.write(action);
        mplew.write(res);
    }

    /**
     * Created by HERTZ on 2023/12/23
     *
     * @notice 梳化間/打扮/存檔外貌
     */
    public static byte[] encodeCombingRoomRes(int styleType, int action, int res) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CombingRoomChangedRes.getValue());
        encodeCombingRoomChangedHeard(mplew, styleType, action, res);
        return mplew.getPacket();
    }

    public static byte[] encodeUpdateCombingRoomSlotCount(int styleType, int action, int slot, int slot2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.CombingRoomChangedRes.getValue());
        encodeCombingRoomChangedHeard(mplew, styleType, action, 1);

        mplew.write(slot);
        mplew.write(slot2);

        return mplew.getPacket();
    }

    public static byte[] encodeUpdateCombingRoomSlotRes(int styleType, int action, int position, int styleID) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CombingRoomChangedRes.getValue());
        encodeCombingRoomChangedHeard(mplew, styleType, action, 2);

        if (action != 6) {
            mplew.write(position);
            PacketHelper.encodeCombingRoomSlot(mplew, styleID);
            mplew.write(0);
        }

        return mplew.getPacket();
    }

    public static byte[] encodeCombingRoomSlotUnknownRes(int styleType, int action, int position, int b1, int b2, int b3, int styleID) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CombingRoomChangedRes.getValue());
        encodeCombingRoomChangedHeard(mplew, styleType, action, 3);
        mplew.write(b1);
        mplew.write(b2);
        mplew.write(b3);
        PacketHelper.encodeCombingRoomSlot(mplew, styleID);
        return mplew.getPacket();
    }

    public static byte[] encodeCombingRoomOldSlotCount(int styleType, int action, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.CombingRoomChangedRes.getValue());
        encodeCombingRoomChangedHeard(mplew, styleType, action, 5);

        mplew.writeInt(slot);

        return mplew.getPacket();
    }

    public static byte[] encodeUpdateCombingRoomInventoryRes(int styleType, int action, Map<Integer, List<Integer>> combingRoomInventorys) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.CombingRoomChangedRes.getValue());
        encodeCombingRoomChangedHeard(mplew, styleType, action, 6);

        if (styleType <= 2) {
            PacketHelper.encodeCombingRoomInventory(mplew, combingRoomInventorys.getOrDefault(3 - styleType, new LinkedList<>()));
        } else if (styleType == 3) {
            for (int i = 3; i > 0; i--) {
                PacketHelper.encodeCombingRoomInventory(mplew, combingRoomInventorys.getOrDefault(i, new LinkedList<>()));
            }
        }

        return mplew.getPacket();
    }

    public static byte[] createJupiterThunder(int chrid, Point pos, int a1, int a2, int skillID, int bulletCount, int a3, int a4, int a5) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.CreateJupiterThunder.getValue());

        mplew.writeInt(chrid);
        int nCount = 1;
        mplew.writeInt(nCount);
        for (int i = 0; i < nCount; i++) {
            boolean b = true;
            mplew.write(b);
            if (b) {
                mplew.writeInt(i + 1);
                mplew.writeInt(1);
                mplew.writeInt(chrid);
                mplew.writePosInt(pos);
                mplew.writeInt(a1);
                mplew.writeInt(a2);
                mplew.writeInt(skillID);
                mplew.writeInt(bulletCount);
                mplew.writeInt(330);
                mplew.writeInt(40000);
                mplew.writeInt(a3);
                mplew.writeInt(a4);
                mplew.writeInt(a5);
            }
        }

        return mplew.getPacket();
    }

    public static byte[] jupiterThunderEnd(int chrid, int a1, int a2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.JupiterThunderEnd.getValue());

        mplew.writeInt(chrid);
        mplew.writeInt(a1);
        mplew.writeInt(a2);

        return mplew.getPacket();
    }

    public static byte[] jupiterThunderAction(int chrid, int a1, int a2, int a3, int a4, int a5, int a6, int a7) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.JupiterThunderAction.getValue());

        mplew.writeInt(chrid);
        mplew.writeInt(a1);
        mplew.writeInt(a2);
        mplew.writeInt(a3);
        if (a1 == 1) {
            mplew.writeInt(a4);
            mplew.writeInt(a5);
            mplew.writeInt(a6);
            mplew.writeInt(a7);
        }

        return mplew.getPacket();
    }

    public static byte[] InhumanSpeedAttackeRequest(int chrid, byte a1, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(-2);
        mplew.writeInt(chrid);
        mplew.write(a1);
        mplew.writeInt(duration);
        return mplew.getPacket();
    }

    public static byte[] onDeadDebuffSet(int type, DeadDebuff deadDebuff) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(OutHeader.DeadDebuffSet);

        mplew.writeShort(type);
        if (type != 2) {
            mplew.writeInt(deadDebuff.Total);
            mplew.writeInt(deadDebuff.getRemain());
            mplew.writeInt(80);  // 掉寶+經驗減少80%
            mplew.writeInt(80);  // 掉寶+經驗減少80%
        }

        return mplew.getPacket();
    }

    public static byte[] sendCTX_DEAD_BUFF_MESSAGE() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CTX_DEAD_BUFF_MESSAGE.getValue());
        mplew.writeHexString("47 00 AE 4D A5 CE B8 67 C5 E7 AD C8 C0 F2 B1 6F A1 42 B1 BC B8 A8 B2 76 B4 EE A4 D6 38 30 25 AE C4 AA 47 A4 A4 A1 49 0D 0A A8 CF A5 CE C5 40 A8 AD B2 C5 A9 47 B9 44 A8 E3 A1 41 B4 4E AF E0 A5 DF A8 E8 B8 D1 B0 A3 A1 43 52 01 00 00 10 27 00 00 01 B4 00 00 00");
        return mplew.getPacket();
    }

    public static byte[] sendSetTeachSkillCost() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(OutHeader.LP_SetTeachSkillCost);
        mplew.writeInt(ServerConfig.TeachCost.size());
        for (int b : ServerConfig.TeachCost) {
            mplew.writeInt(b);
        }

        return mplew.getPacket();
    }

    public static byte[] spawnSecondAtoms(final MapleCharacter chr, final int skillid, final int count) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SPAWN_SECOND_ATOMS.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(2);
        for (int i = 1; i <= 2; ++i) {
            mplew.writeInt((i == 1) ? ((count - 1) * 10) : (count * 10));
            mplew.writeInt(0);
            mplew.writeInt((i == 1) ? (count - 1) : count);
            mplew.writeInt(0);
            mplew.writeInt(chr.getId());
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(skillid);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(1);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(chr.getPosition().x);
            mplew.writeInt(1);
            mplew.writeInt(0);
            mplew.writeShort(0);
            mplew.write(0);
        }
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] AttackSecondAtom(final MapleCharacter chr, final int objid, final int count) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.ATTACK_SECOND_ATOM.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(1);
        mplew.writeInt(objid);
        mplew.writeInt(0);
        mplew.writeInt(1); // 351 new
        return mplew.getPacket();
    }

    public static byte[] removeSecondAtom(int cid, int objectId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.REMOVE_SECOND_ATOM.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(1);
        mplew.writeInt(objectId);
        mplew.writeInt(0);
        mplew.writeInt(1); // 351 new
        return mplew.getPacket();
    }

    public static class AntiMacro {

        public static byte[] cantFindPlayer() {
            return antiMacroResult(AntiType.未找到角色, MapleAntiMacro.SYSTEM_ANTI, null, null, 0);
        }

        public static byte[] nonAttack() {
            return antiMacroResult(AntiType.非攻擊狀態, MapleAntiMacro.SYSTEM_ANTI, null, null, 0);
        }

        public static byte[] alreadyPass() {
            return antiMacroResult(AntiType.已經通過, MapleAntiMacro.SYSTEM_ANTI, null, null, 0);
        }

        public static byte[] antiMacroNow() {
            return antiMacroResult(AntiType.正在測試, MapleAntiMacro.SYSTEM_ANTI, null, null, 0);
        }

        public static byte[] screenshot(String str) {
            return antiMacroResult(AntiType.儲存截圖, MapleAntiMacro.SYSTEM_ANTI, str, null, 0);
        }

        public static byte[] antiMsg(int mode, String str) {
            return antiMacroResult(AntiType.測謊反饋訊息, (byte) mode, str, null, 0);
        }

        public static byte[] getImage(byte[] file, int times) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(OutHeader.LP_AntiMacroResult.getValue());
            mplew.write(HexTool.getByteArrayFromHexString("59 12 7D 32"));
            byte[] UnknownVal = HexTool.getByteArrayFromHexString("18 BF DB 41 10 BF DB 41 11 BF DB 41 7C 7C");
            mplew.writeInt(UnknownVal.length);
            mplew.write(UnknownVal);
            mplew.write(times);
            //mplew.writeFile(file);
            mplew.writeInt(file.length);
            mplew.write(file);
            mplew.write(HexTool.getByteArrayFromHexString("01 65 6D 2E 04 00 00 00 EA D3 F7 5D"));

            return mplew.getPacket();
        }

        public static byte[] failure(int mode) {
            return antiMacroResult(AntiType.測謊失敗, (byte) mode, null, null, 0);
        }

        public static byte[] failureScreenshot(String str) {
            return antiMacroResult(AntiType.失敗截圖, MapleAntiMacro.GM_SKILL_ANTI, str, null, 0);
        }

        public static byte[] success(int mode) {
            return antiMacroResult(AntiType.通過測謊, (byte) mode, null, null, 0);
        }

        public static byte[] successMsg(int mode, String str) {
            return antiMacroResult(AntiType.通過訊息, (byte) mode, str, null, 0);
        }

        private static byte[] antiMacroResult(byte type, byte antiMode, String str, File file, int times) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(OutHeader.LP_AntiMacroResult.getValue());
            mplew.write(type);
            mplew.write(antiMode); // 2 = show msg/save screenshot/maple admin picture(mode 6)
            if (type == AntiType.認證圖片) {
                mplew.write(times);
                mplew.write(false); // if false time is 05:00
                mplew.writeFile(file);
                return mplew.getPacket();
            }
            if (type == AntiType.測謊失敗 || type == AntiType.通過測謊) {
            }
            if (type == AntiType.測謊失敗 || type == AntiType.通過測謊) {
            }
            if (type == AntiType.儲存截圖) { // save screenshot
                mplew.writeMapleAsciiString(str); // file name
                return mplew.getPacket();
            }
            if (type != AntiType.測謊反饋訊息) {
                if (type == AntiType.通過訊息) {
                    mplew.writeMapleAsciiString(str); // passed lie detector message
                } else {
                    if (type != AntiType.失敗截圖) {
                        return mplew.getPacket();
                    }
                    mplew.writeMapleAsciiString(str); // failed lie detector, file name (for screenshot)
                }
                return mplew.getPacket();
            }

            return mplew.getPacket();
        }

        public static byte[] antiMacroBomb(boolean error, int mapid, int channel) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(OutHeader.LP_AntiMacroBombResult.getValue());
            mplew.write(error ? 2 : 1);
            mplew.writeInt(mapid);
            mplew.writeInt(channel);

            return mplew.getPacket();
        }

        private static class AntiType {

            public static byte 未找到角色 = (byte) 0;
            public static byte 非攻擊狀態 = (byte) 1;
            public static byte 已經通過 = (byte) 2;
            public static byte 正在測試 = (byte) 3;
            public static byte 儲存截圖 = (byte) 6;
            public static byte 測謊反饋訊息 = (byte) 7;
            public static byte 認證圖片 = (byte) 8;
            public static byte 測謊失敗 = (byte) 9;
            public static byte 失敗截圖 = (byte) 10;
            public static byte 通過測謊 = (byte) 11;
            public static byte 通過訊息 = (byte) 12;
        }
    }

}
