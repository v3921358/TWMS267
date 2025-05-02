package Server.world.guild;

import Client.MapleCharacterUtil;
import Client.skills.SkillFactory;
import Database.DatabaseLoader.DatabaseConnectionEx;
import Net.server.buffs.MapleStatEffect;
import Packet.GuildPacket;
import Packet.MaplePacketCreator;
import Packet.PacketHelper;
import Packet.UIPacket;
import Server.world.WorldAllianceService;
import Server.world.WorldBroadcastService;
import Server.world.WorldGuildService;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.DateUtil;
import tools.data.MaplePacketLittleEndianWriter;

import java.io.Serializable;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MapleGuild implements Serializable {

    public static final long serialVersionUID = 6322150443228168192L;
    private static final Logger log = LoggerFactory.getLogger(MapleGuild.class);
    private final List<MapleGuildCharacter> members = new CopyOnWriteArrayList<>(); //公會成員的信息
    private final List<MapleGuildCharacter> applyMembers = new ArrayList<>(); //等待加入的公會成員信息
    private final Map<Integer, String> applyInfos = new LinkedHashMap<>();
    private final Map<Integer, MapleGuildSkill> guildSkills = new HashMap<>();
    private final String[] rankTitles = new String[10]; // 1 = master, 2 = jr, 5 = lowest member
    private final int[] rankAuthority = new int[10];
    private final Map<Integer, MapleBBSThread> bbs = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    @Getter
    private final int[] guildExp = {0, 15000, 60000, 135000, 240000, 375000, 540000, 735000, 960000, 1215000, 1500000, 1815000, 2160000, 2535000, 2940000, 3375000, 3840000, 4335000, 4860000, 5415000, 6000000, 6615000, 7260000, 7935000, 8640000};
    @Getter
    private String name;
    private String notice;
    @Getter
    private int id;
    /**
     * 公會的GP點數
     */
    @Getter
    private int gP;
    /**
     * 公會的貢獻點數
     */
    @Getter
    private int contribution;
    @Setter
    @Getter
    private int logo;
    @Setter
    @Getter
    private int logoColor;
    @Getter
    private int leaderId;
    @Getter
    private int capacity;
    @Setter
    @Getter
    private int logoBG;
    @Setter
    @Getter
    private int logoBGColor;
    @Getter
    private int signature;
    @Getter
    private int level;
    @Setter
    @Getter
    private int activities;
    @Setter
    @Getter
    private int onlineTime;
    @Setter
    @Getter
    private int age;
    @Setter
    @Getter
    private byte[] imageLogo = null;
    @Setter
    @Getter
    private boolean bDirty = true;
    @Getter
    private boolean proper = true;
    @Setter
    @Getter
    private boolean allowJoin = true;
    @Setter
    @Getter
    private int joinSetting = 0;
    @Setter
    @Getter
    private int reqLevel = 0;
    @Getter
    private int allianceId = 0;
    @Getter
    private int invitedId = 0;
    @Getter
    private boolean init = false, changed = false, changed_skills = false;
    private MapleGuildCharacter leader = null;

    public MapleGuild(int guildid) {
        this(guildid, null);
    }

    public MapleGuild(int guildid, Map<Integer, Map<Integer, MapleBBSReply>> replies) {
        super();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM guilds WHERE guildid = ?");
            ps.setInt(1, guildid);
            ResultSet rs = ps.executeQuery();
            if (!rs.first()) {
                rs.close();
                ps.close();
                id = -1;
                return;
            }
            id = guildid;
            name = rs.getString("name");
            gP = rs.getInt("GP");
            imageLogo = rs.getBytes("imageLogo");
            logo = rs.getInt("logo");
            logoColor = rs.getInt("logoColor");
            logoBG = rs.getInt("logoBG");
            logoBGColor = rs.getInt("logoBGColor");
            capacity = rs.getInt("capacity");
            rankTitles[0] = rs.getString("rank1title");
            rankTitles[1] = rs.getString("rank2title");
            rankTitles[2] = rs.getString("rank3title");
            rankTitles[3] = rs.getString("rank4title");
            rankTitles[4] = rs.getString("rank5title");
            rankTitles[5] = rs.getString("rank6title");
            rankTitles[6] = rs.getString("rank7title");
            rankTitles[7] = rs.getString("rank8title");
            rankTitles[8] = rs.getString("rank9title");
            rankTitles[9] = rs.getString("rank10title");
            rankAuthority[0] = rs.getInt("rank1authority");
            if (rankAuthority[0] != -1) {
                rankAuthority[0] = -1;
            }
            rankAuthority[1] = rs.getInt("rank2authority");
            rankAuthority[2] = rs.getInt("rank3authority");
            rankAuthority[3] = rs.getInt("rank4authority");
            rankAuthority[4] = rs.getInt("rank5authority");
            rankAuthority[5] = rs.getInt("rank6authority");
            rankAuthority[6] = rs.getInt("rank7authority");
            rankAuthority[7] = rs.getInt("rank8authority");
            rankAuthority[8] = rs.getInt("rank9authority");
            rankAuthority[9] = rs.getInt("rank10authority");
            leaderId = rs.getInt("leader");
            notice = rs.getString("notice");
            signature = rs.getInt("signature");
            allianceId = rs.getInt("alliance");
            allowJoin = rs.getBoolean("allow_join");
            activities = rs.getInt("activities");
            onlineTime = rs.getInt("online_time");
            age = rs.getInt("age");
            rs.close();
            ps.close();

            MapleGuildAlliance alliance = WorldAllianceService.getInstance().getAlliance(allianceId);
            if (alliance == null) {
                allianceId = 0;
            }

            ps = con.prepareStatement("SELECT id, name, level, job, guildrank, guildContribution, alliancerank FROM characters WHERE guildid = ? ORDER BY guildrank ASC, name ASC", ResultSet.CONCUR_UPDATABLE);
            ps.setInt(1, guildid);
            rs = ps.executeQuery();
            if (!rs.first()) {
                System.err.println("公會ID: " + id + " 沒有成員，系統自動解散該公會。");
                rs.close();
                ps.close();
                writeToDB(true);
                proper = false;
                return;
            }
            boolean leaderCheck = false;
            byte gFix = 0, aFix = 0;
            do {
                int chrId = rs.getInt("id");
                byte gRank = rs.getByte("guildrank"), aRank = rs.getByte("alliancerank");

                if (chrId == leaderId) {
                    leaderCheck = true;
                    if (gRank != 1) { //needs updating to 1
                        gRank = 1;
                        gFix = 1;
                    }
                    if (alliance != null) {
                        if (alliance.getLeaderId() == chrId && aRank != 1) {
                            aRank = 1;
                            aFix = 1;
                        } else if (alliance.getLeaderId() != chrId && aRank != 2) {
                            aRank = 2;
                            aFix = 2;
                        }
                    }
                } else {
                    if (gRank == 1) {
                        gRank = 2;
                        gFix = 2;
                    }
                    if (aRank < 3) {
                        aRank = 3;
                        aFix = 3;
                    }
                }
                members.add(new MapleGuildCharacter(chrId, rs.getShort("level"), rs.getString("name"), (byte) -1, rs.getInt("job"), gRank, rs.getInt("guildContribution"), aRank, guildid, false));
            } while (rs.next());
            rs.close();
            ps.close();

            if (!leaderCheck) {
                System.err.println("會長[ " + leaderId + " ]沒有在公會ID為 " + id + " 的公會中，系統自動解散這個公會。");
                writeToDB(true);
                proper = false;
                return;
            }

            if (gFix > 0) {
                ps = con.prepareStatement("UPDATE characters SET guildrank = ? WHERE id = ?");
                ps.setByte(1, gFix);
                ps.setInt(2, leaderId);
                ps.executeUpdate();
                ps.close();
            }

            if (aFix > 0) {
                ps = con.prepareStatement("UPDATE characters SET alliancerank = ? WHERE id = ?");
                ps.setByte(1, aFix);
                ps.setInt(2, leaderId);
                ps.executeUpdate();
                ps.close();
            }

            ps = con.prepareStatement("SELECT * FROM bbs_threads WHERE guildid = ? ORDER BY localthreadid DESC");
            ps.setInt(1, guildid);
            rs = ps.executeQuery();
            while (rs.next()) {
                int tID = rs.getInt("localthreadid");
                MapleBBSThread thread = new MapleBBSThread(tID, rs.getString("name"), rs.getString("startpost"), rs.getLong("timestamp"), guildid, rs.getInt("postercid"), rs.getInt("icon"));
                if (replies != null && replies.containsKey(rs.getInt("threadid"))) {
                    thread.replies.putAll(replies.get(rs.getInt("threadid")));
                }
                bbs.put(tID, thread);
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT * FROM guildskills WHERE guildid = ?");
            ps.setInt(1, guildid);
            rs = ps.executeQuery();
            while (rs.next()) {
                int skillId = rs.getInt("skillid");
                if (skillId < 91000000) { //hack
                    rs.close();
                    ps.close();
                    System.err.println("非公會技能ID: " + skillId + " 在公會ID為 " + id + " 的公會中，系統自動解散該公會。");
                    writeToDB(true);
                    proper = false;
                    return;
                }
                guildSkills.put(skillId, new MapleGuildSkill(skillId, rs.getInt("level"), rs.getLong("timestamp"), rs.getString("purchaser"), "")); //activators not saved
            }
            rs.close();
            ps.close();
            level = calculateLevel();
        } catch (SQLException se) {
            log.error("[MapleGuild] 從數據庫中加載公會信息出錯." + se);
        }
    }

    public static void loadAll() {
        Map<Integer, Map<Integer, MapleBBSReply>> replies = new LinkedHashMap<>();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM bbs_replies");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int tID = rs.getInt("threadid");
                Map<Integer, MapleBBSReply> reply = replies.computeIfAbsent(tID, k -> new HashMap<>());
                reply.put(reply.size(), new MapleBBSReply(reply.size(), rs.getInt("postercid"), rs.getString("content"), rs.getLong("timestamp")));
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT guildid FROM guilds");
            rs = ps.executeQuery();
            while (rs.next()) {
                WorldGuildService.getInstance().addLoadedGuild(new MapleGuild(rs.getInt("guildid"), replies));
            }
            rs.close();
            ps.close();
        } catch (SQLException se) {
            log.error("[MapleGuild] 從數據庫中加載公會信息出錯." + se);
        }
    }

    public static void loadAll(Object toNotify) {
        Map<Integer, Map<Integer, MapleBBSReply>> replies = new LinkedHashMap<>();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM bbs_replies");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int tID = rs.getInt("threadid");
                Map<Integer, MapleBBSReply> reply = replies.computeIfAbsent(tID, k -> new HashMap<>());
                reply.put(reply.size(), new MapleBBSReply(reply.size(), rs.getInt("postercid"), rs.getString("content"), rs.getLong("timestamp")));
            }
            rs.close();
            ps.close();
            boolean cont = false;
            ps = con.prepareStatement("SELECT guildid FROM guilds");
            rs = ps.executeQuery();
            while (rs.next()) {
                GuildLoad.QueueGuildForLoad(rs.getInt("guildid"), replies);
                cont = true;
            }
            rs.close();
            ps.close();
            if (!cont) {
                return;
            }
        } catch (SQLException se) {
            log.error("[MapleGuild] 從數據庫中加載公會信息出錯." + se);
        }
        AtomicInteger FinishedThreads = new AtomicInteger(0);
        GuildLoad.Execute(toNotify);
        synchronized (toNotify) {
            try {
                toNotify.wait();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        while (FinishedThreads.incrementAndGet() != GuildLoad.NumSavingThreads) {
            synchronized (toNotify) {
                try {
                    toNotify.wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /*
     * 創建1個新的公會信息
     */
    public static int createGuild(int leaderId, String name) {
        if (name.length() > 12) {
            return 0;
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT guildid FROM guilds WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.first()) { // 公會名字重複
                rs.close();
                ps.close();
                return 0;
            }
            ps.close();
            rs.close();

            ps = con.prepareStatement("INSERT INTO guilds (`leader`, `name`, `signature`, `alliance`) VALUES (?, ?, ?, 0)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, leaderId);
            ps.setString(2, name);
            ps.setInt(3, (int) (System.currentTimeMillis() / 1000));
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            int ret = 0;
            if (rs.next()) {
                ret = rs.getInt(1);
            }
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException se) {
            log.error("[MapleGuild] 創建公會信息出錯." + se);
            return 0;
        }
    }

    public static void setOfflineGuildStatus(int guildId, byte guildrank, int contribution, byte alliancerank, int chrId) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = ?, guildrank = ?, guildContribution = ?, alliancerank = ? WHERE id = ?");
            ps.setInt(1, guildId);
            ps.setInt(2, guildrank);
            ps.setInt(3, contribution);
            ps.setInt(4, alliancerank);
            ps.setInt(5, chrId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
            System.out.println("SQLException: " + se.getLocalizedMessage());
        }
    }

    public final void writeToDB(boolean bDisband) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            if (!bDisband) {
                StringBuilder buf = new StringBuilder("UPDATE guilds SET GP = ?, imageLogo = ?, logo = ?, logoColor = ?, logoBG = ?, logoBGColor = ?, ");
                for (int i = 1; i < 11; i++) {
                    buf.append("rank").append(i).append("title = ?, ");
                }
                for (int i = 1; i < 11; i++) {
                    buf.append("rank").append(i).append("authority = ?, ");
                }
                buf.append("capacity = ?, notice = ?, alliance = ?, allow_join = ?, activities = ?, online_time = ?, age = ?, leader = ? WHERE guildid = ?");

                PreparedStatement ps = con.prepareStatement(buf.toString());
                ps.setInt(1, gP);
                if (imageLogo == null || imageLogo.length <= 0) {
                    ps.setNull(2, Types.VARBINARY);
                } else {
                    ps.setBytes(2, imageLogo);
                }
                ps.setInt(3, logo);
                ps.setInt(4, logoColor);
                ps.setInt(5, logoBG);
                ps.setInt(6, logoBGColor);
                ps.setString(7, rankTitles[0]);
                ps.setString(8, rankTitles[1]);
                ps.setString(9, rankTitles[2]);
                ps.setString(10, rankTitles[3]);
                ps.setString(11, rankTitles[4]);
                ps.setString(12, rankTitles[5]);
                ps.setString(13, rankTitles[6]);
                ps.setString(14, rankTitles[7]);
                ps.setString(15, rankTitles[8]);
                ps.setString(16, rankTitles[9]);
                ps.setInt(17, rankAuthority[0]);
                ps.setInt(18, rankAuthority[1]);
                ps.setInt(19, rankAuthority[2]);
                ps.setInt(20, rankAuthority[3]);
                ps.setInt(21, rankAuthority[4]);
                ps.setInt(22, rankAuthority[5]);
                ps.setInt(23, rankAuthority[6]);
                ps.setInt(24, rankAuthority[7]);
                ps.setInt(25, rankAuthority[8]);
                ps.setInt(26, rankAuthority[9]);
                ps.setInt(27, capacity);
                ps.setString(28, notice);
                ps.setInt(29, allianceId);
                ps.setBoolean(30, allowJoin);
                ps.setInt(31, activities);
                ps.setInt(32, onlineTime);
                ps.setInt(33, age);
                ps.setInt(34, leaderId);
                ps.setInt(35, id);
                ps.executeUpdate();
                ps.close();

                if (changed) {
                    ps = con.prepareStatement("DELETE FROM bbs_threads WHERE guildid = ?");
                    ps.setInt(1, id);
                    ps.execute();
                    ps.close();

                    ps = con.prepareStatement("DELETE FROM bbs_replies WHERE guildid = ?");
                    ps.setInt(1, id);
                    ps.execute();
                    ps.close();

                    PreparedStatement pse = con.prepareStatement("INSERT INTO bbs_replies (`threadid`, `postercid`, `timestamp`, `content`, `guildid`) VALUES (?, ?, ?, ?, ?)");
                    ps = con.prepareStatement("INSERT INTO bbs_threads(`postercid`, `name`, `timestamp`, `icon`, `startpost`, `guildid`, `localthreadid`) VALUES(?, ?, ?, ?, ?, ?, ?)", DatabaseConnectionEx.RETURN_GENERATED_KEYS);
                    ps.setInt(6, id);
                    for (MapleBBSThread bb : bbs.values()) {
                        ps.setInt(1, bb.ownerID);
                        ps.setString(2, bb.name);
                        ps.setLong(3, bb.timestamp);
                        ps.setInt(4, bb.icon);
                        ps.setString(5, bb.text);
                        ps.setInt(7, bb.localthreadID);
                        ps.execute();
                        ResultSet rs = ps.getGeneratedKeys();
                        if (!rs.next()) {
                            rs.close();
                            continue;
                        }
                        int ourId = rs.getInt(1);
                        rs.close();
                        pse.setInt(5, id);
                        for (MapleBBSReply r : bb.replies.values()) {
                            pse.setInt(1, ourId);
                            pse.setInt(2, r.ownerID);
                            pse.setLong(3, r.timestamp);
                            pse.setString(4, r.content);
                            pse.addBatch();
                        }
                    }
                    pse.executeBatch();
                    pse.close();
                    ps.close();
                }
                if (changed_skills) {
                    ps = con.prepareStatement("DELETE FROM guildskills WHERE guildid = ?");
                    ps.setInt(1, id);
                    ps.execute();
                    ps.close();

                    ps = con.prepareStatement("INSERT INTO guildskills(`guildid`, `skillid`, `level`, `timestamp`, `purchaser`) VALUES(?, ?, ?, ?, ?)");
                    ps.setInt(1, id);
                    for (MapleGuildSkill i : guildSkills.values()) {
                        ps.setInt(2, i.getSkillId());
                        ps.setByte(3, (byte) i.getLevel());
                        ps.setLong(4, i.getTimestamp());
                        ps.setString(5, i.getPurchaser());
                        ps.execute();
                    }
                    ps.close();
                }
                changed_skills = false;
                changed = false;
            } else {
                PreparedStatement ps = con.prepareStatement("DELETE FROM bbs_threads WHERE guildid = ?");
                ps.setInt(1, id);
                ps.execute();
                ps.close();

                ps = con.prepareStatement("DELETE FROM bbs_replies WHERE guildid = ?");
                ps.setInt(1, id);
                ps.execute();
                ps.close();

                ps = con.prepareStatement("DELETE FROM guildskills WHERE guildid = ?");
                ps.setInt(1, id);
                ps.execute();
                ps.close();

                ps = con.prepareStatement("DELETE FROM guilds WHERE guildid = ?");
                ps.setInt(1, id);
                ps.executeUpdate();
                ps.close();

                if (allianceId > 0) {
                    MapleGuildAlliance alliance = WorldAllianceService.getInstance().getAlliance(allianceId);
                    if (alliance != null) {
                        alliance.removeGuild(id, false);
                    }
                }

                broadcast(GuildPacket.guildDisband(id));
            }
        } catch (SQLException se) {
            log.error("[MapleGuild] 保存公會信息出錯." + se);
        }
    }

    public MapleGuildCharacter getLeader() {
        if (leader == null) {
            for (MapleGuildCharacter mgc : members) {
                if (mgc.getId() == leaderId) {
                    leader = mgc;
                    return mgc;
                }
            }
            return null;
        }
        return leader;
    }


    public String getNotice() {
        if (notice == null) {
            return "";
        }
        return notice;
    }

    public void broadcast(byte[] packet) {
        broadcast(packet, -1, BCOp.NONE);
    }

    public void broadcast(byte[] packet, int exception) {
        broadcast(packet, exception, BCOp.NONE);
    }

    public void broadcast(byte[] packet, int exceptionId, BCOp bcop) {
        broadcast(packet, -1, exceptionId, bcop);
    }

    public void broadcast(byte[] packet, int changerID, int exceptionId, BCOp bcop) {
        lock.writeLock().lock();
        try {
            buildNotifications();
        } finally {
            lock.writeLock().unlock();
        }

        lock.readLock().lock();
        try {
            for (MapleGuildCharacter mgc : members) {
                if (bcop == BCOp.DISBAND) {
                    if (mgc.isOnline()) {
                        WorldGuildService.getInstance().setGuildAndRank(mgc.getId(), 0, 5, 0, 5);
                    } else {
                        setOfflineGuildStatus(0, (byte) 5, 0, (byte) 5, mgc.getId());
                    }
                } else if (mgc.isOnline() && mgc.getId() != exceptionId || mgc.isOnline() && bcop == BCOp.CHAT) {
                    if (bcop == BCOp.EMBELMCHANGE) {
                        WorldGuildService.getInstance().changeEmblem(id, changerID, mgc.getId(), this);
                    } else {
                        WorldBroadcastService.getInstance().sendGuildPacket(mgc.getId(), packet, exceptionId, id, bcop == BCOp.CHAT);
                    }
                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private void buildNotifications() {
        if (!bDirty) {
            return;
        }
        List<Integer> mem = new LinkedList<>();
        for (MapleGuildCharacter mgc : members) {
            if (!mgc.isOnline()) {
                continue;
            }
            if (mem.contains(mgc.getId()) || mgc.getGuildId() != id) {
                members.remove(mgc);
                continue;
            }
            mem.add(mgc.getId());
        }
        bDirty = false;
    }

    public void setOnline(int chrId, boolean online, int channel) {
        boolean isBroadcast = true;
        for (MapleGuildCharacter mgc : members) {
            if (mgc.getGuildId() == id && mgc.getId() == chrId) {
                if (mgc.isOnline() == online) {
                    isBroadcast = false;
                }
                mgc.setOnline(online);
                mgc.setChannel((byte) channel);
                break;
            }
        }
        if (isBroadcast) {
            broadcast(GuildPacket.guildMemberOnline(id, chrId, online), chrId);
            if (allianceId > 0) {
                WorldAllianceService.getInstance().sendGuild(GuildPacket.allianceMemberOnline(allianceId, id, chrId, online), id, allianceId);
            }
        }
        bDirty = true; // member formation has changed, update notifications
        init = true;
    }

    public String getRankTitle(int rank) {
        return rankTitles[rank - 1];
    }

    public int getRankAuthority(int rank) {
        return rankAuthority[rank - 1];
    }

    public void setAllianceId(int a) {
        this.allianceId = a;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE guilds SET alliance = ? WHERE guildid = ?");
            ps.setInt(1, a);
            ps.setInt(2, id);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            log.error("[MapleGuild] 保存公會聯盟信息出錯." + e);
        }
    }

    public void setInvitedId(int iid) {
        this.invitedId = iid;
    }

    /*
     * 添加1個新的公會成員
     */
    public int addGuildMember(MapleGuildCharacter newMember) {
        lock.writeLock().lock();
        try {
            if (members.size() >= capacity) {
                return 0;
            }
            for (int i = members.size() - 1; i >= 0; i--) {
                if (members.get(i).getGuildRank() < 5 || members.get(i).getName().compareTo(newMember.getName()) < 0) {
                    members.add(i + 1, newMember);
                    bDirty = true;
                    break;
                }
            }
            //刪除臨時列表
            for (MapleGuildCharacter mgcc : applyMembers) {
                if (mgcc.getId() == newMember.getId()) {
                    applyMembers.remove(mgcc);
                    break;
                }
            }
            applyInfos.remove(newMember.getId());
        } finally {
            lock.writeLock().unlock();
        }
        broadcast(GuildPacket.newGuildMember(newMember, null)); //發送新的玩家加入公會的信息
        gainGP(500, true, newMember.getId());
        if (allianceId > 0) {
            WorldAllianceService.getInstance().sendGuild(allianceId);
        }
        return 1;
    }

    public int addGuildMember(int chrId) {
        MapleGuildCharacter newMember = getApplyMGC(chrId);
        if (newMember == null) {
            return 0;
        }
        applyMembers.remove(newMember);
        applyInfos.remove(chrId);
        //開始加入公會
        lock.writeLock().lock();
        try {
            if (members.size() >= capacity) {
                return 0;
            }
            for (int i = members.size() - 1; i >= 0; i--) {
                if (members.get(i).getGuildRank() < 5 || members.get(i).getName().compareTo(newMember.getName()) < 0) {
                    members.add(i + 1, newMember);
                    bDirty = true;
                    break;
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
        gainGP(500, true, newMember.getId());
        broadcast(GuildPacket.newGuildMember(newMember, null)); //發送新的玩家加入公會的信息
        if (allianceId > 0) {
            WorldAllianceService.getInstance().sendGuild(allianceId);
        }
        return 1;
    }

    /*
     * 添加1個成員的申請列表
     */
    public int addGuildApplyMember(MapleGuildCharacter applyMember, String info) {
        if (getApplyMGC(applyMember.getId()) != null) {
            return 0;
        }
        applyMembers.add(applyMember);
        applyInfos.put(applyMember.getId(), info);
        broadcast(GuildPacket.newGuildMember(applyMember, info)); //發送新的玩家加入公會的信息
        return 1;
    }

    /*
     * 拒絕玩家的公會申請
     * denyGuildApplyMember
     */
    public int denyGuildApplyMember(int fromId) {
        MapleGuildCharacter applyMember = getApplyMGC(fromId);
        if (applyMember == null) {
            return 0;
        }
        applyMembers.remove(applyMember);
        applyInfos.remove(fromId);
        return 1;
    }

    /*
     * 玩家自己離開公會
     */
    public void leaveGuild(MapleGuildCharacter mgc) {
        lock.writeLock().lock();
        try {
            for (MapleGuildCharacter mgcc : members) {
                if (mgcc.getId() == mgc.getId()) {
                    broadcast(GuildPacket.memberLeft(mgcc, false));
                    bDirty = true;
                    gainGP(mgcc.getGuildContribution() > 0 ? -mgcc.getGuildContribution() : -500);
                    members.remove(mgcc);
                    if (mgc.isOnline()) {
                        WorldGuildService.getInstance().setGuildAndRank(mgcc.getId(), 0, 5, 0, 5);
                    } else {
                        setOfflineGuildStatus(0, (byte) 5, 0, (byte) 5, mgcc.getId());
                    }
                    break;
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
        if (bDirty && allianceId > 0) {
            WorldAllianceService.getInstance().sendGuild(allianceId);
        }
    }

    /*
     * 公會驅除玩家
     */
    public void expelMember(MapleGuildCharacter initiator, String name, int chrId) {
        lock.writeLock().lock();
        try {
            for (MapleGuildCharacter mgc : members) {
                if (mgc.getId() == chrId && initiator.getGuildRank() < mgc.getGuildRank()) {
                    broadcast(GuildPacket.memberLeft(mgc, true));
                    bDirty = true;
                    gainGP(mgc.getGuildContribution() > 0 ? -mgc.getGuildContribution() : -500);
                    if (mgc.isOnline()) {
                        WorldGuildService.getInstance().setGuildAndRank(chrId, 0, 5, 0, 5);
                    } else {
                        MapleCharacterUtil.sendNote(mgc.getId(), initiator.getName(), "被公會除名了。", 0);
                        setOfflineGuildStatus(0, (byte) 5, 0, (byte) 5, chrId);
                    }
                    members.remove(mgc);
                    break;
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
        if (bDirty && allianceId > 0) {
            WorldAllianceService.getInstance().sendGuild(allianceId);
        }
    }

    public void changeARank() {
        changeARank(false);
    }

    public void changeARank(boolean leader) {
        if (allianceId <= 0) {
            return;
        }
        for (MapleGuildCharacter mgc : members) {
            byte newRank = 3;
            if (this.leaderId == mgc.getId()) {
                newRank = (byte) (leader ? 1 : 2);
            }
            if (mgc.isOnline()) {
                WorldGuildService.getInstance().setGuildAndRank(mgc.getId(), this.id, mgc.getGuildRank(), mgc.getGuildContribution(), newRank);
            } else {
                setOfflineGuildStatus(this.id, mgc.getGuildRank(), mgc.getGuildContribution(), newRank, mgc.getId());
            }
            mgc.setAllianceRank(newRank);
        }
        WorldAllianceService.getInstance().sendGuild(allianceId);
    }

    public void changeARank(int newRank) {
        if (allianceId <= 0) {
            return;
        }
        for (MapleGuildCharacter mgc : members) {
            if (mgc.isOnline()) {
                WorldGuildService.getInstance().setGuildAndRank(mgc.getId(), this.id, mgc.getGuildRank(), mgc.getGuildContribution(), newRank);
            } else {
                setOfflineGuildStatus(this.id, mgc.getGuildRank(), mgc.getGuildContribution(), (byte) newRank, mgc.getId());
            }
            mgc.setAllianceRank((byte) newRank);
        }
        WorldAllianceService.getInstance().sendGuild(allianceId);
    }

    public boolean changeARank(int chrId, int newRank) {
        if (allianceId <= 0) {
            return false;
        }
        for (MapleGuildCharacter mgc : members) {
            if (chrId == mgc.getId()) {
                if (mgc.isOnline()) {
                    WorldGuildService.getInstance().setGuildAndRank(chrId, this.id, mgc.getGuildRank(), mgc.getGuildContribution(), newRank);
                } else {
                    setOfflineGuildStatus(this.id, mgc.getGuildRank(), mgc.getGuildContribution(), (byte) newRank, chrId);
                }
                mgc.setAllianceRank((byte) newRank);
                WorldAllianceService.getInstance().sendGuild(allianceId);
                return true;
            }
        }
        return false;
    }

    /*
     * 改變公會會長
     */
    public void changeGuildLeader(int newLeader) {
        if (changeRank(newLeader, 1) && changeRank(leaderId, 2)) {
            if (allianceId > 0) {
                int aRank = getMGC(leaderId).getAllianceRank();
                if (aRank == 1) {
                    WorldAllianceService.getInstance().changeAllianceLeader(allianceId, newLeader, true);
                } else {
                    changeARank(newLeader, aRank);
                }
                changeARank(leaderId, 3);
            }
            broadcast(GuildPacket.guildLeaderChanged(id, leaderId, newLeader, allianceId));
            this.leaderId = newLeader;
            try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                PreparedStatement ps = con.prepareStatement("UPDATE guilds SET leader = ? WHERE guildid = ?");
                ps.setInt(1, newLeader);
                ps.setInt(2, id);
                ps.execute();
                ps.close();
            } catch (SQLException e) {
                log.error("[MapleGuild] Saving leaderid ERROR." + e);
            }
        }
    }

    /*
     * 改變玩家的頭銜
     */
    public boolean changeRank(int chrId, int newRank) {
        for (MapleGuildCharacter mgc : members) {
            if (chrId == mgc.getId()) {
                if (mgc.isOnline()) {
                    WorldGuildService.getInstance().setGuildAndRank(chrId, this.id, newRank, mgc.getGuildContribution(), mgc.getAllianceRank());
                } else {
                    setOfflineGuildStatus(this.id, (byte) newRank, mgc.getGuildContribution(), mgc.getAllianceRank(), chrId);
                }
                mgc.setGuildRank((byte) newRank);
                broadcast(GuildPacket.changeRank(mgc));
                return true;
            }
        }
        return false;
    }

    /*
     * 設置公會公告
     */
    public void setGuildNotice(int cid, String notice) {
        this.notice = notice;
        broadcast(GuildPacket.guildNotice(id, cid, notice));
    }

    public void memberLevelJobUpdate(MapleGuildCharacter mgc) {
        for (MapleGuildCharacter member : members) {
            if (member.getId() == mgc.getId()) {
                int old_level = member.getLevel();
                int old_job = member.getJobId();
                member.setJobId(mgc.getJobId());
                member.setLevel((short) mgc.getLevel());
                if (mgc.getLevel() > old_level) {
                    gainGP((mgc.getLevel() - old_level) * mgc.getLevel(), false, mgc.getId());
                    //aftershock: formula changes (below 100 = 40, above 100 = 80) (12000 max) but i prefer level (21100 max), add guildContribution, do setGuildAndRank or just get the MapleCharacter object
                }
                if (old_level != mgc.getLevel()) {
                    broadcast(MaplePacketCreator.sendLevelup(false, mgc.getLevel(), mgc.getName()), mgc.getId());
                }
                if (old_job != mgc.getJobId()) {
                    broadcast(MaplePacketCreator.sendJobup(false, mgc.getJobId(), mgc.getName()), mgc.getId());
                }
                broadcast(GuildPacket.guildMemberLevelJobUpdate(mgc));
                if (allianceId > 0) {
                    WorldAllianceService.getInstance().sendGuild(GuildPacket.updateAlliance(mgc, allianceId), id, allianceId);
                }
                break;
            }
        }
    }

    /*
     * 修改公會職位訊息
     */
    public void changeGradeNameAndAuthority(int changerID, byte gradeIndex, String name, int authority) {
        if (gradeIndex == 1) {
            authority = -1;
        }
        rankTitles[gradeIndex - 1] = name;
        rankAuthority[gradeIndex - 1] = authority;
        broadcast(GuildPacket.gradeNameAndAuthorityChange(id, changerID, rankTitles, rankAuthority));
    }

    /*
     * 解散公會
     */
    public void disbandGuild() {
        writeToDB(true);
        broadcast(null, -1, BCOp.DISBAND);
    }

    /*
     * 修改公會的標識
     */
    public void setGuildEmblem(int changerID, short bg, byte bgcolor, short logo, byte logocolor) {
        this.logoBG = bg;
        this.logoBGColor = bgcolor;
        this.logo = logo;
        this.logoColor = logocolor;
        this.imageLogo = null;
        broadcast(null, changerID, -1, BCOp.EMBELMCHANGE);

        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE guilds SET imageLogo = ?, logo = ?, logoColor = ?, logoBG = ?, logoBGColor = ? WHERE guildid = ?");
            ps.setNull(1, Types.VARBINARY);
            ps.setInt(2, logo);
            ps.setInt(3, logoColor);
            ps.setInt(4, logoBG);
            ps.setInt(5, logoBGColor);
            ps.setInt(6, id);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            log.error("[MapleGuild] Saving guild logo / BG colo ERROR." + e);
        }
    }

    public void setGuildEmblem(int changerID, byte[] imageMark) {
        if (imageMark == null || imageMark.length <= 0 || imageMark.length > 60000) {
            return;
        }
        this.logoBG = 0;
        this.logoBGColor = 0;
        this.logo = 0;
        this.logoColor = 0;
        this.imageLogo = imageMark;
        broadcast(null, changerID, -1, BCOp.EMBELMCHANGE);

        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE guilds SET imageLogo = ?, logo = ?, logoColor = ?, logoBG = ?, logoBGColor = ? WHERE guildid = ?");
            ps.setBytes(1, imageLogo);
            ps.setInt(2, logo);
            ps.setInt(3, logoColor);
            ps.setInt(4, logoBG);
            ps.setInt(5, logoBGColor);
            ps.setInt(6, id);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            log.error("[MapleGuild] Saving guild logo / BG colo ERROR." + e);
        }
    }

    /*
     * 獲得公會成員的信息
     */
    public MapleGuildCharacter getMGC(int chrId) {
        for (MapleGuildCharacter mgc : members) {
            if (mgc.getId() == chrId) {
                return mgc;
            }
        }
        return null;
    }

    /*
     * 獲取臨時申請的成員信息
     */
    public MapleGuildCharacter getApplyMGC(int chrId) {
        for (MapleGuildCharacter mgc : applyMembers) {
            if (mgc.getId() == chrId) {
                return mgc;
            }
        }
        return null;
    }

    /*
     * 增加公會人數上限
     */
    public boolean increaseCapacity(boolean trueMax) {
        if (capacity >= (trueMax ? 200 : 100) || ((capacity + 5) > (trueMax ? 200 : 100))) {
            return false;
        }
        if (trueMax && gP < 25000) {
            return false;
        }
        if (trueMax && gP - 25000 < getGuildExpNeededForLevel(getLevel() - 1)) {
            return false;
        }
        capacity += 5;
        broadcast(GuildPacket.guildCapacityChange(this.id, this.capacity));

        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE guilds SET capacity = ? WHERE guildid = ?");
            ps.setInt(1, this.capacity);
            ps.setInt(2, this.id);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            log.error("[MapleGuild] Saving guild capacity ERROR." + e);
        }
        return true;
    }

    /*
     * 獲得公會GP點數
     */
    public void gainGP(int amount) {
        gainGP(amount, true, -1);
    }

    public void gainGP(int amount, boolean broadcast) {
        gainGP(amount, broadcast, -1);
    }

    public void gainGP(int amount, boolean broadcast, int chrId) {
        if (amount == 0) {
            return;
        }
        if (amount + gP < 0) {
            amount = -gP;
        }
        gP += amount;
        level = calculateLevel();
        broadcast(GuildPacket.updateGuildInfo(id, gP, level)); //更新公會的總貢獻和GP信息
        if (chrId > 0 && amount > 0) {
            MapleGuildCharacter mgc = getMGC(chrId);
            if (mgc != null) {
                mgc.setGuildContribution(mgc.getGuildContribution() + amount);
                if (mgc.isOnline()) {
                    WorldGuildService.getInstance().setGuildAndRank(chrId, this.id, mgc.getGuildRank(), mgc.getGuildContribution(), mgc.getAllianceRank());
                } else {
                    setOfflineGuildStatus(this.id, mgc.getGuildRank(), mgc.getGuildContribution(), mgc.getAllianceRank(), chrId);
                }
                broadcast(GuildPacket.updatePlayerContribution(id, chrId, mgc.getGuildContribution())); //發送更新玩家的貢獻和IGP信息
            }
        }
        if (broadcast) {
            broadcast(UIPacket.getGPMsg(amount));
        }
    }

    public Collection<MapleGuildSkill> getSkills() {
        return guildSkills.values();
    }

    public int getSkillLevel(int skillId) {
        if (!guildSkills.containsKey(skillId)) {
            return 0;
        }
        return guildSkills.get(skillId).getLevel();
    }

    /*
     * 激活公會技能
     */
    public boolean activateSkill(int skillId, int changerID, String name) {
        if (!guildSkills.containsKey(skillId)) {
            return false;
        }
        MapleGuildSkill ourSkill = guildSkills.get(skillId);
        MapleStatEffect effect = SkillFactory.getSkill(skillId).getEffect(ourSkill.getLevel());
        if (ourSkill.getTimestamp() > System.currentTimeMillis() || effect.getPeriod() <= 0) {
            return false;
        }
        ourSkill.setTimestamp(System.currentTimeMillis() + (effect.getPeriod() * 60000L));
        ourSkill.setActivator(name);
        broadcast(GuildPacket.guildSkillPurchased(id, skillId, changerID, ourSkill.getSkillId(), ourSkill.getTimestamp(), ourSkill.getPurchaser(), name));
        return true;
    }

    /*
     * 購買公會技能
     */
    public boolean purchaseSkill(int skillId, String name, int chrId) {
        MapleStatEffect effect = SkillFactory.getSkill(skillId).getEffect(getSkillLevel(skillId) + 1);
        if (effect.getReqGuildLevel() > getLevel() || effect.getLevel() <= getSkillLevel(skillId)) {
            return false;
        }
        MapleGuildSkill ourSkill = guildSkills.get(skillId);
        if (ourSkill == null) {
            ourSkill = new MapleGuildSkill(skillId, effect.getLevel(), 0, name, name);
            guildSkills.put(skillId, ourSkill);
        } else {
            ourSkill.setLevel(effect.getLevel());
            ourSkill.setPurchaser(name);
            ourSkill.setActivator(name);
        }
        if (effect.getPeriod() <= 0) {
            ourSkill.setTimestamp(-1L);
        } else {
            ourSkill.setTimestamp(System.currentTimeMillis() + (effect.getPeriod() * 60000L));
        }
        changed_skills = true;
        gainGP(1000, true, chrId);
        broadcast(GuildPacket.guildSkillPurchased(id, skillId, chrId, ourSkill.getLevel(), ourSkill.getTimestamp(), name, name));
        return true;
    }

    public final int calculateLevel() {
        for (int i = 1; i < 10; i++) {
            if (gP < getGuildExpNeededForLevel(i)) {
                return i;
            }
        }
        return 10;
    }

    public int getGuildExpNeededForLevel(int levelx) {
        if (levelx < 0 || levelx >= guildExp.length) {
            return Integer.MAX_VALUE;
        }
        return guildExp[levelx];
    }

    public void addMemberData(MaplePacketLittleEndianWriter mplew) {
        List<MapleGuildCharacter> players = new ArrayList<>();
        for (MapleGuildCharacter mgc : members) {
            if (mgc.getId() == leaderId) {
                players.add(mgc);
            }
        }
        for (MapleGuildCharacter mgc : members) {
            if (mgc.getId() != leaderId) {
                players.add(mgc);
            }
        }
        if (players.size() != members.size()) {
            System.out.println("公會成員信息加載錯誤 - 實際加載: " + players.size() + " 應當加載: " + members.size());
        }
        //等待公會加入公會的角色信息
        mplew.writeShort(applyMembers.size());
        for (MapleGuildCharacter mgc : applyMembers) {
            mgc.encodeData(mplew);
        }
        //公會成員信息
        mplew.writeShort(players.size());
        for (MapleGuildCharacter mgc : players) {
            mgc.encodeData(mplew);
        }
//        //等待公會加入公會的申請信息
//        mplew.writeInt(applyInfos.size());
//        for (String info : applyInfos.values()) {
//            mplew.writeMapleAsciiString(info);
//        }
    }

    public Collection<MapleGuildCharacter> getMembers() {
        return Collections.unmodifiableCollection(members);
    }

    public void encodeInfoData(MaplePacketLittleEndianWriter mplew) {
        mplew.writeInt(id);
        mplew.write(level);//等級
        mplew.writeMapleAsciiString(name);//公會名
        MapleGuildCharacter gLeader = getLeader();
        mplew.writeMapleAsciiString(gLeader == null ? "" : gLeader.getName());//會長名
        mplew.writeShort(members.size());//公會人數
        mplew.writeShort(gLeader == null ? 1 : gLeader.getLevel()); // 會長等級
        mplew.write(false);
        mplew.writeLong(0);
        mplew.write(allowJoin);
        mplew.writeMapleAsciiString(getNotice()); // 公會介紹
        mplew.writeInt(activities);
        mplew.writeInt(onlineTime);
        mplew.writeInt(age);
        mplew.write(false);
    }

    public void encode(MaplePacketLittleEndianWriter mplew) {
        mplew.writeInt(getId());
        mplew.writeMapleAsciiString(getName());
        for (int i = 1; i <= 10; i++) {
            mplew.writeMapleAsciiString(getRankTitle(i));
            mplew.writeInt(getRankAuthority(i));
        }

        //公會成員信息和等待加入公會的角色信息
        addMemberData(mplew);

        mplew.writeInt(getCapacity());
        mplew.writeShort(getLogoBG());
        mplew.write(getLogoBGColor());
        mplew.writeShort(getLogo());
        mplew.write(getLogoColor());
        mplew.writeMapleAsciiString(getNotice());

        mplew.writeInt(getContribution());
        mplew.writeInt(getGP());
        mplew.write(getLevel());
        mplew.writeInt(getAllianceId() > 0 ? getAllianceId() : 0);
        mplew.writeInt(getGP());
        mplew.writeInt(Integer.parseInt(DateUtil.getCurrentDate().replaceAll("-", "")));
        mplew.write(isAllowJoin());

        mplew.writeLong(PacketHelper.getTime(-2));
        mplew.writeInt(getActivities());
        mplew.writeInt(getOnlineTime());
        mplew.writeInt(getAge());

        //公會技能信息
        mplew.writeShort(getSkills().size()); //AFTERSHOCK: uncomment
        for (MapleGuildSkill skill : getSkills()) {
            mplew.writeInt(skill.getSkillId());
            skill.encode(mplew);
        }

        mplew.write(isAllowJoin() ? 1 : -1);
        if (isAllowJoin()) {
            mplew.write(getJoinSetting());
            mplew.writeInt(getReqLevel());
        }

        // 公會標誌圖片檔案
        encodeImageLogo(mplew);

        mplew.write(1);
        mplew.write(1);
        mplew.writeInt(1);
        mplew.writeInt(0);
        mplew.writeInt(0);

        mplew.writeInt(3);
        mplew.writeInt(3);
        mplew.writeInt(3);
        mplew.writeInt(3);
        mplew.writeInt(3);
        mplew.writeInt(3);
        mplew.writeInt(3);
        mplew.writeInt(3);
        mplew.writeInt(3);
        mplew.writeInt(3);


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

        mplew.write(0);
        mplew.writeLong(PacketHelper.getTime(-1));
        mplew.writeLong(PacketHelper.getTime(-2));
        mplew.writeLong(PacketHelper.getTime(-2));
        mplew.writeShort(0);
    }

    public void encodeSkill(MaplePacketLittleEndianWriter mplew) {

    }

    public void encodeImageLogo(MaplePacketLittleEndianWriter mplew) {
        byte[] logo = getImageLogo();
        mplew.writeInt(logo == null ? 0 : logo.length);
        if (logo != null && logo.length > 0) {
            mplew.write(logo);
        }
        mplew.writeInt(0);
    }

    /*
     * 公會BBS
     */
    public List<MapleBBSThread> getBBS() {
        List<MapleBBSThread> ret = new ArrayList<>(bbs.values());
        ret.sort(new MapleBBSThread.ThreadComparator());
        return ret;
    }

    /*
     * 添加1個新的BBS信息
     */
    public int addBBSThread(String title, String text, int icon, boolean bNotice, int posterID) {
        int add = bbs.get(0) == null ? 1 : 0; //add 1 if no notice
        changed = true;
        int ret = bNotice ? 0 : Math.max(1, bbs.size() + add);
        bbs.put(ret, new MapleBBSThread(ret, title, text, System.currentTimeMillis(), this.id, posterID, icon));
        return ret;
    }

    /*
     * 編輯BBS信息
     */
    public void editBBSThread(int localthreadid, String title, String text, int icon, int posterID, int guildRank) {
        MapleBBSThread thread = bbs.get(localthreadid);
        if (thread != null && (thread.ownerID == posterID || guildRank <= 2)) {
            changed = true;
            thread.setTitle(title);
            thread.setText(text);
            thread.setIcon(icon);
            thread.setTimestamp(System.currentTimeMillis());
            //bbs.put(localthreadid, new MapleBBSThread(localthreadid, title, text, System.currentTimeMillis(), this.id, thread.ownerID, icon));
        }
    }

    /*
     * 刪除BBS信息
     */
    public void deleteBBSThread(int localthreadid, int posterID, int guildRank) {
        MapleBBSThread thread = bbs.get(localthreadid);
        if (thread != null && (thread.ownerID == posterID || guildRank <= 2)) {
            changed = true;
            bbs.remove(localthreadid);
        }
    }

    /*
     * 添加1個BBS的留言信息
     */
    public void addBBSReply(int localthreadid, String text, int posterID) {
        MapleBBSThread thread = bbs.get(localthreadid);
        if (thread != null) {
            changed = true;
            thread.replies.put(thread.replies.size(), new MapleBBSReply(thread.replies.size(), posterID, text, System.currentTimeMillis()));
        }
    }

    /*
     * 刪除1個BBS的留言信息
     */
    public void deleteBBSReply(int localthreadid, int replyid, int posterID, int guildRank) {
        MapleBBSThread thread = bbs.get(localthreadid);
        if (thread != null) {
            MapleBBSReply reply = thread.replies.get(replyid);
            if (reply != null && (reply.ownerID == posterID || guildRank <= 2)) {
                changed = true;
                thread.replies.remove(replyid);
            }
        }
    }

    public boolean hasSkill(int id) {
        return guildSkills.containsKey(id);
    }

    private enum BCOp {

        NONE, DISBAND, EMBELMCHANGE, CHAT
    }
}
