package Server.channel.handler;

import Client.MapleCharacter;
import Client.MapleClient;
import Config.configs.ServerConfig;
import Database.DatabaseConnectionEx;
import Opcode.Headler.OutHeader;
import Opcode.Opcode.GuildOpcode;
import Packet.GuildPacket;
import Server.world.WorldAllianceService;
import Server.world.WorldGuildService;
import Server.world.guild.MapleGuild;
import Server.world.guild.MapleGuildCharacter;
import Server.world.guild.MapleGuildResultOption;
import connection.OutPacket;
import SwordieX.enums.GuildRequestType;
import SwordieX.enums.GuildResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GuildHandler {
    private static final Logger log = LoggerFactory.getLogger(GuildHandler.class);
    private static final Map<String, Pair<Integer, Long>> invited = new HashMap<>(); //[角色名字] [[公會ID] [邀請的時間]]
    private static final List<Integer> ApplyIDs = new ArrayList<>(); //在申請中的角色ID
    private static final ReentrantReadWriteLock applyIDsLock = new ReentrantReadWriteLock();

    public static void addApplyIDs(int id) {
        applyIDsLock.readLock().lock();
        try {
            ApplyIDs.add(id);
        } finally {
            applyIDsLock.readLock().unlock();
        }
    }

    public static void removeApplyIDs(int id) {
        applyIDsLock.readLock().lock();
        try {
            if (ApplyIDs.contains(id)) {
                ApplyIDs.remove(Integer.valueOf(id));
            }
        } finally {
            applyIDsLock.readLock().unlock();
        }
    }

    /*
     * 拒絕公會邀請
     */
    public static void DenyGuildRequest(String from, MapleClient c) {
        MapleCharacter cfrom = c.getChannelServer().getPlayerStorage().getCharacterByName(from);
        if (cfrom != null && invited.remove(c.getPlayer().getName().toLowerCase()) != null) {
            cfrom.getClient().announce(GuildPacket.denyGuildInvitation(c.getPlayer().getName()));
        }
    }

    /*
     * 玩家自己申請加入公會
     * 如果公會沒有同意或者拒絕你的申請
     * 申請後無法向其他公會進行申請 持續時間48小時
     */
    public static void GuildApply(MaplePacketReader slea, MapleClient c) {
        if (c.getPlayer().getGuildId() > 0) {
            //     c.getPlayer().dropMessage(1, "您已經有公會了，無需重複申請.");
            return;
        }
        if (ApplyIDs.contains(c.getPlayer().getId())) {
            c.getPlayer().dropMessage(1, "您已經在公會申請列表中，暫時無法進行此操作.");
            removeApplyIDs(c.getPlayer().getId());
            return;
        }
        int guildId = slea.readInt(); //公會ID
        String info = slea.readMapleAsciiString();
        MapleGuildCharacter guildMember = new MapleGuildCharacter(c.getPlayer());
        guildMember.setGuildId(guildId);
        int ret = WorldGuildService.getInstance().addGuildApplyMember(guildMember, info);
        if (ret == 1) {
            addApplyIDs(c.getPlayer().getId());
            c.announce(GuildPacket.newGuildMember(guildMember, info));
            c.announce(GuildPacket.genericGuildMessage((byte) GuildOpcode.GuildRes_JoinRequest_Done.getValue()));
        } else {
            c.announce(GuildPacket.genericGuildMessage((byte) GuildOpcode.GuildRes_JoinRequest_Unknown.getValue()));
            c.getPlayer().dropMessage(1, "申請加入公會出現錯誤，請稍後再試.");
        }
    }

    /*
     * 接受公會申請
     * [29 01] [01] [37 75 00 00]
     * 應該是同時 接受 多少角色的公會申請
     */
    public static void AcceptGuildApply(MaplePacketReader slea, MapleClient c) {
        if (c.getPlayer().getGuildId() <= 0 || c.getPlayer().getGuildRank() > 2) { //1 == 會長, 2 == 副會長
            return;
        }
        int guildId = c.getPlayer().getGuildId();
        byte amount = slea.readByte();
        int fromId;
        MapleCharacter from;
        for (int i = 0; i < amount; i++) {
            fromId = slea.readInt(); //角色ID
            from = c.getChannelServer().getPlayerStorage().getCharacterById(fromId);
            //暫時只能處理在線的角色申請的信息
            if (from != null && from.getGuildId() <= 0) {
                from.setGuildId(guildId);
                from.setGuildRank((byte) 5);
                int ret = WorldGuildService.getInstance().addGuildMember(from.getMGC());
                if (ret == 0) {
                    from.setGuildId(0);
                    continue;
                }
                from.getClient().announce(GuildPacket.sendGuildResult(MapleGuildResultOption.loadGuild(from)));
                MapleGuild gs = WorldGuildService.getInstance().getGuild(guildId);
                for (byte[] pack : WorldAllianceService.getInstance().getAllianceInfo(gs.getAllianceId(), true)) {
                    if (pack != null) {
                        from.getClient().announce(pack);
                    }
                }
                from.saveGuildStatus();
                respawnPlayer(from);
            }
            if (ApplyIDs.contains(fromId)) {
                removeApplyIDs(fromId);
                break;
            }
        }
    }

    /*
     * 拒絕公會申請
     * [2A 01] [01] [37 75 00 00]
     * 應該是同時 拒絕 多少角色的公會申請
     */
    public static void DenyGuildApply(MaplePacketReader slea, MapleClient c) {
        if (c.getPlayer().getGuildId() <= 0 || c.getPlayer().getGuildRank() > 2) { //1 == 會長, 2 == 副會長
            return;
        }
        int guildId = c.getPlayer().getGuildId();
        byte amount = slea.readByte();
        int fromId;
        for (int i = 0; i < amount; i++) {
            fromId = slea.readInt(); //角色ID
            WorldGuildService.getInstance().denyGuildApplyMember(guildId, fromId);
            if (ApplyIDs.contains(fromId)) {
                removeApplyIDs(fromId);
            }
        }
    }

    /*
     * 公會操作
     */
    public static void Guild(MaplePacketReader slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        MapleGuild guild = chr.getGuild();
        int typeId = slea.readInt();
        GuildRequestType grt = GuildRequestType.getTypeByVal(typeId);
        if (grt == null) {
            log.warn(String.format("未知的工會Request類型 %d", typeId));
            return;
        }
        switch (typeId) {
            case 1: { // 檢查公會名稱是否可用，完成後發送確認是否建立公會視窗
                String name = slea.readMapleAsciiString();
                if (!isGuildNameAcceptable(name)) { // 檢查公會名稱長度
                    c.announce(GuildPacket.sendGuildResult(new MapleGuildResultOption(GuildResponseType.Res_NameAlreadyUsed)));
                    return;
                }
                if (guild != null) {
                    c.announce(GuildPacket.sendGuildResult(new MapleGuildResultOption(GuildResponseType.Res_AlreadyJoinedGuild)));
                    return;
                }
                MapleGuild checkName = WorldGuildService.getInstance().getGuildByName(name);
                if (checkName != null) {
                    c.announce(GuildPacket.sendGuildResult(new MapleGuildResultOption(GuildResponseType.Res_NameAlreadyUsed)));
                    return;
                } else {
                    if (WorldGuildService.getInstance().existCreatingGuildName(name)) {
                        c.announce(GuildPacket.sendGuildResult(new MapleGuildResultOption(GuildResponseType.Res_CreatingGuildAlreadyUsed)));
                        return;
                    } else {
                        WorldGuildService.getInstance().addCreatingGuildName(name, chr.getId());
                        c.announce(GuildPacket.sendGuildResult(MapleGuildResultOption.createGuildAgreeReply(name)));
                    }
                }
                break;
            }
            case 2: { // 同意建立公會，並設定公會一些初始訊息
                String guildName = WorldGuildService.getInstance().getCreatingGuildName(chr.getId()); //取出暫存的名稱
                WorldGuildService.getInstance().removeCreatingGuildName(guildName); //刪除暫存的名稱
                int cost = ServerConfig.CHANNEL_CREATEGUILDCOST;
                if (chr.getGuildId() > 0) {
                    c.announce(GuildPacket.sendGuildResult(new MapleGuildResultOption(GuildResponseType.Res_NameAlreadyUsed)));
                    return;
                } else if (chr.getMeso() < cost) {
                    chr.dropMessage(1, "你沒有足夠的楓幣創建一個公會。當前創建公會需要: " + cost + " 的楓幣.");
                    return;
                }
                if (chr.getGuildId() > 0) {
                    return;
                } else if (chr.getMeso() < 5000000) {
                    chr.dropMessage(1, "不具有資格創建公會,需有500萬楓幣創建費用。");
                    c.announce(GuildPacket.sendGuildResult(new MapleGuildResultOption(GuildResponseType.Res_CancelGuildCreate)));
                    return;
                }
                int guildId = WorldGuildService.getInstance().createGuild(chr.getId(), guildName);
                if (guildId == 0) {
                    c.announce(GuildPacket.sendGuildResult(new MapleGuildResultOption(GuildResponseType.Res_CreatingGuildError)));
                    return;
                }
                chr.gainMeso(-cost, true, true); // 扣除建立公會的楓幣
                try {
                    chr.setGuildId(guildId);
                    chr.setGuildRank((byte) 1);
                    chr.saveGuildStatus();
                    WorldGuildService.getInstance().setGuildMemberOnline(chr.getMGC(), true, c.getChannel());
                } catch (Exception er) {
                    log.error(er.getMessage());
                    c.announce(GuildPacket.sendGuildResult(new MapleGuildResultOption(GuildResponseType.Res_CreatingGuildErrorMoney)));
                    chr.gainMeso(cost, true, true); // 歸還建立公會的楓幣
                    return;
                }

                c.announce(GuildPacket.sendGuildResult(MapleGuildResultOption.createGuild(chr)));
                chr.updateOneInfo(26011, "GuildID", String.valueOf(guildId));
                WorldGuildService.getInstance().gainGP(chr.getGuildId(), 500, chr.getId());
                chr.dropMessage(1, "恭喜你成功創建公會.");

                respawnPlayer(chr);
                break;
            }
            case 19: // 更改公會介紹
                String notice = slea.readMapleAsciiString();
                if (chr.getGuildId() <= 0 || chr.getGuildRank() != 1) {
                    c.getPlayer().dropMessage(-5, "你不具有公會,無法修改。");
                } else {
                    MaplePacketLittleEndianWriter Notice = new MaplePacketLittleEndianWriter();
                    Notice.writeShort(OutHeader.LP_GuildResult.getValue());
                    Notice.writeInt(88);
                    Notice.writeInt(c.getPlayer().getGuild().getId());
                    Notice.writeInt(c.getPlayer().getId());
                    Notice.writeMapleAsciiString(notice);
                    c.announce(Notice.getPacket());
                    break;
                }
            case 20: { // 更改入會設定
                int frist = slea.readByte();
                int two = slea.readInt();
                long three = slea.readLong();
                MaplePacketLittleEndianWriter setting = new MaplePacketLittleEndianWriter();
                setting.writeShort(OutHeader.LP_GuildResult.getValue());
                setting.writeInt(90);
                setting.writeInt(c.getPlayer().getGuild().getId());
                setting.writeInt(c.getPlayer().getId());
                setting.writeInt(frist);
                setting.writeInt(two);
                setting.writeLong(three);
                c.announce(setting.getPacket());
                break;
            }
            case 21: { // 宣傳
                if (c.getPlayer().getGuild().getGP() < 1500) {
                    MaplePacketLittleEndianWriter publicity = new MaplePacketLittleEndianWriter();
                    publicity.writeShort(OutHeader.LP_GuildResult.getValue());
                    publicity.writeInt(94);
                    c.announce(publicity.getPacket());
                }
                break;
            }
            case 35: {
                String name = slea.readMapleAsciiString();
                MaplePacketLittleEndianWriter NotJoin = new MaplePacketLittleEndianWriter();
                NotJoin.writeShort(OutHeader.LP_GuildResult.getValue());
                NotJoin.writeInt(47);
                NotJoin.writeMapleAsciiString(name);
                c.announce(NotJoin.getPacket());
                break;
            }
            case 40: { // update guild skill
                int skillid = slea.readInt();
                int skilllv = slea.readByte();
                int guildid = c.getPlayer().getGuildId();
                int leaderid = c.getPlayer().getGuild().getLeader().getId();
                long timestamp = 94354848000000000L;
                String playerName = c.getPlayer().getName();
                MaplePacketLittleEndianWriter skill = new MaplePacketLittleEndianWriter();
                skill.writeShort(OutHeader.LP_GuildResult.getValue());
                skill.writeInt(106);
                skill.writeInt(guildid);
                skill.writeInt(skillid);
                skill.writeInt(leaderid);
                skill.write(skilllv);
                skill.write(0);
                skill.writeLong(94354848000000000L);
                skill.writeMapleAsciiString(playerName);
                skill.writeShort(0);
                assert skill.getPacket().length == 42;
                String sql = "INSERT INTO guildskills (guildid, skillid, level, timestamp, purchaser) VALUES (?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE level = VALUES(level), timestamp = VALUES(timestamp), purchaser = VALUES(purchaser)";
                try (PreparedStatement ps = DatabaseConnectionEx.getInstance().getConnection().prepareStatement(sql)) {
                    ps.setInt(1, guildid);
                    ps.setInt(2, skillid);
                    ps.setInt(3, skilllv);
                    ps.setLong(4, timestamp);
                    ps.setString(5, playerName);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                c.announce(skill.getPacket());
                break;
            }
            case 44: {
                int gid = slea.readInt();
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(OutHeader.LP_GuildResult.getValue());
                mplew.writeInt(135);
                mplew.writeInt(gid);
                mplew.write(1);
                mplew.writeInt(774); //0
                mplew.writeInt(1196314761); //4
                mplew.writeInt(169478669); //8
                mplew.writeInt(218103808); //12
                mplew.writeInt(1380206665); //16
                mplew.writeInt(285212672); //20
                mplew.writeInt(285212672); //24
                mplew.writeInt(1544); //28
                mplew.writeInt(1198340864); //32
                mplew.writeInt(250); //36
                mplew.writeInt(1497919497); //40
                mplew.writeInt(889192563); //44
                mplew.writeInt(889192660); //48
                mplew.writeInt(1700659668); //52
                mplew.writeInt(2277); //56
                mplew.writeInt(1145681922); //60
                mplew.writeInt(-1925688255); //64
                mplew.writeInt(1808765821); //68
                mplew.writeInt(-954968740); //72
                mplew.writeInt(-69277889); //76
                mplew.writeInt(-1992844322); //80
                mplew.writeInt(-1861947850); //84
                mplew.writeInt(-1330073146); //88
                mplew.writeInt(-1769287966); //92
                mplew.writeInt(-824086086); //96
                mplew.writeInt(811526670); //100
                mplew.writeInt(146424194); //104
                mplew.writeInt(-1543217816); //108
                mplew.writeInt(-668222760); //112
                mplew.writeInt(-2002475608); //116
                mplew.writeInt(718060049); //120
                mplew.writeInt(158712075); //124
                mplew.writeInt(-800067398); //128
                mplew.writeInt(-1533383854); //132
                mplew.writeInt(-1687983658); //136
                mplew.writeInt(-140928716); //140
                mplew.writeInt(-976798226); //144
                mplew.writeInt(-1005513861); //148
                mplew.writeInt(-1082693885); //152
                mplew.writeInt(-34365721); //156
                mplew.writeInt(-824723679); //160
                mplew.writeInt(-1742731749); //164
                mplew.writeInt(1208375408); //168
                mplew.writeInt(-2040660005); //172
                mplew.writeInt(1410336801); //176
                mplew.writeInt(945983236); //180
                mplew.writeInt(-49050078); //184
                mplew.writeInt(-1212143035); //188
                mplew.writeInt(30277805); //192
                mplew.writeInt(-1229368304); //196
                mplew.writeInt(-2025683340); //200
                mplew.writeInt(516811548); //204
                mplew.writeInt(1194385534); //208
                mplew.writeInt(-340681580); //212
                mplew.writeInt(-2000702864); //216
                mplew.writeInt(63561910); //220
                mplew.writeInt(-1473603569); //224
                mplew.writeInt(-1023479806); //228
                mplew.writeInt(-262706587); //232
                mplew.writeInt(817328107); //236
                mplew.writeInt(1198574923); //240
                mplew.writeInt(-723494620); //244
                mplew.writeInt(-386297879); //248
                mplew.writeInt(1948858307); //252
                mplew.writeInt(1234000008); //256
                mplew.writeInt(1077079366); //260
                mplew.writeInt(1279700169); //264
                mplew.writeInt(-1534057047); //268
                mplew.writeInt(730185188); //272
                mplew.writeInt(510228348); //276
                mplew.writeInt(1127580199); //280
                mplew.writeInt(1496913170); //284
                mplew.writeInt(2138288542); //288
                mplew.writeInt(-1887923776); //292
                mplew.writeInt(1381271260); //296
                mplew.writeInt(-1641713102); //300
                mplew.writeInt(240520916); //304
                mplew.writeInt(1059271932); //308
                mplew.writeInt(474743363); //312
                mplew.writeInt(74498626); //316
                mplew.writeInt(-997175499); //320
                mplew.writeInt(2026886645); //324
                mplew.writeInt(21544885); //328
                mplew.writeInt(-1323880988); //332
                mplew.writeInt(234891581); //336
                mplew.writeInt(-1338933080); //340
                mplew.writeInt(609867260); //344
                mplew.writeInt(475144173); //348
                mplew.writeInt(-1087687534); //352
                mplew.writeInt(-43753514); //356
                mplew.writeInt(119843783); //360
                mplew.writeInt(974440129); //364
                mplew.writeInt(449328492); //368
                mplew.writeInt(205956261); //372
                mplew.writeInt(962464460); //376
                mplew.writeInt(-758464331); //380
                mplew.writeInt(1852716860); //384
                mplew.writeInt(-489821231); //388
                mplew.writeInt(-539521900); //392
                mplew.writeInt(-347093191); //396
                mplew.writeInt(-233211829); //400
                mplew.writeInt(1345309660); //404
                mplew.writeInt(52760282); //408
                mplew.writeInt(558086474); //412
                mplew.writeInt(-1607407241); //416
                mplew.writeInt(-890669205); //420
                mplew.writeInt(-1011120858); //424
                mplew.writeInt(-1726485553); //428
                mplew.writeInt(-395945893); //432
                mplew.writeInt(-1071190598); //436
                mplew.writeInt(844385345); //440
                mplew.writeInt(-475664571); //444
                mplew.writeInt(-2113970708); //448
                mplew.writeInt(-2075136729); //452
                mplew.writeInt(620208701); //456
                mplew.writeInt(1298907607); //460
                mplew.writeInt(-1316478512); //464
                mplew.writeInt(1331732463); //468
                mplew.writeInt(1518420766); //472
                mplew.writeInt(1032395430); //476
                mplew.writeInt(-1680126835); //480
                mplew.writeInt(1654276479); //484
                mplew.writeInt(990376368); //488
                mplew.writeInt(169916087); //492
                mplew.writeInt(1135085857); //496
                mplew.writeInt(1133807412); //500
                mplew.writeInt(596822218); //504
                mplew.writeInt(407748584); //508
                mplew.writeInt(973546756); //512
                mplew.writeInt(434506865); //516
                mplew.writeInt(616107209); //520
                mplew.writeInt(-1458595066); //524
                mplew.writeInt(-185388926); //528
                mplew.writeInt(503600211); //532
                mplew.writeInt(-1893408916); //536
                mplew.writeInt(-328766941); //540
                mplew.writeInt(-2080354439); //544
                mplew.writeInt(809818554); //548
                mplew.writeInt(-1136074702); //552
                mplew.writeInt(732639183); //556
                mplew.writeInt(-606666948); //560
                mplew.writeInt(-733239924); //564
                mplew.writeInt(-293986034); //568
                mplew.writeInt(-667849619); //572
                mplew.writeInt(1327875800); //576
                mplew.writeInt(554780496); //580
                mplew.writeInt(-1822782129); //584
                mplew.writeInt(832767128); //588
                mplew.writeInt(1690003788); //592
                mplew.writeInt(-352926615); //596
                mplew.writeInt(173057637); //600
                mplew.writeInt(-865475514); //604
                mplew.writeInt(42236532); //608
                mplew.writeInt(-962448103); //612
                mplew.writeInt(1092386689); //616
                mplew.writeInt(-1984803237); //620
                mplew.writeInt(234658309); //624
                mplew.writeInt(1305734686); //628
                mplew.writeInt(-954042181); //632
                mplew.writeInt(-239540506); //636
                mplew.writeInt(-1425786899); //640
                mplew.writeInt(1478931913); //644
                mplew.writeInt(1990215877); //648
                mplew.writeInt(-265372903); //652
                mplew.writeInt(12729376); //656
                mplew.writeInt(-1087769897); //660
                mplew.writeInt(1366195631); //664
                mplew.writeInt(-46604686); //668
                mplew.writeInt(2136943369); //672
                mplew.writeInt(1804678272); //676
                mplew.writeInt(-1849346706); //680
                mplew.writeInt(785962736); //684
                mplew.writeInt(853888586); //688
                mplew.writeInt(1700581094); //692
                mplew.writeInt(-818097415); //696
                mplew.writeInt(1218143990); //700
                mplew.writeInt(1622892252); //704
                mplew.writeInt(-17706989); //708
                mplew.writeInt(-1657895001); //712
                mplew.writeInt(613642160); //716
                mplew.writeInt(-2144489801); //720
                mplew.writeInt(-442605717); //724
                mplew.writeInt(-1808555067); //728
                mplew.writeInt(-1780953158); //732
                mplew.writeInt(1384415108); //736
                mplew.writeInt(-572325954); //740
                mplew.writeInt(629143062); //744
                mplew.writeInt(443839292); //748
                mplew.writeInt(78562896); //752
                mplew.writeInt(-317100064); //756
                mplew.writeInt(1424842798); //760
                mplew.writeInt(28550); //764
                mplew.writeInt(1162412032); //768
                mplew.writeInt(1118717006); //772
                mplew.writeInt(98912); //776
                mplew.write(0); //780
                c.announce(mplew.getPacket());
                break;
            }
            default:
                log.warn(String.format("未處理的工會Request類型 %d", typeId));
                break;
//            case GuildReq_InviteGuild: { // 226
//                if (chr.getGuild() == null) {
//                    zt = GuildRes_JoinGuild_Set_Refuse;
//                }
//                String name = slea.readMapleAsciiString();
//                final MapleCharacter other = WorldFindService.getInstance().findCharacterByName(name.toLowerCase());
//                if (other.getGuild() != null) {
//                    zt = GuildRes_JoinGuild_AlreadyJoined;
//                }
//                if (zt == null) {
//                    other.send(GuildPacket.inviteGuildDone(chr.getGuild(), chr, other));
//                    chr.send(GuildPacket.guildInvitationDone(name));
//                    break;
//                }
//                break;
//            }
//            case GuildReq_WithdrawGuild: {
//                final int chrId = slea.readInt();
//                name = slea.readMapleAsciiString();
//                if (chrId != chr.getId() || !name.equals(chr.getName()) || chr.getGuild() == null) {
//                    zt = GuildRes_WithdrawGuild_NotJoined;
//                }
//                if (zt == null) {
//                    WorldGuildService.getInstance().leaveGuild(chr.getMGC());
//                }
//                break;
//            }
//            case GuildReq_LoadGuild: //別人加入公會
//                c.announce(GuildPacket.showGuildInfo(c));
//                break;
//            case GuildReq_FindGuildByCid: //看其他玩家的公會信息
//                int fromId = slea.readInt(); //角色ID有時是公會ID
//                MapleGuild guild;
//                //先查找角色信息
//                MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterById(fromId);
//                if (target == null) {
//                    //如果角色為空就找公會ID
//                    guild = WorldGuildService.getInstance().getGuild(fromId);
//                    if (guild == null) {
//                        chr.dropMessage(1, "找不到玩家或公會的信息.");
//                        return;
//                    }
//                    c.announce(GuildPacket.showPlayerGuildInfo(guild));
//                    return;
//                }
//                //角色信息不為空 判斷是否有公會
//                if (target.getGuildId() <= 0) {
//                    chr.dropMessage(1, "玩家[" + target.getName() + "]沒有公會.");
//                    return;
//                }
//                //獲得公會的信息
//                guild = WorldGuildService.getInstance().getGuild(target.getGuildId());
//                if (guild == null) {
//                    chr.dropMessage(1, "玩家[" + target.getName() + "]還沒有公會.");
//                    return;
//                }
//                c.announce(GuildPacket.showPlayerGuildInfo(guild));
//                break;
//            case GuildReq_FindGuildByGID: // 查看公會信息
//                MapleGuild tmpguild = WorldGuildService.getInstance().getGuild(slea.readInt());
//                if (tmpguild != null) {
//                    c.announce(GuildPacket.showPlayerGuildInfo(tmpguild));
//                }
//                break;
//            case GuildReq_LoadMyApplicationList:
//                c.announce(GuildPacket.showGuildLoadApplyList(null));
//                break;

//            case GuildRes_CreateGuildAgree_Reply: // 接受公會邀請
//                if (chr.getGuildId() > 0) {
//                    return;
//                }
//                int guildId = slea.readInt();
//                fromId = slea.readInt();
//                if (fromId != chr.getId()) {
//                    return;
//                }
//                name = chr.getName().toLowerCase();
//                Pair<Integer, Long> gid = invited.remove(name);
//                if (gid != null && guildId == gid.left) {
//                    chr.setGuildId(guildId);
//                    chr.setGuildRank((byte) 5);
//                    int ret = WorldGuildService.getInstance().addGuildMember(chr.getMGC());
//                    if (ret == 0) {
//                        chr.dropMessage(1, "嘗試加入的公會成員數已到達最高限制。");
//                        chr.setGuildId(0);
//                        return;
//                    }
//                    c.announce(GuildPacket.showGuildInfo(c));
//                    MapleGuild gs = WorldGuildService.getInstance().getGuild(guildId);
//                    for (byte[] pack : WorldAllianceService.getInstance().getAllianceInfo(gs.getAllianceId(), true)) {
//                        if (pack != null) {
//                            c.announce(pack);
//                        }
//                    }
//                    chr.saveGuildStatus();
//                    respawnPlayer(c.getPlayer());
//                }
//                break;
//            case GuildReq_KickGuild: // 公會驅除玩家
//                int kickChrId = slea.readInt();
//                String KickChrName = slea.readMapleAsciiString();
//                if (chr.getGuildRank() > 2 || chr.getGuildId() <= 0) {
//                    zt = GuildRes_KickGuild_Unknown;
//                    break;
//                }
//                WorldGuildService.getInstance().expelMember(chr.getMGC(), KickChrName, kickChrId);
//                c.announce(GuildPacket.showGuildInfo(null));
//                break;
//            case GuildReq_RemoveGuild: // 公會驅除玩家
//                if (chr.getGuildRank() > 2 || chr.getGuildId() <= 0) {
//                    return;
//                }
//                WorldGuildService.getInstance().leaveGuild(chr.getMGC());
//                break;
//            case GuildReq_SetGradeNameAndAuthority: { // 公會職位職稱和權限修改
//                if (chr.getGuildId() <= 0 || chr.getGuildRank() != 1) {
//                    return;
//                }
//                byte index = slea.readByte();
//                String rankName = slea.readMapleAsciiString();
//                int authority = slea.readInt();
//                WorldGuildService.getInstance().changeGradeNameAndAuthority(chr.getGuildId(), chr.getId(), index, rankName, authority);
//                break;
//            }
//            case GuildReq_SetMemberGrade: // 職位變化
//                fromId = slea.readInt();
//                byte newRank = slea.readByte();
//                if ((newRank <= 1 || newRank > 5) || chr.getGuildRank() > 2 || (newRank <= 2 && chr.getGuildRank() != 1) || chr.getGuildId() <= 0) {
//                    return;
//                }
//                WorldGuildService.getInstance().changeRank(chr.getGuildId(), fromId, newRank);
//                break;
//            case GuildReq_InputMark: // 更改入會設定
//                if (chr.getGuildId() <= 0 || chr.getGuildRank() != 1) {
//                    return;
//                }
//                chr.getGuild().setAllowJoin(slea.readBool());
//                chr.getGuild().setActivities(slea.readInt());
//                chr.getGuild().setOnlineTime(slea.readInt());
//                chr.getGuild().setAge(slea.readInt());
//                chr.getGuild().broadcast(GuildPacket.changeJoinSetting(chr.getId(), chr.getGuild()));
//                break;
//            case GuildReq_SetMark: // 公會徽章修改
//                if (chr.getGuildId() <= 0 || chr.getGuildRank() != 1) {
//                    return;
//                }
//                if (chr.getMeso() < 1500000) {
//                    chr.dropMessage(1, "楓幣不足 1500000。");
//                    return;
//                }
//                boolean image = slea.readBool();
//                if (!image) {
//                    short bg = slea.readShort();
//                    byte bgcolor = slea.readByte();
//                    short logo = slea.readShort();
//                    byte logocolor = slea.readByte();
//                    WorldGuildService.getInstance().setGuildEmblem(chr.getGuildId(), chr.getId(), bg, bgcolor, logo, logocolor);
//                    chr.gainMeso(-1500000, true, true);
//                } else {
//                    byte[] imageMark;
//                    imageMark = slea.read(slea.readInt());
//                    if (imageMark == null || imageMark.length <= 0 || imageMark.length > 60000) {
//                        return;
//                    }
//                    WorldGuildService.getInstance().setGuildEmblem(chr.getGuildId(), chr.getId(), imageMark);
//                    chr.gainMeso(-1500000, true, true);
//                }
//                respawnPlayer(c.getPlayer());
//                break;
//            case GuildReq_SkillLevelSetUp: // 升級公會技能
//                int skillId = slea.readInt();
//                byte level = slea.readByte();
//                if (skillId > 0) {
//                    chr.dropMessage(1, "當前暫不支持公會技能升級.");
//                    return;
//                }
//                Skill skill = SkillFactory.getSkill(skillId);
//                if (chr.getGuildId() <= 0 || skill == null || skill.getId() < 91000000) {
//                    return;
//                }
//                //檢測新的技能等級
//                int newLevel = WorldGuildService.getInstance().getSkillLevel(chr.getGuildId(), skill.getId()) + level;
//                if (newLevel > skill.getMaxLevel()) {
//                    return;
//                }
//                MapleStatEffect skillid = skill.getEffect(newLevel);
//                if (skillid.getReqGuildLevel() <= 0 || chr.getMeso() < skillid.getPrice()) {
//                    return;
//                }
//                if (WorldGuildService.getInstance().purchaseSkill(chr.getGuildId(), skillid.getSourceId(), chr.getName(), chr.getId())) {
//                    chr.gainMeso(-skillid.getPrice(), true);
//                }
//                break;
//            case 0x3E: // 激活使用公會技能
//                skill = SkillFactory.getSkill(slea.readInt());
//                if (c.getPlayer().getGuildId() <= 0 || skill == null) {
//                    return;
//                }
//                newLevel = WorldGuildService.getInstance().getSkillLevel(chr.getGuildId(), skill.getId());
//                if (newLevel <= 0) {
//                    return;
//                }
//                MapleStatEffect skillii = skill.getEffect(newLevel);
//                if (skillii.getReqGuildLevel() < 0 || chr.getMeso() < skillii.getExtendPrice()) {
//                    return;
//                }
//                if (WorldGuildService.getInstance().activateSkill(chr.getGuildId(), skillii.getSourceid(), chr.getName())) {
//                    chr.gainMeso(-skillii.getExtendPrice(), true);
//                }
//                break;
//            case GuildReq_ChangeGuildMaster: // 改變公會會長
//                fromId = slea.readInt();
//                if (chr.getGuildId() <= 0 || chr.getGuildRank() > 1) {
//                    return;
//                }
//                WorldGuildService.getInstance().setGuildLeader(chr.getGuildId(), fromId);
//                break;
//            case GuildReq_BattleSkillOpen: // 顯示初心者技能信息
//                if (chr.getGuildId() <= 0) {
//                    return;
//                }
//                c.announce(GuildPacket.showGuildBeginnerSkill());
//                break;
            case 47: { // 公會搜索
                int type = slea.readByte(); // 4 - 廣告
                slea.readByte();
                String searchInfo = slea.readMapleAsciiString();
                boolean equals = slea.readBool();
                boolean bUnk1 = slea.readBool();
                boolean bUnk2 = slea.readBool();
                boolean bUnk3 = slea.readBool();
                List<Pair<Integer, MapleGuild>> gui = WorldGuildService.getInstance().getGuildList();
                List<MapleGuild> guilds = new ArrayList<>();
                List<MapleGuild> guilds_list = new ArrayList<>();
                for (Pair<Integer, MapleGuild> g : gui) {
                    MapleGuildCharacter leaderObj = g.getRight().getLeader();
                    String gname = g.getRight().getName().toLowerCase();
                    if (((type == 1 || type == 3) && (equals ? gname.equals(searchInfo) : gname.contains(searchInfo.toLowerCase()))) || (leaderObj != null && (type == 2 || type == 3) && (equals ? leaderObj.equals(searchInfo) : leaderObj.getName().toLowerCase().contains(searchInfo.toLowerCase())))) {
                        guilds.add(g.getRight());
                    }
                }
                c.announce(GuildPacket.guildSearch_Results(type, searchInfo, equals, bUnk1, bUnk2, bUnk3, guilds, guilds_list)); //搜索公會
                break;
//
//            }
            }
        }
    }

    private static boolean isGuildNameAcceptable(String name) {
        return !(name.getBytes().length < 1 || name.getBytes().length > 24);
    }

    private static void respawnPlayer(MapleCharacter chr) {
        if (chr.getMap() == null) {
            return;
        }
        chr.getMap().broadcastMessage(GuildPacket.loadGuildName(chr));
        chr.getMap().broadcastMessage(GuildPacket.loadGuildIcon(chr));
    }


    public static void ChangeGuildNotice(MapleClient c, String[] Notice) {
        OutPacket say = new OutPacket(OutHeader.LP_GuildResult);

    }
}
